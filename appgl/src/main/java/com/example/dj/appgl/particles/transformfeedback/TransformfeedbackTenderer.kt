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
            0.0f, 1.0f,
            0.0f,0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f)

    //相机矩阵
    private val mViewMatrix = FloatArray(16)
    //投影矩阵
    private val mProjectMatrix = FloatArray(16)
    //最终变换矩阵
    private val mMVPMatrix = FloatArray(16)
    //纹理id
    private var textureId = 0

    init {
        vertexBuffer = GLDataUtil.createFloatBuffer(triangleCoords)
        textureBuffer = GLDataUtil.createFloatBuffer(textureVertex)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(0.5f, 0.5f, 0.5f, 1.0f)
        //编译顶点着色程序
        val vertexShaderStr = ResReadUtils.readResource(R.raw.vertex_transform_feedback_shader)
        val vertexShaderId = ShaderUtils.compileVertexShader(vertexShaderStr)
        //编译片段着色程序
        val fragmentShaderStr = ResReadUtils.readResource(R.raw.fragment_triangle_texture_shader)
        val fragmentShaderId = ShaderUtils.compileFragmentShader(fragmentShaderStr)
        //连接程序
        mProgram = ShaderUtils.linkProgram(vertexShaderId, fragmentShaderId)
        //在OpenGLES环境中使用程序
        GLES30.glUseProgram(mProgram)
        //加载纹理
        textureId = TextureUtils.loadTexture(AppCore.getInstance().context, R.drawable.world_map)
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
        //左乘矩阵
        val uMaxtrixLocation = GLES30.glGetUniformLocation(mProgram, "vMatrix")
        // 将前面计算得到的mMVPMatrix(frustumM setLookAtM 通过multiplyMM 相乘得到的矩阵) 传入vMatrix中，与顶点矩阵进行相乘
        GLES30.glUniformMatrix4fv(uMaxtrixLocation, 1, false, mMVPMatrix, 0)

        val aPositionLocation = GLES30.glGetAttribLocation(mProgram, "vPosition")
        GLES30.glEnableVertexAttribArray(aPositionLocation)
        //x y z 所以数据size 是3
        GLES30.glVertexAttribPointer(aPositionLocation, 3, GLES30.GL_FLOAT, false, 0, vertexBuffer)

        val aTextureLocation = GLES20.glGetAttribLocation(mProgram, "aTextureCoord")
        Log.e(TAG, "onDrawFrame: textureLocation=$aTextureLocation")
        //纹理坐标数据 x、y，所以数据size是 2
        GLES30.glVertexAttribPointer(aTextureLocation, 2, GLES30.GL_FLOAT, false, 0, textureBuffer)
        //启用顶点颜色句柄
        GLES30.glEnableVertexAttribArray(aTextureLocation)
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        //绑定纹理
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId)
        //三角形
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, triangleCoords.size / 3)
        //矩形
//        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 6);

        //禁止顶点数组的句柄
        //矩形
//        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 6);

        //禁止顶点数组的句柄
        GLES30.glDisableVertexAttribArray(aPositionLocation)
        GLES30.glDisableVertexAttribArray(aTextureLocation)
    }

}