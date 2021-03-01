#ifndef AUXILIAR_H
#define AUXILIAR_H

#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <semaphore.h>

#define MUTEX_INIT 0
#define MUTEX_LOCK 1
#define MUTEX_UNLOCK 2
#define MUTEX_DESTROY 3
#define SEM_INIT 4
#define SEM_WAIT 5
#define SEM_POST 6
#define SEM_DESTROY 7
#define THREAD_CREATE 8
#define THREAD_JOIN 9
#define SIGNAL_INIT 10
#define COND_INIT 11
#define COND_DESTROY 12
#define COND_WAIT 13
#define COND_SIGNAL 14
#define STDOUT 1

int startFile(char *filename);
int closeFile(int file);
void errorMsg(int objecType);
void lock_mutex(pthread_mutex_t * mutex);
void unlock_mutex(pthread_mutex_t * mutex);
void s_wait(sem_t * sem);
void s_post(sem_t * sem);

#endif
