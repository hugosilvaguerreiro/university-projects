#! /usr/bin/python2
import sys
import argparse
import traceback
from socket import *
from socket_aux import *
from subprocess import call

TIMEOUT = 3
GN = 3


class User:
	def __init__(self, name, port):
		self.messages = {"WCT":"The file has {1} words", "FLW":"The longest word has {0} letters: {1}",\
		 				 "UPP": "File saved as UPP_{2}", "LOW": "File saved as LOW_{2}"}
		self.server_address = (name, port)
		self.conn = None

	def print_list(self, response):
		words = response.split()

		if response == "FPT ERR\n":
			print "ERROR: LST request is not correctly formulated"
			return

		elif response == "FPT EOF\n":
			print "CS can not respond"
			return

		elif len(words) < 2 or len(words) > 2 + len(get_PTCs()) or words[0] != "FPT" \
		or not str_is_int(words[1]) or int(words[1]) != len(words)-2:
			print "ERROR: Bad format : " + response
			return

		for i in range(len(words)-2):
			if(words[i+2] in get_PTCs()):
				print str(i+1) + "- " + words[i+2] +" - "+ get_PTCs()[words[i+2]]
			else:
				print str(i+1) + "- " + words[i+2] +" - "+" PTC not recognized"
		

	def send_list(self):
		try:
			self.conn = tcp_connect(self.server_address)
			self.conn.settimeout(TIMEOUT)
			print "*Sending: LST"
			self.conn.sendall("LST\n")
			data = tcp_recv(self.conn)
			print "*Received: "+ data[:-1]
			self.print_list(data)
			self.conn.close()
		except (error,timeout) as msg:
			print msg

	def send_file(self, ptc, filename):
		try :
			self.conn = tcp_connect(self.server_address)
			self.conn.settimeout(TIMEOUT)
			file = open(filename, 'rb')
			data = file.read()
			file.close()
			print "*Sending: REQ "+ ptc + " " +str(len(data))+ " --data--"
			self.conn.sendall("REQ "+ ptc + " " +str(len(data))+ " " + data + "\n")
			response = ""
			response += tcp_recv(self.conn, stop_char=[" "])
			response += tcp_recv(self.conn, stop_char=[" "])
			if len(response) != 6 or response[:4] != "REP " or response[4:] not in ["F ", "R "]:
				print "ERROR: Bad response: " + response[:-1]
				self.conn.close()
				return
			file = tcp_recv_file(self.conn)
			response += str(len(file)-1) + " " + file
				
		except (error, timeout) as msg:
			print msg
			return

		response = response.split(" ", 3)
		print "*Received: "+" ".join(response[:3])+" --data--"
		print self.messages[ptc].format(len(response[3][:-1].decode("utf-8")),response[3][:-1], filename)
		if ptc == "LOW":
			text_file = open("LOW_" + filename, "w")
			text_file.write(response[3][:-1])
			text_file.close()
		elif ptc == "UPP":
			text_file = open("UPP_" + filename, "w")
			text_file.write(response[3][:-1])
			text_file.close()

		self.conn.close()




if __name__ == "__main__":
	parser = argparse.ArgumentParser(description="RC Project User App")
	parser.add_argument('-n', default=gethostname(), type=str, metavar="CSname", help="name of the machine where the central server (CS) runs. This is an optional argument. If this argument is omitted, the CS should be running on the same machine.")
	parser.add_argument('-p', default=58000+GN, type=int, metavar="CSport", help="port where the CS server accepts user requests, in TCP. This is an optional argument. If omitted, it assumes the value 58000+GN, where GN is the group number.")
	args = parser.parse_args()

	user = User(args.n, args.p)
	print "Type help for commands"
	try:
		while True:
			user_input = raw_input("> ")
			print "------------------------------------------------------------"
			response = user_input.split()
			if response[0] == 'exit' or response[0] == 'quit':
				exit()
			
			if response[0] == 'list':

				user.send_list()

				#contacta CS e pede lista de PTCs possiveis
			elif response[0] == 'request' and len(response) == 3:
				user.send_file(response[1], response[2])
			elif response[0] == 'cat':
				call(["cat",response[1]])
			elif response[0] == 'cls':
				call(["clear"])
				continue
			elif response[0] == 'help':
				print "list \t\t\t  print available PTCs"
				print "request <PTC> <filename>  requests the <PTC> operation on the specified file"
				print "cat <filename> \t\t  prints the file on the screen"
				print "cls \t\t\t  clears screen"
				print "exit \t\t\t  exits the client"
			else:
				print "command unrecognized"
			print "------------------------------------------------------------"
	except error as msg:
		print msg
	except KeyboardInterrupt as msg:
		print msg