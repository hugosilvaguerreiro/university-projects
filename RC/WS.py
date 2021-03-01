from socket import *
from socket_aux import *
from threading import *
from Queue import Queue
from select import select
from signal import *
import argparse, sys, traceback, re

TIMEOUT = 1
THREAD_NUMBER = 5

q=Queue()
kill_event=Event()

def graceful_killer(signum, frame):
	print "\nCtrl-C detected."
	kill_event.set()

def worker(ws):
	def reply(message):
		connection.sendall(message)
		connection.shutdown(SHUT_WR)
		connection.close()
		q.task_done()

	print "Worker is spawned"
	while 1:
		connection = q.get()

		if not connection or kill_event.is_set():
			q.task_done()
			exit()

		connection.settimeout(TIMEOUT)
		print "accepted CS request"
		try:
			data = ""
			for i in range(3):
				data += tcp_recv(connection, stop_char=[" "])
			file = tcp_recv_file(connection)
			data += str(len(file)-1) + " " + file[:-1]
			print ">>>>>>>>>>>>>>>>>>>>>>>>>> "+data

		except (error, timeout) as msg:
			print msg
			return
		print data
		print "received contents"
		
		data = data.split(" ", 4)


		#verify if it is a request
		if data[0] != "WRQ":
			print "Got some trash: " + data[0]
			reply("ERR\n")
			continue

		#verify that PTC is supported
		if data[1] not in ws.ptcs:
			print "PTC not supported: " + data[1] 
			reply("WRP EOF\n")
			continue

		#verify that size is a number
		try:
			data[3] = int(data[3])
		except:
			print "Num bytes is not an integer: " + str(data[3])
			reply("WRP ERR\n")
			continue

		#verify if size matches data size
		if len(data[4]) != data[3]:
			print "Data size does not match: " +  str(len(data[4])) + " vs " + str(data[3])
			reply("WRP ERR\n")
			continue

		job = data[1]
		filename = data[2]
		data = data[4]

		f = open("input_files/" + filename, 'w')
		f.write(data)
		f.close()
		try:
			if job == "LOW":
				print "LOW: " + filename
				data_lower = data.lower()
				reply("REP F " + str(len(data_lower)) + " " + data_lower+"\n")
			if job == "UPP":
				print "UPP: " + filename
				data_upper = data.upper()
				reply("REP F " + str(len(data_upper)) + " " + data_upper+"\n")
			if job == "WCT":
				print "WCT: " + filename
				data_list = re.compile('\w+', re.UNICODE).findall(data.decode("utf-8"))
				reply("REP R " + str(len(str(len(data_list)))) + " " + str(len(data_list))+"\n")
			if job == "FLW":
				print "FLW: " + filename
				data_list = re.compile('\w+', re.UNICODE).findall(data.decode("utf-8"))
				longest = max(data_list+[""], key=len)
				reply("REP R " + str(len(longest.encode("utf-8"))) + " " + longest.encode("utf-8") + "\n")
		except error as msg:
			print msg
			if connection:
				connection.shutdown(SHUT_WR)
				connection.close()
			q.task_done()

class WS:
	def __init__(self, WSname, WSport, PTCs):
		self.name = WSname
		self.port = WSport
		self.ptcs = PTCs
		self.sock = socket(AF_INET, SOCK_STREAM)
		self.sock.bind((self.name, self.port))
		self.sock.listen(1)
		self.threadList = []
		for i in range(THREAD_NUMBER):
			t = Thread(target=worker, args=(self,))
			t.daemon = True
			self.threadList.append(t)
			self.threadList[i].start()

		
	def getWork(self):
		connection, client_addr = tcp_accept(self.name, self.port, self.sock)
		q.put(connection)

	def die_gracefully(self):
		for i in self.threadList:
			q.put(None) #this is to unblock the thread so it can die
		for i in self.threadList:
			i.join()
			print "Worker thread joined."
		self.sock.shutdown(SHUT_WR)
		self.sock.close()

	def register(self, serverName, serverPort):
		message = "REG "+" ".join(self.ptcs)+" "+gethostbyname(gethostname())+" "+str(self.port)+"\n"
		returnStatus, CSadress = udp_client_msg(serverName, serverPort, message)
		return returnStatus

	def unregister(self, serverName, serverPort):
		message = "UNR "+gethostbyname(gethostname())+" "+str(self.port)+"\n"
		returnStatus, CSadress = udp_client_msg(serverName, serverPort, message)
		return returnStatus


if __name__ == "__main__":	
	parser = argparse.ArgumentParser(description="RC Project User App")
	parser.add_argument("PTCs", nargs='*', action='append')
	parser.add_argument('-p', default=59000, type=str, metavar="WSport", help="port where the WS server accepts process tasks, in TCP. This is an optional argument. If omitted, it assumes the value 59000.")
	parser.add_argument('-n', default=gethostname(), type=str, metavar="CSname", help="name of the machine where the central server (CS) runs. This is an optional argument. If this argument is omitted, the CS should be running on the same machine.")
	parser.add_argument('-e', default=58000+GN, type=int, metavar="CSport", help="port where the CS server accepts register requests, in UDP. This is an optional argument. If omitted, it assumes the value 58000+GN, where GN is the group number.")
	args = parser.parse_args()
	if len(args.PTCs[0]) == 0:
		print "ERROR: Need atleast one PTC. Exiting..."
		exit()
	for i in args.PTCs[0]:
		if i not in get_PTCs():
			print "ERROR: Argument '"+i+"' not recognized. Exiting..."
			exit()

	#signal(SIGINT, gracefull_killer)
	#signal(SIGTERM, gracefull_killer)

	try:
		ws = WS(gethostname(), int(args.p), args.PTCs[0])

		if ws.register(args.n, args.e) != "RAK OK\n":
			print "ERROR: the CS did not respond with OK status, (RAK ERR). Exiting..."
			ws.die_gracefully()
			exit()
	except:
		print "ERROR: Could not create and register WS. Exiting and showing traceback..."
		try:
			ws.die_gracefully()
		finally:
			exit()

	while 1:
		if kill_event.is_set():
			try:
				if ws.unregister(args.n, args.e) != "UAK OK\n":
					print "ERROR: the CS did not respond with OK status, (RAK ERR)."
			finally:
				ws.die_gracefully()
				print "Died gracefully. Exiting..."
				exit()

		try:
			ws.getWork()
		except:
			print "WARNING: TCP accept failed: 'tcp_accept' did not succeed. Exiting..."
			kill_event.set()



