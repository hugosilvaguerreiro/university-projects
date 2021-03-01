#include "erros.h"
#include <stdio.h>

void ErrorMsg(int objecType){
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
    default:
      printf("Erro: erro indefinido\n");
    }
}
