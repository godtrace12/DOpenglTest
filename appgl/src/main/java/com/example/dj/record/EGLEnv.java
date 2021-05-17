package com.example.dj.record;

import android.content.Context;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.view.Surface;

import com.example.dj.appgl.filter.RecordFilter;
import com.example.dj.appgl.filter.base.AbstractRect2DFilter;
import com.example.dj.appgl.filter.base.FilterChain;
import com.example.dj.appgl.filter.base.FilterContext;

import java.util.ArrayList;

public class EGLEnv {
    private final EGLConfig mEglConfig;
    private final EGLContext mEglContext;
    private final EGLSurface mEglSurface;
    private final RecordFilter recordFilter;
    private EGLDisplay mEglDisplay;
    private final FilterChain filterChain;


    public EGLEnv(Context context, EGLContext eglContext, Surface surface,int width,int height){
        //1- 获得显示窗口，作为OpenGL的绘制目标
        mEglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        if(mEglDisplay == EGL14.EGL_NO_DISPLAY){
            throw new RuntimeException("eglGetDisplay failed");
        }

        //2- 初始化显示窗口
        int[] version = new int[2];
        boolean initResult = EGL14.eglInitialize(mEglDisplay,version,0,version,1);
        if(!initResult){
            throw new RuntimeException("eglInitialize failed");
        }

        //3- 配置
        // 属性选项
        int[] eglConfigAttributes = {
                EGL14.EGL_RED_SIZE, 8, //颜色缓冲区中红色位数
                EGL14.EGL_GREEN_SIZE, 8,//颜色缓冲区中绿色位数
                EGL14.EGL_BLUE_SIZE, 8, //
                EGL14.EGL_ALPHA_SIZE, 8,//
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT, //opengl es 2.0
                EGL14.EGL_NONE
        };
        int[] numConfigs = new int[1];
        EGLConfig[] configs = new EGLConfig[1];
        //EGL 根据属性选择一个配置
        boolean configResult = EGL14.eglChooseConfig(mEglDisplay, eglConfigAttributes, 0, configs, 0, configs.length,
                numConfigs, 0);
        if(!configResult){
            throw new RuntimeException("eglChooseConfig error: " + EGL14.eglGetError());
        }

        mEglConfig = configs[0];

        //4- 创建EGL 上下文
        int[] contextAttributeList = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION,2,
                EGL14.EGL_NONE
        };

        mEglContext= EGL14.eglCreateContext(mEglDisplay,mEglConfig,eglContext ,contextAttributeList,0);

        if (mEglContext == EGL14.EGL_NO_CONTEXT){
            throw new RuntimeException("eglCreateContext error: " + EGL14.eglGetError());
        }

        // 5- 创建EGLSurface
        int[] surfaceAttribList = {
                EGL14.EGL_NONE
        };

        mEglSurface = EGL14.eglCreateWindowSurface(mEglDisplay, mEglConfig, surface, surfaceAttribList, 0);
        if (mEglSurface == null){
            throw new RuntimeException("eglCreateWindowSurface error: " + EGL14.eglGetError());
        }

        // 6- 绑定当前线程的显示display
        boolean curResult = EGL14.eglMakeCurrent(mEglDisplay,mEglSurface,mEglSurface,mEglContext);
        if(!curResult){
            throw new RuntimeException("eglMakeCurrent error: " + EGL14.eglGetError());
        }

        // ？？？ 为何此处又是一个责任链？？？
        recordFilter = new RecordFilter();
        FilterContext filterContext = new FilterContext();
        filterContext.setSize(width, height);
        filterChain = new FilterChain(new ArrayList<AbstractRect2DFilter>(), 0, filterContext);
    }

    public void draw(int textureId, long timestamp) {
        recordFilter.onDraw(textureId, filterChain);
        EGLExt.eglPresentationTimeANDROID(mEglDisplay, mEglSurface, timestamp);
        //EGLSurface是双缓冲模式
        EGL14.eglSwapBuffers(mEglDisplay, mEglSurface);

    }


    public void release(){
        EGL14.eglDestroySurface(mEglDisplay,mEglSurface);
        EGL14.eglMakeCurrent(mEglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE,
                EGL14.EGL_NO_CONTEXT);
        EGL14.eglDestroyContext(mEglDisplay, mEglContext);
        EGL14.eglReleaseThread();
        EGL14.eglTerminate(mEglDisplay);
        recordFilter.release();
    }


}
