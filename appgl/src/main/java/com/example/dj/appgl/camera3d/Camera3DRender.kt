package com.example.dj.appgl.camera3d

import android.graphics.SurfaceTexture
import android.graphics.SurfaceTexture.OnFrameAvailableListener
import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import androidx.camera.core.Preview
import androidx.lifecycle.LifecycleOwner
import com.example.dj.appgl.R
import com.example.dj.appgl.camera.base.CameraManegerXx
import com.example.dj.appgl.util.GLDataUtil
import com.example.dj.appgl.util.ResReadUtils
import com.example.dj.appgl.util.ShaderUtils
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class Camera3DRender(surfaceView: GLSurfaceView): GLSurfaceView.Renderer, Preview.OnPreviewOutputUpdateListener,
        OnFrameAvailableListener{
    private var glSurfaceView:GLSurfaceView? = null
    private var mCameraManeger:CameraManegerXx? = null
    private var texture:IntArray? = null
    private var mCameraTexture: SurfaceTexture? = null
    private var mWidth:Int? = 0
    private var mHeight:Int? =0

    //------------------ 1 摄像机预览画面------------------
    private var mPosCoordinate: FloatArray? = floatArrayOf(-1f, -1f, -1f, 1f, 1f, -1f, 1f, 1f)
    private val mTexCoordinate = floatArrayOf(0f, 1f, 1f, 1f, 0f, 0f, 1f, 0f)
    private var mPosBuffer: FloatBuffer? = null
    private var mTexBuffer: FloatBuffer? = null
    protected var mProjectMatrix = FloatArray(16)
    protected var mCameraMatrix = FloatArray(16)
    private var mMVPMatrix = FloatArray(16)
    private var mTempMatrix = FloatArray(16)
    private var mProgram = 0

    private var uPosHandle = 0
    private var aTexHandle = 0
    private var mMVPMatrixHandle = 0




    init {
        glSurfaceView = surfaceView
        var lifecycleOwner:LifecycleOwner = glSurfaceView!!.context as LifecycleOwner
        mCameraManeger = CameraManegerXx(lifecycleOwner,this)

        mPosBuffer = GLDataUtil.createFloatBuffer(mPosCoordinate)
        mTexBuffer = GLDataUtil.createFloatBuffer(mTexCoordinate)


        //编译顶点着色程序

        //编译顶点着色程序
        val vertexShaderStr = ResReadUtils.readResource(R.raw.vertex_camera_texture)
        val vertexShaderId = ShaderUtils.compileVertexShader(vertexShaderStr)
        //编译片段着色程序
        //编译片段着色程序
        val fragmentShaderStr = ResReadUtils.readResource(R.raw.fragment_camera_quarter_mirror_shade)
        val fragmentShaderId = ShaderUtils.compileFragmentShader(fragmentShaderStr)
        //连接程序
        //连接程序
        mProgram = ShaderUtils.linkProgram(vertexShaderId, fragmentShaderId)

        Matrix.setIdentityM(mProjectMatrix, 0)
        Matrix.setIdentityM(mCameraMatrix, 0)
        Matrix.setIdentityM(mMVPMatrix, 0)
        Matrix.setIdentityM(mTempMatrix, 0)

    }


    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        texture = IntArray(1)
        mCameraTexture!!.attachToGLContext(texture!![0])
        mCameraTexture!!.setOnFrameAvailableListener(this)

    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        mWidth = width
        mHeight = height

        GLES30.glViewport(0, 0, width, height)
        val ratio = width.toFloat() / height
        Matrix.orthoM(mProjectMatrix, 0, -ratio, ratio, -1f, 1f, 1f, 7f)
        Matrix.setLookAtM(mCameraMatrix, 0, 0f, 0f, 3f,
                0f, 0f, 0f,
                0f, 1.0f, 0.0f) // 3代表眼睛的坐标点

        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mCameraMatrix, 0)

    }

    override fun onDrawFrame(gl: GL10?) {
        mCameraTexture!!.updateTexImage()


        GLES30.glUseProgram(mProgram)
        uPosHandle = GLES20.glGetAttribLocation(mProgram, "position")
        aTexHandle = GLES20.glGetAttribLocation(mProgram, "inputTextureCoordinate")
//        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "textureTransform")

        // 将前面计算得到的mMVPMatrix(frustumM setLookAtM 通过multiplyMM 相乘得到的矩阵) 传入vMatrix中，与顶点矩阵进行相乘
//        GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0)

        GLES30.glVertexAttribPointer(uPosHandle, 2, GLES30.GL_FLOAT, false, 0, mPosBuffer)
        GLES30.glVertexAttribPointer(aTexHandle, 2, GLES30.GL_FLOAT, false, 0, mTexBuffer)

        GLES30.glEnableVertexAttribArray(uPosHandle)
        GLES30.glEnableVertexAttribArray(aTexHandle)
        //顶点个数是4个 mPosCoordinate.length/2每个定点x、y2个坐标，所以得到顶点个数。
        //顶点个数是4个 mPosCoordinate.length/2每个定点x、y2个坐标，所以得到顶点个数。
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, mPosCoordinate!!.size / 2)

        GLES30.glDisableVertexAttribArray(uPosHandle)
        GLES30.glDisableVertexAttribArray(aTexHandle)

        GLES30.glUseProgram(0)

    }


    override fun onUpdated(output: Preview.PreviewOutput?) {
        mCameraTexture = output!!.surfaceTexture
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
        //  请求执行一次 onDrawFrame -- 是否外边已经执行了此操作，此处不用重复？？
        glSurfaceView!!.requestRender()
    }
}