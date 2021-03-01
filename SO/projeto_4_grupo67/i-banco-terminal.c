#include "commandlinereader.h"

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
#include <time.h>

#define TRUE 1
#define FALSE 0
#define COMANDO_DEBITAR "debitar"
#define COMANDO_CREDITAR "creditar"
#define COMANDO_LER_SALDO "lerSaldo"
#define COMANDO_SIMULAR "simular"
#define COMANDO_TRANSFERIR "transferir"
#define COMANDO_SAIR "sair"
#define COMANDO_SAIR_AGORA "agora"
#define COMANDO_SAIR_TERMINAL "sair-terminal"

#define OPERADOR_DEBITAR_TAREFA 1
#define OPERADOR_CREDITAR_TAREFA 2
#define OPERADOR_LER_SALDO_TAREFA 3
#define OPERADOR_TRANSFERIR_TAREFA 4
#define OPERADOR_SAIR_TAREFA 5

#define OPERADOR_SIMULAR 6
#define OPERADOR_SAIR_AGORA 7

#define DEFAULT -1

#define PIPE_SERVIDOR "i-banco-pipe"
#define PERMISSOES 0666
#define MAXARGS 4
#define BUFFER_SIZE 100
#define MAX_CHAR 100

typedef struct{
  int operacao;
  int idConta;
  int idContaDestino;
  int valor;
  char pipeClienteName[MAX_CHAR];
} comando_t;

comando_t criaComando(int operacao, int idConta, int idContaDestino, int valor,char* pipeClienteName) {
  comando_t comando = {DEFAULT, DEFAULT, DEFAULT, DEFAULT, ""};
  comando.operacao = operacao;
  comando.idConta = idConta;
  comando.idContaDestino = idContaDestino;
  comando.valor = valor;
  strcpy(comando.pipeClienteName, pipeClienteName);
  return comando;
}


int main(int argc, char **argv) {
    int ibancoservidor, ibancoterminal;
    char *args[MAXARGS + 1];
    char buffer[BUFFER_SIZE];
    char pipename[MAX_CHAR];

    if(signal(SIGPIPE, SIG_IGN) == SIG_ERR) {
      exit(EXIT_FAILURE);
    }

    strcpy(pipename, argv[1]);
    unlink(pipename);

    if(mkfifo(pipename, PERMISSOES) < 0) {
      exit(EXIT_FAILURE);
    }


    while (1) {
      int numargs;
      time_t inicio, fim;
      comando_t comando;
      char msg[MAX_CHAR];
      numargs = readLineArguments(args, MAXARGS+1, buffer, BUFFER_SIZE);

      /*sair e sair agora*/
      if (numargs > 0 && (strcmp(args[0], COMANDO_SAIR) == 0)){
            if(numargs > 1 && (strcmp(args[1], COMANDO_SAIR_AGORA) == 0)) {
              int deveExecutar = TRUE;
              if((ibancoservidor = open(PIPE_SERVIDOR, O_WRONLY))<0) {
                printf("i-banco nao disponivel\n\n");
                deveExecutar = FALSE;
              }
              /*apenas envia os comandos para o i-banco se o ibanco estiver ativo*/
              if(deveExecutar) {
                comando = criaComando(OPERADOR_SAIR_AGORA, DEFAULT, DEFAULT, DEFAULT, pipename);
                if(write(ibancoservidor, &comando, MAX_CHAR) < 0) {
                  if(errno == EPIPE)
                    printf("i-banco nao disponivel\n\n");/*broken pipe*/
                }
                close(ibancoservidor);
              }
            }
            else {
              int deveExecutar = TRUE;
              if((ibancoservidor = open(PIPE_SERVIDOR, O_WRONLY))<0) {
                printf("i-banco nao disponivel\n\n");
                deveExecutar = FALSE;
              }
              /*apenas envia os comandos para o i-banco se o ibanco estiver ativo*/
              if(deveExecutar) {
                comando = criaComando(OPERADOR_SAIR_TAREFA, DEFAULT, DEFAULT, DEFAULT, pipename);
                if(write(ibancoservidor, &comando, MAX_CHAR) < 0) {
                  if(errno == EPIPE)
                  printf("i-banco nao disponivel\n\n");/*broken pipe*/
                }
                close(ibancoservidor);
                unlink(PIPE_SERVIDOR);
              }
            }
        }
      else if (numargs == 0)
      /* Nenhum argumento; ignora e volta a pedir */
        continue;

      /* Simular */
      else if (strcmp(args[0], COMANDO_SIMULAR) == 0) {
        if(numargs < 2) {
          printf("%s: Sintaxe inválida, tente de novo.\n\n", COMANDO_SIMULAR);
          continue;
        }
        else {
          int deveExecutar = TRUE;
          if((ibancoservidor = open(PIPE_SERVIDOR, O_WRONLY))<0) {
            printf("i-banco nao disponivel\n\n");
            deveExecutar = FALSE;
          }
            /*apenas envia os comandos para o i-banco se o ibanco estiver ativo*/
          if(deveExecutar) {
            comando = criaComando(OPERADOR_SIMULAR, atoi(args[1]), DEFAULT, DEFAULT, pipename);
            if(write(ibancoservidor, &comando, MAX_CHAR) < 0 ) {
                if(errno == EPIPE)
                  printf("i-banco nao disponivel\n\n");/*broken pipe*/
            }
          }
        }
      }

      /*sair terminal*/
      else if (numargs < 0 || (strcmp(args[0], COMANDO_SAIR_TERMINAL) == 0)) {
        unlink(pipename);
        exit(EXIT_SUCCESS);
      }

      /*debitar */
      else if(!strcmp(args[0], COMANDO_DEBITAR)){

        if (numargs < 3) {
          printf("%s: Sintaxe inválida, tente de novo.\n\n", COMANDO_DEBITAR);
          continue;
        }
        else {
          int deveLer = TRUE, deveExecutar = TRUE;
          if((ibancoservidor = open(PIPE_SERVIDOR, O_WRONLY))<0) {
            printf("i-banco nao disponivel\n\n");
            deveExecutar = FALSE;
          }
            /*apenas envia os comandos para o i-banco se o ibanco estiver ativo*/
          if(deveExecutar) {
            comando = criaComando(OPERADOR_DEBITAR_TAREFA, atoi(args[1]), DEFAULT, atoi(args[2]), pipename);
            time(&inicio);
            if(write(ibancoservidor, &comando, MAX_CHAR) < 0 ) {
              if(errno == EPIPE) {
                printf("i-banco nao disponivel\n\n");/*broken pipe*/
              }
              deveLer = FALSE;
              close(ibancoservidor);
            }
            if(deveLer) {
                if((ibancoterminal = open(pipename, O_RDONLY))<0)
                  return -1;

                read(ibancoterminal, msg, MAX_CHAR);

                printf("%s", msg);
                time(&fim);
                printf("Tempo de execucao: %fs\n\n",difftime(fim, inicio));
                close(ibancoterminal);
            }
          }
        }
      }

      else if(!strcmp(args[0], COMANDO_CREDITAR)) {
        if (numargs < 3) {
          printf("%s: Sintaxe inválida, tente de novo.\n\n", COMANDO_CREDITAR);
          continue;
        }
        else {
          int deveLer = TRUE, deveExecutar = TRUE;
          if((ibancoservidor = open(PIPE_SERVIDOR, O_WRONLY))<0) {
            printf("i-banco nao disponivel\n\n");
            deveExecutar = FALSE;
          }
          /*apenas envia os comandos para o i-banco se o ibanco estiver ativo*/
          if(deveExecutar){
            comando = criaComando(OPERADOR_CREDITAR_TAREFA, atoi(args[1]), DEFAULT, atoi(args[2]), pipename);
            time(&inicio);
            if(write(ibancoservidor, &comando, MAX_CHAR) < 0 ) {
              if(errno == EPIPE) {
                printf("i-banco nao disponivel\n\n");/*broken pipe*/
              }
              deveLer = FALSE;
              close(ibancoservidor);
            }
            if(deveLer) {
              if((ibancoterminal = open(pipename, O_RDONLY))<0)
                return -1;

              read(ibancoterminal, msg, MAX_CHAR);
              printf("%s", msg);
              time(&fim);
              printf("Tempo de execucao: %fs\n\n",difftime(fim, inicio));
              close(ibancoterminal);
            }
          }
        }
      }

      else if(!strcmp(args[0], COMANDO_LER_SALDO)) {
        if (numargs < 2) {
          printf("%s: Sintaxe inválida, tente de novo.\n\n", COMANDO_LER_SALDO);
          continue;
        }
        else {
          int deveLer = TRUE, deveExecutar = TRUE;
          if((ibancoservidor = open(PIPE_SERVIDOR, O_WRONLY)) < 0) {
            printf("i-banco nao disponivel\n\n");
            deveExecutar = FALSE;
          }
          /*apenas envia os comandos para o i-banco se o ibanco estiver ativo*/
          if(deveExecutar) {
            comando = criaComando(OPERADOR_LER_SALDO_TAREFA, atoi(args[1]), DEFAULT, DEFAULT, pipename);
            time(&inicio);
            if(write(ibancoservidor, &comando, MAX_CHAR) < 0 ) {
              if(errno == EPIPE) {
                printf("i-banco nao disponivel\n\n");/*broken pipe*/
              }
              close(ibancoservidor);
              deveLer = FALSE;
            }
            else if(deveLer) {
              if((ibancoterminal = open(pipename, O_RDONLY))<0)
                return -1;

              read(ibancoterminal, msg, MAX_CHAR);
              printf("%s", msg);
              time(&fim);
              printf("Tempo de execucao: %fs\n\n",difftime(fim, inicio));
              close(ibancoterminal);
            }
          }
        }
      }

      else if(!strcmp(args[0], COMANDO_TRANSFERIR)){
        if (numargs < 4) {
          printf("%s: Sintaxe inválida, tente de novo.\n\n", COMANDO_TRANSFERIR);
          continue;
        }
        else {
          int deveLer = TRUE, deveExecutar = TRUE;

          if((ibancoservidor = open(PIPE_SERVIDOR, O_WRONLY))<0) {
            printf("i-banco nao disponivel\n\n");
            deveExecutar = FALSE;
          }
          /*apenas envia os comandos para o i-banco se o ibanco estiver ativo*/
          if(deveExecutar) {
            comando = criaComando(OPERADOR_TRANSFERIR_TAREFA, atoi(args[1]), atoi(args[2]), atoi(args[3]), pipename);
            time(&inicio);

            if(write(ibancoservidor, &comando, MAX_CHAR) < 0 ) {
              if(errno == EPIPE) {
                printf("i-banco nao disponivel\n\n");/*broken pipe*/
              }
              deveLer = FALSE;
              close(ibancoservidor);
            }
            else if(deveLer) {
              if((ibancoterminal = open(pipename, O_RDONLY))<0)
                return -1;

              read(ibancoterminal, msg, MAX_CHAR);
              printf("%s", msg);
              time(&fim);
              printf("Tempo de execucao: %fs\n\n",difftime(fim, inicio));
              close(ibancoterminal);
            }
          }
        }
      }
      else{
        printf("Comando desconhecido. Tente de novo.\n\n");
      }
    }
}
