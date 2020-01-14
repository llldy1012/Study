#include<stdio.h>
#include<stdlib.h>
#include<unistd.h>
#include<string.h>
#include<sys/types.h>
#include<wait.h>
int main(){
	
        
	int fd[2],nbytes;
	 
    	char readbuffer[200];
	memset(readbuffer,0,sizeof(readbuffer)); 
	if(pipe(fd)<0)//创建管道
	{
        	perror("pipe");//错误输出函数，没有错误的时候就显示 error 0
        	exit(0);
    	}
        
        
    	pid_t p1,p2;
    	p1=fork();
    	if(p1<0)  //创建进程失败
    	{
        	fprintf(stderr,"fork failed");
        	exit(-1);
    	}
    	else if(p1==0)
    	{
		printf("这里是进程1，id: %d\n",getpid());
    		FILE *fp;
		fp = fopen("1.txt","w+");
		char c[]="Hello";
    		fwrite(c,strlen(c) + 1,1,fp);
		fclose(fp);
		close(fd[0]);  
            	write(fd[1],"ok",100); //写管道消息 
		printf("向管道写入消息完成\n\n");
    	}
    	else  
    	{	
		wait(NULL);
		printf("这里是进程2，id: %d\n",getpid());
        	p2=fork();
        	if(p2<0)//创建进程失败
        	{
            		fprintf(stderr,"fork failed");
            		exit(-1);
        	}
        else if(p2==0)
        {
        	close(fd[1]); //close write
            	read(fd[0],readbuffer,sizeof(readbuffer));// 从读端读消息
		printf("从管道读消息完成\n\n");
            	if(!strcmp(readbuffer,"ok")) 
            	{
			FILE *f;
			f = fopen("1.txt","r");
                	char data[200];
			fread(data,200,1,f);
			fclose(f);
                	printf("文本中的内容为：%s \n",data);//打印文本文件中的内容
		
            	}
        }
        else  
        {
            exit(0);
        }
    }

    return 0;
}

