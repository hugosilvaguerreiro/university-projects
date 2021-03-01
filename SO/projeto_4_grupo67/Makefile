CFLAGS = -g -Wall -pedantic

all: i-banco-terminal i-banco 

i-banco-terminal: commandlinereader.o i-banco-terminal.o
	gcc -o i-banco-terminal i-banco-terminal.o commandlinereader.o

i-banco: contas.o commandlinereader.o i-banco.o auxiliar.o
	gcc -pthread -o i-banco i-banco.o contas.o commandlinereader.o auxiliar.o

i-banco-terminal.o: i-banco-terminal.c commandlinereader.h
	gcc $(CFLAGS) -c i-banco-terminal.c

i-banco.o: i-banco.c contas.h commandlinereader.h auxiliar.h
	gcc $(CFLAGS) -c i-banco.c

contas.o: contas.c contas.h auxiliar.h
	gcc $(CFLAGS) -c contas.c

commandlinereader.o: commandlinereader.c commandlinereader.h
	gcc $(CFLAGS) -c commandlinereader.c

auxiliar.o: auxiliar.c auxiliar.h
	gcc $(CFLAGS) -c auxiliar.c

clean:
	rm -f *.o i-banco i-banco-terminal *.txt
