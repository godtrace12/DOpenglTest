package com.example.dj.appgl.light

import android.graphics.Color
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.SystemClock
import android.renderscript.Matrix4f
import android.util.Log
import com.example.dj.appgl.R
import com.example.dj.appgl.base.AppCore
import com.example.dj.appgl.util.GLDataUtil
import com.example.dj.appgl.util.ResReadUtils
import com.example.dj.appgl.util.ShaderUtils
import com.example.dj.appgl.util.TextureUtils
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
/****
 * 光照贴图
 * **/
class TextureMapLightRenderer: GLSurfaceView.Renderer{

    //2-------------- 立方体物体顶点纹理坐标 ----------------------
    private var cubeVertices: FloatArray? = floatArrayOf(
            // 顶点               // 法向量          // 纹理坐标
            -0.5f, -0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f,
            0.5f, -0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 1.0f, 0.0f,
            0.5f, 0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 1.0f, 1.0f,
            0.5f, 0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 1.0f, 1.0f,
            -0.5f, 0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 0.0f, 1.0f,
            -0.5f, -0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f,
            -0.5f, -0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
            0.5f, -0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f,
            0.5f, 0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f,
            0.5f, 0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f,
            -0.5f, 0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f,
            -0.5f, -0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
            -0.5f, 0.5f, 0.5f, -1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
            -0.5f, 0.5f, -0.5f, -1.0f, 0.0f, 0.0f, 1.0f, 1.0f,
            -0.5f, -0.5f, -0.5f, -1.0f, 0.0f, 0.0f, 0.0f, 1.0f,
            -0.5f, -0.5f, -0.5f, -1.0f, 0.0f, 0.0f, 0.0f, 1.0f,
            -0.5f, -0.5f, 0.5f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f,
            -0.5f, 0.5f, 0.5f, -1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
            0.5f, 0.5f, 0.5f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
            0.5f, 0.5f, -0.5f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f,
            0.5f, -0.5f, -0.5f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f,
            0.5f, -0.5f, -0.5f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f,
            0.5f, -0.5f, 0.5f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f,
            0.5f, 0.5f, 0.5f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
            -0.5f, -0.5f, -0.5f, 0.0f, -1.0f, 0.0f, 0.0f, 1.0f,
            0.5f, -0.5f, -0.5f, 0.0f, -1.0f, 0.0f, 1.0f, 1.0f,
            0.5f, -0.5f, 0.5f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f,
            0.5f, -0.5f, 0.5f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f,
            -0.5f, -0.5f, 0.5f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f,
            -0.5f, -0.5f, -0.5f, 0.0f, -1.0f, 0.0f, 0.0f, 1.0f,
            -0.5f, 0.5f, -0.5f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f,
            0.5f, 0.5f, -0.5f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f,
            0.5f, 0.5f, 0.5f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f,
            0.5f, 0.5f, 0.5f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f,
            -0.5f, 0.5f, 0.5f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f,
            -0.5f, 0.5f, -0.5f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f
    )


    //光源的shadeCode
    private var vertexLightShaderCode:String? = null

    private var fragmentLightShaderCode:String? = null

    // 物体的shadeCode
    protected var vertexShaderCode: String? = null
    protected var fragmentShaderCode: String? = null

    // 光源
    private var mLightPosInModelSpace: FloatArray? = floatArrayOf(0f, 0.4f, 1f, 1f)
    private val mLightPosInWorldSpace:FloatArray? = FloatArray(4)
    private var mLightPosInEyeSpace = FloatArray(4)
    // 视点
    private val mViewPos = floatArrayOf(1.5f, -1f, 4f, 1f)

    private var mMVPMatrix:FloatArray?= FloatArray(16)
    private var projectionMatrix:FloatArray?=FloatArray(16)
    private var viewMatrix:FloatArray?=FloatArray(16)
    private var modelMatrix:FloatArray?=FloatArray(16)
    private var mLightMVPMatrix:FloatArray?=FloatArray(16)
    private var mLightModelMatrix:FloatArray?=FloatArray(16)

    private var bg = Color.BLACK
    private var mWidth:Int=0
    private var mHeight:Int=0

    // 纹理
    private var ambient:Int = 0
    private var diffuse:Int = 0
    private var specular:Int = 0

    init {
        vertexShaderCode = ResReadUtils.readResource(R.raw.light_maps_vertex)
        fragmentShaderCode = ResReadUtils.readResource(R.raw.light_maps_fragment)
        vertexLightShaderCode = ResReadUtils.readResource(R.raw.vertex_point_light_shade)
        fragmentLightShaderCode = ResReadUtils.readResource(R.raw.fragment_point_light_shade)

    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {

        // 设置背景色
        GLES20.glClearColor(Color.red(bg) / 255.0f, Color.green(bg) / 255.0f,
                Color.blue(bg) / 255.0f, Color.alpha(bg) / 255.0f)
        //纹理的创建也需要放到opengl线程
        ambient = TextureUtils.loadTexture(AppCore.getInstance().context,R.drawable.ic_light_maps_image1)
        diffuse = TextureUtils.loadTexture(AppCore.getInstance().context,R.drawable.ic_light_maps_image2)
        specular = TextureUtils.loadTexture(AppCore.getInstance().context,R.drawable.ic_light_maps_image3)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        mWidth = width
        mHeight = height
        var ratio:Float =(width+0.0f)/height
        // 设置透视投影矩阵，近点是3，远点是7
        // 设置透视投影矩阵，近点是3，远点是7
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 2f, 8f)
        Matrix.setLookAtM(viewMatrix, 0, mViewPos[0], mViewPos[1], mViewPos[2],
                0f, 0f, 0f,
                0f, 1.0f, 0.0f)
        GLES20.glViewport(0, 0, width, height)

    }

    override fun onDrawFrame(gl: GL10?) {
        // 设置显示范围
        GLES20.glViewport(0, 0, mWidth, mHeight)

        // 设置显示范围
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        // 清屏
        // 清屏
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        val angleInDegrees = 360.0f / 10000.0f * (SystemClock.uptimeMillis() % 10000L)

        Matrix.setIdentityM(mLightModelMatrix, 0)
        Matrix.rotateM(mLightModelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f);

        //Matrix.translateM(mLightModelMatrix, 0, 0.0f, 0.0f, 1.0f);
        Matrix.multiplyMV(mLightPosInWorldSpace, 0, mLightModelMatrix, 0, mLightPosInModelSpace, 0)
        Matrix.multiplyMV(mLightPosInEyeSpace, 0, viewMatrix, 0, mLightPosInWorldSpace, 0)

        Matrix.setIdentityM(modelMatrix, 0)

        drawCube()
        drawLight()
    }



    fun drawLight(){

        // ---------- 绘制光源 ---------------
        var vertexShaderId:Int = ShaderUtils.compileVertexShader(vertexLightShaderCode)
        var fragmentShaderId:Int = ShaderUtils.compileFragmentShader(fragmentLightShaderCode)
        val lightProgram: Int = ShaderUtils.linkProgram(vertexShaderId, fragmentShaderId)
        GLES20.glUseProgram(lightProgram)
        // 传入顶点坐标
        // 传入顶点坐标
        val lightPositionHandle = GLES20.glGetAttribLocation(lightProgram, "aPosition")
        GLES20.glEnableVertexAttribArray(lightPositionHandle)
        GLES20.glVertexAttribPointer(lightPositionHandle, 3, GLES20.GL_FLOAT,
                false, 3 * 4, GLDataUtil.createFloatBuffer(mLightPosInModelSpace))

        Matrix.multiplyMM(mLightMVPMatrix, 0, viewMatrix, 0, mLightModelMatrix, 0)
        Matrix.multiplyMM(mLightMVPMatrix, 0, projectionMatrix, 0, mLightMVPMatrix, 0)

        val mMVPMatrixHandle = GLES20.glGetUniformLocation(lightProgram, "uMVPMatrix")
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mLightMVPMatrix, 0)

        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1)

        GLES20.glDisableVertexAttribArray(lightPositionHandle)
    }

    fun drawCube(){
        var vertexShaderId:Int = ShaderUtils.compileVertexShader(vertexShaderCode)
        var fragmentShaderId:Int = ShaderUtils.compileFragmentShader(fragmentShaderCode)
        val shaderProgram: Int = ShaderUtils.linkProgram(vertexShaderId, fragmentShaderId)
        GLES20.glUseProgram(shaderProgram)
        // 传入顶点坐标
        val positionHandle = GLES20.glGetAttribLocation(shaderProgram, "aPosition")
        GLES20.glEnableVertexAttribArray(positionHandle)
        val vertexBuffer: FloatBuffer = GLDataUtil.createFloatBuffer(cubeVertices)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT,
                false, 8 * 4, vertexBuffer)

        // 法向量
        vertexBuffer.position(3)
        val normalHandle = GLES20.glGetAttribLocation(shaderProgram, "aNormal")
        GLES20.glEnableVertexAttribArray(normalHandle)
        GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT,
                false, 8 * 4, vertexBuffer)

        // 纹理坐标
        vertexBuffer.position(6)
        val aTexCoordsHandle = GLES20.glGetAttribLocation(shaderProgram, "aTexCoords")
        GLES20.glEnableVertexAttribArray(aTexCoordsHandle)
        GLES20.glVertexAttribPointer(aTexCoordsHandle, 2, GLES20.GL_FLOAT,
                false, 8 * 4, vertexBuffer)

        val mMVMatrixHandle = GLES20.glGetUniformLocation(shaderProgram, "uMVMatrix")
        val mMVPMatrixHandle = GLES20.glGetUniformLocation(shaderProgram, "uMVPMatrix")
        val mNormalPosHandle = GLES20.glGetUniformLocation(shaderProgram, "normalMatrix")//如何理解？??


        Matrix.multiplyMM(mMVPMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0)
        Matrix.multiplyMM(mMVPMatrix, 0, projectionMatrix, 0, mMVPMatrix, 0)
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0)

        // 生成法线矩阵，移除对法向量错误缩放的影响
        // Normal = mat3(transpose(inverse(model))) * aNormal; model对应于model乘以view矩阵
        // 逆矩阵计算比较耗时，所以不放着色器中进行，改为放到cpu中计算
        val normalMatrix = Matrix4f()
        normalMatrix.loadMultiply(Matrix4f(viewMatrix), Matrix4f(modelMatrix))
        normalMatrix.inverse()
        normalMatrix.transpose()
        GLES20.glUniformMatrix4fv(mNormalPosHandle, 1, false, normalMatrix.array, 0)

        //1--立方体纹理材质
        val materialAmbientPosHandle = GLES20.glGetUniformLocation(shaderProgram, "material.ambient")
        val materialDiffusePosHandle = GLES20.glGetUniformLocation(shaderProgram, "material.diffuse")
        val materialSpecularPosHandle = GLES20.glGetUniformLocation(shaderProgram, "material.specular")
        val materialShininessPosHandle = GLES20.glGetUniformLocation(shaderProgram, "material.shininess")

        //启用纹理
        TextureUtils.bindTexture(materialAmbientPosHandle, ambient, 0)
        TextureUtils.bindTexture(materialDiffusePosHandle, diffuse, 1)
        TextureUtils.bindTexture(materialSpecularPosHandle, specular, 2)

        GLES20.glUniform1f(materialShininessPosHandle, 256.0f)

        // 2-- 光照
        val lightAmbientPosHandle = GLES20.glGetUniformLocation(shaderProgram, "light.ambient")
        val lightDiffusePosHandle = GLES20.glGetUniformLocation(shaderProgram, "light.diffuse")
        val lightSpecularPosHandle = GLES20.glGetUniformLocation(shaderProgram, "light.specular")
        val lightPosHandle = GLES20.glGetUniformLocation(shaderProgram, "light.position")
        GLES20.glUniform3f(lightPosHandle, mLightPosInEyeSpace[0], mLightPosInEyeSpace[1], mLightPosInEyeSpace[2])

        GLES20.glUniform3f(lightAmbientPosHandle, 0.2f, 0.2f, 0.2f)
        GLES20.glUniform3f(lightDiffusePosHandle, 0.5f, 0.5f, 0.5f)
        GLES20.glUniform3f(lightSpecularPosHandle, 1.0f, 1.0f, 1.0f)


        // 绘制顶点
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, cubeVertices!!.size/(3+3+2))
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(normalHandle)
        GLES20.glDisableVertexAttribArray(aTexCoordsHandle)
    }





}