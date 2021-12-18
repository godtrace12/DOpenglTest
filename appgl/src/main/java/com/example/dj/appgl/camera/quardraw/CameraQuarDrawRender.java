package com.example.dj.appgl.camera.quardraw;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.Matrix;

import com.example.dj.appgl.R;
import com.example.dj.appgl.camera.base.BaseCameraRenderer;
import com.example.dj.appgl.camera.base.CameraManeger;
import com.example.dj.appgl.util.GLDataUtil;
import com.example.dj.appgl.util.ResReadUtils;
import com.example.dj.appgl.util.ShaderUtils;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 *  四分屏预览
 *
 * **/
public class CameraQuarDrawRender extends BaseCameraRenderer {
    private static final String TAG = "CameraTriangleRender";
    private Context mContext;
    private CameraManeger mCameraManeger;
    private SurfaceTexture mCameraTexture;
    private SurfaceTexture.OnFrameAvailableListener listener;

    private int mProgram;

    private int uPosHandle;
    private int aTexHandle;
    private int mMVPMatrixHandle;

    //透视矩阵、相机矩阵定义放在基类中，方便传给其他绘制对象
//    private float[] mProjectMatrix = new float[16];
//    private float[] mCameraMatrix  = new float[16];
    private float[] mMVPMatrix     = new float[16];
    private float[] mTempMatrix     = new float[16];

    private float[] mPosCoordinate = {
            -1, -1,
            -1, 1,
            1, -1,
            1, 1};
    private float[] mTexCoordinate = {
            1, 1,
            0, 1,
            1, 0,
            0, 0};

    private FloatBuffer mPosBuffer;
    private FloatBuffer mTexBuffer;

    public CameraQuarDrawRender(Context mContext, SurfaceTexture.OnFrameAvailableListener listener) {
        this.mContext = mContext;
        Matrix.setIdentityM(mProjectMatrix, 0);
        Matrix.setIdentityM(mCameraMatrix, 0);
        Matrix.setIdentityM(mMVPMatrix, 0);
        Matrix.setIdentityM(mTempMatrix, 0);
        this.listener = listener;
        mCameraManeger = new CameraManeger();
        mPosBuffer = GLDataUtil.createFloatBuffer(mPosCoordinate);
        mTexBuffer = GLDataUtil.createFloatBuffer(mTexCoordinate);

    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //编译顶点着色程序
        String vertexShaderStr = ResReadUtils.readResource(R.raw.vertex_camera_texture);
        int vertexShaderId = ShaderUtils.compileVertexShader(vertexShaderStr);
        //编译片段着色程序
        String fragmentShaderStr = ResReadUtils.readResource(R.raw.fragment_camera_quarter_draw_shade);
        int fragmentShaderId = ShaderUtils.compileFragmentShader(fragmentShaderStr);
        //连接程序
        mProgram = ShaderUtils.linkProgram(vertexShaderId, fragmentShaderId);

        createAndBindVideoTexture();
        mCameraManeger.OpenCameraBack(mCameraTexture);

        // 调用父类，完成另外添加进来的图形的初始化
        super.onSurfaceCreated(gl,config);

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

        GLES30.glViewport(0, 0, width, height);
        float ratio = (float)width/height;
        Matrix.orthoM(mProjectMatrix,0,-ratio,ratio,-1,1,1,7);
        Matrix.setLookAtM(mCameraMatrix, 0, 0, 0, 3,
                0f, 0f, 0f,
                0f, 1.0f, 0.0f);// 3代表眼睛的坐标点
//        triangleRender.setProjAndCamMatrix(mProjectMatrix,mCameraMatrix);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mCameraMatrix, 0);
        // 调用父类，完成另外添加进来的图形的透视、相机矩阵初始化
        super.onSurfaceChanged(gl,width,height);
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
        //顶点个数是4个 mPosCoordinate.length/2每个定点x、y2个坐标，所以得到顶点个数。
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP,0,mPosCoordinate.length/2);

        GLES30.glDisableVertexAttribArray(uPosHandle);
        GLES30.glDisableVertexAttribArray(aTexHandle);

        GLES30.glUseProgram(0);
        GLES20.glDepthFunc(GLES20.GL_LESS);

        /********* 开始绘制三角形 *********/
        GLES20.glDepthFunc(GLES20.GL_LEQUAL);
        // 调用父类，完成另外添加进来的图形的绘制
        super.onDrawFrame(gl);

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
