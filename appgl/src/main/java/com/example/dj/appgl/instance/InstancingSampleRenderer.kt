package com.example.dj.appgl.instance

import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import com.example.dj.appgl.R
import com.example.dj.appgl.base.AppCore
import com.example.dj.appgl.util.GLDataUtil
import com.example.dj.appgl.util.ResReadUtils
import com.example.dj.appgl.util.ShaderUtils
import com.example.dj.appgl.util.TextureUtils
import org.opencv.core.Mat
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


/**
 * 创建日期：12/12/21 9:35 PM
 * @author daijun
 * @version 1.0
 * @des：只保留VP矩阵，model矩阵由shader进行相乘
 */
class InstancingSampleRenderer:GLSurfaceView.Renderer {
    private var textureBuffer: FloatBuffer? = null
    private var vertexBuffer: FloatBuffer? = null

    //每个面6个点，3个点组成一个三角形
    private val vertexSixTotal = floatArrayOf( //正面
            -1.0f, 1.0f, 1.0f,  //正面左上0
            1.0f, 1.0f, 1.0f,  //正面右上3
            1.0f, -1.0f, 1.0f,  //正面右下2
            -1.0f, 1.0f, 1.0f,  //正面左上0
            1.0f, -1.0f, 1.0f,  //正面右下2
            -1.0f, -1.0f, 1.0f,  //正面左下1
            //左面
            -1.0f, 1.0f, 1.0f,  //正面左上0
            -1.0f, -1.0f, 1.0f,  //正面左下1
            -1.0f, -1.0f, -1.0f,  //反面左下5
            -1.0f, 1.0f, 1.0f,  //正面左上0
            -1.0f, -1.0f, -1.0f,  //反面左下5
            -1.0f, 1.0f, -1.0f,  //反面左上4
            //上面
            -1.0f, 1.0f, 1.0f,  //正面左上0
            1.0f, 1.0f, 1.0f,  //正面右上3
            1.0f, 1.0f, -1.0f,  //反面右上7
            -1.0f, 1.0f, 1.0f,  //正面左上0
            1.0f, 1.0f, -1.0f,  //反面右上7
            -1.0f, 1.0f, -1.0f,  //反面左上4
            //后面
            1.0f, -1.0f, -1.0f,  //反面右下6
            1.0f, 1.0f, -1.0f,  //反面右上7
            -1.0f, 1.0f, -1.0f,  //反面左上4
            1.0f, -1.0f, -1.0f,  //反面右下6
            -1.0f, 1.0f, -1.0f,  //反面左上4
            -1.0f, -1.0f, -1.0f,  //反面左下5
            //右面
            1.0f, -1.0f, -1.0f,  //反面右下6
            1.0f, 1.0f, -1.0f,  //反面右上7
            1.0f, 1.0f, 1.0f,  //正面右上3
            1.0f, -1.0f, -1.0f,  //反面右下6
            1.0f, 1.0f, 1.0f,  //正面右上3
            1.0f, -1.0f, 1.0f,  //正面右下2
            //下面
            1.0f, -1.0f, -1.0f,  //反面右下6
            -1.0f, -1.0f, -1.0f,  //反面左下5
            -1.0f, -1.0f, 1.0f,  //正面左下1
            1.0f, -1.0f, -1.0f,  //反面右下6
            -1.0f, -1.0f, 1.0f,  //正面左下1
            1.0f, -1.0f, 1.0f)


    //每个面6个点坐标,先按每个面都用同一个纹理处理
    //每个面6个点坐标,先按每个面都用同一个纹理处理
    /**  纹理坐标个数要与定点坐标个数一致，顶点数组是36个，纹理坐标个数也必须一一对应是36个
     * 且对应的坐标转换也要一致，下方每个面的纹理坐标都一样，所以对应的前面顶点坐标组成三角形
     * 时，坐标也要跟转换后的纹理坐标一一对应。
     */
    var textureVertex = floatArrayOf(
            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f)

    //相机矩阵
    private val mViewMatrix = FloatArray(16)
    //投影矩阵
    private val mProjectMatrix = FloatArray(16)
    //最终变换矩阵
    private val mMVPMatrix = FloatArray(16)
    //渲染程序
    private var mProgram = 0
    //累计旋转过的角度
    private var mAngle = 0.0f

    private var mRatio:Float = 1.0f
    // 相机位置
    @Volatile
    private var eyeX:Float = 0.0f
    @Volatile
    private var eyeY:Float = 0.0f
    @Volatile
    private var eyeZ:Float = 8.0f
    // 相机cam Up参数
    @Volatile
    private var mUpX:Float = 0.0f
    @Volatile
    private var mUpY:Float = 1.0f
    @Volatile
    private var mUpZ:Float = 0.0f
    @Volatile
    private var mNear:Float = 1.0f
    @Volatile
    private var mFar:Float = 10.0f

    //纹理id列表
    private lateinit var textureIds: IntArray

    // 使用二维数组的方式,4个
    var translationArray = Array(2) { FloatArray(2) }
    var mInstanceModelMtxArray:FloatArray? = null
    var mInstanceModelMtxBuffer:FloatBuffer? = null
    var instanceMatrixHandle:Int =2

    init {
        vertexBuffer = GLDataUtil.createFloatBuffer(vertexSixTotal)
        textureBuffer = GLDataUtil.createFloatBuffer(textureVertex)
        mUpX = 0.0f
        mUpY = 1.0f
        mUpZ = 0.0f
        // 生成实例数组
        var modelMatrices = createMatrices()
        mInstanceModelMtxArray = FloatArray(2*16)//存储了2个矩阵数据，每个矩阵16个点
        for (index in 0..1){
            System.arraycopy(modelMatrices[index],0,mInstanceModelMtxArray,index*16,16)
        }
        mInstanceModelMtxBuffer = GLDataUtil.createFloatBuffer(mInstanceModelMtxArray)

    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
        //开启深度测试
        GLES30.glEnable(GLES20.GL_DEPTH_TEST)
        GLES30.glClearColor(0.5f, 0.5f, 0.5f, 1.0f)
        //编译顶点着色程序
        val vertexShaderStr = ResReadUtils.readResource(R.raw.vertex_instaning_cubic_shader)
        val vertexShaderId = ShaderUtils.compileVertexShader(vertexShaderStr)
        //编译片段着色程序
        val fragmentShaderStr = ResReadUtils.readResource(R.raw.fragment_cubic_texture_shader)
        val fragmentShaderId = ShaderUtils.compileFragmentShader(fragmentShaderStr)
        //连接程序
        mProgram = ShaderUtils.linkProgram(vertexShaderId, fragmentShaderId)
        //在OpenGLES环境中使用程序
        GLES30.glUseProgram(mProgram)
        //加载纹理
        val resList = intArrayOf(R.drawable.hzw1, R.drawable.hzw2, R.drawable.hzw3, R.drawable.hzw4, R.drawable.hzw5, R.drawable.hzw6)
        textureIds = TextureUtils.loadTextures(AppCore.getInstance().context, resList)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        val ratio = width.toFloat() / height // 比例-1
//        val ratio = height.toFloat()/width // 比例-2
        mRatio = ratio
        initTransformMatrix()
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        GLES20.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
        initTransformMatrix()

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
        GLES30.glVertexAttribPointer(aTextureLocation, 2, GLES30.GL_FLOAT, false, 0, textureBuffer)
        //启用纹理颜色句柄
        GLES30.glEnableVertexAttribArray(aTextureLocation)

        // 实例化相关
//        instanceMatrixHandle = GLES30.glGetAttribLocation(mProgram, "aInstanceMatrix")
        Log.e("dj==", "instanceMatrixHandle=$instanceMatrixHandle posHandle=$aPositionLocation textCoordHandle=$aTextureLocation")
        GLES30.glEnableVertexAttribArray(instanceMatrixHandle)
        GLES30.glEnableVertexAttribArray(instanceMatrixHandle + 1)
        GLES30.glEnableVertexAttribArray(instanceMatrixHandle + 2)
        GLES30.glEnableVertexAttribArray(instanceMatrixHandle + 3)
        for (i in 0..1){
            GLES30.glVertexAttribPointer(instanceMatrixHandle, 4, GLES20.GL_FLOAT,
                    false, 16 * 4, mInstanceModelMtxBuffer)
            mInstanceModelMtxBuffer!!.position(4)
            GLES30.glVertexAttribPointer(instanceMatrixHandle + 1, 4, GLES20.GL_FLOAT,
                    false, 16 * 4, mInstanceModelMtxBuffer)
            mInstanceModelMtxBuffer!!.position(8)
            GLES30.glVertexAttribPointer(instanceMatrixHandle + 2, 4, GLES20.GL_FLOAT,
                    false, 16 * 4, mInstanceModelMtxBuffer)
            mInstanceModelMtxBuffer!!.position(12)
            GLES30.glVertexAttribPointer(instanceMatrixHandle + 3, 4, GLES20.GL_FLOAT,
                    false, 16 * 4, mInstanceModelMtxBuffer)

            GLES30.glVertexAttribDivisor(instanceMatrixHandle, 1)
            GLES30.glVertexAttribDivisor(instanceMatrixHandle + 1, 1)
            GLES30.glVertexAttribDivisor(instanceMatrixHandle + 2, 1)
            GLES30.glVertexAttribDivisor(instanceMatrixHandle + 3, 1)
        }


        //启用纹理
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        //每个面6个顶点数据，使用不同的纹理贴图
        for (i in textureIds.indices) {
//            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureIds[i])
            GLES30.glDrawArrays(GLES30.GL_TRIANGLES, i * 6, 6)
        }

//        GLES30.glDrawArraysInstanced(GLES30.GL_TRIANGLES, 0, 36,2)


        //禁止顶点数组的句柄
        GLES30.glDisableVertexAttribArray(aPositionLocation)
        GLES30.glDisableVertexAttribArray(aTextureLocation)

        // 禁止实例化具柄
        GLES30.glDisableVertexAttribArray(instanceMatrixHandle)
        GLES30.glDisableVertexAttribArray(instanceMatrixHandle + 1)
        GLES30.glDisableVertexAttribArray(instanceMatrixHandle + 2)
        GLES30.glDisableVertexAttribArray(instanceMatrixHandle + 3)
    }

    private fun initTransformMatrix(){
//        Log.e("dj==", "initTransformMatrix: Near=$mNear Far=$mFar")
        // 设置模型矩阵
        Matrix.setIdentityM(mProjectMatrix,0)
        //设置透视投影
        Matrix.frustumM(mProjectMatrix, 0, -mRatio, mRatio,-1.0f, 1.0f,  mNear, mFar) // 比例-1
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ,  //摄像机坐标
                0f, 0f, 0f,  //目标物的中心坐标
                mUpX, mUpY, mUpZ) //相机方向
        //例如设置up方向为y轴正方向，upx = 0,upy = 1,upz = 0。这是相机正对着目标图像
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0)

    }

    private fun createMatrices(): Array<FloatArray> {
        var modelMatrices = Array(2) { FloatArray(2) }
        for (index in modelMatrices.indices){
            var modelMatrix = FloatArray(16)
            Matrix.setIdentityM(modelMatrix,0)
            if(index == 0){
                Matrix.translateM(modelMatrix,0,0.2f,0.2f,0.0f)
            }else{
                Matrix.translateM(modelMatrix,0,-0.2f,-0.2f,0.0f)
            }
            modelMatrices[index] = modelMatrix
        }
        return modelMatrices
    }


}