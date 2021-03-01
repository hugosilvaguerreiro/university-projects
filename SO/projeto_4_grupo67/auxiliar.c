#include "auxiliar.h"
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <errno.h>
#include <pthread.h>
#include <sys/types.h>
#include <fcntl.h>

void errorMsg(int objecType){
  switch (objecType) {
    case MUTEX_INIT:
        printf("Erro: falha ao inicializar o mutex\n");
        break;
    case MUTEX_LOCK:
        printf("Erro: falha ao bloquear o mutex\n");
        break;
    case MUTEX_DESTROY:
        printf("Erro: falha ao destruir o mutex\n");
        break;
    case MUTEX_UNLOCK:
        printf("Erro: falha ao desbloquear o mutex\n");
        break;
    case SEM_INIT:
        printf("Erro: falha ao inicializar o semaforo\n");
        break;
    case SEM_WAIT:
        printf("Erro: falha ao esperar pelo semaforo\n");
        break;
    case SEM_POST:
        printf("Erro: falha ao assinalar o semaforo\n");
        break;
    case SEM_DESTROY:
        printf("Erro: falha ao destruir o semaforo\n");
        break;
    case THREAD_CREATE:
        printf("Erro: falha ao criar a thread\n");
        break;
    case THREAD_JOIN:
        printf("Erro: falha a esperar pela tarefa\n");
        break;
    case SIGNAL_INIT:
      printf("Erro ao instalar o signal handler, o programa ira terminar\n");
      break;
    case COND_INIT:
      printf("Erro ao inicializar a variavel de condicao, o programa ira terminar\n");
      break;
    case COND_DESTROY:
      printf("Erro ao destruir a variavel de condicao\n");
      break;
    case COND_WAIT:
      printf("Erro ao esperar pela variavel de condicao\n");
      break;
    case COND_SIGNAL:
      printf("Erro assinalar a variavel de condicao\n");
      break;
    default:
      printf("Erro: erro indefinido\n");
    }
}

int startFile(char *fileName) {
  int file;
  while((file = open(fileName, O_CREAT|O_EXCL|O_WRONLY|O_APPEND, S_IWUSR| S_IRUSR)) < 0) {
      if(errno == EEXIST)
          unlink(fileName);
      else {
          printf("problemas a abrir o ficheiro %s \n", fileName);
          return -1;
      }
  }
  return file;
}

int closeFile(int file) {
  while(close(file) < 0) {
      if(errno == EINTR)
        continue;
      else {
          printf("problemas a fechar o ficheiro\n");
          return -1;
      }
  }
  return 0;
}

void lock_mutex(pthread_mutex_t * mutex) {
    if(pthread_mutex_lock(mutex)!=0){
      errorMsg(MUTEX_LOCK);
      exit(EXIT_FAILURE);
  }
}

void unlock_mutex(pthread_mutex_t * mutex) {
    if(pthread_mutex_unlock(mutex)!=0){
      errorMsg(MUTEX_UNLOCK);
      exit(EXIT_FAILURE);
  }
}

void s_wait(sem_t * sem) {
    if(sem_wait(sem)!=0){
    errorMsg(SEM_WAIT);
    exit(EXIT_FAILURE);
  }
}

void s_post(sem_t * sem) {
    if(sem_post(sem)!=0){
    errorMsg(SEM_POST);
    exit(EXIT_FAILURE);
  }
}
