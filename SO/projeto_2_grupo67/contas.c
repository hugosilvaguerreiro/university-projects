#include "contas.h"
#include "erros.h"
#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <signal.h>
#include <errno.h>
#include <pthread.h>

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

int debitar(int idConta, int valor) {
		if(pthread_mutex_lock(&mutexPool[idConta])!=0){
			ErrorMsg(MUTEX_LOCK);
			exit(EXIT_FAILURE);
		}
		atrasar();
  	if (!contaExiste(idConta)){
			if(pthread_mutex_unlock(&mutexPool[idConta])!= 0){
				ErrorMsg(MUTEX_UNLOCK);
				exit(EXIT_FAILURE);
			}
			return -1;
		}
  	if (contasSaldos[idConta - 1] < valor){
			if(pthread_mutex_unlock(&mutexPool[idConta])!= 0){
				ErrorMsg(MUTEX_UNLOCK);
				exit(EXIT_FAILURE);
			}
			return -1;
		}
  	atrasar();
  	contasSaldos[idConta - 1] -= valor;
		if(pthread_mutex_unlock(&mutexPool[idConta])!= 0){
			ErrorMsg(MUTEX_UNLOCK);
			exit(EXIT_FAILURE);
		}
  	return 0;
}

int creditar(int idConta, int valor) {
	atrasar();
	if(pthread_mutex_lock(&mutexPool[idConta])){
		ErrorMsg(MUTEX_LOCK);
		exit(EXIT_FAILURE);
	}
	if (!contaExiste(idConta)){
		if(pthread_mutex_unlock(&mutexPool[idConta])!= 0){
			ErrorMsg(MUTEX_UNLOCK);
			exit(EXIT_FAILURE);
		}
  	return -1;
	}
  	contasSaldos[idConta - 1] += valor;
		if(pthread_mutex_unlock(&mutexPool[idConta])!=0){
			ErrorMsg(MUTEX_UNLOCK);
			exit(EXIT_FAILURE);
		}
  	return 0;
}

int lerSaldo(int idConta) {
	atrasar();
	if(pthread_mutex_lock(&mutexPool[idConta])!=0){
		ErrorMsg(MUTEX_LOCK);
		exit(EXIT_FAILURE);
	}
  	if (!contaExiste(idConta)){
			if(pthread_mutex_unlock(&mutexPool[idConta])!=0){
				ErrorMsg(MUTEX_UNLOCK);
				exit(EXIT_FAILURE);
			}
			return -1;
		}
		if(pthread_mutex_unlock(&mutexPool[idConta])!=0){
			ErrorMsg(MUTEX_UNLOCK);
			exit(EXIT_FAILURE);
		}
  	return contasSaldos[idConta - 1];
}

void signalHandler(int SigNum) {
	sig_num = SIGNALHANDLER; /*ativa a flag de controlo do signal */
}

void simular(int numAnos) {
	int i, j, novo;

  	for(i=0; i<=numAnos; i++) {
		printf("\nSIMULACAO: Ano %d\n", i);
	    printf("===================\n");
	    for(j=1; j<=NUM_CONTAS; j++) {
			if(i == 0)
	        	printf("Conta %d, Saldo %d\n", j, lerSaldo(j));
      		else {
      			int saldo = lerSaldo(j);
						while (printf("Conta %d, Saldo %d\n", j, novo = SaldoNovo(saldo)) < 0) {
								if (errno == EINTR)
									continue;
								else
									break;
						}
	        	printf("Conta %d, Saldo %d\n", j, novo = SaldoNovo(saldo));
	        	creditar(j, novo - saldo);
					}
	    }
		if(sig_num == SIGNALHANDLER){ /*termina a simulacao do ano apos ter recebido o signal */
			printf("Simulacao terminada por signal\n");
			exit(0);
		}
  	}
	exit(0);
}
