#include "contas.h"
#include "auxiliar.h"
#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <signal.h>
#include <errno.h>
#include <pthread.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>


#define MAX(A, B) ((A < B) ? B : A)
#define SaldoNovo(SaldoAnterior) ( MAX( (int) (SaldoAnterior * (1 + TAXAJURO) - CUSTOMANUTENCAO), 0))
#define atrasar() sleep(ATRASO)
extern pthread_mutex_t mutexPool[NUM_CONTAS];
int sig_num = 0; /* flag de controlo do signal recebido do processo pai */
int contasSaldos[NUM_CONTAS];


int contaExiste(int idConta) {
	return (idConta > 0 && idConta <= NUM_CONTAS);
}

void inicializarContas() {
	int i;
  	for (i=0; i<NUM_CONTAS; i++)
    	contasSaldos[i] = 0;
}

void escreveFicheiro(char* nomeComando, int idConta, int idContaDestino, int valor, int file) {
    char comando_s[MAX_CHAR];
    pthread_t tid = pthread_self();

    if(idContaDestino == DEFAULT)
    	if(valor == DEFAULT)
    		snprintf(comando_s, MAX_CHAR, "%lu:%s(%d)\n", tid, nomeComando, idConta);
    	else
    		snprintf(comando_s, MAX_CHAR, "%lu:%s(%d, %d)\n", tid, nomeComando, idConta, valor);
    else
    	snprintf(comando_s, MAX_CHAR, "%lu:%s(%d, %d, %d)\n", tid, nomeComando, idConta, idContaDestino, valor);

    if(write(file, comando_s, strlen(comando_s)) < 0) {
      printf("problemas a escrever no ficheiro log.txt\n");
    }
}

int debitar(int idConta, int valor, int fd) {
	lock_mutex(&mutexPool[idConta-1]);
	atrasar();
	if (!contaExiste(idConta)){
		if(fd != DEFAULT)
			escreveFicheiro("debitar", idConta, DEFAULT, valor, fd);
		unlock_mutex(&mutexPool[idConta-1]);

		return -1;
	}
	if (contasSaldos[idConta - 1] < valor){
		if(fd != DEFAULT)
			escreveFicheiro("debitar", idConta, DEFAULT, valor, fd);
		unlock_mutex(&mutexPool[idConta-1]);

		return -1;
	}
  	atrasar();
  	contasSaldos[idConta - 1] -= valor;

		if(fd != DEFAULT)
			escreveFicheiro("debitar", idConta, DEFAULT, valor, fd);

		unlock_mutex(&mutexPool[idConta-1]);
  	return 0;
}

int debitar_sem_locks(int idConta, int valor) {
	atrasar();
  	if (!contaExiste(idConta)){
			return -1;
	}
  	if (contasSaldos[idConta - 1] < valor){
		return -1;
	}
  	atrasar();
  	contasSaldos[idConta - 1] -= valor;

  	return 0;
}

int creditar(int idConta, int valor, int fd) {
	atrasar();
	lock_mutex(&mutexPool[idConta-1]);
	if (!contaExiste(idConta)){
		if(fd != DEFAULT)
			escreveFicheiro("creditar",idConta, DEFAULT,  valor, fd);
		unlock_mutex(&mutexPool[idConta-1]);
  	return -1;
	}
  	contasSaldos[idConta - 1] += valor;

		if(fd != DEFAULT)
			escreveFicheiro("creditar", idConta, DEFAULT,  valor, fd);

		unlock_mutex(&mutexPool[idConta-1]);
  	return 0;
}

int creditar_sem_locks(int idConta, int valor) {
	atrasar();
	if (!contaExiste(idConta)){
  	return -1;
	}
  	contasSaldos[idConta - 1] += valor;

  	return 0;
}

int lerSaldo(int idConta, int fd) {
	atrasar();
	lock_mutex(&mutexPool[idConta-1]);
	if (!contaExiste(idConta)){
		unlock_mutex(&mutexPool[idConta-1]);
		if(fd != DEFAULT)
			escreveFicheiro("lerSaldo", idConta, DEFAULT, DEFAULT, fd);
		return -1;
	}
	if(fd != DEFAULT)
		escreveFicheiro("lerSaldo", idConta, DEFAULT, DEFAULT, fd);
	unlock_mutex(&mutexPool[idConta-1]);
	return contasSaldos[idConta - 1];
}

int transferir(int idContaOrigem, int idContaDestino, int valor, int fd) {
	atrasar();
	if(idContaOrigem == idContaDestino || valor < 0 || !contaExiste(idContaOrigem) || !contaExiste(idContaDestino) || lerSaldo(idContaOrigem, DEFAULT) < valor) {
		escreveFicheiro("transferir",idContaOrigem, idContaDestino,  valor, fd);
		return -1;
	}
	lock_mutex(&mutexPool[idContaOrigem-1]);
	lock_mutex(&mutexPool[idContaDestino-1]);

	creditar_sem_locks(idContaDestino,valor);
	debitar_sem_locks(idContaOrigem, valor);
	escreveFicheiro("transferir",idContaOrigem, idContaDestino,  valor, fd);

	unlock_mutex(&mutexPool[idContaOrigem-1]);
	unlock_mutex(&mutexPool[idContaDestino-1]);
	return 0;
}

void signalHandler(int SigNum) {
	sig_num = SIGNALHANDLER; /*ativa a flag de controlo do signal */
}

void simular(int numAnos) {
	int i, j, novo, file, saldo = 0;
	pid_t pid = getpid();
	char fileName[MAX_FILENAME];

	snprintf(fileName, MAX_FILENAME, "i-banco-sim-%d.txt", pid);

	file = startFile(fileName);
	dup2(file, STDOUT);
	for(i=0; i<=numAnos; i++) {
		printf("\nSIMULACAO: Ano %d\n", i);
	    printf("===================\n");

	    for(j=1; j<=NUM_CONTAS; j++) {
					if(i == 0){
	        	printf("Conta %d, Saldo %d\n", j, saldo = lerSaldo(j, DEFAULT));
					}
      		else {
      			saldo = lerSaldo(j, DEFAULT);
						while (printf("Conta %d, Saldo %d\n", j, novo = SaldoNovo(saldo)) < 0) {
								if (errno == EINTR)
									continue;
								else
									break;
						}
	        	creditar(j, novo - saldo, DEFAULT);
					}
	    }
		if(sig_num == SIGNALHANDLER){ /*termina a simulacao do ano apos ter recebido o signal */
			printf("Simulacao terminada por signal\n");
			closeFile(file);
			exit(0);
		}
  }
	closeFile(file);
	exit(0);
}
