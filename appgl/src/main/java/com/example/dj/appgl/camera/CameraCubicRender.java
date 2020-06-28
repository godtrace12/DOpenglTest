package com.example.dj.appgl.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.example.dj.appgl.R;
import com.example.dj.appgl.base.AppCore;
import com.example.dj.appgl.camera.base.CameraManeger;
import com.example.dj.appgl.util.GLDataUtil;
import com.example.dj.appgl.util.ResReadUtils;
import com.example.dj.appgl.util.ShaderUtils;
import com.example.dj.appgl.util.TextureUtils;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CameraCubicRender implements GLSurfaceView.Renderer {
    private Context mContext;
    private CameraManeger mCameraManeger;
    private SurfaceTexture mCameraTexture;
    private SurfaceTexture.OnFrameAvailableListener listener;

    private int mProgram;

    private int uPosHandle;
    private int aTexHandle;
    private int mMVPMatrixHandle;

    private float[] mProjectMatrix = new float[16];
    private float[] mCameraMatrix  = new float[16];
    private float[] mMVPMatrix     = new float[16];
    private float[] mTempMatrix     = new float[16];

    private float[] mPosCoordinate = {
            -1, -1,
            -1, 1,
            1, -1,
            1, 1};
    private float[] mTexCoordinate = {
            0, 1,
            1, 1,
            0, 0,
            1, 0};

    private FloatBuffer mPosBuffer;
    private FloatBuffer mTexBuffer;



    //三角形变换临时矩阵
    private final float[] rotationMatrix = new float[16];
    private final float[] scaleMatrix = new float[16];
    private final float[] mCubicRotateResultMatrix = new float[16];
    private final float[] mCubicScaleResultMatrix = new float[16];
    private float angle =0;


    /** 立方体绘制  **/
    //每个面6个点，3个点组成一个三角形
    private static final float[] vertexCubic={
            //正面
            -1.0f,1.0f,1.0f,    //正面左上0
            1.0f,1.0f,1.0f,     //正面右上3
            1.0f,-1.0f,1.0f,    //正面右下2
            -1.0f,1.0f,1.0f,    //正面左上0
            1.0f,-1.0f,1.0f,    //正面右下2
            -1.0f,-1.0f,1.0f,   //正面左下1

            //左面
            -1.0f,1.0f,1.0f,    //正面左上0
            -1.0f,-1.0f,1.0f,   //正面左下1
            -1.0f,-1.0f,-1.0f,   //反面左下5
            -1.0f,1.0f,1.0f,    //正面左上0
            -1.0f,-1.0f,-1.0f,   //反面左下5
            -1.0f,1.0f,-1.0f,    //反面左上4

            //上面
            -1.0f,1.0f,1.0f,    //正面左上0
            1.0f,1.0f,1.0f,     //正面右上3
            1.0f,1.0f,-1.0f,    //反面右上7
            -1.0f,1.0f,1.0f,    //正面左上0
            1.0f,1.0f,-1.0f,     //反面右上7
            -1.0f,1.0f,-1.0f,    //反面左上4

            //后面
            1.0f,-1.0f,-1.0f,    //反面右下6
            1.0f,1.0f,-1.0f,     //反面右上7
            -1.0f,1.0f,-1.0f,    //反面左上4
            1.0f,-1.0f,-1.0f,    //反面右下6
            -1.0f,1.0f,-1.0f,    //反面左上4
            -1.0f,-1.0f,-1.0f,   //反面左下5

            //右面
            1.0f,-1.0f,-1.0f,    //反面右下6
            1.0f,1.0f,-1.0f,     //反面右上7
            1.0f,1.0f,1.0f,     //正面右上3
            1.0f,-1.0f,-1.0f,    //反面右下6
            1.0f,1.0f,1.0f,     //正面右上3
            1.0f,-1.0f,1.0f,    //正面右下2

            //下面
            1.0f,-1.0f,-1.0f,    //反面右下6
            -1.0f,-1.0f,-1.0f,   //反面左下5
            -1.0f,-1.0f,1.0f,   //正面左下1
            1.0f,-1.0f,-1.0f,    //反面右下6
            -1.0f,-1.0f,1.0f,   //正面左下1
            1.0f,-1.0f,1.0f,    //正面右下2

    };


    //每个面6个点坐标,先按每个面都用同一个纹理处理
    /**  纹理坐标个数要与定点坐标个数一致，顶点数组是36个，纹理坐标个数也必须一一对应是36个
     *   且对应的坐标转换也要一致，下方每个面的纹理坐标都一样，所以对应的前面顶点坐标组成三角形
     *   时，坐标也要跟转换后的纹理坐标一一对应。
     * **/
    float vertexTextureCubic[] = {
            0.0f,0.0f,
            1.0f,0.0f,
            1.0f,1.0f,
            0.0f,0.0f,
            1.0f,1.0f,
            0.0f,1.0f,

            0.0f,0.0f,
            1.0f,0.0f,
            1.0f,1.0f,
            0.0f,0.0f,
            1.0f,1.0f,
            0.0f,1.0f,

            0.0f,0.0f,
            1.0f,0.0f,
            1.0f,1.0f,
            0.0f,0.0f,
            1.0f,1.0f,
            0.0f,1.0f,

            0.0f,0.0f,
            1.0f,0.0f,
            1.0f,1.0f,
            0.0f,0.0f,
            1.0f,1.0f,
            0.0f,1.0f,

            0.0f,0.0f,
            1.0f,0.0f,
            1.0f,1.0f,
            0.0f,0.0f,
            1.0f,1.0f,
            0.0f,1.0f,

            0.0f,0.0f,
            1.0f,0.0f,
            1.0f,1.0f,
            0.0f,0.0f,
            1.0f,1.0f,
            0.0f,1.0f,

    };

    private FloatBuffer vertexCubicBuffer;
    private FloatBuffer textureCubicBuffer;
    //渲染程序
    private int mProgramCubic;
    //纹理id列表
    private int[] textureIds;

    public CameraCubicRender(Context mContext, SurfaceTexture.OnFrameAvailableListener listener) {
        this.mContext = mContext;
        Matrix.setIdentityM(mProjectMatrix, 0);
        Matrix.setIdentityM(mCameraMatrix, 0);
        Matrix.setIdentityM(mMVPMatrix, 0);
        Matrix.setIdentityM(mTempMatrix, 0);
        this.listener = listener;
        mCameraManeger = new CameraManeger();
        mPosBuffer = GLDataUtil.createFloatBuffer(mPosCoordinate);
        mTexBuffer = GLDataUtil.createFloatBuffer(mTexCoordinate);

        //立方体相关初始化
        vertexCubicBuffer = GLDataUtil.createFloatBuffer(vertexCubic);
        textureCubicBuffer = GLDataUtil.createFloatBuffer(vertexTextureCubic);



    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //编译顶点着色程序
        String vertexShaderStr = ResReadUtils.readResource(R.raw.vertex_camera_texture);
        int vertexShaderId = ShaderUtils.compileVertexShader(vertexShaderStr);
        //编译片段着色程序
        String fragmentShaderStr = ResReadUtils.readResource(R.raw.fragment_camera_quarter_mirror_shade);
        int fragmentShaderId = ShaderUtils.compileFragmentShader(fragmentShaderStr);
        //连接程序
        mProgram = ShaderUtils.linkProgram(vertexShaderId, fragmentShaderId);

        createAndBindVideoTexture();
        mCameraManeger.OpenCamera(mCameraTexture);


        // 立方体绘制相关初始化
        //编译顶点着色程序
        String vertexCubicShaderStr = ResReadUtils.readResource(R.raw.vertex_cubic_texture_shader);
        int vertexCubicShaderId = ShaderUtils.compileVertexShader(vertexCubicShaderStr);
        //编译片段着色程序
        String fragmentCubicShaderStr = ResReadUtils.readResource(R.raw.fragment_cubic_texture_shader);
        int fragmentCubicShaderId = ShaderUtils.compileFragmentShader(fragmentCubicShaderStr);
        //连接程序
        mProgramCubic = ShaderUtils.linkProgram(vertexCubicShaderId, fragmentCubicShaderId);
        int[] resList = {R.drawable.hzw1,R.drawable.hzw2,R.drawable.hzw3,R.drawable.hzw4,R.drawable.hzw5,R.drawable.hzw6};
        textureIds = TextureUtils.loadTextures(AppCore.getInstance().getContext(),resList);

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

        GLES30.glViewport(0, 0, width, height);
        float ratio = (float)width/height;
        Matrix.orthoM(mProjectMatrix,0,-ratio,ratio,-1,1,1,7);
        Matrix.setLookAtM(mCameraMatrix, 0, 0, 0, 5,
                0f, 0f, 0f,
                0f, 1.0f, 0.0f);// 3代表眼睛的坐标点
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mCameraMatrix, 0);

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        /********** 绘制摄像头画面  ****************/
        //在OpenGLES环境中使用程序
        GLES30.glUseProgram(mProgram);
        uPosHandle           = GLES20.glGetAttribLocation (mProgram, "position");
        aTexHandle           = GLES20.glGetAttribLocation (mProgram, "inputTextureCoordinate");
        mMVPMatrixHandle    = GLES20.glGetUniformLocation(mProgram, "textureTransform");
        // 将前面计算得到的mMVPMatrix(frustumM setLookAtM 通过multiplyMM 相乘得到的矩阵) 传入vMatrix中，与顶点矩阵进行相乘
        GLES30.glUniformMatrix4fv(mMVPMatrixHandle,1,false,mMVPMatrix,0);

        mCameraTexture.updateTexImage();//通过此方法更新接收到的预览数据

        GLES30.glVertexAttribPointer(uPosHandle,2,GLES30.GL_FLOAT,false,0, mPosBuffer);
        GLES30.glVertexAttribPointer(aTexHandle,2,GLES30.GL_FLOAT,false,0, mTexBuffer);

        GLES30.glEnableVertexAttribArray(uPosHandle);
        GLES30.glEnableVertexAttribArray(aTexHandle);
        //??? 不太明白
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP,0,mPosCoordinate.length/2);

        GLES30.glDisableVertexAttribArray(uPosHandle);
        GLES30.glDisableVertexAttribArray(aTexHandle);

        GLES30.glUseProgram(0);
        GLES20.glDepthFunc(GLES20.GL_LESS);

        /********* 开始绘制三角形 *********/
        drawCubic();
        GLES20.glDepthFunc(GLES20.GL_LEQUAL);

    }

    private void drawCubic(){
        GLES30.glUseProgram(mProgramCubic);
        Matrix.setIdentityM(rotationMatrix,0);
        Matrix.rotateM(rotationMatrix,0,angle,0,1,0);
        Matrix.multiplyMM(mCubicRotateResultMatrix,0,mMVPMatrix,0,rotationMatrix,0);

        Matrix.setIdentityM(scaleMatrix,0);
        Matrix.scaleM(scaleMatrix,0,0.25f,0.25f,0.25f);

        Matrix.multiplyMM(mCubicScaleResultMatrix,0,mCubicRotateResultMatrix,0,scaleMatrix,0);


        //左乘矩阵
        int uMaxtrixLocation = GLES30.glGetUniformLocation(mProgramCubic,"vMatrix");
        // 将前面计算得到的mMVPMatrix(frustumM setLookAtM 通过multiplyMM 相乘得到的矩阵) 传入vMatrix中，与顶点矩阵进行相乘
        GLES30.glUniformMatrix4fv(uMaxtrixLocation,1,false, mCubicScaleResultMatrix,0);

        int aPositionLocation = GLES30.glGetAttribLocation(mProgramCubic,"vPosition");
        GLES30.glEnableVertexAttribArray(aPositionLocation);
        //x y z 所以数据size 是3
        GLES30.glVertexAttribPointer(aPositionLocation,3,GLES30.GL_FLOAT,false,0,vertexCubicBuffer);

        int aTextureLocation = GLES20.glGetAttribLocation(mProgramCubic,"aTextureCoord");
        //纹理坐标数据 x、y，所以数据size是 2
        GLES30.glVertexAttribPointer(aTextureLocation, 2, GLES30.GL_FLOAT, false, 0, textureCubicBuffer);
        //启用纹理颜色句柄
        GLES30.glEnableVertexAttribArray(aTextureLocation);
        //启用纹理
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        //每个面6个顶点数据，使用不同的纹理贴图
        for (int i = 0; i < textureIds.length; i++) {
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureIds[i]);
            GLES30.glDrawArrays(GLES30.GL_TRIANGLES,i*6,6);

        }

        //禁止顶点数组的句柄
        GLES30.glDisableVertexAttribArray(aPositionLocation);
        GLES30.glDisableVertexAttribArray(aTextureLocation);
        GLES30.glUseProgram(0);
        angle += 1;
    }


    private void createAndBindVideoTexture(){
        int[] texture = new int[1];
        GLES30.glGenTextures(1, texture, 0);//生成一个OpenGl纹理
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);//申请纹理存储区域并设置相关参数
        GLES30.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MIN_FILTER,GL10.GL_LINEAR);
        GLES30.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

        mCameraTexture = new SurfaceTexture(texture[0]);//以上面OpenGl生成的纹理函数参数创建SurfaceTexture,SurfaceTexture接收的数据将传入该纹理
        mCameraTexture.setOnFrameAvailableListener(listener);//设置SurfaceTexture的回调，通过摄像头预览数据已更新
    }

}
