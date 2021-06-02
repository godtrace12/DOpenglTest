package com.example.dj.appgl.camera3d.obj

import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.Matrix
import android.util.Log
import com.example.dj.appgl.R
import com.example.dj.appgl.base.AppCore
import com.example.dj.appgl.camera.base.AbsObjectRender
import com.example.dj.appgl.skybox.SkyboxRenderer
import com.example.dj.appgl.util.GLDataUtil
import com.example.dj.appgl.util.ResReadUtils
import com.example.dj.appgl.util.ShaderUtils
import com.example.dj.appgl.util.TextureUtils
import java.nio.FloatBuffer

class CubicRender: AbsObjectRender() {
    private val TAG = "TriangleColorRender"

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
    private var shaderProgram:Int =0


    /**
     * 【说明】： 在onSurfaceCreated中调用,program要在onSurfaceCreated中调用才能成功
     * @author daijun
     * @date 2020/6/30 13:57
     * @param
     * @return
     */
    override fun initProgram() {
        vertexShaderCode =  ResReadUtils.readResource(R.raw.texture_vertext)
        fragmentShaderCode =  ResReadUtils.readResource(R.raw.texture_fragment)
        cubeTexture = TextureUtils.loadTexture(AppCore.getInstance().context,R.drawable.hzw2)
        textureRenderer = TextureRenderer()


//        var vertexShaderId:Int = ShaderUtils.compileVertexShader(vertexShaderCode)
//        var fragmentShaderId:Int = ShaderUtils.compileFragmentShader(fragmentShaderCode)
//        mProgram = ShaderUtils.linkProgram(vertexShaderId, fragmentShaderId)
//        shaderProgram = mProgram
        mProgram = textureRenderer!!.shaderProgram
        if (mProgram == 0){
            Log.e(TAG, "initProgram: cubic 初始化失败")
        }
        var width = 1080
        var height = 2076
        var ratio:Float = ((width+0.0f)/height)
        Log.e(TAG, "onSurfaceChanged: mWidth="+width+" mHeight="+height+" ratio="+ratio)
        //初始化矩阵
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 1f, 7f)
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 1f, 0f, 0f, 0.2f, 0f, 1f, 0f)

    }

    override fun onDrawFrame() {
//        projectionMatrix = projectMatrix
//        viewMatrix = cameraMatrix
        drawTexture()

    }

    private fun drawTexture() {
//        Log.e(TAG, "drawTexture: cubic render")
        textureRenderer!!.start()
        val vertexBuffer: FloatBuffer = GLDataUtil.createFloatBuffer(cubeVertices)
        GLES20.glVertexAttribPointer(textureRenderer!!.positionHandle, 3, GLES20.GL_FLOAT,
                false, 5 * 4, vertexBuffer)
        vertexBuffer.position(3)
        GLES20.glVertexAttribPointer(textureRenderer!!.textCoordsHandle, 2, GLES20.GL_FLOAT,
                false, 5 * 4, vertexBuffer)
        Matrix.setIdentityM(modelMatrix, 0)
//        Matrix.translateM(modelMatrix, 0, 0.5f, 0.5f, -2f)
        Matrix.scaleM(modelMatrix, 0, 0.5f, 0.5f, 0.5f)
        Matrix.rotateM(modelMatrix, 0, 45f, 1.0f, 0f, 0f)
        Matrix.multiplyMM(mMVPMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mMVPMatrix, 0, projectionMatrix, 0, mMVPMatrix, 0)
        GLES20.glUniformMatrix4fv(textureRenderer!!.mMVPMatrixHandle, 1, false, mMVPMatrix, 0)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES30.GL_TEXTURE_2D, cubeTexture)
        GLES20.glUniform1i(textureRenderer!!.texturePosHandle, 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36)
        textureRenderer!!.end()
    }


    inner class TextureRenderer{
         var shaderProgram:Int =0
        var positionHandle:Int = 0
        var textCoordsHandle:Int = 0
        var mMVPMatrixHandle:Int =0
        var texturePosHandle:Int =0

        init {
            var vertexShaderId:Int = ShaderUtils.compileVertexShader(vertexShaderCode)
            var fragmentShaderId:Int = ShaderUtils.compileFragmentShader(fragmentShaderCode)
            shaderProgram = ShaderUtils.linkProgram(vertexShaderId, fragmentShaderId)
            positionHandle = GLES20.glGetAttribLocation(shaderProgram, "aPosition")
            textCoordsHandle = GLES20.glGetAttribLocation(shaderProgram, "aTexCoords")
            mMVPMatrixHandle = GLES20.glGetUniformLocation(shaderProgram, "uMVPMatrix")
            texturePosHandle = GLES20.glGetUniformLocation(shaderProgram, "texture")
        }

        fun start() {
            GLES20.glUseProgram(shaderProgram)
            GLES20.glEnableVertexAttribArray(positionHandle)
            GLES20.glEnableVertexAttribArray(texturePosHandle)
        }

        fun end() {
            GLES20.glDisableVertexAttribArray(positionHandle)
            GLES20.glDisableVertexAttribArray(texturePosHandle)
            GLES20.glUseProgram(0)
        }


    }


}