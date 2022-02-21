package com.example.dj.appgl.particles.transformfeedback

import android.opengl.GLSurfaceView
import com.example.dj.appgl.base.AbsGLSurfaceActivity

/**
 * 创建日期：2/7/22 5:57 PM
 * @author daijun
 * @version 1.0
 * @des：transformfeedback原理例子
 */
class TransformfeedbackActivity: AbsGLSurfaceActivity() {
    override fun bindRenderer(): GLSurfaceView.Renderer {
        return TransformfeedbackTenderer()
    }
}