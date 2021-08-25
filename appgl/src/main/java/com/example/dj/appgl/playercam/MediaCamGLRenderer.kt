package com.example.dj.appgl.playercam

import android.content.Context
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.opengl.*
import android.util.Log
import android.view.Surface
import com.example.dj.appgl.R
import com.example.dj.appgl.camera.base.CameraManeger
import com.example.dj.appgl.util.GLDataUtil
import com.example.dj.appgl.util.ResReadUtils
import com.example.dj.appgl.util.ShaderUtils
import java.io.IOException
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * 创建日期：6/23/21 7:08 AM
 * @author daijun
 * @version 1.0
 * @des：mediaplayer 视频播放render
 */
class MediaCamGLRenderer(ctx:Context?, listener: SurfaceTexture.OnFrameAvailableListener?):GLSurfaceView.Renderer {
    private var mContext: Context? = null
    private var mScreenWidth:Int? = 0
    private var mScreenHeight:Int? = 0
    /*** 1- 视频播放相关属性     ***/
    //透视矩阵、相机矩阵定义放在基类中，方便传给其他绘制对象
    private val mMVPMatrix = FloatArray(16)
    private val mTempMatrix = FloatArray(16)
    protected var mProjectMatrix = FloatArray(16)
    private var mProgram = 0

    // 原来的方向不对
    private val mPosCoordinate = floatArrayOf(
            -1f, -1f,1f,
            -1f, 1f,1f,
            1f, -1f,1f,
            1f, 1f,1f)

    private val mTexCoordinate = floatArrayOf(0f, 0f, 0f, 1f, 1f, 0f, 1f, 1f)

    private var mPosBuffer: FloatBuffer? = null
    private var mTexBuffer: FloatBuffer? = null

    lateinit var mPlayer: MediaPlayer
    //!!! 此路径需根据自己情况，改为自己手机里的视频路径
//    private val videoUrl:String = "/storage/emulated/0/Android/data/aom.example.dj.appgl/files/big_buck_bunny.mp4"
    private val videoUrl:String = "https://stream7.iqilu.com/10339/upload_transcode/202002/18/20200218114723HDu3hhxqIT.mp4"
    //    private val videoUrl:String = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4"
    private var textureId = 0
    private lateinit var surfaceTexture:SurfaceTexture
    private var listener: SurfaceTexture.OnFrameAvailableListener? = null
    // 旋转矩阵
    private val rotateOriMatrix = FloatArray(16)

    /**** 2-  摄像头预览相关   *****/
    private var mCameraManeger: CameraManeger? = null
    private var mCameraTexture: SurfaceTexture? = null
    private var mProgramCam = 0
    //透视矩阵、相机矩阵定义放在基类中，方便传给其他绘制对象
    private val mMVPMatrixCam = FloatArray(16)
    private val mPosCoordinateCam = floatArrayOf(
            -1f, -1f,0.5f,
            -1f, 1f,0.5f,
            1f, -1f,0.5f,
            1f, 1f,0.5f)
    private val mTexCoordinateCam = floatArrayOf(0f, 1f, 1f, 1f, 0f, 0f, 1f, 0f)
    private var mPosBufferCam: FloatBuffer? = null
    private var mTexBufferCam: FloatBuffer? = null
    protected var mCameraMatrix = FloatArray(16)


    init {
        this.mContext = ctx
        Matrix.setIdentityM(mProjectMatrix, 0)
        Matrix.setIdentityM(mCameraMatrix, 0)
        Matrix.setIdentityM(mMVPMatrix, 0)
        Matrix.setIdentityM(mTempMatrix, 0)
        mPlayer = MediaPlayer()
        mPosBuffer = GLDataUtil.createFloatBuffer(mPosCoordinate)
        mTexBuffer = GLDataUtil.createFloatBuffer(mTexCoordinate)

        //2、 摄像头相关
        Matrix.setIdentityM(mMVPMatrixCam, 0)
        mPosBufferCam = GLDataUtil.createFloatBuffer(mPosCoordinateCam)
        mTexBufferCam = GLDataUtil.createFloatBuffer(mTexCoordinateCam)
        // 摄像头打开相关工具类
        mCameraManeger = CameraManeger()
    }

    fun initMediaPlayer(){
        mPlayer.reset()
        mPlayer.setDataSource(videoUrl)
        val texture = IntArray(1)
        GLES30.glGenTextures(1, texture, 0) //生成一个OpenGl纹理
        textureId = texture[0]
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]) //申请纹理存储区域并设置相关参数
        GLES30.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR.toFloat())
        GLES30.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR.toFloat())
        GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE)
        GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE)
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,0)
        surfaceTexture = SurfaceTexture(textureId)
        surfaceTexture.setOnFrameAvailableListener(listener)

        val surface = Surface(surfaceTexture)
        mPlayer.setSurface(surface)
        surface.release()
        try {
            mPlayer.prepare()
            mPlayer.start()
        }catch (e:IOException){
            Log.e(Companion.TAG, "initMediaPlayer: $e")
        }
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        //1、 播放器相关着色器初始化
        initPlayerProgram()
        //2、 摄像头预览相关初始化
        initCameraProgram()
    }

    // 1-- 播放器着色器初始化
    private fun initPlayerProgram(){
        //编译顶点着色程序
        val vertexShaderStr = ResReadUtils.readResource(R.raw.vertex_media_player_shade)
        val vertexShaderId = ShaderUtils.compileVertexShader(vertexShaderStr)
        //编译片段着色程序
        // fragment_media_player_normal_shade  --正常
        // fragment_media_player_nostalgia_shade  -- 怀旧滤镜
        // fragment_media_player_negative_shade  -- 负面滤镜
        val fragmentShaderStr = ResReadUtils.readResource(R.raw.fragment_media_player_normal_shade)
        val fragmentShaderId = ShaderUtils.compileFragmentShader(fragmentShaderStr)
        //连接程序
        mProgram = ShaderUtils.linkProgram(vertexShaderId, fragmentShaderId)
        initMediaPlayer()
    }

    //2、 摄像头预览着色器相关初始化
    private fun initCameraProgram(){
        //编译顶点着色程序
        val vertexShaderStrCam = ResReadUtils.readResource(R.raw.vertex_cameraplayer_texture)
        val vertexShaderIdCam = ShaderUtils.compileVertexShader(vertexShaderStrCam)
        //编译片段着色程序
        val fragmentShaderStrCam = ResReadUtils.readResource(R.raw.fragment_camera_transparent_shade)
        val fragmentShaderIdCam = ShaderUtils.compileFragmentShader(fragmentShaderStrCam)
        //连接程序
        mProgramCam = ShaderUtils.linkProgram(vertexShaderIdCam, fragmentShaderIdCam)
        createAndBindVideoTexture()
        mCameraManeger!!.OpenCamera(mCameraTexture)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        mScreenWidth = width
        mScreenHeight = height
    }

    // 因为关闭了深度检测，但是又多传了深度信息，导致绘制异常，sololution：1-去除深度信息 2-摄像头预览的深度改为最上层
    override fun onDrawFrame(gl: GL10?) {
        GLES30.glViewport(0, 0, mScreenWidth!!, mScreenHeight!!)

        GLES30.glEnable(GLES20.GL_DEPTH_TEST)
        GLES30.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        drawVideoPicture(gl)
        // 2、绘制摄像头预览画面
        drawCamPicture(gl)
        GLES30.glUseProgram(0)
    }

    //1- 绘制视频播放画面
    private fun drawVideoPicture(gl: GL10?){
        GLES30.glUseProgram(mProgram)
        var uPosHandle = GLES20.glGetAttribLocation(mProgram, "position")
        var aTexHandle = GLES20.glGetAttribLocation(mProgram, "inputTextureCoordinate")
        var mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "textureTransform")
        var mTexRotateMatrixHandle = GLES20.glGetUniformLocation(mProgram,"uTextRotateMatrix")
        // 将前面计算得到的mMVPMatrix(frustumM setLookAtM 通过multiplyMM 相乘得到的矩阵) 传入vMatrix中，与顶点矩阵进行相乘
        GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0)
        surfaceTexture!!.updateTexImage()

        //?? 为何要取矩阵？？!!!
        surfaceTexture.getTransformMatrix(rotateOriMatrix)

        GLES30.glVertexAttribPointer(uPosHandle, 3, GLES30.GL_FLOAT, false, 0, mPosBuffer)
        GLES30.glVertexAttribPointer(aTexHandle, 2, GLES30.GL_FLOAT, false, 8, mTexBuffer)
        GLES30.glUniformMatrix4fv(mTexRotateMatrixHandle, 1, false, rotateOriMatrix, 0)

        GLES30.glEnableVertexAttribArray(uPosHandle)
        GLES30.glEnableVertexAttribArray(aTexHandle)
        //顶点个数是4个 mPosCoordinate.length/2每个定点x、y2个坐标，所以得到顶点个数。
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, mPosCoordinate.size / 2)
        GLES30.glDisableVertexAttribArray(uPosHandle)
        GLES30.glDisableVertexAttribArray(aTexHandle)
//        GLES30.glUseProgram(0)
    }

    //2- 绘制摄像头预览画面
    private fun drawCamPicture(gl: GL10?){
        //开启混合模式
        GLES30.glEnable(GLES20.GL_BLEND)
        GLES30.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        var camWidth = 300
        var camHeight = 533
        GLES30.glViewport(50, 0, camWidth, camHeight)
        GLES30.glUseProgram(mProgramCam)
        var uPosHandleCam = GLES20.glGetAttribLocation(mProgramCam, "position")
        var aTexHandleCam = GLES20.glGetAttribLocation(mProgramCam, "inputTextureCoordinate")
        var mMVPMatrixHandleCam = GLES20.glGetUniformLocation(mProgramCam, "textureTransform")
        // 将前面计算得到的mMVPMatrix(frustumM setLookAtM 通过multiplyMM 相乘得到的矩阵) 传入vMatrix中，与顶点矩阵进行相乘
        GLES30.glUniformMatrix4fv(mMVPMatrixHandleCam, 1, false, mMVPMatrixCam, 0)
        mCameraTexture!!.updateTexImage()
        GLES30.glVertexAttribPointer(uPosHandleCam, 3, GLES30.GL_FLOAT, false, 0, mPosBufferCam)
        GLES30.glVertexAttribPointer(aTexHandleCam, 2, GLES30.GL_FLOAT, false, 0, mTexBufferCam)
        GLES30.glEnableVertexAttribArray(uPosHandleCam)
        GLES30.glEnableVertexAttribArray(aTexHandleCam)
        //顶点个数是4个 mPosCoordinate.length/2每个定点x、y2个坐标，所以得到顶点个数。
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, mPosCoordinateCam.size / 2)
        GLES30.glDisableVertexAttribArray(uPosHandleCam)
        GLES30.glDisableVertexAttribArray(aTexHandleCam)

        //关闭混合模式
        GLES30.glDisable(GLES20.GL_BLEND)
//        GLES30.glUseProgram(0)
    }


    // 创建摄像头纹理
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

    companion object {
        private const val TAG = "MediaGLRenderer"
    }


}