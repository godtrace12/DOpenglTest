package com.example.dj.appgl.ball

import android.graphics.Bitmap
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import com.example.dj.appgl.R
import com.example.dj.appgl.base.AppCore
import com.example.dj.appgl.base.IRenderGesture
import com.example.dj.appgl.util.ResReadUtils
import com.example.dj.appgl.util.ShaderUtils
import com.example.dj.appgl.util.TextureUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * 创建日期：12/12/21 3:36 PM
 * @author daijun
 * @version 1.0
 * @des：
 */
class EarthMapRenderer() : GLSurfaceView.Renderer,IRenderGesture {
    private val TAG = "EarthMapRenderer"
    private val BYTES_PER_FLOAT = 4

    //顶点位置缓存
    private var vertexBuffer: FloatBuffer? = null

    //纹理顶点位置缓存
    private var mTexVertexBuffer: FloatBuffer? = null

    //渲染程序
    private var mProgram = 0

    //图片生成的位图
    private val mBitmap: Bitmap? = null

    //纹理id
    private var textureId = 0

    //向量个数
    private var vCount = 0

    //相关属性id
    private var mHProjMatrix = 0
    private var mHViewMatrix = 0
    private var mHModelMatrix = 0
    private var mHUTexture = 0
    private var mHPosition = 0
    private var mHCoordinate = 0

    //相机矩阵
    private val mViewMatrix = FloatArray(16)

    //投影矩阵
    private val mProjectMatrix = FloatArray(16)

    private val mModelMatrix = FloatArray(16)


    init {
        calculateAttribute()
    }

    //计算顶点坐标和纹理坐标
    private fun calculateAttribute() {
        val radius = 1.0f // 球的半径
        val angleSpan = Math.PI / 90f // 将球进行单位切分的角度
        val alVertix = ArrayList<Float>()
        val textureVertix = ArrayList<Float>()
        var vAngle = 0.0
        while (vAngle < Math.PI) {
            var hAngle = 0.0
            while (hAngle < 2 * Math.PI) {
                val x0 = (radius * Math.sin(vAngle) * Math.cos(hAngle)).toFloat()
                val y0 = (radius * Math.sin(vAngle) * Math.sin(hAngle)).toFloat()
                val z0 = (radius * Math.cos(vAngle)).toFloat()
                val x1 = (radius * Math.sin(vAngle) * Math.cos(hAngle + angleSpan)).toFloat()
                val y1 = (radius * Math.sin(vAngle) * Math.sin(hAngle + angleSpan)).toFloat()
                val z1 = (radius * Math.cos(vAngle)).toFloat()
                val x2 = (radius * Math.sin(vAngle + angleSpan) * Math.cos(hAngle + angleSpan)).toFloat()
                val y2 = (radius * Math.sin(vAngle + angleSpan) * Math.sin(hAngle + angleSpan)).toFloat()
                val z2 = (radius * Math.cos(vAngle + angleSpan)).toFloat()
                val x3 = (radius * Math.sin(vAngle + angleSpan) * Math.cos(hAngle)).toFloat()
                val y3 = (radius * Math.sin(vAngle + angleSpan) * Math.sin(hAngle)).toFloat()
                val z3 = (radius * Math.cos(vAngle + angleSpan)).toFloat()
                val s0 = (-hAngle / Math.PI / 2).toFloat()
                val s1 = (-(hAngle + angleSpan) / Math.PI / 2).toFloat()
                val t0 = (vAngle / Math.PI).toFloat()
                val t1 = ((vAngle + angleSpan) / Math.PI).toFloat()
                alVertix.add(x1)
                alVertix.add(y1)
                alVertix.add(z1)
                alVertix.add(x0)
                alVertix.add(y0)
                alVertix.add(z0)
                alVertix.add(x3)
                alVertix.add(y3)
                alVertix.add(z3)
                textureVertix.add(s1) // x1 y1对应纹理坐标
                textureVertix.add(t0)
                textureVertix.add(s0) // x0 y0对应纹理坐标
                textureVertix.add(t0)
                textureVertix.add(s0) // x3 y3对应纹理坐标
                textureVertix.add(t1)
                alVertix.add(x1)
                alVertix.add(y1)
                alVertix.add(z1)
                alVertix.add(x3)
                alVertix.add(y3)
                alVertix.add(z3)
                alVertix.add(x2)
                alVertix.add(y2)
                alVertix.add(z2)
                textureVertix.add(s1) // x1 y1对应纹理坐标
                textureVertix.add(t0)
                textureVertix.add(s0) // x3 y3对应纹理坐标
                textureVertix.add(t1)
                textureVertix.add(s1) // x2 y3对应纹理坐标
                textureVertix.add(t1)
                hAngle = hAngle + angleSpan
            }
            vAngle = vAngle + angleSpan
        }
        vCount = alVertix.size / 3
        vertexBuffer = convertToFloatBuffer(alVertix)
        mTexVertexBuffer = convertToFloatBuffer(textureVertix)
    }

    //动态数组转FloatBuffer
    private fun convertToFloatBuffer(data: ArrayList<Float>): FloatBuffer? {
        val d = FloatArray(data.size)
        for (i in d.indices) {
            d[i] = data[i]
        }
        val buffer = ByteBuffer.allocateDirect(data.size * 4)
        buffer.order(ByteOrder.nativeOrder())
        val ret = buffer.asFloatBuffer()
        ret.put(d)
        ret.position(0)
        return ret
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        //将背景设置为灰色
        GLES30.glClearColor(0.5f, 0.5f, 0.5f, 1.0f)

        //编译顶点着色程序
        val vertexShaderStr = ResReadUtils.readResource(R.raw.vertex_ball_shade)
        val vertexShaderId = ShaderUtils.compileVertexShader(vertexShaderStr)
        //编译片段着色程序
        val fragmentShaderStr = ResReadUtils.readResource(R.raw.fragment_ball_shade)
        val fragmentShaderId = ShaderUtils.compileFragmentShader(fragmentShaderStr)
        //连接程序
        mProgram = ShaderUtils.linkProgram(vertexShaderId, fragmentShaderId)
        //在OpenGLES环境中使用程序
        GLES30.glUseProgram(mProgram)

        //编译glprogram并获取控制句柄
        mHProjMatrix = GLES30.glGetUniformLocation(mProgram, "uProjMatrix")
        mHViewMatrix = GLES30.glGetUniformLocation(mProgram, "uViewMatrix")
        mHModelMatrix = GLES30.glGetUniformLocation(mProgram, "uModelMatrix")
        mHUTexture = GLES30.glGetUniformLocation(mProgram, "uTexture")
        mHPosition = GLES30.glGetAttribLocation(mProgram, "aPosition")
        mHCoordinate = GLES30.glGetAttribLocation(mProgram, "aCoordinate")

        //加载纹理
        textureId = TextureUtils.loadTexture(AppCore.getInstance().context, R.drawable.world_map)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        //设置绘制窗口
        GLES30.glViewport(0, 0, width, height)
        setSize(width, height)
    }

    fun setSize(width: Int, height: Int) {
        //计算宽高比
        val ratio = width.toFloat() / height
        //situation1 -- 透视投影矩阵/视锥
        Matrix.perspectiveM(mProjectMatrix,0, 60F,ratio,1f,300f);
//        Matrix.frustumM(mProjectMatrix,0,-ratio,ratio,-1,1,3,100);
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0, 0f, 4f,1f, 0.0f, 0.0f,0f, 0f,1f, 0f);
        //situation2 -- vr球，查看球内壁
//        Matrix.perspectiveM(mProjectMatrix, 0, 90f, ratio, 0f, 300f)
//        Matrix.setLookAtM(mViewMatrix, 0, 0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0f, 1.0f, 0.0f)

        //模型矩阵
        Matrix.setIdentityM(mModelMatrix, 0)

        //Matrix.rotateM(mViewMatrix,0,180,0,0,1);
    }

    override fun onDrawFrame(gl: GL10?) {
        //把颜色缓冲区设置为我们预设的颜色
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
//        updateModelTransformMatrix(1.0F,1.0F,1.0F)
        GLES30.glUniformMatrix4fv(mHProjMatrix, 1, false, mProjectMatrix, 0)
        GLES30.glUniformMatrix4fv(mHViewMatrix, 1, false, mViewMatrix, 0)
        GLES30.glUniformMatrix4fv(mHModelMatrix, 1, false, mModelMatrix, 0)
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId)
        GLES30.glEnableVertexAttribArray(mHPosition)
        GLES30.glVertexAttribPointer(mHPosition, 3, GLES30.GL_FLOAT, false, 0, vertexBuffer)
        GLES30.glEnableVertexAttribArray(mHCoordinate)
        GLES30.glVertexAttribPointer(mHCoordinate, 2, GLES30.GL_FLOAT, false, 0, mTexVertexBuffer)
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vCount)
        GLES30.glDisableVertexAttribArray(mHCoordinate)
        GLES30.glDisableVertexAttribArray(mHPosition)
    }


    override fun setTouchLocation(x: Float, y: Float) {
        super.setTouchLocation(x, y)

    }

    override fun updateModelTransformMatrix(xAngle: Float, yAngle: Float, curScale: Float) {
//        Log.e(TAG, "updateModelTransformMatrix: curScale= $curScale xAngle=$xAngle yAngle=$yAngle")
        var modelOriMatrix = FloatArray(16)
        Matrix.setIdentityM(modelOriMatrix, 0)
        val scaleMatrix = FloatArray(16)
        Matrix.setIdentityM(scaleMatrix, 0)
        var calScale = curScale
        //设置缩放比例
        Matrix.scaleM(scaleMatrix, 0, calScale, calScale, calScale)
        Matrix.multiplyMM(mModelMatrix,0,modelOriMatrix,0,scaleMatrix,0)

        var rotateYMatrix = FloatArray(16)
        Matrix.setIdentityM(rotateYMatrix, 0)
        Matrix.rotateM(rotateYMatrix,0,yAngle,0.0F,1.0F,0.0F)

        var rotateXMatrix = FloatArray(16)
        Matrix.setIdentityM(rotateXMatrix, 0)
        Matrix.rotateM(rotateXMatrix,0,xAngle,1.0F,0.0F,0.0F)

        Matrix.multiplyMM(mModelMatrix,0,mModelMatrix,0,rotateYMatrix,0)
        Matrix.multiplyMM(mModelMatrix,0,mModelMatrix,0,rotateXMatrix,0)

    }

}