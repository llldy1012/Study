#include<stdio.h>

#define N 5000
struct frame                 //定义用于clock算法的结构体
{
    int num;
    int useFlag ;
};

struct frame page[N];


int  clock()
{
    float rate;
    //rate = missTime/(float)count;                         //缺页率
    char fileName[][12] = {"workload1","workload2","workload3","workload4","workload5","workload6"};
    int fileNum;
    for (fileNum= 0 ;fileNum < 6;fileNum++)            //依次遍历六个workload
    {
        FILE *fp;
        int missTime = 0;                                                     //记录missTime
        int hitTime = 0;                                                         //记录hitTime
        int count = 0;                                                             //用于记录输入了多少个帧
        int tf = 0;                                                                     //用于当每一个页都被使用时决定谁被替换
        if((fp = fopen(fileName[fileNum],"r"))==NULL)           //打开workload，如果打不开则报错
        {
            printf("The file can not open\n");
            return -1;
        }
        while (!feof(fp))                                                     //一直循环，直到文件里取出的为空
        {
            /* code */
            int temp;
            int flag = 0;
            fscanf(fp,"%d",&temp);                                 //将文件中取出的帧号赋值给temp
            //temp/=N;
            
            for (int  i = 0; i < N; i++)                                  //遍历每一个页
            {
                if(page[i].num == temp)                          //如果出现匹配，useflag赋值为1，hitTime++
                {
                    page[i].useFlag = 1;
                    hitTime++;
                    flag = 1;
                    break;
                }
            }
            if(flag == 1)                                   //如果flag==1，直接进入下一轮循环
            {
                count++;
            //     if(count%50000 == 0){
            //     rate = missTime/(float)count;
            //     printf("%f,%d ",rate,count);
            // }
                continue;
            }
            else
            {
                missTime ++;                             //如果flag == 0，则missTime++
                if(missTime<=N)                       //若missTime <= N,说明页还有空余，直接进入剩余页的第一个就行
                {
                    page[count%N].num = temp;
                    page[count%N].useFlag = 1;
                    
                }
                else
                {
                    while (page[tf%N].useFlag!=0)        //如果每一个页都装有帧，则将所有的useFlag赋值为0，然后从第一个开始替换
                    {
                        /* code */
                        page[tf%N].useFlag = 0;
                        tf++;
                    }
                    page[tf%N].num = temp;
                    
                }
                
            }
            count++;
            //if(count%50000 == 0){
             //   rate = missTime/(float)count;
               // printf("%f,%d ",rate,count);
            //}
        }
        fclose(fp);
        // float rate;
        rate = missTime/(float)count;                         //缺页率
        printf("%s:\n",fileName[fileNum]);
        printf("缺页次数 :%d\n",missTime);
        printf("命中次数 :%d\n",hitTime);
        printf("总    数 :%d\n",count);
        printf("缺页率   :%f\n\n",rate);
    }
    return 0;
}

int FIFO(){
    int page[N] = {0};
    float rate;
    char fileName[][15] = {"workload1","workload2","workload3","workload4","workload5","workload6"};
    int fileNum;
    for ( fileNum = 0; fileNum < 6; fileNum++)
    {
        FILE* fp;
        int missTime = 0;
        int hitTime = 0;
        int count = 0;
        int tf = 0;
        if((fp = fopen(fileName[fileNum],"r"))==NULL)
        {
            printf("The file can not open\n");
            return -1;
        }
        while (!feof(fp))
        {
            /* code */
            int temp;
            int flag = 0;
            fscanf(fp,"%d",&temp);
            for ( int i = 0; i < N; i++)
            {
                if(page[i]==temp)
                {
                    hitTime++;
                    flag = 1;
                    break;
                }
            }
            if (flag == 1)
            {
                /* code */
                count++;
                // if(count%50000 == 0){
                // rate = missTime/(float)count;
                // printf("%f,%d ",rate,count);
                // }
                continue;
            }
            else
            {
                missTime++;
                if (missTime<=N)
                {
                    page[count%N] = temp;
                }
                else
                {
                    page[tf%N] = temp;
                    tf++;
                }
                count++;    
            //     if(count%50000 == 0){   //每50000个输出一次
            //     rate = missTime/(float)count;
            //     printf("%f,%d ",rate,count);
            // }
            }  
        }
        fclose(fp);
        // float rate;
        rate = missTime/(float)count;
        printf("%s:\n",fileName[fileNum]);
        printf("缺页次数 :%d\n",missTime);
        printf("命中次数 :%d\n",hitTime);
        printf("总    数 :%d\n",count);
        printf("缺页率   :%f\n\n",rate);
    }
    return 0;

}

int LRU()
{
    int page[N] = {0};
    float rate;
    char fileName[][15] = {"workload1","workload2","workload3","workload4","workload5","workload6"};
    int fileNum;
    for ( fileNum = 0; fileNum < 6; fileNum++)
    {
        FILE* fp;
        int missTime = 0;
        int hitTime = 0;
        int count = 0;
        int tf = 0;
        if((fp = fopen(fileName[fileNum],"r"))==NULL)
        {
            printf("The file can not open\n");
            return -1;
        }
        while (!feof(fp))
        {
            int temp;
            int flag = 0;
            fscanf(fp,"%d",&temp);
            
            for (int  i = 0; i < N; i++)
            {
                if(page[i] == temp)
                {
                    hitTime++;
                    flag = 1;
                    
                    int ding = page[i];
                    for(int j= i; j<N-1; j++)                                //类比堆栈，因为当前的temp被用到了，所以将其移至栈顶,并依次前移一位i+1到N-1
                    {
                        page[j] = page[j+1];
                    }
                    page[N-1] = ding;
                    break;

                }
            }
            if(flag == 1)
            {
                count++;
            //     if(count%50000 == 0){
            //     rate = missTime/(float)count;
            //     printf("%f,%d ",rate,count);
            // }
                continue;
            }
            else
            {
                missTime++;
                if(missTime<=N)
                {
                    page[count%N] == temp;
                }
                else
                {
                    
                    for(int j = 0; j<N-1;j++)                       //将栈底帧移除，然后其余的向前移一位，然后将新的帧放在栈顶
                    {
                        page[j] = page[j+1];
                    }
                    page[N-1] = temp;
                }
                count++;
                // if(count%50000 == 0){
                // rate = missTime/(float)count;
                // printf("%f,%d ",rate,count);
            // }
            }
            
        }
        fclose(fp);
        // float rate;
        rate = missTime/(float)count;
        printf("%s:\n",fileName[fileNum]);
        printf("缺页次数 :%d\n",missTime);
        printf("命中次数 :%d\n",hitTime);
        printf("总    数 :%d\n",count);
        printf("缺页率   :%f\n\n",rate);
    }
    return 0;
}

int main(){
    
    printf("N = %d\n\n",N);
    printf("～～～～～～clock算法～～～～～～\n");
    clock();
    sleep(5);
    printf("～～～～～～FIFO算法～～～～～～\n");
    FIFO();
    sleep(5);
    printf("～～～～～～LRU算法～～～～～～\n");
    LRU();
    return 0;
}
