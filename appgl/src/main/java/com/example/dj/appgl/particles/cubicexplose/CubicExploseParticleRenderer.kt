package com.example.dj.appgl.particles.cubicexplose

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
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * 创建日期：3/20/22 7:51 PM
 * @author daijun
 * @version 1.0
 * @des：
 */
class CubicExploseParticleRenderer : GLSurfaceView.Renderer{

    var cubicVertexTextCoords = floatArrayOf(
            //position            //texture coord
            -0.05f, -0.05f, -0.05f, 0.0f, 0.0f,
            0.05f, -0.05f, -0.05f, 1.0f, 0.0f,
            0.05f, 0.05f, -0.05f, 1.0f, 1.0f,
            0.05f, 0.05f, -0.05f, 1.0f, 1.0f,
            -0.05f, 0.05f, -0.05f, 0.0f, 1.0f,
            -0.05f, -0.05f, -0.05f, 0.0f, 0.0f,
            -0.05f, -0.05f, 0.05f, 0.0f, 0.0f,
            0.05f, -0.05f, 0.05f, 1.0f, 0.0f,
            0.05f, 0.05f, 0.05f, 1.0f, 1.0f,
            0.05f, 0.05f, 0.05f, 1.0f, 1.0f,
            -0.05f, 0.05f, 0.05f, 0.0f, 1.0f,
            -0.05f, -0.05f, 0.05f, 0.0f, 0.0f,
            -0.05f, 0.05f, 0.05f, 1.0f, 0.0f,
            -0.05f, 0.05f, -0.05f, 1.0f, 1.0f,
            -0.05f, -0.05f, -0.05f, 0.0f, 1.0f,
            -0.05f, -0.05f, -0.05f, 0.0f, 1.0f,
            -0.05f, -0.05f, 0.05f, 0.0f, 0.0f,
            -0.05f, 0.05f, 0.05f, 1.0f, 0.0f,
            0.05f, 0.05f, 0.05f, 1.0f, 0.0f,
            0.05f, 0.05f, -0.05f, 1.0f, 1.0f,
            0.05f, -0.05f, -0.05f, 0.0f, 1.0f,
            0.05f, -0.05f, -0.05f, 0.0f, 1.0f,
            0.05f, -0.05f, 0.05f, 0.0f, 0.0f,
            0.05f, 0.05f, 0.05f, 1.0f, 0.0f,
            -0.05f, -0.05f, -0.05f, 0.0f, 1.0f,
            0.05f, -0.05f, -0.05f, 1.0f, 1.0f,
            0.05f, -0.05f, 0.05f, 1.0f, 0.0f,
            0.05f, -0.05f, 0.05f, 1.0f, 0.0f,
            -0.05f, -0.05f, 0.05f, 0.0f, 0.0f,
            -0.05f, -0.05f, -0.05f, 0.0f, 1.0f,
            -0.05f, 0.05f, -0.05f, 0.0f, 1.0f,
            0.05f, 0.05f, -0.05f, 1.0f, 1.0f,
            0.05f, 0.05f, 0.05f, 1.0f, 0.0f,
            0.05f, 0.05f, 0.05f, 1.0f, 0.0f,
            -0.05f, 0.05f, 0.05f, 0.0f, 0.0f,
            -0.05f, 0.05f, -0.05f, 0.0f, 1.0f
    )
    protected var vertexShaderCode: String? = null
    protected var fragmentShaderCode: String? = null

    private val modelMatrix: FloatArray? = FloatArray(16)
    private val mMVPMatrix: FloatArray? = FloatArray(16)
    private var viewMatrix: FloatArray? = FloatArray(16)
    private var projectionMatrix: FloatArray? = FloatArray(16)
    private var cubeTexture: Int = 0
    var vertexBuffer: FloatBuffer? = null

    // 实例化渲染相关
    var shaderProgram: Int = 0
    var positionHandle: Int = 0
    var textCoordsHandle: Int = 0
    var mMVPMatrixHandle: Int = 0
    var texturePosHandle: Int = 0
    var instanceMatrixHandle: Int = 0
    var particlePosHandle:Int=0 //位置偏移

    // 使用二维数组的方式,4个
    var mInstanceModelMtxArray: FloatArray? = null
    var mInstanceModelMtxBuffer: FloatBuffer? = null
    var instanceCount: Int = 27

    //累计旋转过的角度
    private var angle = 0f
    private var mWidth:Int = 0
    private var mHeight:Int = 0

    // 位置偏移
    private var mParticlesPos: FloatArray = FloatArray(instanceCount*3)
    private var mParticlesPosBuffer: FloatBuffer? = null
    private var mLastUsedParticle:Int = 0

    // 粒子
    private var mParticlesContainer = arrayOfNulls<CubicParticle>(instanceCount)

    init {
        vertexShaderCode = ResReadUtils.readResource(R.raw.fragment_cubic_particle_vertex)
        fragmentShaderCode = ResReadUtils.readResource(R.raw.texture_es30_fragment)

    }


    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        cubeTexture = TextureUtils.loadTexture(AppCore.getInstance().context, R.drawable.hzw5)
        initInstance()
        initInstanceMatrixArray()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        mWidth = width
        mHeight = height
        // 粒子初始化
        generateNewParticle()
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        GLES20.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
        initProjectViewMatrix(mWidth, mHeight)
        // 启用相关handle
        intanceStart()
        val vertexBuffer: FloatBuffer = GLDataUtil.createFloatBuffer(cubicVertexTextCoords)
        GLES30.glVertexAttribPointer(positionHandle, 3, GLES30.GL_FLOAT,
                false, 5 * 4, vertexBuffer)
        vertexBuffer.position(3)
        GLES30.glVertexAttribPointer(textCoordsHandle, 2, GLES30.GL_FLOAT,
                false, 5 * 4, vertexBuffer)
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.multiplyMM(mMVPMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mMVPMatrix, 0, projectionMatrix, 0, mMVPMatrix, 0)
        GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0)

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, cubeTexture)
        GLES30.glUniform1i(texturePosHandle, 0)

        updateParticles()

        mParticlesPosBuffer!!.position(0)
        GLES30.glVertexAttribPointer(particlePosHandle, 3, GLES20.GL_FLOAT,
                false, 0, mParticlesPosBuffer)
        GLES30.glVertexAttribDivisor(particlePosHandle, 1)

        GLES30.glDrawArraysInstanced(GLES30.GL_TRIANGLES, 0, 36, instanceCount)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)
        // 关闭相关handle
        intanceEnd()
//        angle += 8
//        if (angle >= 360) {
//            angle = 0F
//        }
    }

    // 设置透视和view矩阵
    private fun initProjectViewMatrix(aWidth: Int, aHeight: Int) {
        var width = aWidth
        var height = aHeight
        var ratio: Float = ((width + 0.0f) / height)
        //初始化矩阵
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 1f, 7f)
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0.0f, 3f, 0f, 0f, 0.0f, 0f, 1f, 0f)
    }

    fun initInstance(){
        var vertexShaderId: Int = ShaderUtils.compileVertexShader(vertexShaderCode)
        var fragmentShaderId: Int = ShaderUtils.compileFragmentShader(fragmentShaderCode)
        shaderProgram = ShaderUtils.linkProgram(vertexShaderId, fragmentShaderId)
        positionHandle = GLES30.glGetAttribLocation(shaderProgram, "aPosition")
        textCoordsHandle = GLES30.glGetAttribLocation(shaderProgram, "aTexCoords")
        mMVPMatrixHandle = GLES30.glGetUniformLocation(shaderProgram, "uMVPMatrix")
        texturePosHandle = GLES30.glGetUniformLocation(shaderProgram, "texture")
        instanceMatrixHandle = GLES30.glGetAttribLocation(shaderProgram, "aInstanceMatrix")
        particlePosHandle = GLES30.glGetAttribLocation(shaderProgram,"aOffset")
        Log.e("dj==", "initInstance: posHandle="+particlePosHandle)
    }


    private fun initInstanceMatrixArray(){
        var modelMatrices = createMatrices()
        mInstanceModelMtxArray = FloatArray(instanceCount * 16)//存储了2个矩阵数据，每个矩阵16个点
        for (index in 0..(instanceCount - 1)) {
            System.arraycopy(modelMatrices[index], 0, mInstanceModelMtxArray, index * 16, 16)
        }
        mInstanceModelMtxBuffer = GLDataUtil.createFloatBuffer(mInstanceModelMtxArray)

        createDeamonPos()
    }

    // 计算3*3*3个位置，分为前中后三排
    private fun createMatrices(): Array<FloatArray> {
        var modelMatrices = Array(instanceCount) { FloatArray(16) }
        var index = 0;
        for (i in -1..1) {
            for (j in -1..1) {
                for (k in -1..1) {
                    var modelMatrix = FloatArray(16)
                    var disUnit = 0.8f
                    var disX = i * disUnit
                    var disY = j * disUnit
                    var disZ = k * disUnit
                    Matrix.setIdentityM(modelMatrix, 0)
                    Matrix.translateM(modelMatrix, 0, disX, disY, disZ)
                    Matrix.rotateM(modelMatrix, 0, angle, 0.0f, 1.0f, 0.0f)
                    Matrix.scaleM(modelMatrix, 0, 0.3f, 0.3f, 0.3f)
                    if (index >= instanceCount) {
                        break
                    }
                    modelMatrices[index] = modelMatrix
                    index++
                }
            }
        }
        return modelMatrices
    }

    // 模拟粒子偏移量
    private fun createDeamonPos(){
        for (i in 0..(instanceCount-1)){
            //创建3个偏移量   Math.random()产生0-1之内的浮点数,随机数转移到[-1，1]范围之内
            var posDx:Float = ((Math.random()*2000-1000)/1000).toFloat()
            var posDy:Float = ((Math.random()*2000-1000)/1000).toFloat()
            var posDz:Float = ((Math.random()*2000-1000)/1000).toFloat()
            Log.e("dj===", "i= $i createDeamonPos: "+posDx+" dy="+posDy+" dz="+posDz)
//            mParticlesPos[i*3+0] = i*0.1f
//            mParticlesPos[i*3+1] = i*0.2f
//            mParticlesPos[i*3+2] = 0.0f
            mParticlesPos[i*3+0] = posDx
            mParticlesPos[i*3+1] = posDy
            mParticlesPos[i*3+2] = posDz

        }
        mParticlesPosBuffer = GLDataUtil.createFloatBuffer(mParticlesPos)
    }

    private fun intanceStart(){
        GLES30.glUseProgram(shaderProgram)
        GLES30.glEnableVertexAttribArray(positionHandle)
        GLES30.glEnableVertexAttribArray(texturePosHandle)
//        GLES30.glEnableVertexAttribArray(instanceMatrixHandle)
//        GLES30.glEnableVertexAttribArray(instanceMatrixHandle + 1)
//        GLES30.glEnableVertexAttribArray(instanceMatrixHandle + 2)
//        GLES30.glEnableVertexAttribArray(instanceMatrixHandle + 3)
        GLES30.glEnableVertexAttribArray(particlePosHandle)
    }

    private fun intanceEnd(){
        GLES30.glDisableVertexAttribArray(positionHandle)
        GLES30.glDisableVertexAttribArray(texturePosHandle)
//        GLES30.glDisableVertexAttribArray(instanceMatrixHandle)
//        GLES30.glDisableVertexAttribArray(instanceMatrixHandle + 1)
//        GLES30.glDisableVertexAttribArray(instanceMatrixHandle + 2)
//        GLES30.glDisableVertexAttribArray(instanceMatrixHandle + 3)
        GLES30.glDisableVertexAttribArray(particlePosHandle)
        GLES30.glUseProgram(0)
    }

    // 产生新的particles
    private fun generateNewParticle(){
        for (i in 0 until instanceCount){
            // 主方向
            var mainDir = arrayOf(0.0f,2.0f,0.0f)
            var spread = 1.5f
            var randomDirX = ((Math.random()*2000-1000)/1000).toFloat()*spread
            var randomDirY = ((Math.random()*2000-1000)/1000).toFloat()*spread
            var randomDirZ = ((Math.random()*2000-1000)/1000).toFloat()*spread

            var speed = arrayOf(0.0f,0.0f,0.0f)
            speed[0] = mainDir[0]+randomDirX
            speed[1] = mainDir[0]+randomDirY
            speed[2] = mainDir[0]+randomDirZ
            if(mParticlesContainer[i]==null){
                var particleTmp = CubicParticle()
                particleTmp.life = 5.0f
                particleTmp.dx = ((Math.random()*2000-1000)/3000).toFloat()
                particleTmp.dy = ((Math.random()*2000-1000)/3000).toFloat()
                particleTmp.dz = ((Math.random()*2000-1000)/3000).toFloat()

                particleTmp.dxSpeed = speed[0]
                particleTmp.dySpeed = speed[1]
                particleTmp.dzSpeed = speed[2]
                mParticlesContainer[i] = particleTmp
            }else{
                mParticlesContainer[i]!!.life = 5.0f
                mParticlesContainer[i]!!.life = 5.0f
                mParticlesContainer[i]!!.dx = ((Math.random()*2000-1000)/3000).toFloat()
                mParticlesContainer[i]!!.dy = ((Math.random()*2000-1000)/3000).toFloat()
                mParticlesContainer[i]!!.dz = ((Math.random()*2000-1000)/3000).toFloat()

                mParticlesContainer[i]!!.dxSpeed = speed[0]
                mParticlesContainer[i]!!.dySpeed = speed[1]
                mParticlesContainer[i]!!.dzSpeed = speed[2]
            }
        }
    }

    //产生单个粒子
    private fun generateNewSingleParticle(i:Int){
        // 主方向
        var mainDir = arrayOf(0.0f,2.0f,0.0f)
        var spread = 1.5f
        var randomDirX = ((Math.random()*2000-1000)/1000).toFloat()*spread
        var randomDirY = ((Math.random()*2000-1000)/1000).toFloat()*spread
        var randomDirZ = ((Math.random()*2000-1000)/1000).toFloat()*spread

        var speed = arrayOf(0.0f,0.0f,0.0f)
        speed[0] = mainDir[0]+randomDirX
        speed[1] = mainDir[0]+randomDirY
        speed[2] = mainDir[0]+randomDirZ
        mParticlesContainer[i]!!.life = 5.0f
        mParticlesContainer[i]!!.life = 5.0f
        mParticlesContainer[i]!!.dx = ((Math.random()*2000-1000)/3000).toFloat()
        mParticlesContainer[i]!!.dy = ((Math.random()*2000-1000)/3000).toFloat()
        mParticlesContainer[i]!!.dz = ((Math.random()*2000-1000)/3000).toFloat()

        mParticlesContainer[i]!!.dxSpeed = speed[0]
        mParticlesContainer[i]!!.dySpeed = speed[1]
        mParticlesContainer[i]!!.dzSpeed = speed[2]
    }


    // 更新
    private fun updateParticles(){
        for (i in 0 until instanceCount){
            var particleIndex = findExpiredParticle()
            if(particleIndex >=0){
                generateNewSingleParticle(i)
            }
        }
        var particlesCount = 0
        var lifeDis = 0.1f
        var delta = 0.1f
        for (i in 0 until instanceCount){
            var cubicParticle = mParticlesContainer[i]
            if(cubicParticle!!.life >0.0f){
                cubicParticle.life -= lifeDis
                if(cubicParticle.life >0.0f){
                    // 位置计算
                    var disSpeed = (delta*0.5).toFloat()
                    var speedYTmp = cubicParticle.dySpeed + disSpeed

                    var posXNow = cubicParticle.dx + cubicParticle.dxSpeed * delta
                    var posYNow = cubicParticle.dy + speedYTmp * delta
                    var posZNow = cubicParticle.dz + cubicParticle.dzSpeed * delta

                    mParticlesContainer[i]!!.dySpeed = speedYTmp //只更新y方向上的速度
                    mParticlesContainer[i]!!.dx = posXNow
                    mParticlesContainer[i]!!.dy = posYNow
                    mParticlesContainer[i]!!.dz = posZNow

                }
            }
            mParticlesPos[i*3+0] = mParticlesContainer[i]!!.dx
            mParticlesPos[i*3+1] = mParticlesContainer[i]!!.dy
            mParticlesPos[i*3+2] = mParticlesContainer[i]!!.dz
        }
        //数据更新后，重新放到buffer里边
        mParticlesPosBuffer = GLDataUtil.createFloatBuffer(mParticlesPos)

    }

    private fun findExpiredParticle():Int{
        for (i in mLastUsedParticle..instanceCount-1){
            if (mParticlesContainer[i]!!.life <=0){
                mLastUsedParticle = i
                return i
            }
        }
        for (i in 0..mLastUsedParticle){
            if (mParticlesContainer[i]!!.life <=0){
                mLastUsedParticle = i
                return i
            }
        }
        return 0
    }


}