package com.example.dj.appgl.player

import android.content.Context
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.opengl.*
import android.util.Log
import android.view.Surface
import com.example.dj.appgl.R
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
class MediaGLRenderer(ctx:Context?,listener: SurfaceTexture.OnFrameAvailableListener?):GLSurfaceView.Renderer {
    private var mContext: Context? = null
    //透视矩阵、相机矩阵定义放在基类中，方便传给其他绘制对象
    private val mMVPMatrix = FloatArray(16)
    private val mTempMatrix = FloatArray(16)
    protected var mProjectMatrix = FloatArray(16)
    protected var mCameraMatrix = FloatArray(16)
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
    private val videoUrl:String = "/storage/emulated/0/Android/data/aom.example.dj.appgl/files/big_buck_bunny.mp4"
//    private val videoUrl:String = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4"
    private var textureId = 0
    private lateinit var surfaceTexture:SurfaceTexture
    private var listener: SurfaceTexture.OnFrameAvailableListener? = null

    private var uPosHandle = 0
    private var aTexHandle = 0
    private var mMVPMatrixHandle = 0
    private var mTexRotateMatrixHandle = 0
    // 旋转矩阵
    private val rotateOriMatrix = FloatArray(16)

    init {
        this.mContext = ctx
        Matrix.setIdentityM(mProjectMatrix, 0)
        Matrix.setIdentityM(mCameraMatrix, 0)
        Matrix.setIdentityM(mMVPMatrix, 0)
        Matrix.setIdentityM(mTempMatrix, 0)
        mPlayer = MediaPlayer()
        mPosBuffer = GLDataUtil.createFloatBuffer(mPosCoordinate)
        mTexBuffer = GLDataUtil.createFloatBuffer(mTexCoordinate)
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

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {

    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glEnable(GLES20.GL_DEPTH_TEST)
        GLES30.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        GLES30.glUseProgram(mProgram)
        uPosHandle = GLES20.glGetAttribLocation(mProgram, "position")
        aTexHandle = GLES20.glGetAttribLocation(mProgram, "inputTextureCoordinate")
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "textureTransform")
        mTexRotateMatrixHandle = GLES20.glGetUniformLocation(mProgram,"uTextRotateMatrix")
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
        GLES30.glUseProgram(0)
    }

    companion object {
        private const val TAG = "MediaGLRenderer"
    }


}