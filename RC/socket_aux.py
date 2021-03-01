from socket import *

GN = 3
TIMEOUT = 5
NRTRIES= 3


DESCR = {"WCT":"word count", "UPP":"convert to upper case", "LOW":"convert to lower case", "FLW":"find longest word"}

def get_PTCs():
	return DESCR


def str_is_int(string):
	try:
		int(string)
		return True
	except:
		return False

#TCP WRAPPERS

def tcp_connect(server_address):
	# Create a TCP/IP socket
	sock = socket(AF_INET, SOCK_STREAM)
	while 1:
		print 'connecting to %s port %s' % server_address
		try:
			sock.connect(server_address)
			return sock
		except:
			sock.shutdown(SHUT_WR)
			sock.close()
			raise #re raises last exception

def tcp_accept(name, port, sock):
	print "Waiting at " + name + " " + str(port) 
	connection, client_address = sock.accept()
	return connection, client_address


def tcp_recv(conn, close=False, stop_char="", nbytes=-1):
	data = ""
	try:
		while nbytes != 0:
			if nbytes != -1:
				nbytes -= 1

			rec = conn.recv(1)
			data += rec
			if len(rec) == 0 or data[-1] in stop_char:
				break
	finally:
		if close:
			conn.shutdown(SHUT_WR)
			conn.close()
		return data

#WIP
def tcp_recv_file(conn):
	size = tcp_recv(conn, stop_char=" ")
	size = int(size)
	data = tcp_recv(conn, nbytes = size+1)
	return data

#UDP WRAPPERS

def udp_client_msg(server, port, msg, tries = NRTRIES):
	connected = False

	clientSocket = socket(AF_INET, SOCK_DGRAM)
	clientSocket.settimeout(TIMEOUT)

	print "Sending datagram to "+str(server)+":"+str(port)

	while not connected and tries > 0:
		try:
			tries -= 1
			clientSocket.sendto(msg,(server, port))
			retMessage, serverAddress = clientSocket.recvfrom(16)
			connected = True
		except timeout:
			if tries > 0:
				print "ERROR: Timeout in connection. Retrying connection..."
			else:
				print "ERROR: No response check connection..."
				raise timeout
		finally:
			clientSocket.close()

	return retMessage, serverAddress

def udp_server_msg(name, port, msg):
	connected = False

	serverSocket = socket(AF_INET, SOCK_DGRAM)
	serverSocket.bind(name, port)

	serverSocket.settimeout(TIMEOUT)
	try:
		receivedMessage, serverAddress = clientSocket.recvfrom(512)
		serverSocket.sendto(msg, serverAddress)
	finally:
		serverSocket.close()

	return receivedMessage, clientAddress

'''print "ERROR: recv_all went wrong"'''