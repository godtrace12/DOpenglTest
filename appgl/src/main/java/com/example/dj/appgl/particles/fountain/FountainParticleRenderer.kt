package com.example.dj.appgl.particles.fountain

import android.content.Context
import android.graphics.Color
import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * 创建日期：3/26/22 12:05 PM
 * @author daijun
 * @version 1.0
 * @des：喷泉粒子系统
 * 一个粒子有位置信息（x,y,z）、运动方向、颜色、生命值（开始和结束的时间）等属性
 * 流程拆解
 * 梳理粒子特性（坐标、颜色、运动矢量、开始时间）以及 重力和阻力的影响、持续时间
 * 粒子发生器如何发射粒子（反射点、方向、数量）
 * 编写着色器glsl代码
 */
class FountainParticleRenderer : GLSurfaceView.Renderer {
    private val mContext: Context? = null

    private var mProgram: ParticleShaderProgram? = null
    private var mParticleSystem: FountainParticleSystem? = null
    private var mSystemStartTimeNS: Long = 0
    private var mParticleShooter: FountainParticlesShooter? = null

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(0f, 0f, 0f, 0f)

        mProgram = ParticleShaderProgram()

        //定义粒子系统 最大包含1w个粒子，超过最大之后复用最前面的

        //定义粒子系统 最大包含1w个粒子，超过最大之后复用最前面的
        mParticleSystem = FountainParticleSystem(10000)

        //粒子系统开始时间

        //粒子系统开始时间
        mSystemStartTimeNS = System.nanoTime()

        //定义粒子发射器

        //定义粒子发射器
        mParticleShooter = FountainParticlesShooter(Geometry.Point(0f, -0.9f, 0f),
                Color.rgb(255, 50, 5),
                Geometry.Vector(0f, 0.3f, 0f))
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height);
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
        //当前（相对）时间 单位秒
        val curTime = (System.nanoTime() - mSystemStartTimeNS) / 1000000000f
        //粒子发生器添加粒子
        mParticleShooter!!.addParticles(mParticleSystem!!, curTime)
        //使用Program
        mProgram!!.useProgram()
        //设置Uniform变量
        mProgram!!.setUniforms(curTime)
        //设置attribute变量
        //设置attribute变量
        mParticleSystem!!.bindData(mProgram!!)
        //开始绘制粒子
        //开始绘制粒子
        mParticleSystem!!.draw()
    }
}