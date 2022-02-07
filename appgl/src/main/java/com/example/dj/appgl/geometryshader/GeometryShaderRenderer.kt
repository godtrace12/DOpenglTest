package com.example.dj.appgl.geometryshader

import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import com.example.dj.appgl.R
import com.example.dj.appgl.base.AppCore
import com.example.dj.appgl.util.ResReadUtils
import com.example.dj.appgl.util.ShaderUtils
import com.example.dj.appgl.util.TextureUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * 创建日期：2/7/22 5:49 PM
 * @author daijun
 * @version 1.0
 * @des：几何着色器--billboard广告牌技术
 */
class GeometryShaderRenderer:GLSurfaceView.Renderer {
    private val TAG = "GeometryShaderRenderer"
    private var vertexBuffer: FloatBuffer? = null
    private var textureBuffer: FloatBuffer? = null

    //渲染程序
    private var mProgram = 0

    //3个定点，等腰直角
    var triangleCoords = floatArrayOf( // 矩形全部点位
            //            0.5f,0.5f,0.0f,
            //            -0.5f, -0.5f, 0.0f,
            //            -0.5f,0.5f,0.0f,
            -0.5f, 0.5f, 0.0f,  // top  0.5202312
            // 左上
            0.5f, 0.5f, 0.0f,  // bottom left
            // 右上
            0.5f, -0.5f, 0.0f, // bottom right
            // 右下
            -0.5f, -0.5f, 0.0f
    )

    //纹理坐标2
    // 三角形3个定点对应在纹理坐标系中的坐标
//    private val textureVertex = floatArrayOf( // 矩形全部点位
//            //            1.0f,0.0f,
//            //            0.0f, 1.0f,
//            //            0.0f,0.0f,
//            1.0f, 0.0f,
//            0.0f, 1.0f,
//            1.0f, 1.0f)

    // 颜色数据
    private val textureVertex = floatArrayOf( // 矩形全部点位
            //            1.0f,0.0f,
            //            0.0f, 1.0f,
            //            0.0f,0.0f,
            1.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f)

    //相机矩阵
    private val mViewMatrix = FloatArray(16)

    //投影矩阵
    private val mProjectMatrix = FloatArray(16)

    //最终变换矩阵
    private val mMVPMatrix = FloatArray(16)


    //纹理id
    private var textureId = 0

//    //变换矩阵
//    private int uMatrixLocation;

    //    //变换矩阵
    //    private int uMatrixLocation;

    init {
        val byteBuffer = ByteBuffer.allocateDirect(triangleCoords.size * 4)
        byteBuffer.order(ByteOrder.nativeOrder())
        vertexBuffer = byteBuffer.asFloatBuffer()
        vertexBuffer!!.put(triangleCoords)
        vertexBuffer!!.position(0)
        textureBuffer = ByteBuffer.allocateDirect(textureVertex.size * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
        //传入指定的数据
        textureBuffer!!.put(textureVertex)
        textureBuffer!!.position(0)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(0.5f, 0.5f, 0.5f, 1.0f)
        //编译顶点着色程序
        val vertexShaderStr = ResReadUtils.readResource(R.raw.vertex_geometry_billboard_shader)
        val vertexShaderId = ShaderUtils.compileVertexShader(vertexShaderStr)
        //编译片段着色程序
        val fragmentShaderStr = ResReadUtils.readResource(R.raw.fragment_geometry_billboard_shader)
        val fragmentShaderId = ShaderUtils.compileFragmentShader(fragmentShaderStr)
        //编译几何着色程序
        val geometryShaderStr = ResReadUtils.readResource(R.raw.geometry_line_billboard_shader)
        val geometryShaderId = ShaderUtils.compileGeometryShader(geometryShaderStr)
        //连接程序
        mProgram = ShaderUtils.linkProgram(vertexShaderId,geometryShaderId, fragmentShaderId)
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
        //        Matrix.orthoM(mProjectMatrix,0,-ratio,ratio,-1,1,3,7);
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0, 0f, 0f, 6.0f,  //摄像机坐标
                0f, 0f, 0f,  //目标物的中心坐标
                0f, 1.0f, 0.0f) //相机方向
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
        //纹理坐标数据 x、y，所以数据size是 2
        GLES30.glVertexAttribPointer(aTextureLocation, 3, GLES30.GL_FLOAT, false, 0, textureBuffer)
        //启用顶点颜色句柄
        GLES30.glEnableVertexAttribArray(aTextureLocation)
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        //绑定纹理
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId)
        //三角形
//        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, triangleCoords.size / 3)
        // 画点，通过几何着色器生成其他图形
        GLES30.glDrawArrays(GLES30.GL_POINTS, 0, triangleCoords.size / 3)


        //禁止顶点数组的句柄
        GLES30.glDisableVertexAttribArray(aPositionLocation)
        GLES30.glDisableVertexAttribArray(aTextureLocation)
    }
}