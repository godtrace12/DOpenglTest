package com.example.dj.appgl.nativegl

import android.opengl.GLSurfaceView
import com.example.dj.appgl.base.AbsGLSurfaceActivity
import com.example.dj.appgl.nativegl.render.NativeGLRender

/**
 * 创建日期：6/20/21 12:56 PM
 * @author daijun
 * @version 1.0
 * @des：
 */
class NativeGLActivity : AbsGLSurfaceActivity() {
    override fun bindRenderer(): GLSurfaceView.Renderer {
        var render = NativeGLRender()
        render.init(nativeGLType)
        return render
    }
}