/*
// Operações sobre contas, versao 1
// Sistemas Operativos, DEI/IST/ULisboa 2016-17
*/

#ifndef CONTAS_H
#define CONTAS_H

#include "auxiliar.h"

#define NUM_CONTAS 10
#define TAXAJURO 0.1
#define CUSTOMANUTENCAO 1

#define ATRASO 1
#define SIGNALHANDLER 1 /*macro para gerir a flag tratada no signal handler do SIGUSR1 */

#define MAX_FILENAME 50
#define MAX_CHAR 100

#define STDOUT 1
#define DEFAULT -1

void inicializarContas();
int contaExiste(int idConta);
int debitar(int idConta, int valor,int fd);
int debitar_sem_locks(int idConta, int valor);
int creditar(int idConta, int valor, int fd);
int creditar_sem_locks(int idConta, int valor);
int lerSaldo(int idConta, int fd);
void signalHandler(int sigNum); /* funcao para ativar a flag de controlo do signal */
void simular(int numAnos);
int transferir(int idContaOrigem, int idContaDestino, int valor, int fd);


#endif