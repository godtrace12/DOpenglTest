package com.example.dj.appgl.mrt

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
 * 创建日期：12/12/21 3:36 PM
 * @author daijun
 * @version 1.0
 * @des：OpenGL ES 多目标渲染（MRT）
 */
class MrtRenderer() : GLSurfaceView.Renderer,IRenderGesture {
    private val TAG = "EarthMapRenderer"
    private val BYTES_PER_FLOAT = 4
    private val MULTI_TARGET_NUM = 4 // 目标个数

    //多目标渲染程序
    private var mMrtProgram = 0
    //渲染程序
    private var mProgram = 0
    //纹理id
    private var textureId = 0
    //3个定点，等腰直角
    var triangleCoords = floatArrayOf( // 矩形全部点位
            1.0f,1.0f,0.0f,
            -1.0f, -1.0f, 0.0f,
            -1.0f,1.0f,0.0f,
            1.0f, 1.0f, 0.0f,  // top  0.5202312
            -1.0f, -1.0f, 0.0f,  // bottom left
            1.0f, -1.0f, 0.0f // bottom right
    )
    //纹理坐标2
    // 三角形3个定点对应在纹理坐标系中的坐标
    private val textureVertex = floatArrayOf( // 矩形全部点位
            1.0f,0.0f,
            0.0f, 1.0f,
            0.0f,0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f)

    //相机矩阵
    private val mViewMatrix = FloatArray(16)
    //投影矩阵
    private val mProjectMatrix = FloatArray(16)
    private val mMvpMatrix = FloatArray(16)

    //窗口长宽
    private var mWidth:Int=0
    private var mHeight:Int=0

    // ------------------- mrt纹理绘图 ------------------
    private var vertexBuffer: FloatBuffer? = null
    private var textureBuffer: FloatBuffer? = null
    //相关属性id
    private var mHMrtMvpMatrix = 0 //uniform
    private var mHMrtPosition = 0 // attribute
    private var mHMrtTextCoordinate = 0 // attribute
    private lateinit var mAttachTextIds : IntArray //绑定的text

    // ------------------------ FBO --------------------
    protected lateinit var frameBuffers: IntArray
    protected lateinit var frameTextures: IntArray
    private var frameBufferMTR = 0

    // ------------------  纹理显示 -----------
    //相关属性id
    private var mDispMvpMatrix = 0 //uniform
    private var mDispPosition = 0 // attribute
    private var mDispTextCoordinate = 0 // attribute

    init {
        initData()
    }

    private fun initData(){
        vertexBuffer = GLDataUtil.createFloatBuffer(triangleCoords)
        textureBuffer = GLDataUtil.createFloatBuffer(textureVertex)
        mAttachTextIds = IntArray(MULTI_TARGET_NUM)
    }

    private fun createFBO(){
        GLES30.glGenTextures(mAttachTextIds.size,mAttachTextIds,0)
        // 创建一个frame buffer用于绑定多个渲染目标
        frameBuffers = IntArray(1)
        GLES30.glGenFramebuffers(frameBuffers.size, frameBuffers, 0)
        frameBufferMTR = frameBuffers[0]
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, frameBufferMTR)
        // 将4个渲染目标绑定到frame buffer上的4个attachment(附着)上
        for (i in mAttachTextIds.indices) {
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mAttachTextIds[i])
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE)
            GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, mWidth, mHeight, 0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, null)
            GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0 + i, GLES30.GL_TEXTURE_2D, mAttachTextIds[i], 0)
            // 检测fbo绑定是否成功
            if (GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER) != GLES30.GL_FRAMEBUFFER_COMPLETE) {
                throw RuntimeException("FBO附着异常")
            }
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)
        }

        val attachments = intArrayOf(GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_COLOR_ATTACHMENT1, GLES30.GL_COLOR_ATTACHMENT2)
        val attachmentBuffer = IntBuffer.allocate(attachments.size)
        attachmentBuffer.put(attachments)
        attachmentBuffer.position(0)
        GLES30.glDrawBuffers(attachments.size, attachmentBuffer)
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        //将背景设置为灰色
        GLES30.glClearColor(0.5f, 0.5f, 0.5f, 1.0f)

        //编译MRT顶点着色程序
        val vertexShaderStr = ResReadUtils.readResource(R.raw.vertex_base_es30_mvp_shader)
        val vertexShaderId = ShaderUtils.compileVertexShader(vertexShaderStr)
        //编译MRT片段着色程序
        val fragmentShaderStr = ResReadUtils.readResource(R.raw.fragment_mrt_es30_render_shader)
        val fragmentShaderId = ShaderUtils.compileFragmentShader(fragmentShaderStr)
        //连接程序
        mMrtProgram = ShaderUtils.linkProgram(vertexShaderId, fragmentShaderId)
        GLES30.glUseProgram(mMrtProgram)
        //编译glprogram并获取控制句柄
        mHMrtMvpMatrix = GLES30.glGetUniformLocation(mMrtProgram, "vMatrix")
        mHMrtPosition = GLES30.glGetAttribLocation(mMrtProgram, "vPosition")
        mHMrtTextCoordinate = GLES30.glGetAttribLocation(mMrtProgram, "vTextureCoord")

        //编译展示顶点着色程序
        val vertexDisplayShaderStr = ResReadUtils.readResource(R.raw.vertex_base_es30_mvp_shader)
        val vertexDisplayShaderId = ShaderUtils.compileVertexShader(vertexDisplayShaderStr)
        //编译片段着色程序
        val fragmentDisplayShaderStr = ResReadUtils.readResource(R.raw.fragment_mrt_display_shader)
        val fragmentDisplayShaderId = ShaderUtils.compileFragmentShader(fragmentDisplayShaderStr)
        //连接程序
        mProgram = ShaderUtils.linkProgram(vertexDisplayShaderId, fragmentDisplayShaderId)
        //在OpenGLES环境中使用程序
        GLES30.glUseProgram(mProgram)
        mDispMvpMatrix = GLES30.glGetUniformLocation(mProgram, "vMatrix")
        mDispPosition = GLES30.glGetAttribLocation(mProgram, "vPosition")
        mDispTextCoordinate = GLES30.glGetAttribLocation(mProgram, "vTextureCoord")
        //加载纹理
        textureId = TextureUtils.loadTexture(AppCore.getInstance().context, R.drawable.world_map)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        mWidth = width
        mHeight = height
        var ratio = width.toFloat() / height
        //设置绘制窗口
        GLES30.glViewport(0, 0, width, height)
        initTransformMatrix(ratio)
        createFBO()
    }

    override fun onDrawFrame(gl: GL10?) {
        //把颜色缓冲区设置为我们预设的颜色
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
//        updateModelTransformMatrix(1.0F,1.0F,1.0F)
        //1、 多目标渲染，绘制到FBO绑定的颜色附着中
        GLES30.glUseProgram(mMrtProgram)
        //开启FBO
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER,frameBufferMTR)
        GLES30.glUniformMatrix4fv(mHMrtMvpMatrix, 1, false, mMvpMatrix, 0)
        GLES30.glEnableVertexAttribArray(mHMrtPosition)
        GLES30.glVertexAttribPointer(mHMrtPosition, 3, GLES30.GL_FLOAT, false, 0, vertexBuffer)
        GLES30.glEnableVertexAttribArray(mHMrtTextCoordinate)
        GLES30.glVertexAttribPointer(mHMrtTextCoordinate, 2, GLES30.GL_FLOAT, false, 0, textureBuffer)
        // 启用纹理
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId)
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, triangleCoords.size / 3)
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)


        //2、渲染到屏幕上
        GLES30.glUseProgram(mProgram)
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER,0)//关闭FBO
        GLES30.glUniformMatrix4fv(mDispMvpMatrix, 1, false, mMvpMatrix, 0)
        GLES30.glEnableVertexAttribArray(mDispPosition)
        GLES30.glVertexAttribPointer(mDispPosition, 3, GLES30.GL_FLOAT, false, 0, vertexBuffer)
        GLES30.glEnableVertexAttribArray(mDispTextCoordinate)
        GLES30.glVertexAttribPointer(mDispTextCoordinate, 2, GLES30.GL_FLOAT, false, 0, textureBuffer)

        for (i in 0 until MULTI_TARGET_NUM) {
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0 + i)
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mAttachTextIds[i])
            //glUniform1i 使用解析 https://blog.csdn.net/mumuzi_1/article/details/62047112
            GLES30.glUniform1i(GLES30.glGetUniformLocation(mProgram, "vTexture$i"), i)
        }
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, triangleCoords.size / 3)
    }



    fun initTransformMatrix(ratio:Float){
        Matrix.frustumM(mProjectMatrix, 0, -ratio, ratio,-1.0f, 1.0f,  3.0f, 10.0f) // 比例-1
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0, 0.0f, 0.0f, 4.0f,  //摄像机坐标
                0f, 0f, 0f,  //目标物的中心坐标
                0.0f, 1.0f, 0.0f)
        Matrix.multiplyMM(mMvpMatrix,0,mProjectMatrix,0,mViewMatrix,0)
    }

    override fun updateModelTransformMatrix(xAngle: Float, yAngle: Float, curScale: Float) {

    }

}