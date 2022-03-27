package com.example.dj.appgl.particles.fountain

import android.opengl.GLES30
import com.example.dj.appgl.util.GLDataUtil
import java.nio.FloatBuffer

/**
 * 创建日期：3/27/22 6:29 AM
 * @author daijun
 * @version 1.0
 * @des：
 */
class VertexArray(var particlesParam: FloatArray?) {
    companion object{
        const val BYTES_PER_FLOAT = 4
    }
    //粒子数组
    private var particles: FloatArray? = null
    private var mParticlesPosBuffer: FloatBuffer? = null

    init {
        particles = particlesParam
        mParticlesPosBuffer = GLDataUtil.createFloatBuffer(particles)

    }

    fun setVertexAttributePointer(dataOffset: Int, location: Int, count: Int, stride: Int) {
        mParticlesPosBuffer!!.position(dataOffset)
        GLES30.glVertexAttribPointer(location, count, GLES30.GL_FLOAT, false, stride, mParticlesPosBuffer)
        GLES30.glEnableVertexAttribArray(location)
        mParticlesPosBuffer!!.position(0)
    }

    fun updateBuffer(particles:FloatArray,particleOffset:Int,stride: Int){
//        mParticlesPosBuffer!!.put(particles,particleOffset,stride)
        mParticlesPosBuffer = GLDataUtil.createFloatBuffer(particles)

    }

}