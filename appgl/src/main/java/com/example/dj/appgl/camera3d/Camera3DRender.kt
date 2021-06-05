package com.example.dj.appgl.camera3d

import android.content.Context
import android.graphics.SurfaceTexture
import android.graphics.SurfaceTexture.OnFrameAvailableListener
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.Matrix
import com.example.dj.appgl.R
import com.example.dj.appgl.camera.base.BaseCameraRenderer
import com.example.dj.appgl.camera.base.CameraManeger
import com.example.dj.appgl.util.GLDataUtil
import com.example.dj.appgl.util.ResReadUtils
import com.example.dj.appgl.util.ShaderUtils
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class Camera3DRender(ctx: Context?, listener: OnFrameAvailableListener?): BaseCameraRenderer(){
    private val TAG = "Camera3DRender"
    private var mContext: Context? = null
    private var mCameraManeger: CameraManeger? = null
    private var mCameraTexture: SurfaceTexture? = null
    private var listener: OnFrameAvailableListener? = null

    private var mProgram = 0

    private var uPosHandle = 0
    private var aTexHandle = 0
    private var mMVPMatrixHandle = 0

    //透视矩阵、相机矩阵定义放在基类中，方便传给其他绘制对象
    private val mMVPMatrix = FloatArray(16)
    private val mTempMatrix = FloatArray(16)

    private val mPosCoordinate = floatArrayOf(
            -1f, -1f,1f,
            -1f, 1f,1f,
            1f, -1f,1f,
            1f, 1f,1f)
    private val mTexCoordinate = floatArrayOf(0f, 1f, 1f, 1f, 0f, 0f, 1f, 0f)

    private var mPosBuffer: FloatBuffer? = null
    private var mTexBuffer: FloatBuffer? = null

    init {
        this.mContext = ctx
        Matrix.setIdentityM(mProjectMatrix, 0)
        Matrix.setIdentityM(mCameraMatrix, 0)
        Matrix.setIdentityM(mMVPMatrix, 0)
        Matrix.setIdentityM(mTempMatrix, 0)
        this.listener = listener
        mCameraManeger = CameraManeger()
        mPosBuffer = GLDataUtil.createFloatBuffer(mPosCoordinate)
        mTexBuffer = GLDataUtil.createFloatBuffer(mTexCoordinate)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        //编译顶点着色程序
        val vertexShaderStr = ResReadUtils.readResource(R.raw.vertex_camera3d_texture)
        val vertexShaderId = ShaderUtils.compileVertexShader(vertexShaderStr)
        //编译片段着色程序
        val fragmentShaderStr = ResReadUtils.readResource(R.raw.fragment_camera_shade)
        val fragmentShaderId = ShaderUtils.compileFragmentShader(fragmentShaderStr)
        //连接程序
        mProgram = ShaderUtils.linkProgram(vertexShaderId, fragmentShaderId)
        createAndBindVideoTexture()
        mCameraManeger!!.OpenCamera(mCameraTexture)

        // 调用父类，完成另外添加进来的图形的初始化
        super.onSurfaceCreated(gl, config)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        super.onSurfaceChanged(gl, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glEnable(GLES20.GL_DEPTH_TEST)
        GLES30.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        //开启深度测试
        /********** 绘制摄像头画面   */
        //在OpenGLES环境中使用程序
        GLES30.glUseProgram(mProgram)
        uPosHandle = GLES20.glGetAttribLocation(mProgram, "position")
        aTexHandle = GLES20.glGetAttribLocation(mProgram, "inputTextureCoordinate")
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "textureTransform")
        // 将前面计算得到的mMVPMatrix(frustumM setLookAtM 通过multiplyMM 相乘得到的矩阵) 传入vMatrix中，与顶点矩阵进行相乘
        GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0)
        mCameraTexture!!.updateTexImage() //通过此方法更新接收到的预览数据
        GLES30.glVertexAttribPointer(uPosHandle, 3, GLES30.GL_FLOAT, false, 0, mPosBuffer)
        GLES30.glVertexAttribPointer(aTexHandle, 2, GLES30.GL_FLOAT, false, 0, mTexBuffer)
        GLES30.glEnableVertexAttribArray(uPosHandle)
        GLES30.glEnableVertexAttribArray(aTexHandle)
        //顶点个数是4个 mPosCoordinate.length/2每个定点x、y2个坐标，所以得到顶点个数。
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, mPosCoordinate.size / 2)
        GLES30.glDisableVertexAttribArray(uPosHandle)
        GLES30.glDisableVertexAttribArray(aTexHandle)
        GLES30.glUseProgram(0)
        /********* 开始绘制三角形  */
        // 调用父类，完成另外添加进来的图形的绘制
        super.onDrawFrame(gl)
    }

    private fun createAndBindVideoTexture() {
        val texture = IntArray(1)
        GLES30.glGenTextures(1, texture, 0) //生成一个OpenGl纹理
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]) //申请纹理存储区域并设置相关参数
        GLES30.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR.toFloat())
        GLES30.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR.toFloat())
        GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE)
        GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE)
        mCameraTexture = SurfaceTexture(texture[0]) //以上面OpenGl生成的纹理函数参数创建SurfaceTexture,SurfaceTexture接收的数据将传入该纹理
        mCameraTexture!!.setOnFrameAvailableListener(listener) //设置SurfaceTexture的回调，通过摄像头预览数据已更新
    }

}