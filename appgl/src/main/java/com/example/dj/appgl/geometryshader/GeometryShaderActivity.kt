package com.example.dj.appgl.geometryshader

import android.opengl.GLSurfaceView
import com.example.dj.appgl.base.AbsGLSurfaceActivity

/**
 * 创建日期：2/7/22 5:57 PM
 * @author daijun
 * @version 1.0
 * @des：几何着色器相关例子
 */
class GeometryShaderActivity: AbsGLSurfaceActivity() {
    override fun bindRenderer(): GLSurfaceView.Renderer {
        return GeometryShaderRenderer()
    }
}