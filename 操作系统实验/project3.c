#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <string.h>
#include <unistd.h>
#include <time.h>
#include <sys/wait.h>
#define THREADNUM 20

//构建PCB结构体
struct VirtualPCB
{
    int tid;            //线程id
    int priority;        //优先级
    int arriveTime;        //到达时间
    int waitTime;        //等待时间
    int startTime;        //开始时间
    int runTime;        //运行时间
    //int endTime;        //完成时间
    //int aroundTime;        //周转时间
    int tempRunTime;    //临时运行时间，用于记时RR
    int isFinish;        //是否结束 0/1
}PCBs[THREADNUM];


//初始化PCB，为PCB中的数据赋值
void initPCB()
{
    int i;
    srand(time(NULL));
    for(i = 0; i < THREADNUM; i++){
        PCBs[i].tid = i + 1;
        PCBs[i].priority =    rand()%19 + 1;        //优先级为0~19范围内
        PCBs[i].tempRunTime = PCBs[i].runTime = rand()%19 + 1;        //运行的时间0~19范围内
        PCBs[i].arriveTime = 0;        //同时到达
        PCBs[i].waitTime = 0;
        PCBs[i].isFinish = 0;
    }
}


void *childFunc(void *arg)
{
    int n = *(int *)arg;
    while(1){
        printf("Thread:%-2d     TID:%-2d        Priority:%-2d       arriveTime:%-2d        needTime:%-2d \n",n,PCBs[n-1].tid,PCBs[n-1].priority,PCBs[n-1].arriveTime,PCBs[n-1].runTime);
        sleep(1);    //延时1s
        break;
    }
    pthread_exit(0);
}


void *Children(void* vargp)
{
    int ret[THREADNUM];
    initPCB();
    pthread_t tid[THREADNUM];
    int i;

    printf("20个子线程的情况如下：\n");
    for(i=0; i<THREADNUM; i++) {
        int k =i+1; 
        ret[i] = pthread_create(&tid[i],NULL,&childFunc, &k);
        if(ret[i] == 0) 
        { 
            sleep(1); 
        }
        else{ 
            printf("Thread%2d failed!\n",i+1); 
            } 
        pthread_join (tid[i], NULL);
    }
    pthread_exit(0);
}


//FCFS算法
void handleFCFS()
{
    printf("\n\n################# FCFS (单位：s)#################\n");
    int i;
    int startTime = 0;   //开始时间
    int totallwaitTime = 0; //总等待时间
    for(i = 0; i<THREADNUM; i++)
    {
        if(PCBs[i].isFinish == 0)
        {
            printf("Thread: %2d     startTime: %3d      runTime: %2d\n",PCBs[i].tid,startTime,PCBs[i].runTime);
            totallwaitTime += startTime;
            startTime += PCBs[i].runTime;
            PCBs[i].isFinish = 1;
        }
    }
    printf("totalwaitTime：%d", totallwaitTime);
}


//SJF算法
void handleSJF()
{
    printf("\n\n################# SJF (单位：s)#################\n");
    //重新初始化
    for(int k = 0 ;k < THREADNUM; k++) 
    { 
        PCBs[k].isFinish = 0; 
    }

    int i,j;
    int startTime = 0;    //开始时间
    int waitTime = 0;    //等待时间

    for ( i = 0; i < THREADNUM; i++)
    {
        for ( j = 0; j < THREADNUM; j++)
        {
            if((PCBs[j].isFinish == 0) && (PCBs[j].runTime == i))                        //为被执行过且运行时间等于i
            {
                printf("Thread: %2d     startTime: %3d      runTime: %2d\n",PCBs[j].tid,startTime,PCBs[j].runTime);
                waitTime += startTime;
                startTime += PCBs[j].runTime;
                PCBs[j].isFinish = 1;
            }
        }
    }
    printf("totalwaitTime：%d", waitTime);
}


//优先级调度
void handlePriority()
{
    printf("\n\n################# Priority (单位：s)#################\n");
    //重新初始化
    for(int k = 0 ;k < THREADNUM; k++) 
    { 
        PCBs[k].isFinish = 0; 
    }

    int i,j;
    int startTime = 0;    //开始时间
    int waitTime = 0;    //等待时间

    for ( i = 0; i < THREADNUM; i++)
    {
        for ( j = 0; j < THREADNUM; j++)
        {
            if((PCBs[j].priority == i )&& (PCBs[j].isFinish == 0))
            {
                printf("Thread: %2d     startTime: %3d      runTime: %2d\n",PCBs[j].tid,startTime,PCBs[j].runTime);
                waitTime += startTime;
                startTime += PCBs[j].runTime;
                PCBs[j].isFinish = 1;
            }
        }
    }
    printf("totalwaitTime：%d", waitTime);
}


//RR调度
void handleRR( int rr)
{
    printf("\n\n################# RR(单位：s，时间片%d)#################\n",rr);
    //重新初始化
    for(int k = 0 ;k < THREADNUM; k++) 
    { 
        PCBs[k].isFinish = 0; 
    }

    int i,j;
    int startTime = 0;           //开始时间
    int waitTime = 0;           //等待时间
    int finishNum  = 0;       //对已经完成的进程计数
    int isFinish = 0;             // 判断是否全部完成

    while (isFinish == 0)
    {
        for ( i = 0; i <THREADNUM ; i++)
        {
            if (PCBs[i].tempRunTime>0 && PCBs[i].isFinish == 0 )      //剩余需要的时间大于0
            {
                if (PCBs[i].tempRunTime - rr <= 0)
                {
                    
                    PCBs[i].waitTime = startTime + PCBs[i].tempRunTime -PCBs[i].runTime;
                    printf("Thread:%2d      startTime:%3d       runTime:%2d\n",PCBs[i].tid,startTime,PCBs[i].tempRunTime);
                    startTime += PCBs[i].tempRunTime;
                    PCBs[i].tempRunTime = 0;
                    PCBs[i].isFinish = 1;
                    finishNum ++;
                }
                else
                {
                    printf("Thread:%2d      startTime:%3d       runTime:%2d\n",PCBs[i].tid,startTime,rr);
                    PCBs[i].tempRunTime -=rr;
                    startTime +=rr;
                }   
            }
        }
        if (finishNum == 20)
        {
            isFinish = 1;
        }
    }
    for ( j = 0; j < THREADNUM; j++)
        {
            waitTime  += PCBs[j].waitTime;
        }
    printf("totalwaitTime: %d\n",waitTime);

}



int main()
{
    int ret1;
    pthread_t tid1;
    ret1 = pthread_create(&tid1, NULL, &Children,NULL);
    if(ret1 == 0)
    {
        printf("creating child threads..\n..\n");
        sleep(20);
    }
    else{
        printf("Create Main Thread Failed!\n");
    }
    handleFCFS();
    handleSJF();
    handlePriority();
    printf("\n\nPlease enter RR time :");
    int rr;
    scanf("%d",&rr);
    handleRR(rr);

    return 0;
}


