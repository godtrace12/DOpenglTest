package com.example.dj.appgl.filter.base;

import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.Matrix;
import android.util.Log;

import com.example.dj.appgl.util.GLDataUtil;
import com.example.dj.appgl.util.ResReadUtils;
import com.example.dj.appgl.util.ShaderUtils;

import java.nio.FloatBuffer;

public class AbstractRect2DFilter {
    private static final String TAG = "AbstractRect2DFilter";
    protected FloatBuffer vertexBuffer;
    protected FloatBuffer textureBuffer;
    //顶点坐标索引
    protected int vPositionCoordLoc;
    //纹理坐标索引
    protected int vTextureCoordLoc;
    //纹理索引
    protected int vTextureLoc;
    //渲染程序
    protected int mProgram;

    public float[] vertexCoords = {
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f
    };

    public float[] textureCoords = {
            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
    };

    protected int mWidth;
    protected int mHeight;

    public AbstractRect2DFilter(int vertexResId, int fragmentResId) {
        initCoords();
        initGL(vertexResId,fragmentResId);
    }

    private void initCoords(){
        // 顶点着色器坐标保存
        vertexBuffer = GLDataUtil.createFloatBuffer(vertexCoords);

        // 片元着色器坐标保存
        textureBuffer = GLDataUtil.createFloatBuffer(textureCoords);

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
        vPositionCoordLoc = GLES30.glGetAttribLocation(mProgram,"vPosition");
        vTextureCoordLoc = GLES20.glGetAttribLocation(mProgram,"vTextureCoord");
        vTextureLoc = GLES20.glGetUniformLocation(mProgram, "vTexture");
    }

    public void setSize(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    public int onDraw(int textureId,FilterChain filterChain) {
        FilterContext filterContext = filterChain.filterContext;
//        Log.e(TAG, "onDraw: mWidth="+mWidth+" mHeight="+mHeight);
        // 1- 设置MVP矩阵
        GLES30.glUseProgram(mProgram);
        GLES30.glViewport(0, 0, filterContext.width, filterContext.height);
//        GLES30.glViewport(0, 0, mWidth, mHeight);


//        Log.e(TAG, "onDraw: width="+filterContext.width+" height="+filterContext.height);
        float ratio = (float) mWidth/mHeight;

        //2- 执行绘图操作
        //左乘矩阵
        //x y 所以数据size 是2
        vertexBuffer.position(0);
        GLES30.glVertexAttribPointer(vPositionCoordLoc,2,GLES30.GL_FLOAT,false,0,vertexBuffer);
        GLES30.glEnableVertexAttribArray(vPositionCoordLoc);

        //纹理坐标是xy 所以数据size是 2
        textureBuffer.position(0);
        GLES30.glVertexAttribPointer(vTextureCoordLoc, 2, GLES30.GL_FLOAT, false, 0, textureBuffer);
        //启用顶点颜色句柄
        GLES30.glEnableVertexAttribArray(vTextureCoordLoc);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        //绑定纹理
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,textureId);
        // 0: 图层ID  GL_TEXTURE0
        // GL_TEXTURE1 ， 1
        GLES30.glUniform1i(vTextureLoc,0);

        beforeDraw(filterChain.filterContext);

        // 进行绘图
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP,0,vertexCoords.length/2);

        afterDraw();

        // 为何 texture=0？
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,0);
        return textureId;
    }


    public void release(){
        if(mProgram !=-1){
            GLES30.glDeleteProgram(mProgram);
        }
    }

    public void beforeDraw(FilterContext filterContext){

    }

    public void afterDraw(){

    }

}
