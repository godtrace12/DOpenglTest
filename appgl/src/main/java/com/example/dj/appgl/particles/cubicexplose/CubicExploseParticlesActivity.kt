package com.example.dj.appgl.particles.cubicexplose

import android.opengl.GLSurfaceView
import com.example.dj.appgl.base.AbsGLSurfaceActivity

/**
 * 创建日期：3/20/22 7:50 PM
 * @author daijun
 * @version 1.0
 * @des：
 */
class CubicExploseParticlesActivity : AbsGLSurfaceActivity()  {
    override fun bindRenderer(): GLSurfaceView.Renderer {
        return CubicExploseParticleRenderer()
    }
}