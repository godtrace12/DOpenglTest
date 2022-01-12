package com.example.dj.appgl.mrt

import android.opengl.GLSurfaceView
import com.example.dj.appgl.ball.EarthMapRenderer
import com.example.dj.appgl.base.AbsGLSurfaceActivity

/**
 * 创建日期：12/11/21 8:17 PM
 * @author daijun
 * @version 1.0
 * @des：OpenGL ES 多目标渲染（MRT）
 */
class MrtRenderActivity: AbsGLSurfaceActivity() {
    override fun bindRenderer(): GLSurfaceView.Renderer {
        return MrtRenderer()
    }
}