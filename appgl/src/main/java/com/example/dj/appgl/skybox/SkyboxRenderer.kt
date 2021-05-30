package com.example.dj.appgl.skybox

import android.graphics.Color
import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.Matrix
import android.util.Log
import com.example.dj.appgl.R
import com.example.dj.appgl.base.AppCore
import com.example.dj.appgl.skybox.base.AbsSensorRenderer
import com.example.dj.appgl.util.GLDataUtil
import com.example.dj.appgl.util.ResReadUtils
import com.example.dj.appgl.util.ShaderUtils
import com.example.dj.appgl.util.TextureUtils
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class SkyboxRenderer: AbsSensorRenderer() {
    //立方体的8个顶点
    private val skyboxVertices = floatArrayOf(
            -1f, 1f, 1f,  // 上左前顶点
            1f, 1f, 1f,  // 上右前顶点
            -1f, 1f, -1f,  // 上左后顶点
            1f, 1f, -1f,  // 上右后顶点
            -1f, -1f, 1f,  // 下左前顶点
            1f, -1f, 1f,  // 下右前顶点
            -1f, -1f, -1f,  // 下左后顶点
            1f, -1f, -1f)

    // 立方体索引
    private val skyboxIndex = shortArrayOf(
            // Front
            1, 3, 0,
            0, 3, 2,  // Back
            4, 6, 5,
            5, 6, 7,  // Left
            0, 2, 4,
            4, 2, 6,  // Right
            5, 7, 1,
            1, 7, 3,  // Top
            5, 1, 4,
            4, 1, 0,  // Bottom
            6, 2, 7,
            7, 2, 3
    )

    private val mMVPMatrix: FloatArray? = FloatArray(16)
    private val projectionMatrix: FloatArray? = FloatArray(16)
    private val viewMatrix: FloatArray? = FloatArray(16)
    private val modelMatrix: FloatArray? = FloatArray(16)

    private var cubeTexture:Int = 0
    private var skyboxTexture:Int = 0

    private var rotationMatrix:FloatArray? = FloatArray(16)

    private var skyboxVertexShader: String? = null
    private var skyboxFragmentShader:String? = null


    protected var vertexShaderCode: String? = null
    protected var fragmentShaderCode: String? = null

    private var skyBoxRenderer: SkyBoxRenderer? = null
    private var mWidth:Int = 0
    private var mHeight:Int = 0
    protected var bg = Color.BLACK

    init {
        vertexShaderCode =  ResReadUtils.readResource(R.raw.texture_vertext)
        fragmentShaderCode =  ResReadUtils.readResource(R.raw.texture_fragment)
        skyboxVertexShader = ResReadUtils.readResource(R.raw.advanced_opengl_cube_maps_vertex)
        skyboxFragmentShader = ResReadUtils.readResource(R.raw.advanced_opengl_cube_maps_fragment)

        Matrix.setIdentityM(rotationMatrix, 0)
    }

//    constructor(){
//
//        vertexShaderCode =  ResReadUtils.readResource(R.raw.texture_vertext)
//        fragmentShaderCode =  ResReadUtils.readResource(R.raw.texture_fragment)
//        skyboxVertexShader = ResReadUtils.readResource(R.raw.advanced_opengl_cube_maps_vertex)
//        skyboxFragmentShader = ResReadUtils.readResource(R.raw.advanced_opengl_cube_maps_fragment)
//
//        Matrix.setIdentityM(rotationMatrix, 0)
//    }

    override fun rotation(rotateMatrix: FloatArray) {
        rotationMatrix = rotateMatrix

    }


    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        mWidth = width
        mHeight = height
        var ratio:Float = ((width+0.0f)/height)
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 1f, 1000f)
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 0f, 0f, 0f, -1f, 0f, 1f, 0f)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
//        super.onSurfaceCreated(gl, config)

        skyboxTexture = TextureUtils.createTextureCube(AppCore.getInstance().context, intArrayOf(
                R.drawable.ic_cube_maps_right, R.drawable.ic_cube_maps_left, R.drawable.ic_cube_maps_top,
                R.drawable.ic_cube_maps_bottom, R.drawable.ic_cube_maps_back, R.drawable.ic_cube_maps_front
        ))
        skyBoxRenderer = SkyBoxRenderer()

        // 设置背景色

        // 设置背景色
        GLES20.glClearColor(Color.red(bg) / 255.0f, Color.green(bg) / 255.0f,
                Color.blue(bg) / 255.0f, Color.alpha(bg) / 255.0f)
    }

    override fun onDrawFrame(gl: GL10?) {
        Log.e(Companion.TAG, "onDrawFrame: ")
        // 设置显示范围
        GLES20.glViewport(0, 0, mWidth, mHeight)
        //GLES20.glEnable(GLES20.GL_CULL_FACE);
        //GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        // 清屏
        // 清屏
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        drawSkyBox()
    }



    private fun drawSkyBox() {
        skyBoxRenderer!!.start()
        val vertexBuffer: FloatBuffer = GLDataUtil.createFloatBuffer(skyboxVertices)
        GLES20.glVertexAttribPointer(skyBoxRenderer!!.positionHandle, 3, GLES20.GL_FLOAT,
                false, 3 * 4, vertexBuffer)
        Matrix.setIdentityM(modelMatrix, 0)
        //Matrix.rotateM(modelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f);
        Matrix.multiplyMM(mMVPMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mMVPMatrix, 0, rotationMatrix, 0, mMVPMatrix, 0)
        Matrix.rotateM(mMVPMatrix, 0, 90f, 1f, 0f, 0f)
        Matrix.multiplyMM(mMVPMatrix, 0, projectionMatrix, 0, mMVPMatrix, 0)
        GLES20.glUniformMatrix4fv(skyBoxRenderer!!.mMVPMatrixHandle, 1, false, mMVPMatrix, 0)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, skyboxTexture)
        GLES30.glUniform1i(skyBoxRenderer!!.skyBoxPosHandle, 0)
        //GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);
        GLES30.glDrawElements(GLES30.GL_TRIANGLES, 36,
                GLES30.GL_UNSIGNED_SHORT, GLDataUtil.createShortBuffer(skyboxIndex))
        skyBoxRenderer!!.end()
    }


    inner class SkyBoxRenderer{
        private var shaderProgram:Int =0
        var positionHandle:Int = 0
        var mMVPMatrixHandle:Int =0
        var skyBoxPosHandle:Int =0

        init {
            var vertexShaderId:Int = ShaderUtils.compileVertexShader(skyboxVertexShader)
            var fragmentShaderId:Int = ShaderUtils.compileFragmentShader(skyboxFragmentShader)
            shaderProgram = ShaderUtils.linkProgram(vertexShaderId,fragmentShaderId)
            positionHandle = GLES30.glGetAttribLocation(shaderProgram, "aPosition")
            mMVPMatrixHandle = GLES30.glGetUniformLocation(shaderProgram, "uMVPMatrix")
            skyBoxPosHandle = GLES30.glGetUniformLocation(shaderProgram, "skybox")
        }


        fun start() {
            GLES20.glUseProgram(shaderProgram)
            GLES20.glEnableVertexAttribArray(positionHandle)
        }

        fun end(){
            GLES20.glDisableVertexAttribArray(positionHandle)
            GLES20.glUseProgram(0)
        }

    }

    companion object {
        private const val TAG = "SkyboxRenderer"
    }


}