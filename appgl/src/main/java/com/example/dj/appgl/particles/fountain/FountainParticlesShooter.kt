package com.example.dj.appgl.particles.fountain

import android.opengl.Matrix
import java.util.*

/**
 * 创建日期：3/27/22 6:42 AM
 * @author daijun
 * @version 1.0
 * @des：
 */
class FountainParticlesShooter(position: Geometry.Point?, color: Int, direction: Geometry.Vector?) {
    //发射粒子的位置
    private var position: Geometry.Point? = null

    //发射粒子的颜色
    private var color = 0

    //发射粒子的方法
    private var direction: Geometry.Vector? = null

    private val rotationMatrix = FloatArray(16)
    private val random: Random = Random()
    val angleVarianceInDegrees = 20f

    init {
        this.position = position
        this.color = color
        this.direction = direction
    }


    /**
     * 调用粒子系统对象添加粒子
     *
     * @param particleSystem
     * @param currentTime
     */
    fun addParticles(particleSystem: FountainParticleSystem, currentTime: Float) {
        Matrix.setRotateEulerM(rotationMatrix, 0,
                (random.nextFloat() - 0.5f) * angleVarianceInDegrees,
                (random.nextFloat() - 0.5f) * angleVarianceInDegrees,
                (random.nextFloat() - 0.5f) * angleVarianceInDegrees)

        val tmpDirectionFloat = FloatArray(4)

        Matrix.multiplyMV(tmpDirectionFloat, 0,
                rotationMatrix, 0, floatArrayOf(direction!!.x, direction!!.y, direction!!.z, 1f), 0)

        val newDirection = Geometry.Vector(tmpDirectionFloat[0], tmpDirectionFloat[1], tmpDirectionFloat[2])
        particleSystem.addParticle(position!!, color, newDirection, currentTime)
    }

}