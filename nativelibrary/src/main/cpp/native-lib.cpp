//
// Created by daijun on 6/18/21.
//

#include <jni.h>
#include <string>
#include <jni.h>


extern "C"
JNIEXPORT jstring JNICALL Java_com_example_dj_media_DPlayer_getStringFromNDK(JNIEnv *env, jclass clazz) {
    return env->NewStringUTF("Hellow World，这是隔壁老李头的NDK的第一行代码");
}

