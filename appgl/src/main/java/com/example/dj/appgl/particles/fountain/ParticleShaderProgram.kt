package com.example.dj.appgl.particles.fountain

import android.content.Context
import android.opengl.GLES30
import com.example.dj.appgl.R
import com.example.dj.appgl.util.ResReadUtils
import com.example.dj.appgl.util.ShaderUtils

/**
 * 创建日期：3/27/22 7:14 AM
 * @author daijun
 * @version 1.0
 * @des：
 */
class ParticleShaderProgram {
    private val U_TIME = "u_Time"

    private val A_POSITION = "a_Position"
    private val A_COLOR = "a_Color"
    private val A_DIRECTION = "a_Direction"
    private val A_PATRICLE_START_TIME = "a_PatricleStartTime"

    private var program = 0

    private var uTimeLocation = 0
    private var aPositionLocation = 0
    private var aColorLocation = 0
    private var aDirectionLocation = 0
    private var aPatricleStartTimeLocation = 0

    init {
        //生成program
        val vertexShaderCode: String = ResReadUtils.readResource( R.raw.vertex_fountain_particle)
        val fragmentShaderCode: String = ResReadUtils.readResource( R.raw.fragment_fountain)
        var vertexShaderId: Int = ShaderUtils.compileVertexShader(vertexShaderCode)
        var fragmentShaderId: Int = ShaderUtils.compileFragmentShader(fragmentShaderCode)
        program = ShaderUtils.linkProgram(vertexShaderId, fragmentShaderId)

        //获取uniform 和attribute的location
        uTimeLocation = GLES30.glGetUniformLocation(program, U_TIME)
        aPositionLocation = GLES30.glGetAttribLocation(program, A_POSITION)
        aColorLocation = GLES30.glGetAttribLocation(program, A_COLOR)
        aDirectionLocation = GLES30.glGetAttribLocation(program, A_DIRECTION)
        aPatricleStartTimeLocation = GLES30.glGetAttribLocation(program, A_PATRICLE_START_TIME)
    }

    /**
     * 设置 始终如一的Uniform变量
     * @param curTime
     */
    fun setUniforms(curTime: Float) {
        GLES30.glUniform1f(uTimeLocation, curTime)
    }

    fun getProgram(): Int {
        return program
    }

    fun getaPositionLocation(): Int {
        return aPositionLocation
    }

    fun getaColorLocation(): Int {
        return aColorLocation
    }

    fun getaDirectionLocation(): Int {
        return aDirectionLocation
    }

    fun getaPatricleStartTimeLocation(): Int {
        return aPatricleStartTimeLocation
    }

    fun useProgram() {
        GLES30.glUseProgram(program)
    }
}