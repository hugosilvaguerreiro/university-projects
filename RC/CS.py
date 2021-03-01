from socket import *
from socket_aux import *
from threading import *
from Queue import Queue
from select import select
from signal import *
from math import ceil
import argparse, sys

TIMEOUT = 1
THREAD_NUMBER = 5

q=Queue()
wsSemaphore = Semaphore(THREAD_NUMBER + 1)
kill_event=Event()
workingServers = {}
ptcs={}
requestNumber = 0

lockRequestNumber = Lock()
workingServersLock = Lock()
ptcsLock = Lock()

def getRequestNumber():
	lockRequestNumber.acquire()

	global requestNumber
	val = requestNumber
	requestNumber += 1

	lockRequestNumber.release()
	return val

def graceful_killer(signum, frame):
	print "\nCtrl-C detected."
	kill_event.set()


def worker(cs):
	def split(message, nrOfServers): 
		possibleSplits = [" ", "\n", ".", ",", ";", "\t"]
		output = []
		n = int(ceil(len(message) / nrOfServers))
		i = 0
		while i < len(message):
			j = i
			if i + n < len(message) and message[i+n] not in possibleSplits:
				while j + n < len(message) and message[j + n] not in possibleSplits:
					j += 1
				output += [message[i:j + n]]
			elif i + n > len(message):
				output += [message[i:]]
			else:
				output += [message[i:i+n]]
			i = j + n
		if len(output) > nrOfServers:
			output[-2] += output[-1]
			output.pop()
		return output

	def distribute_work(reqptc, data, filename):
		ptcsLock.acquire() #LOCK ACQUIRE

		nrServersAvailable = len(ptcs[reqptc])

		ptcsLock.release() #LOCK RELEASE

		if(nrServersAvailable == 0):
			return "REP EOF\n"
		splitFile = split(data, nrServersAvailable)
		parts = [(i, splitFile[i]) for i in range(len(splitFile))]
		to_receive = send_to_available_servers(reqptc, parts, filename)

		responses = receive_from_servers(to_receive, filename)
		return [responses[i] for i in range(len(splitFile))]


	def send_to_available_servers(reqptc, parts, filename):
		connections = {}
		j = 0
		ptcsLock.acquire() #LOCK ACQUIRE

		for part in parts:
			serverAddr = ptcs[reqptc][j]
			j += 1
			filePart = str(part[0])
			fileSend = filename + filePart.zfill(3)
			try:
				connections[part[0]] = (tcp_connect(serverAddr))
				connections[part[0]].settimeout(TIMEOUT)
				connections[part[0]].sendall("WRQ "+ reqptc + " " + fileSend + " " + str(len(part[1])) + " " + part[1] + "\n")
			except (timeout, error) as msg: #problem with WS
				print msg
				ptcsLock.release() #LOCK RELEASE
				unregister_working_server(serverAddr)
				connections[part[0]] = 0
				raise

		ptcsLock.release() #LOCK RELEASE
		return connections

	def receive_from_servers(to_receive, filename):
		for part in to_receive:
			try:
				conn = to_receive[part]
				if to_receive[part] != 0:
					to_receive[part] = tcp_recv(conn, close = True)

				f = open("output_files/" + filename + str(part).zfill(3), 'w')
				f.write(to_receive[part])
				f.close()
			except (timeout, error) as msg:
				print msg
				unregister_working_server(serverAddr)
				to_receive[part] = 0
				raise
				
		return to_receive

	def parse_ws_response(reqptc, responses):
		if(responses == "REP EOF\n"):
			return "REP EOF\n"

		if reqptc in ["FLW", "UPP", "LOW"]:
			output = ""
		else:
			output = 0

		for response in responses:
			response = response.split(" ", 3)
			if response[1] == "EOF" or response[1] == "ERR":
				return "REP " + response[1]
			elif response[1] == "F":
				output += response[-1][:-1]
			else:
				if reqptc == "FLW" and len(response[3][:-1].decode("utf-8")) > len(output.decode("utf-8")):
					output = response[3][:-1]
				elif reqptc == "WCT":
					output += int(response[-1])
		if reqptc in ["UPP", "LOW"]:
			return "REP F " + str(len(output)) + " " + output + "\n"
		else:
			return "REP R " + str(len(str(output))) + " " + str(output) + "\n"

	def do_REQ(connection, reqptc):
		try:
			fileData = tcp_recv_file(connection)
			if(fileData[-1] != "\n"):
				return "REP ERR\n"
		except error as msg:
			print msg
			return "REP ERR\n"

		filename = str(getRequestNumber())
		filename = filename.zfill(5) #add leading zeroes
		f = open("input_files/"+filename, 'w')
		fileData = fileData[:-1] #removes \n
		f.write(fileData)
		f.close()

		responses = distribute_work(reqptc, fileData, filename)
		output = parse_ws_response(reqptc, responses)
		f = open("output_files/" + filename, 'w')
		f.write(output[:-1])
		f.close()
		return output

	def reply(message):
		connection.sendall(message) #was previously "WRP ERR\n" --> ask teacher
		connection.shutdown(SHUT_WR)
		connection.close()
		q.task_done()

	print "Worker is spawned"

	while 1:
		connection = q.get()

		if not connection and kill_event.is_set():
			q.task_done()
			exit()

		print "accepted user connection"

		#Protocol error checking begins

		try:
			data = tcp_recv(connection, stop_char=[" ","\n"])
		except error as msg:
			print msg
			reply("ERR\n")
			continue

		if data == "LST\n":
			ptcsLock.acquire() #LOCK ACQUIRE
			reply("FPT "+ str(len(ptcs)) + " " + " ".join(ptcs) + "\n")
			ptcsLock.release() #LOCK RELEASE

			continue
		elif data == "REQ ":
			try:
				reqptc = tcp_recv(connection, stop_char=" ")[:-1]
			except error as msg:
				print msg
				reply("REP ERR\n")
				continue
			ptcsLock.acquire() #LOCK ACQUIRE
			if reqptc not in ptcs:
				ptcsLock.release() #LOCK RELEASE
				reply("REP EOF\n")
				continue
			else:
				ptcsLock.release()
				try:
					reply(do_REQ(connection, reqptc))
					continue
				except (timeout, error) as msg:
					print msg
					reply("REP ERR\n")
					continue
				except:
					reply("REP ERR\n")
					continue
		else:
			reply("ERR")



def manage(port):
	CSport = port
	serverSocket = None
	messages = {"RegOk":"RAK OK\n", 
						"RegNotOk": "RAK NOK\n", 
						"UnRegOk": "UAK OK\n", 
						"UnRegNotOk": "UAK NOK\n", 
						"Error": "ERR\n"}

	def register_working_server(WSptcs, serverAddr):
		for i in WSptcs:
			if i not in get_PTCs():
				return messages["RegNotOk"]

		workingServersLock.acquire() #LOCK ACQUIRE

		if serverAddr not in workingServers:
			workingServers[serverAddr] = WSptcs
			ptcsLock.acquire() #LOCK ACQUIRE
			for i in WSptcs:
				if i not in ptcs:
					ptcs[i] = []
				ptcs[i] += [serverAddr]

			ptcsLock.release() #LOCK RELEASE
			workingServersLock.release() #LOCK RELEASE
			return messages["RegOk"]

		workingServersLock.release() #LOCK RELEASE
		return messages["RegNotOk"]


	def unregister_working_server(serverAddr):
		workingServersLock.acquire() #LOCK ACQUIRE

		if serverAddr in workingServers:

			ptcsLock.acquire() #LOCK ACQUIRE
			for i in workingServers[serverAddr]:
				ptcs[i].remove(serverAddr) #remove the working server from the ptcs he could work in
				if len(ptcs[i]) == 0: #if there is no working server capable of doing the PTC it should be removed from the available PTCs
					ptcs.pop(i, None)

			workingServers.pop(serverAddr, None)

			ptcsLock.release() #LOCK RELEASE
			workingServersLock.release() #LOCK RELEASE
			return messages["UnRegOk"]

		workingServersLock.release() #LOCK RELEASE
		return messages["UnRegNotOk"]
 

	def validate_received_message(message):
		if message != None:
			message = message.split()
			messageLenght = len(message)
			if message[0] == "REG" and messageLenght >= 4:
				return register_working_server(message[1:-2], (message[-2], int(message[-1])))
			elif message[0] == "UNR" and messageLenght == 3:
				return unregister_working_server((message[-2], int(message[-1])))
			else:
				return messages["Error"]

	print "Started managing working servers"
	while 1:
		try:
			if kill_event.is_set():
				exit()
			serverSocket = socket(AF_INET, SOCK_DGRAM)
			serverSocket.bind((gethostname(), CSport))

			serverSocket.settimeout(TIMEOUT)
			receivedMessage, serverAddress = serverSocket.recvfrom(512)

			print "IN MANAGER: A message was received"
			msg = validate_received_message(receivedMessage)

			serverSocket.sendto(msg, serverAddress)
			serverSocket.close()

		except timeout:
			receivedMessage = None
			clientAddress = None
			serverSocket.close()


class CS:
	def __init__(self, CSport):
		self.name = gethostname()
		self.port = CSport
		self.nrWorkingServers = 0
		self.requestSock = socket(AF_INET, SOCK_STREAM)
		self.requestSock.bind((self.name, self.port))
		self.requestSock.listen(1)

		self.threadList = []
		for i in range(THREAD_NUMBER):
			t = Thread(target=worker, args=(self,))
			t.daemon = True
		 	self.threadList.append(t)
		 	self.threadList[i].start()

		#WSmanager = working_server_manager(CSport) #Manages the working servers registration  
		t = Thread(target=manage, args=(CSport,))
		t.daemon = True
		self.WSMThread = t
		self.WSMThread.start()

	def getWork(self):
		connection, client_addr = tcp_accept(self.name, self.port, self.requestSock)
		q.put(connection)

	def die_gracefully(self):
		kill_event.set()
		self.requestSock.shutdown(SHUT_RDWR)
		self.requestSock.close()
		for i in self.threadList:
			q.put(None) #this is to unblock the thread so it can die
		for i in self.threadList:
			i.join()
			print "Worker thread joined."
		self.WSMThread.join()



if __name__ == "__main__":	
	parser = argparse.ArgumentParser(description="RC Project Central Server")
	parser.add_argument('-p', default=59000, type=str, metavar="CSport", help="The well-known port where the CS server accepts requests, in TCP.  This  is  an  optional  argument.  If  omitted,  it  assumes  the  value 58003. ")
	args = parser.parse_args()
	

	signal(SIGINT, graceful_killer)
	#signal(SIGTERM, graceful_killer)
	try:
		cs = CS(int(args.p))
	except error as msg:
		print msg
		try:
			cs.die_gracefully()
		except:
			pass
		finally:
			exit()

	while 1:
		if kill_event.is_set():
			cs.die_gracefully()
			print "Died gracefully. Exiting..."
			exit()

		try:
			cs.getWork()
		except error as msg:
			print msg
			kill_event.set()