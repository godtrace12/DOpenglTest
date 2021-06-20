package com.example.dj.media;

/**
 * 创建日期：6/19/21 8:26 PM
 *
 * @author daijun
 * @version 1.0
 * @des： jni opengl的render类
 */
public class DNativeRender {
    public native void nativeInit();
    public native void nativeSetParamsInt(int paramType, int value0, int value1);
    public native void nativeOnSurfaceCreated();
    public native void nativeOnSurfaceChanged(int width, int height);
    public native void nativeOnDrawFrame();
}
