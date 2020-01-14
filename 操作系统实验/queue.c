#include<stdio.h>
#include<stdlib.h>
#include<sys/msg.h>
#include<string.h>
#include<wait.h>


typedef struct mymsg
{
	long mytype;   //储存消息类型
	unsigned char mytext[128];     //储存消息内容
} mymsg;


int main(){
	FILE *f;
	pid_t p1,p2;
	key_t key;
	key = ftok(".","t");
	int msgid = msgget(key,0666|IPC_CREAT);
	p1 = fork();
	if(p1<0){
		printf("error,fork failed");
	}
	else if(p1==0){
		printf("这里是进程1，id: %d\n",getpid());
		printf("正在发送.......\n");
		struct mymsg msg1;
		msg1.mytype = getppid();
		f = fopen("2.txt","w");
		if(f==NULL){
			printf("can not open the file\n");
		}
		char c[]="hello";
    		fwrite(c,strlen(c) + 1,1,f);
		fclose(f);
		strcpy(msg1.mytext,"ok");
		if(msgsnd(msgid,&msg1,sizeof(msg1.mytext),IPC_NOWAIT)<0){
			printf("error\n");
			exit(-1);		
		}
		else{
			printf("发送完成.......\n\n");
			exit(0);		
		}	
				
	}
	else{
		wait(NULL);
		p2 = fork();
		if(p2<0){
			printf("error,fork failed");
		}
		else if(p2==0){
			printf("这里是进程2，id: %d\n",getpid());
			printf("正在接收.......\n");
			struct mymsg msg2;
			msg2.mytype = getppid();
			if(msgrcv(msgid,&msg2,200,getppid(),IPC_NOWAIT)<0){

				printf("error\n");
			}
			else{
				printf("接收完成.......\n\n");
				if(strcmp("ok",msg2.mytext)==0){
					f = fopen("2.txt","r");
					if(f==NULL){
						printf("error");
					}
					else{
						char data[200];
						fread(data,200,1,f);
						fclose(f);
						printf("2.txt中的内容为： ");
                				printf("%s \n",data);//打印
					}
				}
			}
		}
		else{
			wait(NULL);
			exit(0);		
		}
	}
	return 0;
}		
		
		

			





