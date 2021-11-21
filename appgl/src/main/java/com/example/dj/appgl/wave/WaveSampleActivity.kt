package com.example.dj.appgl.wave

import android.opengl.GLSurfaceView
import com.example.dj.appgl.base.AbsGLSurfaceActivity

/**
 * 创建日期：11/14/21 9:57 PM
 * @author daijun
 * @version 1.0
 * @des：
 */
class WaveSampleActivity: AbsGLSurfaceActivity() {
    override fun bindRenderer(): GLSurfaceView.Renderer {
        return WaveRenderer(this,1.0f,1.0f)
    }
}