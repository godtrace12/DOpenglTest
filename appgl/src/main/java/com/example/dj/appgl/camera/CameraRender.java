package com.example.dj.appgl.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.example.dj.appgl.camera.base.BaseCameraRenderer;
import com.example.dj.appgl.camera.base.CameraManeger;
import com.example.dj.appgl.filter.CameraFilter;
import com.example.dj.appgl.filter.ScreenFilter;
import com.example.dj.appgl.filter.ScreenFilter2;
import com.example.dj.appgl.filter.base.AbstractRect2DFilter;
import com.example.dj.appgl.filter.base.FilterChain;
import com.example.dj.appgl.filter.base.FilterContext;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CameraRender implements GLSurfaceView.Renderer {
    private static final String TAG = "CameraRender";
    private CameraManeger mCameraManeger;
    private SurfaceTexture mCameraTexture;
    private SurfaceTexture.OnFrameAvailableListener listener;

    private CameraFilter cameraFilter;
    private ScreenFilter2 screenFilter;
    int[] texture;
    List<AbstractRect2DFilter> filters = new ArrayList<>();
    FilterChain filterChain;
    float[] mtx = new float[16];

    public CameraRender(Context mContext, SurfaceTexture.OnFrameAvailableListener listener) {
        this.listener = listener;
        mCameraManeger = new CameraManeger();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        cameraFilter = new CameraFilter();
        screenFilter = new ScreenFilter2();
        createAndBindVideoTexture();
        mCameraManeger.OpenCamera(mCameraTexture);
        filters.add(cameraFilter);
        filters.add(screenFilter);
        filterChain = new FilterChain(filters,0,new FilterContext());
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.e(TAG, "onSurfaceChanged: dj------ width="+width+"  ----height="+height);
        cameraFilter.setSize(width,height);
        screenFilter.setSize(width,height);

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        mCameraTexture.updateTexImage();//通过此方法更新接收到的预览数据
        mCameraTexture.getTransformMatrix(mtx);
        filterChain.setTransformMatrix(mtx);
        filterChain.proceed(texture[0]);
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
