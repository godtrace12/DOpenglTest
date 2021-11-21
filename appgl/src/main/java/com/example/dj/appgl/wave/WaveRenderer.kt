package com.example.dj.appgl.wave

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import com.example.dj.appgl.R
import com.example.dj.appgl.base.AppCore
import com.example.dj.appgl.base.IRenderGesture
import com.example.dj.appgl.util.GLDataUtil
import com.example.dj.appgl.util.ResReadUtils
import com.example.dj.appgl.util.ShaderUtils
import com.example.dj.appgl.util.TextureUtils
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * 创建日期：11/14/21 10:02 PM
 * @author daijun
 * @version 1.0
 * @des：
 */
class WaveRenderer(ctx: Context?, override var touchX: Float, override var touchY: Float):GLSurfaceView.Renderer,IRenderGesture {
    private var mContext: Context? = null
    //透视矩阵、相机矩阵定义放在基类中，方便传给其他绘制对象
    private val mMVPMatrix = FloatArray(16)
    private val mTempMatrix = FloatArray(16)
    private val mViewMatrix = FloatArray(16)
    protected var mProjectMatrix = FloatArray(16)
    private var mProgram = 0
    // 原来的方向不对
    private val mPosCoordinate = floatArrayOf(
            1f, 1f, 0.0f, // top
            -1f, -1f, 0.0f, // bottom left
            1f, -1f, 0.0f, // bottom right

            1f, 1f, 0.0f, // top
            -1f, -1f, 0.0f,// bottom left
            -1f,1f,0.0f// top left
    )

    private val mTexCoordinate = floatArrayOf(1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,0.0f,0.0f)
    private var mPosBuffer: FloatBuffer? = null
    private var mTexBuffer: FloatBuffer? = null

    //纹理id
    private var textureId = 0

    // 波动相关参数
    // 图片分辨率
    private val mImgResolution = floatArrayOf(2048.0f,2048.0f)
    // 时间-动态改变
    private var mTime:Float = 0.0f
    private var mResolutionBuffer: FloatBuffer? = null
    private var mFrameIndex =0
    private var mWidth:Int? = 1
    private var mHeight:Int? = 1

    init {
        this.mContext = ctx
        Matrix.setIdentityM(mProjectMatrix, 0)
        Matrix.setIdentityM(mMVPMatrix, 0)
        Matrix.setIdentityM(mTempMatrix, 0)

        mPosBuffer = GLDataUtil.createFloatBuffer(mPosCoordinate)
        mTexBuffer = GLDataUtil.createFloatBuffer(mTexCoordinate)
        mResolutionBuffer = GLDataUtil.createFloatBuffer(mImgResolution)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(0.5f, 0.5f, 0.5f, 1.0f)
        //编译顶点着色程序
        val vertexShaderStr = ResReadUtils.readResource(R.raw.vertex_wave_texture_shader)
        val vertexShaderId = ShaderUtils.compileVertexShader(vertexShaderStr)
        //编译片段着色程序
        // 1-- 左右波动
//        val fragmentShaderStr = ResReadUtils.readResource(R.raw.fragment_wave_texture_shader)
        // 2-- 中间圆心点击水波纹波动
        val fragmentShaderStr = ResReadUtils.readResource(R.raw.fragment_point_wave_texture_shader)
        val fragmentShaderId = ShaderUtils.compileFragmentShader(fragmentShaderStr)
        //连接程序
        mProgram = ShaderUtils.linkProgram(vertexShaderId, fragmentShaderId)
        //在OpenGLES环境中使用程序
        GLES30.glUseProgram(mProgram)
        //加载纹理
        textureId = TextureUtils.loadTexture(AppCore.getInstance().context, R.drawable.ic_cube_maps_left)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        mWidth = width
        mHeight = height
        touchX = mWidth!! /2.0f
        touchY = mHeight!! /2.0f
        val ratio = width.toFloat() / height
        //设置透视投影
        // 1-- 正交投影
//        Matrix.orthoM(mProjectMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
        // 2-- 透视投影
        Matrix.frustumM(mProjectMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0, 0f, 0f, 6.0f,  //摄像机坐标
                0f, 0f, 0f,  //目标物的中心坐标
                0f, 1.0f, 0.0f) //相机方向

        //接着是摄像机顶部的方向了，如下图，很显然相机旋转，up的方向就会改变，这样就会会影响到绘制图像的角度。
        //例如设置up方向为y轴正方向，upx = 0,upy = 1,upz = 0。这是相机正对着目标图像
        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0)

    }

    override fun onDrawFrame(gl: GL10?) {
//        Log.e(TAG, "onDrawFrame: dj------- mTime="+mTime)
        //左乘矩阵
        val uMaxtrixLocation = GLES30.glGetUniformLocation(mProgram, "vMatrix")
        // 将前面计算得到的mMVPMatrix(frustumM setLookAtM 通过multiplyMM 相乘得到的矩阵) 传入vMatrix中，与顶点矩阵进行相乘
        GLES30.glUniformMatrix4fv(uMaxtrixLocation, 1, false, mMVPMatrix, 0)

        val aPositionLocation = GLES30.glGetAttribLocation(mProgram, "vPosition")
        GLES30.glEnableVertexAttribArray(aPositionLocation)
        //x y z 所以数据size 是3
        GLES30.glVertexAttribPointer(aPositionLocation, 3, GLES30.GL_FLOAT, false, 0, mPosBuffer)

        val aTextureLocation = GLES20.glGetAttribLocation(mProgram, "aTextureCoord")
//        Log.e(Companion.TAG, "onDrawFrame: textureLocation=$aTextureLocation")
        //纹理坐标数据 x、y，所以数据size是 2
        GLES30.glVertexAttribPointer(aTextureLocation, 2, GLES30.GL_FLOAT, false, 0, mTexBuffer)
        //启用顶点颜色句柄
        GLES30.glEnableVertexAttribArray(aTextureLocation)

        //波浪相关
        val aResolutionLocation = GLES30.glGetUniformLocation(mProgram,"u_resolution")
        val aTimeLocation = GLES30.glGetUniformLocation(mProgram,"u_time")
//        GLES30.glUniformMatrix2fv(aResolutionLocation, 2, false, mImgResolution, 0)
        GLES20.glUniform2f(aResolutionLocation, 2048.0f, 2048.0f)
        GLES30.glUniform1f(aTimeLocation,mTime)

        // 点击坐标传入水波纹点击
        val aTouchLocation = GLES30.glGetUniformLocation(mProgram,"u_TouchXY")
        if (aTouchLocation !=0){
            GLES30.glUniform2f(aTouchLocation,touchX/mWidth!!,touchY/mHeight!!)
        }

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        //绑定纹理
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId)
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 6)
        //禁止顶点数组的句柄
        GLES30.glDisableVertexAttribArray(aPositionLocation)
        GLES30.glDisableVertexAttribArray(aTextureLocation)
//        mTime++
        mTime = (mFrameIndex % 150) /120.0f
        mFrameIndex++
    }

    companion object {
        private const val TAG = "WaveRenderer"
    }

//    override var touchX: Float
//        get() = touchX
//        set(value) {
////            touchX = value
//        }
//    override var touchY: Float
//        get() = touchY
//        set(value) {
////            touchY = value
//        }

    override fun setTouchLocation(x: Float, y: Float) {
        touchX = x
        touchY = y
        super.setTouchLocation(x, y)

    }

}