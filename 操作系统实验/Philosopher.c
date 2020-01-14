#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <unistd.h>
#include <semaphore.h>

#define N 5                                                 //哲学家数量
#define LEFT i                    //第i位哲学家的左边
#define RIGHT (i+1)%N                        //第i位哲学家的右边


//#define LEFT (i+N-1)%N                    //第i位哲学家的左边
//#define RIGHT (i)%N 
//pthread_mutex_t  mutex = PTHREAD_MUTEX_INITIALIZER ,chopsticks[N];  //信号量
sem_t chopsticks[N];
sem_t room;


void* philosopher(void* arg)
{
   
    int i = *(int*) arg;
    while(1){
        printf("哲学家 %d 正在思考...\n",i);
        sleep(2);
        sem_wait(&room);
        sem_wait(&chopsticks[LEFT]);
        sem_wait(&chopsticks[RIGHT]);

        printf("哲学家 %d 开始就餐...\n",i);
        sleep(2);
        sem_post(&chopsticks[LEFT]);
        sem_post(&room);
        sem_post(&chopsticks[RIGHT]);
    }
}

int main()
{
    pthread_t philosopherid[N];
    int philosophers[N] = {0, 1, 2, 3, 4};//代表5个哲学家的编号
    for (int i = 0; i < N; ++i)
    {
        sem_init(&chopsticks[i],0,1);
    }
    sem_init(&room,0,4);
    for (int i=0; i<N; i++) {
		pthread_create(&philosopherid[i], NULL, philosopher, &philosophers[i]);
	}
    for (int i = 0; i < N; i++)
    {
        pthread_join(philosopherid[i],NULL);
    }
    
    
    
    return 0;
}
