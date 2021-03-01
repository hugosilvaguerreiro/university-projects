CFLAGS = -g -Wall -pedantic

i-banco: contas.o commandlinereader.o i-banco.o erros.o
	gcc -pthread -o i-banco i-banco.o contas.o commandlinereader.o erros.o

contas.o: contas.c contas.h erros.h
	gcc $(CFLAGS) -c contas.c

commandlinereader.o: commandlinereader.c commandlinereader.h
	gcc $(CFLAGS) -c commandlinereader.c

erros.o: erros.c erros.h
	gcc $(CFLAGS) -c erros.c

i-banco.o: i-banco.c contas.h commandlinereader.h erros.h
	gcc $(CFLAGS) -c i-banco.c

clean:
	rm -f *.o i-banco
