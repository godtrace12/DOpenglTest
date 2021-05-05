package com.example.dj.appgl.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES30;

import com.example.dj.appgl.camera.base.BaseCameraRenderer;
import com.example.dj.appgl.camera.base.CameraManeger;
import com.example.dj.appgl.filter.CameraFilter;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CameraRender extends BaseCameraRenderer {
    private static final String TAG = "CameraRender";
    private CameraManeger mCameraManeger;
    private SurfaceTexture mCameraTexture;
    private SurfaceTexture.OnFrameAvailableListener listener;

    private CameraFilter cameraFilter;
    int[] texture;

    public CameraRender(Context mContext, SurfaceTexture.OnFrameAvailableListener listener) {
        this.listener = listener;
        mCameraManeger = new CameraManeger();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        cameraFilter = new CameraFilter();
        createAndBindVideoTexture();
        mCameraManeger.OpenCamera(mCameraTexture);
        // 调用父类，完成另外添加进来的图形的初始化
        super.onSurfaceCreated(gl,config);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        cameraFilter.setSize(width,height);
        // 调用父类，完成另外添加进来的图形的透视、相机矩阵初始化
        super.onSurfaceChanged(gl,width,height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        mCameraTexture.updateTexImage();//通过此方法更新接收到的预览数据
        cameraFilter.onDraw(texture[0]);
        // 调用父类，完成另外添加进来的图形的绘制
        super.onDrawFrame(gl);
    }


    private void createAndBindVideoTexture(){
        texture = new int[1];
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
