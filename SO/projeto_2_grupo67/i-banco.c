/*
// Projeto SO - exercicio 1, version 1
// Sistemas Operativos, DEI/IST/ULisboa 2016-17
*/

#include "commandlinereader.h"
#include "contas.h"
#include "erros.h"
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <wait.h>
#include <errno.h>
#include <pthread.h>
#include <semaphore.h>
#define TRUE 1
#define FALSE 0
#define COMANDO_DEBITAR "debitar"
#define COMANDO_CREDITAR "creditar"
#define COMANDO_LER_SALDO "lerSaldo"
#define COMANDO_SIMULAR "simular"
#define COMANDO_SAIR "sair"
#define COMANDO_SAIR_AGORA "agora"
#define OPERADOR_DEBITAR_TAREFA 1
#define OPERADOR_CREDITAR_TAREFA 2
#define OPERADOR_LER_SALDO_TAREFA 3
#define OPERADOR_SAIR_TAREFA 4
#define NUM_TRABALHADORAS 3
#define CMD_BUFFER_DIM (NUM_TRABALHADORAS * 2)
#define DEFAULT -1
#define MAXARGS 3
#define BUFFER_SIZE 100
#define MAX_PROCESSES 20

typedef struct{
  int operacao;
  int idConta;
  int valor;
} comando_t;

int buff_write_idx = 0, buff_read_idx = 0;
pthread_t pool[NUM_TRABALHADORAS]; /*pool de tarefas*/
pthread_mutex_t mutexPool[NUM_CONTAS]; /*pool de mutexes para sincronizar os acessos as contas*/
pthread_mutex_t mutex, mutex; /*mutexes para sincronizar a leitura e insercao no buffer*/
comando_t cmd_buffer[CMD_BUFFER_DIM]; /*buffer circular de de comandos*/
sem_t trabalhadoras, principal; /*semaforos*/

/*Funcao que insere comandos no buffer*/
void inserirComando(int operacao, int idconta, int valor){
  if(sem_wait(&principal)!=0){
    ErrorMsg(SEM_WAIT);
    exit(EXIT_FAILURE);
  }
  if(pthread_mutex_lock(&mutex)!= 0){
    ErrorMsg(MUTEX_LOCK);
    exit(EXIT_FAILURE);
  }
  cmd_buffer[buff_write_idx].operacao = operacao;
  cmd_buffer[buff_write_idx].idConta = idconta;
  cmd_buffer[buff_write_idx].valor = valor;
  buff_write_idx = (buff_write_idx + 1)%CMD_BUFFER_DIM;

  if(pthread_mutex_unlock(&mutex)!= 0){
      ErrorMsg(MUTEX_UNLOCK);
      exit(EXIT_FAILURE);
  }
  if(sem_post(&trabalhadoras)!=0){
    ErrorMsg(SEM_POST);
    exit(EXIT_FAILURE);
  }
}

void lerComando(comando_t *comando){
  if(sem_wait(&trabalhadoras)!=0){
    ErrorMsg(SEM_WAIT);
    exit(EXIT_FAILURE);
  }
  if(pthread_mutex_lock(&mutex)!=0){
      ErrorMsg(MUTEX_LOCK);
      exit(EXIT_FAILURE);
  }
  *comando = cmd_buffer[buff_read_idx];
  buff_read_idx = (buff_read_idx + 1)%CMD_BUFFER_DIM;
  if(pthread_mutex_unlock(&mutex)!=0){
    ErrorMsg(MUTEX_UNLOCK);
    exit(EXIT_FAILURE);
  }
  if(sem_post(&principal)!=0){
    ErrorMsg(SEM_POST);
    exit(EXIT_FAILURE);
  }
}

/*Recebe comandos do stdin e insere-os na buffer de comandos*/
void tarefaPrincipal(){
  char *args[MAXARGS + 1];
  char buffer[BUFFER_SIZE];
  int num_processes = 0; /*variavel de controlo do numero total de processos criados*/

  printf("Bem-vinda/o ao i-banco\n\n");
  while (1) {
    int i;
    int numargs;
    numargs = readLineArguments(args, MAXARGS+1, buffer, BUFFER_SIZE);
    /* EOF (end of file) do stdin ou comando "sair" */
    if (numargs < 0 || (numargs > 0 && (strcmp(args[0], COMANDO_SAIR) == 0))) {
      pid_t pid;
      int numFilhos = num_processes;
      /* sair agora */

      if(args[1] != NULL && (strcmp(args[1], COMANDO_SAIR_AGORA) == 0))
        kill(0, SIGUSR1);

      printf("i-banco vai terminar.\n--\n");

      while (numFilhos > 0) {
        int status;
        pid = wait(&status);

        if (pid < 0) {
          if (errno == EINTR) {
            continue;
          }
          else {
            perror("Erro inesperado ao esperar por processo filho.");
            exit (EXIT_FAILURE);
          }
        }
        numFilhos--;

        if(WIFEXITED(status))
          printf("FILHO TERMINADO (PID=%d; terminou normalmente)\n", pid);
        else
          printf("FILHO TERMINADO (PID=%d; terminou abruptamente)\n", pid);
      }
      /*Enviar comando para o buffer*/
      for(i =0; i < NUM_TRABALHADORAS; i++){
        inserirComando(OPERADOR_SAIR_TAREFA,0,0);
      }
      /*Sincronizar a terminacao das tarefas*/
      for(i=0; i < NUM_TRABALHADORAS; i++){
        if(pthread_join(pool[i], NULL) != 0){
          ErrorMsg(THREAD_JOIN);
          exit(EXIT_FAILURE);
        }
      }
      printf("--\ni-banco terminou.\n");

      return;

    }
    else if (numargs == 0)
    /* Nenhum argumento; ignora e volta a pedir */
      continue;

    /* Simular */
    else if (strcmp(args[0], COMANDO_SIMULAR) == 0) {
      pid_t pid;

      if(numargs < 2)
        printf("%s: Sintaxe inválida, tente de novo.\n", COMANDO_SIMULAR);

      else if(num_processes < MAX_PROCESSES) {
        pid = fork();

        if(pid < 0){
          perror("Erro no fork");
          exit(EXIT_FAILURE);
        }

        else if(pid == 0)
          simular(atoi(args[1]));

        else
          num_processes++;
      }
      else
        printf("Numero de processos maximo atingido\n");
    }
    else if(!strcmp(args[0], COMANDO_DEBITAR)){

      if (numargs < 3) {
        printf("%s: Sintaxe inválida, tente de novo.\n", COMANDO_DEBITAR);
        continue;
      }
      inserirComando(OPERADOR_DEBITAR_TAREFA, atoi(args[1]), atoi(args[2]));
    }

    else if(!strcmp(args[0], COMANDO_CREDITAR)){
      if (numargs < 3) {
        printf("%s: Sintaxe inválida, tente de novo.\n", COMANDO_CREDITAR);
        continue;
      }
      inserirComando(OPERADOR_CREDITAR_TAREFA, atoi(args[1]), atoi(args[2]));
      }

    else if(!strcmp(args[0], COMANDO_LER_SALDO)){
      if (numargs < 2) {
        printf("%s: Sintaxe inválida, tente de novo.\n", COMANDO_LER_SALDO);
        continue;
      }
      inserirComando(OPERADOR_LER_SALDO_TAREFA, atoi(args[1]), 0);
    }
    else{
      printf("Comando desconhecido. Tente de novo.\n");
    }
  }
}

/*le os comandos e executa-os na sua repetiva tarefa*/
void *tarefasTrabalhadoras(){
  comando_t comando;
  while(TRUE){
    lerComando(&comando);
    /*Debitar*/
    if(comando.operacao == OPERADOR_DEBITAR_TAREFA){
      if (debitar (comando.idConta, comando.valor) < 0)
        printf("%s(%d, %d): Erro\n\n", COMANDO_DEBITAR, comando.idConta, comando.valor);
      else
        printf("%s(%d, %d): OK\n\n", COMANDO_DEBITAR, comando.idConta, comando.valor);
    }
    /*Creditar*/
    else if(comando.operacao == OPERADOR_CREDITAR_TAREFA){
      if (creditar (comando.idConta, comando.valor) < 0)
        printf("%s(%d, %d): Erro\n\n", COMANDO_CREDITAR, comando.idConta, comando.valor);
      else
        printf("%s(%d, %d): OK\n\n", COMANDO_CREDITAR, comando.idConta, comando.valor);
    }
    /*LerSaldo*/
    else if(comando.operacao == OPERADOR_LER_SALDO_TAREFA){
      int saldo = lerSaldo(comando.idConta);
        if (saldo < 0)
          printf("%s(%d): Erro.\n\n", COMANDO_LER_SALDO, comando.idConta);
        else
          printf("%s(%d): O saldo da conta é %d.\n\n", COMANDO_LER_SALDO, comando.idConta, saldo);
    }
    else if(comando.operacao == OPERADOR_SAIR_TAREFA){
      pthread_exit(NULL);
    }
  }
  ErrorMsg(-1);
  pthread_exit(NULL); /*caso, por alguma razao, o fluxo do programa chegue aqui*/
}

int main (int argc, char** argv) {
  int i;

  for(i=0; i< CMD_BUFFER_DIM; i++){
    comando_t comando = {DEFAULT, DEFAULT, DEFAULT};
    cmd_buffer[i] =  comando;
  }

  if(pthread_mutex_init(&mutex, NULL) != 0)
    ErrorMsg(MUTEX_INIT);

  for(i=0;i<NUM_CONTAS; i++){
    if(pthread_mutex_init(&mutexPool[i],NULL)!= 0){
        ErrorMsg(MUTEX_INIT);
        exit(EXIT_FAILURE);
    }
  }

  if((sem_init(&trabalhadoras,0,0) != 0 || sem_init(&principal, 0, CMD_BUFFER_DIM)) != 0){
    ErrorMsg(SEM_INIT);
    exit(EXIT_FAILURE);
  }
  for(i = 0; i < NUM_TRABALHADORAS; i++){
    if(pthread_create(&pool[i], NULL, &tarefasTrabalhadoras, NULL)!=0){
      ErrorMsg(THREAD_CREATE);
      exit(EXIT_FAILURE);
    }
  }
  /*instalacao do signal handler*/
  if(signal(SIGUSR1, signalHandler) == SIG_ERR){
    ErrorMsg(SIGNAL_INIT);
		exit(EXIT_FAILURE);
	}
	inicializarContas();
  tarefaPrincipal();

  if(pthread_mutex_destroy(&mutex) != 0)
    ErrorMsg(MUTEX_DESTROY);

  for(i=0; i< NUM_CONTAS; i++){
    if(pthread_mutex_destroy(&mutexPool[i])!= 0){
      ErrorMsg(MUTEX_DESTROY);
    }
  }
  if(sem_destroy(&trabalhadoras) != 0 || sem_destroy(&principal) != 0){
    ErrorMsg(SEM_DESTROY);
  }
  exit(EXIT_SUCCESS);
}
