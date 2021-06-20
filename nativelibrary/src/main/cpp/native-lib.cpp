//
// Created by daijun on 6/18/21.
//

#include <jni.h>
#include <string>
#include <jni.h>
#include <android/log.h>
#include "DNativeRenderContext.h"

#define  TAG    "dj------"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,TAG,__VA_ARGS__)

#define NATIVE_GLRENDER_CLASS_NAME "com/example/dj/media/DNativeRender"
#define NATIVE_PLAYER_CLASS_NAME "com/example/dj/media/DPlayer"

//extern "C"
//JNIEXPORT jstring JNICALL Java_com_example_dj_media_DPlayer_getStringFromNDK(JNIEnv *env, jclass clazz) {
//    return env->NewStringUTF("Hellow World，这是隔壁老李头的NDK的第一行代码");
//}

// 1,-------------------- DPlayer测试相关代码 ----------------

extern "C"
JNIEXPORT jstring JNICALL getNativeString(JNIEnv *env, jclass clazz) {
    LOGE("dj---------- getNativeString");
    return env->NewStringUTF("Hellow World，jni动态注册的第一行代码");
}

extern "C"
JNIEXPORT void JNICALL nativeStringInit(JNIEnv *env, jclass clazz) {
    LOGE("dj---------- nativeInit","dj---");
}

//2，--------------------- DNativeRender测试相关代码 ----------------

JNIEXPORT void JNICALL nativeInit(JNIEnv *env, jobject instance)
{
    DNativeRenderContext::GetInstance();

}

JNIEXPORT void JNICALL nativeSetParamsInt
        (JNIEnv *env, jobject instance, jint paramType, jint value0, jint value1)
{
    DNativeRenderContext::GetInstance()->SetParamsInt(paramType, value0, value1);
}

JNIEXPORT void JNICALL nativeOnSurfaceCreated(JNIEnv *env, jobject instance)
{
    DNativeRenderContext::GetInstance()->OnSurfaceCreated();
}

extern "C"
JNIEXPORT void JNICALL nativeOnSurfaceChanged(JNIEnv *env, jobject instance, jint width, jint height)
{
    DNativeRenderContext::GetInstance()->OnSurfaceChanged(width,height);
}

extern "C"
JNIEXPORT void JNICALL nativeOnDrawFrame(JNIEnv *env, jobject instance)
{
    DNativeRenderContext::GetInstance()->OnDrawFrame();
}


// DPlayer jni注册方法
static JNINativeMethod g_PlayerMethods[] = {
        {"nativeStringInit","()V",(void *)(nativeStringInit)},
        {"getNativeString","()Ljava/lang/String;",(void *)(getNativeString)},
};

// DNativeRender jni注册方法
static JNINativeMethod g_RenderMethods[] = {
        {"nativeInit",                      "()V",       (void *)(nativeInit)},
        {"nativeSetParamsInt",              "(III)V",    (void *)(nativeSetParamsInt)},
        {"nativeOnSurfaceCreated",          "()V",       (void *)(nativeOnSurfaceCreated)},
        {"nativeOnSurfaceChanged",          "(II)V",     (void *)(nativeOnSurfaceChanged)},
        {"nativeOnDrawFrame",               "()V",       (void *)(nativeOnDrawFrame)},
};

//动态注册jni函数
static int RegisterNativeMethods(JNIEnv *env, const char *className, JNINativeMethod *methods, int methodNum){
//    printf("dj-------- RegisterNativeMethods methodNum=%s",methodNum);
    jclass clazz = env->FindClass(className);
    if(clazz == NULL){
        return JNI_FALSE;
    }
    int regisResult = env->RegisterNatives(clazz, methods, methodNum);
    printf("dj---------- regisResult=%d",regisResult);
    if (regisResult < 0)
    {
        return JNI_FALSE;
    }
    return JNI_TRUE;
}

// 动态注销jni函数
static void UnregisterNativeMethods(JNIEnv *env, const char *className)
{
    jclass clazz = env->FindClass(className);
    if (clazz == NULL)
    {
        return;
    }
    if (env != NULL)
    {
        env->UnregisterNatives(clazz);
    }
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm,void* reserved){
//    printf("dj----------- jniOnload");
    JNIEnv* env = NULL;
    jint result = JNI_ERR;
    if (vm->GetEnv((void**) &env, JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }
    jint regRet = RegisterNativeMethods(env, NATIVE_PLAYER_CLASS_NAME, g_PlayerMethods,
                                        sizeof(g_PlayerMethods) /
                                        sizeof(g_PlayerMethods[0]));
    if (regRet != JNI_TRUE)
    {
        return JNI_ERR;
    }

    regRet = RegisterNativeMethods(env, NATIVE_GLRENDER_CLASS_NAME, g_RenderMethods,
                                   sizeof(g_RenderMethods) /
                                   sizeof(g_RenderMethods[0]));
    if (regRet != JNI_TRUE)
    {
        return JNI_ERR;
    }

    return JNI_VERSION_1_6;
}

JNIEXPORT void JNI_OnUnload(JavaVM *jvm, void *p)
{
    JNIEnv *env = NULL;
    if (jvm->GetEnv((void **) (&env), JNI_VERSION_1_6) != JNI_OK)
    {
        return;
    }

    UnregisterNativeMethods(env, NATIVE_PLAYER_CLASS_NAME);
    UnregisterNativeMethods(env,NATIVE_GLRENDER_CLASS_NAME);

}

