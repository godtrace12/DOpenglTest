package com.example.dj.appgl.particles.cubicexplose

import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.example.dj.appgl.R
import com.example.dj.appgl.base.AppCore
import com.example.dj.appgl.util.GLDataUtil
import com.example.dj.appgl.util.ResReadUtils
import com.example.dj.appgl.util.ShaderUtils
import com.example.dj.appgl.util.TextureUtils
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * 创建日期：3/20/22 7:51 PM
 * @author daijun
 * @version 1.0
 * @des：
 */
class CubicExploseParticleRenderer : GLSurfaceView.Renderer{

    var cubicVertexTextCoords = floatArrayOf(
            //position            //texture coord
            -0.5f, -0.5f, -0.5f, 0.0f, 0.0f,
            0.5f, -0.5f, -0.5f, 1.0f, 0.0f,
            0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
            0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
            -0.5f, 0.5f, -0.5f, 0.0f, 1.0f,
            -0.5f, -0.5f, -0.5f, 0.0f, 0.0f,
            -0.5f, -0.5f, 0.5f, 0.0f, 0.0f,
            0.5f, -0.5f, 0.5f, 1.0f, 0.0f,
            0.5f, 0.5f, 0.5f, 1.0f, 1.0f,
            0.5f, 0.5f, 0.5f, 1.0f, 1.0f,
            -0.5f, 0.5f, 0.5f, 0.0f, 1.0f,
            -0.5f, -0.5f, 0.5f, 0.0f, 0.0f,
            -0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
            -0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
            -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
            -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
            -0.5f, -0.5f, 0.5f, 0.0f, 0.0f,
            -0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
            0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
            0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
            0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
            0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
            0.5f, -0.5f, 0.5f, 0.0f, 0.0f,
            0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
            -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
            0.5f, -0.5f, -0.5f, 1.0f, 1.0f,
            0.5f, -0.5f, 0.5f, 1.0f, 0.0f,
            0.5f, -0.5f, 0.5f, 1.0f, 0.0f,
            -0.5f, -0.5f, 0.5f, 0.0f, 0.0f,
            -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
            -0.5f, 0.5f, -0.5f, 0.0f, 1.0f,
            0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
            0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
            0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
            -0.5f, 0.5f, 0.5f, 0.0f, 0.0f,
            -0.5f, 0.5f, -0.5f, 0.0f, 1.0f
    )
    protected var vertexShaderCode: String? = null
    protected var fragmentShaderCode: String? = null

    private val modelMatrix: FloatArray? = FloatArray(16)
    private val mMVPMatrix: FloatArray? = FloatArray(16)
    private var viewMatrix: FloatArray? = FloatArray(16)
    private var projectionMatrix: FloatArray? = FloatArray(16)
    private var cubeTexture: Int = 0
    var vertexBuffer: FloatBuffer? = null

    // 实例化渲染相关
    var shaderProgram: Int = 0
    var positionHandle: Int = 0
    var textCoordsHandle: Int = 0
    var mMVPMatrixHandle: Int = 0
    var texturePosHandle: Int = 0
    var instanceMatrixHandle: Int = 0

    // 使用二维数组的方式,4个
    var mInstanceModelMtxArray: FloatArray? = null
    var mInstanceModelMtxBuffer: FloatBuffer? = null
    var instanceCount: Int = 27

    //累计旋转过的角度
    private var angle = 0f
    private var mWidth:Int = 0
    private var mHeight:Int = 0

    init {
        vertexShaderCode = ResReadUtils.readResource(R.raw.cam3d_cubic_intancing_vertext)
        fragmentShaderCode = ResReadUtils.readResource(R.raw.texture_es30_fragment)
//        vertexBuffer = GLDataUtil.createFloatBuffer(cubicVertexTextCoords)

    }


    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        cubeTexture = TextureUtils.loadTexture(AppCore.getInstance().context, R.drawable.hzw5)
        initInstance()
        initInstanceMatrixArray()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        mWidth = width
        mHeight = height
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        GLES20.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
        initProjectViewMatrix(mWidth, mHeight)
        // 启用相关handle
        intanceStart()
        val vertexBuffer: FloatBuffer = GLDataUtil.createFloatBuffer(cubicVertexTextCoords)
        GLES30.glVertexAttribPointer(positionHandle, 3, GLES30.GL_FLOAT,
                false, 5 * 4, vertexBuffer)
        vertexBuffer.position(3)
        GLES30.glVertexAttribPointer(textCoordsHandle, 2, GLES30.GL_FLOAT,
                false, 5 * 4, vertexBuffer)
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.multiplyMM(mMVPMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mMVPMatrix, 0, projectionMatrix, 0, mMVPMatrix, 0)
        GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0)

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, cubeTexture)
        GLES30.glUniform1i(texturePosHandle, 0)

        var posRecOri = 0
        mInstanceModelMtxBuffer!!.position(posRecOri)
        GLES30.glVertexAttribPointer(instanceMatrixHandle, 4, GLES20.GL_FLOAT,
                false, 16 * 4, mInstanceModelMtxBuffer)
        mInstanceModelMtxBuffer!!.position(posRecOri + 4)
        GLES30.glVertexAttribPointer(instanceMatrixHandle + 1, 4, GLES20.GL_FLOAT,
                false, 16 * 4, mInstanceModelMtxBuffer)
        mInstanceModelMtxBuffer!!.position(posRecOri + 8)
        GLES30.glVertexAttribPointer(instanceMatrixHandle + 2, 4, GLES20.GL_FLOAT,
                false, 16 * 4, mInstanceModelMtxBuffer)
        mInstanceModelMtxBuffer!!.position(posRecOri + 12)
        GLES30.glVertexAttribPointer(instanceMatrixHandle + 3, 4, GLES20.GL_FLOAT,
                false, 16 * 4, mInstanceModelMtxBuffer)
        // 1、这个函数告诉了OpenGL该什么时候更新顶点属性的内容至新一组数据。它的第一个参数是需要的顶点属性，第二个参数是属性除数(Attribute Divisor)。
        // 默认情况下，属性除数是0，告诉OpenGL我们需要在顶点着色器的每次迭代时更新顶点属性。将它设置为1时，我们告诉OpenGL我们希望在渲染一个新实例的时候更新顶点属性。
        // 2、mat4的顶点属性，让我们能够存储一个实例化数组的变换矩阵。然而，当我们顶点属性的类型大于vec4时，就要多进行一步处理了。
        // 顶点属性最大允许的数据大小等于一个vec4。因为一个mat4本质上是4个vec4
        GLES30.glVertexAttribDivisor(instanceMatrixHandle, 1)
        GLES30.glVertexAttribDivisor(instanceMatrixHandle + 1, 1)
        GLES30.glVertexAttribDivisor(instanceMatrixHandle + 2, 1)
        GLES30.glVertexAttribDivisor(instanceMatrixHandle + 3, 1)


//        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 36)
        GLES30.glDrawArraysInstanced(GLES30.GL_TRIANGLES, 0, 36, instanceCount)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)
        // 关闭相关handle
        intanceEnd()
//        angle += 8
//        if (angle >= 360) {
//            angle = 0F
//        }
    }

    // 设置透视和view矩阵
    private fun initProjectViewMatrix(aWidth: Int, aHeight: Int) {
        var width = aWidth
        var height = aHeight
        var ratio: Float = ((width + 0.0f) / height)
        //初始化矩阵
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 1f, 7f)
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0.0f, 3f, 0f, 0f, 0.0f, 0f, 1f, 0f)
    }

    fun initInstance(){
        var vertexShaderId: Int = ShaderUtils.compileVertexShader(vertexShaderCode)
        var fragmentShaderId: Int = ShaderUtils.compileFragmentShader(fragmentShaderCode)
        shaderProgram = ShaderUtils.linkProgram(vertexShaderId, fragmentShaderId)
        positionHandle = GLES30.glGetAttribLocation(shaderProgram, "aPosition")
        textCoordsHandle = GLES30.glGetAttribLocation(shaderProgram, "aTexCoords")
        mMVPMatrixHandle = GLES30.glGetUniformLocation(shaderProgram, "uMVPMatrix")
        texturePosHandle = GLES30.glGetUniformLocation(shaderProgram, "texture")
        instanceMatrixHandle = GLES30.glGetAttribLocation(shaderProgram, "aInstanceMatrix")
    }


    private fun initInstanceMatrixArray(){
        var modelMatrices = createMatrices()
        mInstanceModelMtxArray = FloatArray(instanceCount * 16)//存储了2个矩阵数据，每个矩阵16个点
        for (index in 0..(instanceCount - 1)) {
            System.arraycopy(modelMatrices[index], 0, mInstanceModelMtxArray, index * 16, 16)
        }
        mInstanceModelMtxBuffer = GLDataUtil.createFloatBuffer(mInstanceModelMtxArray)
    }

    // 计算3*3*3个位置，分为前中后三排
    private fun createMatrices(): Array<FloatArray> {
        var modelMatrices = Array(instanceCount) { FloatArray(16) }
        var index = 0;
        for (i in -1..1) {
            for (j in -1..1) {
                for (k in -1..1) {
                    var modelMatrix = FloatArray(16)
                    var disUnit = 0.8f
                    var disX = i * disUnit
                    var disY = j * disUnit
                    var disZ = k * disUnit
                    Matrix.setIdentityM(modelMatrix, 0)
                    Matrix.translateM(modelMatrix, 0, disX, disY, disZ)
                    Matrix.rotateM(modelMatrix, 0, angle, 0.0f, 1.0f, 0.0f)
                    Matrix.scaleM(modelMatrix, 0, 0.3f, 0.3f, 0.3f)
                    if (index >= instanceCount) {
                        break
                    }
                    modelMatrices[index] = modelMatrix
                    index++
                }
            }
        }
        return modelMatrices
    }

    private fun intanceStart(){
        GLES30.glUseProgram(shaderProgram)
        GLES30.glEnableVertexAttribArray(positionHandle)
        GLES30.glEnableVertexAttribArray(texturePosHandle)
        GLES30.glEnableVertexAttribArray(instanceMatrixHandle)
        GLES30.glEnableVertexAttribArray(instanceMatrixHandle + 1)
        GLES30.glEnableVertexAttribArray(instanceMatrixHandle + 2)
        GLES30.glEnableVertexAttribArray(instanceMatrixHandle + 3)
    }

    private fun intanceEnd(){
        GLES30.glDisableVertexAttribArray(positionHandle)
        GLES30.glDisableVertexAttribArray(texturePosHandle)
        GLES30.glDisableVertexAttribArray(instanceMatrixHandle)
        GLES30.glDisableVertexAttribArray(instanceMatrixHandle + 1)
        GLES30.glDisableVertexAttribArray(instanceMatrixHandle + 2)
        GLES30.glDisableVertexAttribArray(instanceMatrixHandle + 3)
        GLES30.glUseProgram(0)
    }


}