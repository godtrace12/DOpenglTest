package com.example.dj.appgl.ball

import android.opengl.GLSurfaceView
import com.example.dj.appgl.base.AbsGLSurfaceActivity

/**
 * 创建日期：12/11/21 8:17 PM
 * @author daijun
 * @version 1.0
 * @des：展示球
 */
class EarthTextureActivity: AbsGLSurfaceActivity() {
    override fun bindRenderer(): GLSurfaceView.Renderer {
        return EarthMapRenderer()
    }
}