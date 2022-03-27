package com.example.dj.appgl.particles.fountain

import android.graphics.Color
import android.opengl.GLES30

/**
 * 创建日期：3/27/22 6:27 AM
 * @author daijun
 * @version 1.0
 * @des：
 */
class FountainParticleSystem(var partilceCountParam: Int) {
    //位置 xyz
    private val POSITION_COMPONENT_COUNT = 3
    //颜色 rgb
    private val COLOR_COMPONENT_COUNT = 3
    //运动矢量 xyz
    private val VECTOR_COMPONENT_COUNT = 3
    //开始时间
    private val PARTICLE_START_TIME_COMPONENT_COUNT = 1

    private val TOTAL_COMPONENT_COUNT = (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT
            + VECTOR_COMPONENT_COUNT + PARTICLE_START_TIME_COMPONENT_COUNT)

    //步长
    private val STRIDE: Int = TOTAL_COMPONENT_COUNT * VertexArray.BYTES_PER_FLOAT
    //粒子游标
    private var nextParticle = 0
    //粒子计数
    private var curParticleCount = 0
    //粒子数组
    private var particles: FloatArray? = null

    //最大粒子数量
    private var maxParticleCount = 0

    //VBO
    private var vertexArray: VertexArray? = null
    init {
        maxParticleCount = partilceCountParam
        particles = FloatArray(maxParticleCount * TOTAL_COMPONENT_COUNT)
        vertexArray = VertexArray(particles)
    }


    /**
     * 添加粒子到FloatBuffer
     *
     * @param position        位置
     * @param color           颜色
     * @param direction       运动矢量
     * @param particStartTime 开始时间
     */
    fun addParticle(position: Geometry.Point, color: Int, direction: Geometry.Vector, particStartTime: Float) {
        val particleOffset = nextParticle * TOTAL_COMPONENT_COUNT
        var currentOffset = particleOffset
        nextParticle++
        if (curParticleCount < maxParticleCount) {
            curParticleCount++
        }
        //重复使用，避免内存过大
        if (nextParticle === maxParticleCount) {
            nextParticle = 0
        }
        //填充 位置坐标 xyz
        particles!![currentOffset++] = position.x
        particles!![currentOffset++] = position.y
        particles!![currentOffset++] = position.z

        //填充 颜色 rgb
        particles!![currentOffset++] = (Color.red(color)+0.0f) / 255
        particles!![currentOffset++] = (Color.green(color)+0.0f) / 255
        particles!![currentOffset++] = (Color.blue(color)+0.0f) / 255

        //填充 运动矢量
        particles!![currentOffset++] = direction.x
        particles!![currentOffset++] = direction.y
        particles!![currentOffset++] = direction.z

        //填充粒子开始时间
        particles!![currentOffset++] = particStartTime

        //把新增的粒子添加到顶点数组FloatBuffer中
        vertexArray!!.updateBuffer(particles!!, particleOffset, TOTAL_COMPONENT_COUNT)
    }


    fun bindData(program: ParticleShaderProgram) {
        var dataOffset = 0
        vertexArray!!.setVertexAttributePointer(dataOffset,
                program.getaPositionLocation(),
                POSITION_COMPONENT_COUNT, STRIDE)
        dataOffset += POSITION_COMPONENT_COUNT
        vertexArray!!.setVertexAttributePointer(dataOffset,
                program.getaColorLocation(),
                COLOR_COMPONENT_COUNT, STRIDE)
        dataOffset += COLOR_COMPONENT_COUNT
        vertexArray!!.setVertexAttributePointer(dataOffset,
                program.getaDirectionLocation(),
                VECTOR_COMPONENT_COUNT, STRIDE)
        dataOffset += VECTOR_COMPONENT_COUNT
        vertexArray!!.setVertexAttributePointer(dataOffset,
                program.getaPatricleStartTimeLocation(),
                PARTICLE_START_TIME_COMPONENT_COUNT, STRIDE)
    }

    fun draw() {
        GLES30.glDrawArrays(GLES30.GL_POINTS, 0, curParticleCount)
    }

}