/*
 * Copyright 2009-2011 Cedric Priscal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <stdbool.h>
#include <jni.h>
#include <asm/termbits.h>
#include <stdio.h>
#include <termios.h>
#include <unistd.h>
#include <sys/time.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <string.h>
#include <pthread.h>
#include <stdlib.h>
#include <sys/socket.h>
#include <math.h>
#include <termios.h>
#include <poll.h>
#include <time.h>
#include <error.h>
#include <errno.h>
#include<stdarg.h>

#include "android/log.h"
static const char *TAG="serial_port";
#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)

//static int save_fd0 = -1;
static FILE *save_fd0 = NULL;
static int count = 0;
static char frames[128] = {0};

int openLogSendFile(){
    int exist_ret = 0;
    int cn = 0;

    if(access("/sdcard/CanDataLog/", F_OK) < 0){
        if(system("mkdir /sdcard/CanDataLog") != 0)
            return -2;
    }

    char saveFileName[64];
    while(exist_ret == 0){
        memset(saveFileName, 0, 64);
        sprintf(saveFileName, "/sdcard/CanDataLog/save_recv%d.xls", cn++);
        exist_ret = access(saveFileName, F_OK);
    }
//    save_fd0 = open(saveFileName, O_RDWR | O_CREAT, 0777);
    save_fd0 = fopen(saveFileName,"w") ;
    if (save_fd0 == NULL){
        LOGE("Log file Open faild !");
        return -1;
    }
    count = 0;
    fprintf(save_fd0,"Index\tType\tFormat\tId\tDLC\tData0\tData1\tData2\tData3\tData4\tData5\tData6\tData7\n");
    return 0;
}

int closeLogSendFile(){
//    if (save_fd0 != -1)
//        close(save_fd0);
//    save_fd0 = -1;
//    count = 0;
//    return 0;
    if (save_fd0 != NULL)
        fclose(save_fd0);
    save_fd0 = NULL;
    count = 0;
    return 0;
}

int write_to_file(int FF, int RTR, int ID, int DLC, int *data){
//    memset(frames, 0, 128);
//    sprintf(frames, "%d, Type=%s, Format=%s, id=%08x, dlc=%d, data=%02x %02x %02x %02x %02x %02x %02x %02x\n",
//            count,
//            FF==0?"Standard":"Extend",
//            RTR==0?"Data":"Remote",
//            ID,
//            DLC,
//            data[0],
//            data[1],
//            data[2],
//            data[3],
//            data[4],
//            data[5],
//            data[6],
//            data[7]);count++;
//    return write(save_fd0, frames, strlen(frames));、
//    if (save_fd0 == NULL)
//        return -2;
    fprintf(save_fd0,"%d\t%s\t%s\t%d\t%d\t%02x\t%02x\t%02x\t%02x\t%02x\t%02x\t%02x\t%02x\n",
            count++,
            FF==0?"Standard":"Extend",
            RTR==0?"Data":"Remote",
            ID,
            DLC,
            data[0],
            data[1],
            data[2],
            data[3],
            data[4],
            data[5],
            data[6],
            data[7]) ;
    return 0;
}

JNIEXPORT jint JNICALL Java_com_android_emcocansniffer_FileHelper_open
  	(JNIEnv *env, jclass thiz){
	return openLogSendFile();
}

JNIEXPORT jint JNICALL Java_com_android_emcocansniffer_FileHelper_write
        (JNIEnv *env, jclass thiz, jint FF, jint RTR, jint ID, jint DLC, jintArray data){
    int *dataArr = (*env)->GetIntArrayElements(env, data, 0);
    if (dataArr == NULL){
        LOGE("write --> DATA array error !!!\n");
        return -1;
    }
    int ret = write_to_file(FF, RTR, ID, DLC, dataArr);
    (*env)->ReleaseIntArrayElements(env, data, dataArr, 0);
    return ret;
}

JNIEXPORT int JNICALL Java_com_android_emcocansniffer_FileHelper_close
  	(JNIEnv *env, jobject thiz){
	return closeLogSendFile();
}

