package com.example.dj.appgl.filter.base;


import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.Matrix;

import com.example.dj.appgl.R;
import com.example.dj.appgl.util.GLDataUtil;
import com.example.dj.appgl.util.ResReadUtils;
import com.example.dj.appgl.util.ShaderUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/** 钜型纹理渲染Filter积累
 * **/
public class AbsRectangleTextureFilter {
    private FloatBuffer vertexBuffer;
    private FloatBuffer textureBuffer;
    //渲染程序
    private int mProgram;

    public final float[] vertexCoords = {
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f
    };

    public final float[] textureCoords = {
            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
    };

    int mWidth;
    int mHeight;

    //相机矩阵
    private final float[] mViewMatrix = new float[16];
    //投影矩阵
    private final float[] mProjectMatrix = new float[16];
    //最终变换矩阵
    private final float[] mMVPMatrix = new float[16];

    public AbsRectangleTextureFilter(int vertexResId, int fragmentResId) {
        initCoords();
        initGL(vertexResId,fragmentResId);
    }

    private void initCoords(){
        // 顶点着色器坐标保存
//        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertexCoords.length*4);
//        byteBuffer.order(ByteOrder.nativeOrder());
//        vertexBuffer = byteBuffer.asFloatBuffer();
//        vertexBuffer.clear();
//        //把这门语法() 推送给GPU
//        vertexBuffer.put(vertexCoords);
        vertexBuffer = GLDataUtil.createFloatBuffer(vertexCoords);

        // 片元着色器坐标保存
//        textureBuffer = ByteBuffer.allocateDirect(textureCoords.length * 4)
//                .order(ByteOrder.nativeOrder())
//                .asFloatBuffer();
//        textureBuffer.clear();
//        //传入指定的数据
//        textureBuffer.put(textureCoords);
//        textureBuffer.position(0);
        textureBuffer = GLDataUtil.createFloatBuffer(textureCoords);

        Matrix.setIdentityM(mProjectMatrix, 0);
        Matrix.setIdentityM(mMVPMatrix, 0);
        Matrix.setIdentityM(mViewMatrix, 0);
    }

    public void initGL(int vertexResId, int fragmentResId) {
        //编译顶点着色程序
        String vertexShaderStr = ResReadUtils.readResource(vertexResId);
        int vertexShaderId = ShaderUtils.compileVertexShader(vertexShaderStr);
        //编译片段着色程序
        String fragmentShaderStr = ResReadUtils.readResource(fragmentResId);
        int fragmentShaderId = ShaderUtils.compileFragmentShader(fragmentShaderStr);
        //连接程序
        mProgram = ShaderUtils.linkProgram(vertexShaderId, fragmentShaderId);
    }

    public void setSize(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    public int onDraw(int textureId) {
        GLES30.glUseProgram(mProgram);
        // 1- 设置MVP矩阵
        GLES30.glViewport(0, 0, mWidth, mHeight);
        float ratio = (float) mWidth/mHeight;
        //设置透视投影
        Matrix.frustumM(mProjectMatrix,0,-ratio,ratio,-1,1,3,7);
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix,0,0,0,3.0f,//摄像机坐标
                0f,0f,0f,//目标物的中心坐标
                1f,0.0f,0.0f);//相机方向
        //接着是摄像机顶部的方向了，如下图，很显然相机旋转，up的方向就会改变，这样就会会影响到绘制图像的角度。
        //例如设置up方向为y轴正方向，upx = 0,upy = 1,upz = 0。这是相机正对着目标图像
        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix,0,mProjectMatrix,0,mViewMatrix,0);

        //2- 执行绘图操作
        //左乘矩阵
        int uMaxtrixLocation = GLES30.glGetUniformLocation(mProgram,"vMatrix");
        // 将前面计算得到的mMVPMatrix(frustumM setLookAtM 通过multiplyMM 相乘得到的矩阵) 传入vMatrix中，与顶点矩阵进行相乘
        GLES30.glUniformMatrix4fv(uMaxtrixLocation,1,false,mMVPMatrix,0);

        int aPositionLocation = GLES30.glGetAttribLocation(mProgram,"vPosition");
        GLES30.glEnableVertexAttribArray(aPositionLocation);
        //x y 所以数据size 是3
        GLES30.glVertexAttribPointer(aPositionLocation,2,GLES30.GL_FLOAT,false,0,vertexBuffer);

        int aTextureLocation = GLES20.glGetAttribLocation(mProgram,"aTextureCoord");
        //纹理坐标是xy 所以数据size是 2
        GLES30.glVertexAttribPointer(aTextureLocation, 2, GLES30.GL_FLOAT, false, 0, textureBuffer);
        //启用顶点颜色句柄
        GLES30.glEnableVertexAttribArray(aTextureLocation);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        //绑定纹理
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,textureId);
        // 0: 图层ID  GL_TEXTURE0
        // GL_TEXTURE1 ， 1
//        GLES20.glUniform1i(aTextureLocation,0);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP,0,vertexCoords.length/2);

        //禁止顶点数组的句柄
//        GLES30.glDisableVertexAttribArray(aPositionLocation);
//        GLES30.glDisableVertexAttribArray(aTextureLocation);
        GLES30.glDisableVertexAttribArray(aPositionLocation);
        GLES30.glDisableVertexAttribArray(aTextureLocation);

        GLES30.glUseProgram(0);
        GLES20.glDepthFunc(GLES20.GL_LESS);
        return textureId;
    }


    public void release(){
        GLES30.glDeleteProgram(mProgram);
    }

}
