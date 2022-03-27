package com.example.dj.appgl.particles.fountain

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
        particleSystem.addParticle(position!!, color, direction!!, currentTime)
    }

}