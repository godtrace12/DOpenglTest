package com.example.dj.appgl.wave

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.example.dj.appgl.R
import com.example.dj.appgl.base.AppCore
import com.example.dj.appgl.base.IRenderGesture
import com.example.dj.appgl.util.GLDataUtil
import com.example.dj.appgl.util.ResReadUtils
import com.example.dj.appgl.util.ShaderUtils
import com.example.dj.appgl.util.TextureUtils
import java.nio.FloatBuffer
import java.nio.IntBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * 创建日期：11/14/21 10:02 PM
 * @author daijun
 * @version 1.0
 * @des：VBO（Vertex Buffer Object）是指顶点缓冲区对象，而 EBO（Element Buffer Object）是指图元索引缓冲区对象
 * VAO（Vertex Array Object）是指顶点数组对象，主要用于管理 VBO 或 EBO ，减少 glBindBuffer 、glEnableVertexAttribArray、
 * glVertexAttribPointer 这些调用操作，高效地实现在顶点数组配置之间切换。
 */
class WaveRenderer(ctx: Context?):GLSurfaceView.Renderer,IRenderGesture {
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

    // vbo(顶点缓冲区对象)使用
    //vbo id
    private var vboPosId: Int = 0
    private var vboTextId: Int = 0
    //vao id
    private var vaoId:Int = 0
    // 是否使用vao
    private var isUseVao:Boolean = true

    //每一次取点的时候取几个点
    val COORDS_PER_VERTEX = 3
    val COORDS_PER_FRAGMENT =2

    //每一次取的总的点 大小
    private val vertexStride = COORDS_PER_VERTEX * GLDataUtil.SIZEOF_FLOAT // 4 bytes per vertex
    private val fragmentStride = COORDS_PER_FRAGMENT * GLDataUtil.SIZEOF_FLOAT
    private var touchX:Float = 1.0F
    private var touchY:Float = 1.0F

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
        createVBO()
        if(isUseVao){
            val aPositionLocation = GLES30.glGetAttribLocation(mProgram, "vPosition")
            val aTextureLocation = GLES20.glGetAttribLocation(mProgram, "aTextureCoord")
            createVAO(aPositionLocation,aTextureLocation)
        }
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
//        GLES30.glVertexAttribPointer(aPositionLocation, 3, GLES30.GL_FLOAT, false, 0, mPosBuffer)

        val aTextureLocation = GLES20.glGetAttribLocation(mProgram, "aTextureCoord")
        //纹理坐标数据 x、y，所以数据size是 2
//        GLES30.glVertexAttribPointer(aTextureLocation, 2, GLES30.GL_FLOAT, false, 0, mTexBuffer)
        //启用顶点颜色句柄
        GLES30.glEnableVertexAttribArray(aTextureLocation)

        //只使用VBO
        if(!isUseVao){
            useDetailVBO(vboPosId,aPositionLocation,vertexStride,COORDS_PER_VERTEX)
            // 片元着色器，每个顶点(x,y,z)对应于一个纹理坐标(x,y)，所以片元，每次绘制2个坐标数据
            useDetailVBO(vboTextId,aTextureLocation,fragmentStride,COORDS_PER_FRAGMENT)
        }else{  //使用vao管理vbo
            GLES30.glBindVertexArray(vaoId)
        }

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
//        GLES30.glDisableVertexAttribArray(aPositionLocation)
//        GLES30.glDisableVertexAttribArray(aTextureLocation)
//        mTime++
        if(isUseVao){
            GLES30.glDisableVertexAttribArray(vaoId)
        }
        mTime = (mFrameIndex % 150) /120.0f
        mFrameIndex++
    }

    companion object {
        private const val TAG = "WaveRenderer"
    }


    fun createVBO(){
        //1. 创建VBO
        var vbos = IntArray(2)
        GLES30.glGenBuffers(vbos.size,vbos,0)
        vboPosId = vbos[0]
        vboTextId = vbos[1]
        createDetailVBO(vboPosId,mPosCoordinate.size,mPosBuffer)
        createDetailVBO(vboTextId,mTexCoordinate.size,mTexBuffer)
    }

    fun createVAO(posHandle: Int,textHandle:Int){
        var vaos = IntArray(1)
        GLES30.glGenVertexArrays(1,vaos,0)
        vaoId = vaos[0]
        GLES30.glBindVertexArray(vaos[0])
        // 管理顶点vbo
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER,vboPosId)
        GLES30.glEnableVertexAttribArray(posHandle)
        //把使用vbo中的这段代码放到vao中。
        GLES30.glVertexAttribPointer(posHandle, COORDS_PER_VERTEX, GLES30.GL_FLOAT, false, vertexStride, 0)

        // 管理片纹理vbo
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER,vboTextId)
        GLES30.glEnableVertexAttribArray(textHandle)
        //把使用vbo中的这段代码放到vao中。
        GLES30.glVertexAttribPointer(textHandle, COORDS_PER_FRAGMENT, GLES30.GL_FLOAT, false, fragmentStride, 0)

    }

    //创建vbo
    fun createDetailVBO(voId:Int,dataCount:Int,dataBuffer: FloatBuffer?){
        //2、绑定vbo
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER,voId)
        //3、分配VBO需要的缓存大小
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER,dataCount* GLDataUtil.SIZEOF_FLOAT,
                null,GLES30.GL_STATIC_DRAW)
        //4、为VBO设置顶点数据的值
        GLES30.glBufferSubData(GLES30.GL_ARRAY_BUFFER,0,dataCount * GLDataUtil.SIZEOF_FLOAT,dataBuffer)
        //5、解绑VBO
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER,0)
    }

    //使用vbo
    fun useDetailVBO(voId:Int,posHandle:Int,vtStride:Int,coordPerVer:Int){
        //1. 绑定VBO
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, voId)
        //2. 设置顶点数据
        GLES30.glVertexAttribPointer(posHandle, coordPerVer, GLES30.GL_FLOAT, false, vtStride, 0)
        //3. 解绑VBO
        GLES30.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
    }


    override fun setTouchLocation(x: Float, y: Float) {
        touchX = x
        touchY = y
        super.setTouchLocation(x, y)

    }

    override fun updateModelTransformMatrix(xAngle: Float, yAngle: Float, curScale: Float) {

    }

}