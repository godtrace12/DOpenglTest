package com.example.dj.appgl.particles.fountain

import android.opengl.GLSurfaceView
import com.example.dj.appgl.base.AbsGLSurfaceActivity
import com.example.dj.appgl.particles.cubicexplose.CubicExploseParticleRenderer

/**
 * 创建日期：3/26/22 9:27 PM
 * @author daijun
 * @version 1.0
 * @des：
 */
class FountainParticlesActivity: AbsGLSurfaceActivity()  {
    override fun bindRenderer(): GLSurfaceView.Renderer {
        return FountainParticleRenderer()
    }
}