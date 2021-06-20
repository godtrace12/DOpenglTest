package com.example.dj.appgl.nativegl.render

import android.opengl.GLSurfaceView
import com.example.dj.media.DNativeRender
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * 创建日期：6/20/21 12:58 PM
 * @author daijun
 * @version 1.0
 * @des：
 */
class NativeGLRender : GLSurfaceView.Renderer {
    var mNativeRender:DNativeRender = DNativeRender()

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        mNativeRender.nativeOnSurfaceCreated()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        mNativeRender.nativeOnSurfaceChanged(width,height)
    }

    override fun onDrawFrame(gl: GL10?) {
        mNativeRender.nativeOnDrawFrame()
    }

    fun init(){
        mNativeRender.nativeInit()
        mNativeRender.nativeSetParamsInt(0,0,0)
    }



}