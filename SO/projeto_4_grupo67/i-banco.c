/*
// Projeto SO - exercicio 4
// Sistemas Operativos, DEI/IST/ULisboa 2016-17
*/

#include "commandlinereader.h"
#include "contas.h"
#include "auxiliar.h"
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <wait.h>
#include <errno.h>
#include <pthread.h>
#include <semaphore.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>

#define TRUE 1
#define FALSE 0
#define COMANDO_DEBITAR "debitar"
#define COMANDO_CREDITAR "creditar"
#define COMANDO_LER_SALDO "lerSaldo"
#define COMANDO_SIMULAR "simular"
#define COMANDO_TRANSFERIR "transferir"
#define COMANDO_SAIR "sair"
#define COMANDO_SAIR_AGORA "agora"

#define FILENAME "log.txt"
#define PIPE_SERVIDOR "i-banco-pipe"
#define DEFAULT_NAME ""
#define PERMISSOES 0666

#define OPERADOR_DEBITAR_TAREFA 1
#define OPERADOR_CREDITAR_TAREFA 2
#define OPERADOR_LER_SALDO_TAREFA 3
#define OPERADOR_TRANSFERIR_TAREFA 4
#define OPERADOR_SAIR_TAREFA 5

#define OPERADOR_SIMULAR 6
#define OPERADOR_SAIR_AGORA 7

#define NUM_TRABALHADORAS 3
#define CMD_BUFFER_DIM (NUM_TRABALHADORAS * 2)
#define DEFAULT -1
#define MAXARGS 4
#define BUFFER_SIZE 100
#define MAX_PROCESSES 20
#define MAX_TERMINAIS 10

typedef struct{
  int operacao;
  int idConta;
  int idContaDestino;
  int valor;
  char pipeClienteName[MAX_CHAR];
} comando_t;

pthread_cond_t cond;

int numComandos = 0;
int buff_write_idx = 0, buff_read_idx = 0;
pthread_t pool[NUM_TRABALHADORAS]; /*pool de tarefas*/
pthread_mutex_t mutexPool[NUM_CONTAS]; /*pool de mutexes para sincronizar os acessos as contas*/
pthread_mutex_t mutex; /*mutexes para sincronizar a leitura e insercao no nometerminalcliente*/
pthread_mutex_t mutex_cond;
comando_t cmd_nometerminalcliente[CMD_BUFFER_DIM]; /*nometerminalcliente circular de de comandos*/
sem_t trabalhadoras, principal; /*semaforos*/


int fileLog; /*descritor do ficheiro do log.txt*/
int currentTerminalPipe;


void signalHandlerPipe(int SigNum) {
  printf("Terminal nao disponivel\n");
}

/*Funcao que insere comandos no nometerminalcliente*/
void inserirComando(comando_t comando){
  s_wait(&principal);
  lock_mutex(&mutex);

  cmd_nometerminalcliente[buff_write_idx]  = comando;

  buff_write_idx = (buff_write_idx + 1)%CMD_BUFFER_DIM;

  lock_mutex(&mutex_cond);
  numComandos++;
  unlock_mutex(&mutex_cond);

  unlock_mutex(&mutex);
  s_post(&trabalhadoras);
}

void lerComando(comando_t *comando){
  s_wait(&trabalhadoras);
  lock_mutex(&mutex);

  *comando = cmd_nometerminalcliente[buff_read_idx];
  buff_read_idx = (buff_read_idx + 1)%CMD_BUFFER_DIM;

  unlock_mutex(&mutex);
  s_post(&principal);
}

/*Recebe comandos do stdin e insere-os na nometerminalcliente de comandos*/
void tarefaPrincipal(){

  comando_t inputPipe  = {DEFAULT, DEFAULT, DEFAULT, DEFAULT, ""};

  int ibancoservidor;
  int num_processes = 0;

  printf("Bem-vinda/o ao i-banco\n\n");

  if((ibancoservidor = open(PIPE_SERVIDOR, O_RDONLY))<0)
      exit(EXIT_FAILURE);
  if((fileLog = startFile(FILENAME)) < 0) /* se o ficheiro nao for criado com sucesso entao a thread termina*/
    exit(EXIT_FAILURE);


  while (1) {
    read(ibancoservidor, &inputPipe, sizeof(comando_t)); /*ver o tamanho maximo do comando*/
    /* EOF (end of file) do stdin ou comando "sair" */
    if (inputPipe.operacao == OPERADOR_SAIR_TAREFA || inputPipe.operacao == OPERADOR_SAIR_AGORA) {
      int i;
      pid_t pid;
      int numFilhos = num_processes;

      /* sair agora */
      if(inputPipe.operacao == OPERADOR_SAIR_AGORA) {
        kill(0, SIGUSR1);
        inputPipe.operacao = OPERADOR_SAIR_TAREFA;
      }

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
        inserirComando(inputPipe);
      }
      /*Sincronizar a terminacao das tarefas*/
      for(i=0; i < NUM_TRABALHADORAS; i++){
        if(pthread_join(pool[i], NULL) != 0){
          errorMsg(THREAD_JOIN);
          exit(EXIT_FAILURE);
        }
      }
      printf("--\ni-banco terminou.\n");
      if(closeFile(ibancoservidor) < 0)
  		  exit(EXIT_FAILURE);

      unlink(PIPE_SERVIDOR);
      return;

    }

    if(inputPipe.operacao == OPERADOR_SIMULAR) {

          pid_t pid;

          if(num_processes < MAX_PROCESSES) {

            lock_mutex(&mutex_cond);

            while(numComandos > 0)pthread_cond_wait(&cond, &mutex_cond);
              pid = fork();

              unlock_mutex(&mutex_cond);

              if(pid < 0){
                exit(EXIT_FAILURE);
              }

              else if(pid == 0){
                simular(inputPipe.idConta);
              }
              else {
                num_processes++;
              }
          }

    }
    else if(!(strcmp(inputPipe.pipeClienteName, DEFAULT_NAME) == 0)) {

      if((currentTerminalPipe = open(inputPipe.pipeClienteName, O_WRONLY))<0) {
        if(errno == ENOENT )
          printf("%d\n", errno);
        exit(EXIT_FAILURE);
      }
      inserirComando(inputPipe);
      strcpy(inputPipe.pipeClienteName, DEFAULT_NAME);
    }
  }
  if(closeFile(fileLog) < 0)
  	exit(EXIT_FAILURE);
}

/*####################  DECREMENTA O CONTADOR DE COMANDOS NO BUFFER ################*/
void decrementa() {
  lock_mutex(&mutex_cond);
  numComandos--;
  if(numComandos == 0) {/*Enquanto nao enviar o signal para o processo principal iniciar o simular nao continua*/
    while(pthread_cond_signal(&cond) < 0) {
      errorMsg(COND_SIGNAL);
    }
  }
  unlock_mutex(&mutex_cond);
}

/*le os comandos e executa-os na sua repetiva tarefa*/
void *tarefasTrabalhadoras() {
  comando_t comando;

  while(TRUE) {
  	char msg[MAX_CHAR];
    lerComando(&comando);

    /*Debitar*/
    if(comando.operacao == OPERADOR_DEBITAR_TAREFA) {
      if (debitar (comando.idConta, comando.valor, fileLog) < 0) {
        snprintf(msg, MAX_CHAR, "debitar(%d, %d):Erro\n", comando.idConta, comando.valor);
        write(currentTerminalPipe, msg, MAX_CHAR);
        close(currentTerminalPipe);

      }
      else {
        snprintf(msg, MAX_CHAR, "debitar(%d, %d):OK\n", comando.idConta, comando.valor);
        write(currentTerminalPipe, msg, MAX_CHAR);
        close(currentTerminalPipe);

      }
      decrementa();
    }
    /*Creditar*/
    else if(comando.operacao == OPERADOR_CREDITAR_TAREFA) {
      if (creditar (comando.idConta, comando.valor, fileLog) < 0) {
        snprintf(msg, MAX_CHAR, "creditar(%d, %d):Erro\n", comando.idConta, comando.valor);
        write(currentTerminalPipe, msg, MAX_CHAR);
        close(currentTerminalPipe);

      }
      else {
        snprintf(msg, MAX_CHAR, "creditar(%d, %d):OK\n", comando.idConta, comando.valor);
        write(currentTerminalPipe, msg, MAX_CHAR);
        close(currentTerminalPipe);

      }
      decrementa();
    }
    /*LerSaldo*/
    else if(comando.operacao == OPERADOR_LER_SALDO_TAREFA) {
      int saldo = lerSaldo(comando.idConta, fileLog);
        if (saldo < 0) {
          snprintf(msg, MAX_CHAR, "lerSaldo(%d):Erro\n", comando.idConta);
          write(currentTerminalPipe, msg, MAX_CHAR);
          close(currentTerminalPipe);

        }
        else {
          snprintf(msg, MAX_CHAR, "lerSaldo(%d): O saldo da conta é %d\n", comando.idConta, saldo);
          write(currentTerminalPipe, msg, MAX_CHAR);
          close(currentTerminalPipe);

        }


        decrementa();
    }
    /*Transferir*/
    else if(comando.operacao == OPERADOR_TRANSFERIR_TAREFA){
      if (transferir (comando.idConta, comando.idContaDestino, comando.valor, fileLog) < 0){
        snprintf(msg, MAX_CHAR, "Erro ao transferir %d da conta %d para a conta %d\n",comando.valor, comando.idConta, comando.idContaDestino);
        write(currentTerminalPipe, msg, MAX_CHAR);
        close(currentTerminalPipe);

      }
      else{
        snprintf(msg, MAX_CHAR, "transferir(%d, %d, %d): OK\n", comando.idConta,comando.idContaDestino, comando.valor);
        write(currentTerminalPipe, msg, MAX_CHAR);
        close(currentTerminalPipe);

      }
      decrementa();
    }
    else if(comando.operacao == OPERADOR_SAIR_TAREFA){
      pthread_exit(NULL);
    }
    else{
      errorMsg(-1);
      pthread_exit(NULL); /*caso, por alguma razao, o fluxo do programa chegue aqui*/
    }
  }
}

int main (int argc, char** argv) {
  int i;

  unlink(PIPE_SERVIDOR);
  if(mkfifo(PIPE_SERVIDOR, PERMISSOES) < 0) {
    exit(EXIT_FAILURE);
  }

  if(signal(SIGUSR1, signalHandler) == SIG_ERR){
    errorMsg(SIGNAL_INIT);
    exit(EXIT_FAILURE);
  }

  if(signal(SIGPIPE, signalHandlerPipe) == SIG_ERR){
    errorMsg(SIGNAL_INIT);
    exit(EXIT_FAILURE);
  }
  if(pthread_cond_init(&cond, NULL) != 0) {
    errorMsg(COND_INIT);
    exit(EXIT_FAILURE);
  }

  for(i=0; i< CMD_BUFFER_DIM; i++){
    comando_t comando = {DEFAULT, DEFAULT, DEFAULT, DEFAULT,""};
    cmd_nometerminalcliente[i] =  comando;
  }

  if(pthread_mutex_init(&mutex_cond, NULL) != 0) {
    errorMsg(MUTEX_INIT);
    exit(EXIT_FAILURE);
  }

  if(pthread_mutex_init(&mutex, NULL) != 0) {
    errorMsg(MUTEX_INIT);
    exit(EXIT_FAILURE);
  }

  for(i=0;i<NUM_CONTAS; i++){
    if(pthread_mutex_init(&mutexPool[i],NULL)!= 0){
        errorMsg(MUTEX_INIT);
        exit(EXIT_FAILURE);
    }
  }

  if((sem_init(&trabalhadoras,0,0) != 0 || sem_init(&principal, 0, CMD_BUFFER_DIM)) != 0){
    errorMsg(SEM_INIT);
    exit(EXIT_FAILURE);
  }

  for(i = 0; i < NUM_TRABALHADORAS; i++) {
    if(pthread_create(&pool[i], NULL, &tarefasTrabalhadoras, NULL)!=0){
      errorMsg(THREAD_CREATE);
      exit(EXIT_FAILURE);
    }
  }

  inicializarContas();
  tarefaPrincipal();

  if(pthread_mutex_destroy(&mutex) != 0){
    errorMsg(MUTEX_DESTROY);
  }

  for(i=0; i< NUM_CONTAS; i++){
    if(pthread_mutex_destroy(&mutexPool[i])!= 0){
      errorMsg(MUTEX_DESTROY);
    }
  }
  if(sem_destroy(&trabalhadoras) != 0 || sem_destroy(&principal) != 0){
    errorMsg(SEM_DESTROY);
  }

  if(pthread_cond_destroy(&cond) < 0)
    errorMsg(COND_DESTROY);
  return 0;
}
