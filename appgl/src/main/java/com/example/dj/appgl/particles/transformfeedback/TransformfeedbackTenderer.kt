package com.example.dj.appgl.particles.transformfeedback

import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import com.example.dj.appgl.R
import com.example.dj.appgl.base.AppCore
import com.example.dj.appgl.basicdraw.TriangleTextureRenderer
import com.example.dj.appgl.util.GLDataUtil
import com.example.dj.appgl.util.ResReadUtils
import com.example.dj.appgl.util.ShaderUtils
import com.example.dj.appgl.util.TextureUtils
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * 创建日期：2/21/22 8:11 AM
 * @author daijun
 * @version 1.0
 * @des：
 */
class TransformfeedbackTenderer: GLSurfaceView.Renderer{
    private val TAG = "TriangleTextureRenderer"
    private var vertexBuffer: FloatBuffer? = null
    private var textureBuffer: FloatBuffer? = null

    //渲染程序
    private var mProgram = 0

    //3个定点，等腰直角
    var triangleCoords = floatArrayOf( // 矩形全部点位
            0.5f,0.5f,0.0f,
            -0.5f, -0.5f, 0.0f,
            -0.5f,0.5f,0.0f,
            0.5f, 0.5f, 0.0f,  // top  0.5202312
            -0.5f, -0.5f, 0.0f,  // bottom left
            0.5f, -0.5f, 0.0f // bottom right
    )

    //纹理坐标2
    // 三角形3个定点对应在纹理坐标系中的坐标
    private val textureVertex = floatArrayOf( // 矩形全部点位
            1.0f,0.0f,
            0.0f,1.0f,
            0.0f,0.0f,
            1.0f,0.0f,
            0.0f,1.0f,
            1.0f,1.0f)

    //相机矩阵
    private val mViewMatrix = FloatArray(16)
    //投影矩阵
    private val mProjectMatrix = FloatArray(16)
    //最终变换矩阵
    private val mMVPMatrix = FloatArray(16)
    //纹理id
    private var textureId = 0
    // vbo(顶点缓冲区对象)使用
    //vbo id
    private var vboPosId: Int = 0
    private var vboTextId: Int = 0
    //vao id
    private var vaoId:Int = 0

    init {
        vertexBuffer = GLDataUtil.createFloatBuffer(triangleCoords)
        textureBuffer = GLDataUtil.createFloatBuffer(textureVertex)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(0.5f, 0.5f, 0.5f, 1.0f)
        //编译顶点着色程序
//        val vertexShaderStr = ResReadUtils.readResource(R.raw.vertex_transform_feedback_shader) //不使用transform_feedback不能有多个输出？
        val vertexShaderStr = ResReadUtils.readResource(R.raw.vertex_triangle_texture_shader)
        val vertexShaderId = ShaderUtils.compileVertexShader(vertexShaderStr)
        //编译片段着色程序
//        val fragmentShaderStr = ResReadUtils.readResource(R.raw.fragment_transform_feedback_shader)
        val fragmentShaderStr = ResReadUtils.readResource(R.raw.fragment_triangle_texture_shader)
        val fragmentShaderId = ShaderUtils.compileFragmentShader(fragmentShaderStr)
        val transformVaryings = arrayOf("outPos","outTex")
        //连接程序
//        mProgram = ShaderUtils.linkProgram(vertexShaderId, fragmentShaderId,transformVaryings)
        mProgram = ShaderUtils.linkProgram(vertexShaderId, fragmentShaderId)

        Log.e(TAG, "onSurfaceCreated: mProgram=$mProgram")
        //在OpenGLES环境中使用程序
        GLES30.glUseProgram(mProgram)
        //加载纹理
        textureId = TextureUtils.loadTexture(AppCore.getInstance().context, R.drawable.world_map)
        val aPositionLocation = GLES30.glGetAttribLocation(mProgram, "vPosition")
        val aTextureLocation = GLES20.glGetAttribLocation(mProgram, "aTextureCoord")
        Log.e(TAG, "onSurfaceCreated: aPosLoc=$aPositionLocation aTextLoc=$aTextureLocation")
        createVBO()
        createVAO(aPositionLocation,aTextureLocation,3,2)
    }

    // posHandle: 顶点属性在glsl着色器中的位置  textHandle:纹理坐标在glsl着色器中的位置
    // COORDS_PER_VERTEX：每个顶点坐标占用几个数据  COORDS_PER_FRAGMENT：每个纹理坐标占用几个数据
    private fun createVAO(posHandle: Int,textHandle:Int,COORDS_PER_VERTEX:Int,COORDS_PER_FRAGMENT:Int){
        var vaos = IntArray(1)
        GLES30.glGenVertexArrays(1,vaos,0)
        vaoId = vaos[0]
        GLES30.glBindVertexArray(vaos[0])
        // 管理顶点vbo
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER,vboPosId)
        GLES30.glEnableVertexAttribArray(posHandle)
        //把使用vbo中的这段代码放到vao中。
        GLES30.glVertexAttribPointer(posHandle, COORDS_PER_VERTEX, GLES30.GL_FLOAT, false, COORDS_PER_VERTEX * GLDataUtil.SIZEOF_FLOAT, 0)

        // 管理片纹理vbo
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER,vboTextId)
        GLES30.glEnableVertexAttribArray(textHandle)
        //把使用vbo中的这段代码放到vao中。
        GLES30.glVertexAttribPointer(textHandle, COORDS_PER_FRAGMENT, GLES30.GL_FLOAT, false, COORDS_PER_FRAGMENT * GLDataUtil.SIZEOF_FLOAT, 0)

    }

    private fun createVBO(){
        //1. 创建VBO
        var vbos = IntArray(2)
        GLES30.glGenBuffers(vbos.size,vbos,0)
        vboPosId = vbos[0]
        vboTextId = vbos[1]
        createDetailVBO(vboPosId,triangleCoords.size,vertexBuffer)
        createDetailVBO(vboTextId,textureVertex.size,textureBuffer)
    }

    //创建vbo
    private fun createDetailVBO(voId:Int,dataCount:Int,dataBuffer: FloatBuffer?){
        //2、绑定vbo
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER,voId)
        //3、分配VBO需要的缓存大小，设置顶点数据的值
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER,dataCount* GLDataUtil.SIZEOF_FLOAT,
                dataBuffer,GLES30.GL_STATIC_DRAW) // [mod 2022-04-05] 没有subData的情况，直接用glBufferData
        //4、解绑VBO
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER,0)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        val ratio = width.toFloat() / height
        Log.e(TAG, "onSurfaceChanged: ratio=$ratio")
        //设置透视投影
        Matrix.frustumM(mProjectMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0, 0f, 0f, 3.0f,  //摄像机坐标
                0f, 0f, 0f,  //目标物的中心坐标
                0f, 1.0f, 0.0f) //相机方向

        //接着是摄像机顶部的方向了，如下图，很显然相机旋转，up的方向就会改变，这样就会会影响到绘制图像的角度。
        //例如设置up方向为y轴正方向，upx = 0,upy = 1,upz = 0。这是相机正对着目标图像
        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        GLES20.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
        //左乘矩阵
        val uMaxtrixLocation = GLES30.glGetUniformLocation(mProgram, "vMatrix")
        // 将前面计算得到的mMVPMatrix(frustumM setLookAtM 通过multiplyMM 相乘得到的矩阵) 传入vMatrix中，与顶点矩阵进行相乘
        GLES30.glUniformMatrix4fv(uMaxtrixLocation, 1, false, mMVPMatrix, 0)

        // 启用顶点坐标和纹理坐标属性
        val aPositionLocation = GLES30.glGetAttribLocation(mProgram, "vPosition")
        GLES30.glEnableVertexAttribArray(aPositionLocation)
        //启用顶点颜色句柄
        val aTextureLocation = GLES20.glGetAttribLocation(mProgram, "aTextureCoord")
        GLES30.glEnableVertexAttribArray(aTextureLocation)

        // 使用VAO
        GLES30.glBindVertexArray(vaoId)
        //激活纹理
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        //绑定纹理
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId)
        //矩形
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, triangleCoords.size / 3)
        GLES30.glDisableVertexAttribArray(vaoId)

        //禁止顶点数组的句柄
        GLES30.glDisableVertexAttribArray(aPositionLocation)
        GLES30.glDisableVertexAttribArray(aTextureLocation)
    }

}