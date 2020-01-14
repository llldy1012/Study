#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/wait.h>
#include <string.h>
#include <sys/types.h>
#include <pthread.h>



//生成素数线程
void* MyThread1() {
	printf("生成1～N的素数（包含N），请输入N：");
	int n;
	int i = 0;
	int j = 0;
	scanf("%d",&n);
	for( i = 2; i<=n; i++){
		for( j=2; j<=i/2; j++){
			if(i%j == 0){
				break;
			}
		}
		if(j>i/2){
			printf("%d ",i);
		}
	}
	printf("\n\n");
}


//生成Fibonacci 序列
void* MyThread2(){
	printf("生成25位Fibonacci序列：\n");
	long long Fibonacci[25];
	Fibonacci[0] = Fibonacci[1] = 1;
	int i;
	for (i = 2; i <= 24; i++) {
		Fibonacci[i] = Fibonacci[i - 1] + Fibonacci[i - 2];
	}
	/* 输出 每10位换一行*/
	for (i = 0; i <= 24; i++) {
		printf("%lld ", Fibonacci[i]);
		if (i % 10 == 9)
			printf("\n");
	}
	printf("\n");
}



//2号进程创建两个线程
int thread_two(){
	int ret1=0,ret2=0;
	pthread_t id1,id2;     //id
	ret1 = pthread_create(&id1,NULL,MyThread1,NULL);   //创建thread1
	//判断是否创建成功
	if(ret1){
		printf("Create pthread error!\n");
		return 1;
	}
	pthread_join(id1,NULL);   //主线程等待线程1结束
	//判断是否创建成功
	ret2 = pthread_create(&id2,NULL,MyThread2,NULL);  //创建thread2
	if(ret2){
		printf("Create pthread error!\n");
		return 1;
	}
	pthread_join(id2,NULL);   //主线程等待线程2结束

	printf("main thread exit!\n");  //主程序退出
	printf("\n\n");
	return 0;
}



//两个进程创建的不同操作
void fun(int i){
	switch(i){
		case 4: printf("----进入进程4，进程ID：%d\n", getpid());
			printf("----这里是进程4，父进程ID：%d\n\n", getppid());

			char a[2] = "";
			printf("Please input \"ls\" or \"ps\" or \"cp\"\n");
			while (1) {
				scanf("%s", a);
				char b[10] = "/bin/";
				strcat(b, a);
				if (strcmp(a, "ls") == 0 || strcmp(a, "ps") == 0) {
					execlp(b, a, NULL);
				} else if (strcmp(a, "cp") == 0) {

					printf("请输入cp命令的操作数\ncp ");
					char option[3];
					scanf("%s", option);
					char sourceFile[10];
					scanf("%s", sourceFile);
					char objectFile[10];
					scanf("%s", objectFile);

					execlp(b, a, option, sourceFile, objectFile,NULL);
				} else {
					printf("INVALID ENTRY    Please input \"ls\" or \"ps\" or \"cp\"\n");
				}
			}
		case 5: //进程5打印“hellow world”
			printf("\n\n");
			printf("----进入进程5，进程ID：%d\n", getpid());
			printf("----这里是进程5，父进程ID：%d\n", getppid());
            		printf("----执行一个可执行程序:\n");
            		system("./a.out");
			printf("\n");
            		break;

	}
	exit(0);
}



//3,4两个不同进程执行的不同操作
int childfun(int i){
	switch(i){
		case 2:  //调用thread_two()函数，创建两个线程
			printf("----进入进程2，进程ID：%d\n", getpid());
			printf("----这里是进程2，父进程ID：%d\n", getppid());
			printf("----执行线程1和线程2\n\n");

			thread_two();
			break;
		case 3: //调用process_two()函数创建两个进程
			printf("----进入进程3，进程ID：%d\n", getpid());
			printf("----这里是进程3，父进程ID：%d\n", getppid());
			printf("----创造进程4和进程5\n\n");
			Createprocess_two();
			break;
	}
	exit(0);
}


//3号进程创建两个不同进程，编号为4和5
int Createprocess_two(){
	int i;
    	for (int i = 4; i <= 5; i++){
        	pid_t child=fork();
        	//create process failed
        	if (child==-1){
            		printf("Error hanppened in fork function!\n");
            		return 0;
		}
		//exert function when success
		if(child==0){
		    fun(i);
		}
		if(child>0){
			wait(NULL);
		}
	}

	//for (int i = 0; i < 2; i++){
	//	//Parent process waits child process
	//	pid_t tempPid=wait(NULL);
	//	printf("The process %d exit\n", tempPid);
	//}

	    return 0;
}



//1号进程创建两个不同的进程，编号为2和3
int Createprocess(){
	int i;
	for(i=2; i<4; i++){
		pid_t child;
		child = fork();
		if(child == -1){
			printf("Error happened in fork function/n");
			return 0;
		}
		if(child == 0){
			childfun(i);  //调用这个函数让两个进程执行不同的程序
			exit(0);
		}
		if(child > 0){
			wait(NULL);
		}
	}
	//for(i=0; i<2; i++){
	//	pid_t cpid = wait(NULL);
	//	printf("the process %d exit \n",cpid);
	//	return 0;
	//}
	//最后父进程退出
	printf("The no.1 parent process ID is %d exit\n",getpid());
	return 0;
	
}


int main(){
	Createprocess();
	return 0;
}
