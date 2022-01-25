package com.example.dj.appgl.camera3d.obj

import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.Matrix
import android.util.Log
import com.example.dj.appgl.R
import com.example.dj.appgl.base.AppCore
import com.example.dj.appgl.camera.base.AbsObjectRender
import com.example.dj.appgl.util.GLDataUtil
import com.example.dj.appgl.util.ResReadUtils
import com.example.dj.appgl.util.ShaderUtils
import com.example.dj.appgl.util.TextureUtils
import java.nio.FloatBuffer

class CubicIntancingRender: AbsObjectRender() {
    private val TAG = "CubicIntancingRender"

    private var cubeVertices: FloatArray? = floatArrayOf( // positions          // texture Coords
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

    private var textureRenderer: TextureRenderer? = null


    private val modelMatrix: FloatArray? = FloatArray(16)
    private val mMVPMatrix: FloatArray? = FloatArray(16)
    private var viewMatrix: FloatArray? = FloatArray(16)
    private var projectionMatrix: FloatArray? = FloatArray(16)
    private var cubeTexture:Int = 0

    // 使用二维数组的方式,4个
    var mInstanceModelMtxArray:FloatArray? = null
    var mInstanceModelMtxBuffer:FloatBuffer? = null
    var instanceCount:Int = 2
    //累计旋转过的角度
    private var angle = 0f

    /**
     * 【说明】： 在onSurfaceCreated中调用,program要在onSurfaceCreated中调用才能成功
     * @author daijun
     * @date 2020/6/30 13:57
     * @param
     * @return
     */
    override fun initProgram() {
        vertexShaderCode =  ResReadUtils.readResource(R.raw.cam3d_cubic_intancing_vertext)
        fragmentShaderCode =  ResReadUtils.readResource(R.raw.texture_es30_fragment)
        cubeTexture = TextureUtils.loadTexture(AppCore.getInstance().context,R.drawable.hzw5)
        textureRenderer = TextureRenderer()

        mProgram = textureRenderer!!.shaderProgram
        if (mProgram == 0){
            Log.e(TAG, "initProgram: cubic 初始化失败")
        }
    }

    // 设置透视和view矩阵
    private fun initProjectViewMatrix(aWidth : Int,aHeight:Int){
        var width = aWidth
        var height = aHeight
        var ratio:Float = ((width+0.0f)/height)
        //初始化矩阵
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 1f, 7f)
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0.0f, 3f, 0f, 0f, 0.0f, 0f, 1f, 0f)
    }

    override fun onDrawFrame() {
        drawTexture()
    }

    private fun drawTexture() {
        initProjectViewMatrix(mWidth,mHeight)
        textureRenderer!!.start()
        val vertexBuffer: FloatBuffer = GLDataUtil.createFloatBuffer(cubeVertices)
        GLES30.glVertexAttribPointer(textureRenderer!!.positionHandle, 3, GLES30.GL_FLOAT,
                false, 5 * 4, vertexBuffer)
        vertexBuffer.position(3)
        GLES30.glVertexAttribPointer(textureRenderer!!.textCoordsHandle, 2, GLES30.GL_FLOAT,
                false, 5 * 4, vertexBuffer)
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.multiplyMM(mMVPMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mMVPMatrix, 0, projectionMatrix, 0, mMVPMatrix, 0)
        GLES30.glUniformMatrix4fv(textureRenderer!!.mMVPMatrixHandle, 1, false, mMVPMatrix, 0)

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, cubeTexture)
        GLES30.glUniform1i(textureRenderer!!.texturePosHandle, 0)


        for (i in 0..(instanceCount-1)){
            mInstanceModelMtxBuffer!!.position(0)
            GLES30.glVertexAttribPointer(textureRenderer!!.instanceMatrixHandle, 4, GLES20.GL_FLOAT,
                    false, 16 * 4, mInstanceModelMtxBuffer)
            mInstanceModelMtxBuffer!!.position(4)
            GLES30.glVertexAttribPointer(textureRenderer!!.instanceMatrixHandle + 1, 4, GLES20.GL_FLOAT,
                    false, 16 * 4, mInstanceModelMtxBuffer)
            mInstanceModelMtxBuffer!!.position(8)
            GLES30.glVertexAttribPointer(textureRenderer!!.instanceMatrixHandle + 2, 4, GLES20.GL_FLOAT,
                    false, 16 * 4, mInstanceModelMtxBuffer)
            mInstanceModelMtxBuffer!!.position(12)
            GLES30.glVertexAttribPointer(textureRenderer!!.instanceMatrixHandle + 3, 4, GLES20.GL_FLOAT,
                    false, 16 * 4, mInstanceModelMtxBuffer)

            GLES30.glVertexAttribDivisor(textureRenderer!!.instanceMatrixHandle, 1)
            GLES30.glVertexAttribDivisor(textureRenderer!!.instanceMatrixHandle + 1, 1)
            GLES30.glVertexAttribDivisor(textureRenderer!!.instanceMatrixHandle + 2, 1)
            GLES30.glVertexAttribDivisor(textureRenderer!!.instanceMatrixHandle + 3, 1)
        }


//        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 36)
        GLES30.glDrawArraysInstanced(GLES30.GL_TRIANGLES, 0, 36,instanceCount)
//        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)
//        GLES30.glDrawArraysInstanced(GLES20.GL_TRIANGLES, 0, 6, 100)

        textureRenderer!!.end()
//        angle +=2
//        if(angle >= 360){
//            angle = 0F
//        }
    }


    inner class TextureRenderer{
         var shaderProgram:Int =0
        var positionHandle:Int = 0
        var textCoordsHandle:Int = 0
        var mMVPMatrixHandle:Int =0
        var texturePosHandle:Int =0
        var instanceMatrixHandle:Int =0

        init {
            var vertexShaderId:Int = ShaderUtils.compileVertexShader(vertexShaderCode)
            var fragmentShaderId:Int = ShaderUtils.compileFragmentShader(fragmentShaderCode)
            shaderProgram = ShaderUtils.linkProgram(vertexShaderId, fragmentShaderId)
            positionHandle = GLES30.glGetAttribLocation(shaderProgram, "aPosition")
            textCoordsHandle = GLES30.glGetAttribLocation(shaderProgram, "aTexCoords")
            mMVPMatrixHandle = GLES30.glGetUniformLocation(shaderProgram, "uMVPMatrix")
            texturePosHandle = GLES30.glGetUniformLocation(shaderProgram, "texture")
            instanceMatrixHandle = GLES30.glGetAttribLocation(shaderProgram, "aInstanceMatrix")
            Log.e(TAG, "instanceMatrixHandle=$instanceMatrixHandle posHandle=$positionHandle textCoordHandle=$texturePosHandle")

            var modelMatrices = createMatrices()
            mInstanceModelMtxArray = FloatArray(instanceCount*16)//存储了2个矩阵数据，每个矩阵16个点
            for (index in 0..(instanceCount-1)){
                System.arraycopy(modelMatrices[index],0,mInstanceModelMtxArray,index*16,16)
            }
            mInstanceModelMtxBuffer = GLDataUtil.createFloatBuffer(mInstanceModelMtxArray)
        }

        private fun createMatrices(): Array<FloatArray> {
            var modelMatrices = Array(instanceCount) { FloatArray(16) }
            for (index in modelMatrices.indices){
                var modelMatrix = FloatArray(16)
                Matrix.setIdentityM(modelMatrix,0)
                if(index == 0){
                    Matrix.translateM(modelMatrix,0,0.8f,0.8f,0.0f)
                }else{
                    Matrix.translateM(modelMatrix,0,0.0f,-0.8f,0.0f)
                }
                modelMatrices[index] = modelMatrix
            }
            return modelMatrices
        }

        fun start() {
            GLES30.glUseProgram(shaderProgram)
            GLES30.glEnableVertexAttribArray(positionHandle)
            GLES30.glEnableVertexAttribArray(texturePosHandle)
            GLES30.glEnableVertexAttribArray(instanceMatrixHandle)
            GLES30.glEnableVertexAttribArray(instanceMatrixHandle + 1)
            GLES30.glEnableVertexAttribArray(instanceMatrixHandle + 2)
            GLES30.glEnableVertexAttribArray(instanceMatrixHandle + 3)
        }

        fun end() {
            GLES30.glDisableVertexAttribArray(positionHandle)
            GLES30.glDisableVertexAttribArray(texturePosHandle)
            GLES30.glDisableVertexAttribArray(instanceMatrixHandle)
            GLES30.glDisableVertexAttribArray(instanceMatrixHandle + 1)
            GLES30.glDisableVertexAttribArray(instanceMatrixHandle + 2)
            GLES30.glDisableVertexAttribArray(instanceMatrixHandle + 3)
            GLES30.glUseProgram(0)
        }


    }


}