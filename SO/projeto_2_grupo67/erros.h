#ifndef ERROS_H
#define ERROS_H

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


void ErrorMsg(int objecType);

#endif
