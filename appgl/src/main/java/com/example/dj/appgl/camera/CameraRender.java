package com.example.dj.appgl.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.GLES11Ext;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.os.Environment;
import android.util.Log;

import com.example.dj.appgl.camera.base.BaseCameraRenderer;
import com.example.dj.appgl.camera.base.CameraManeger;
import com.example.dj.appgl.filter.CameraFilter;
import com.example.dj.appgl.filter.ScreenFilter;
import com.example.dj.appgl.filter.ScreenFilter2;
import com.example.dj.appgl.filter.SoulFilter;
import com.example.dj.appgl.filter.base.AbstractRect2DFilter;
import com.example.dj.appgl.filter.base.FilterChain;
import com.example.dj.appgl.filter.base.FilterContext;
import com.example.dj.record.MediaRecorder;

import java.io.File;
import java.io.IOException;
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
    private SoulFilter soulFilter;
    int[] texture;
    List<AbstractRect2DFilter> filters = new ArrayList<>();
    FilterChain filterChain;
    private MediaRecorder mRecorder;
    float[] mtx = new float[16];
    private Context mContext;

    public CameraRender(Context context, SurfaceTexture.OnFrameAvailableListener listener) {
        this.listener = listener;
        mCameraManeger = new CameraManeger();
        this.mContext = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        cameraFilter = new CameraFilter();
        screenFilter = new ScreenFilter2();
        soulFilter = new SoulFilter();
        createAndBindVideoTexture();
        mCameraManeger.OpenCamera(mCameraTexture);
        filters.add(cameraFilter);
        filters.add(screenFilter);
        filters.add(soulFilter);
        filterChain = new FilterChain(filters,0,new FilterContext());
        //录制视频的宽、高
        String recordFilePath = "dj/a.mp4";
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)){
            String videoPath = mContext.getExternalFilesDir(null)
                    .getAbsolutePath()
                    + File.separator
                    + "Record";
            Log.e(TAG, "File Dir: "+videoPath);
            File file = new File(videoPath);
            if (!file.exists()) {
                file.mkdirs();
            }
            recordFilePath = videoPath + File.separator
                    + "vrecord.mp4";
            Log.e(TAG, "File Path: "+recordFilePath);
//            File videoFile = new File(recordFilePath);
//            if(!videoFile.exists()){
//                videoFile.mkdir();
//            }

        }

        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        String filePath2 = mContext.getExternalFilesDir(null)
                .getAbsolutePath();

        Log.e("dj", "onSurfaceCreated: "+filePath+"\n"+"filePath2="+filePath2);
        recordFilePath = filePath2+"/a.mp4";
        Log.e(TAG, "File Path: "+recordFilePath);

        mRecorder = new MediaRecorder(mContext, recordFilePath,
                EGL14.eglGetCurrentContext(),
                480, 640);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.e(TAG, "onSurfaceChanged: dj------ width="+width+"  ----height="+height);
        mCameraTexture.setDefaultBufferSize(width, height);
        cameraFilter.setSize(width,height);
        screenFilter.setSize(width,height);
        soulFilter.setSize(width,height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        mCameraTexture.updateTexImage();//通过此方法更新接收到的预览数据
        mCameraTexture.getTransformMatrix(mtx);
        filterChain.setTransformMatrix(mtx);
        int textureProcessedId = filterChain.proceed(texture[0]);
        //录制
        mRecorder.fireFrame(textureProcessedId,mCameraTexture.getTimestamp());
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

    public void onSurfaceDestroyed() {
        filterChain.release();
    }

    public void startRecord(){
        try {
            Log.e(TAG, "startRecord: 开始进行录制" );
            mRecorder.start(1);
        } catch (IOException e) {
            Log.e(TAG, "startRecord: exception"+e.getMessage());
            e.printStackTrace();
        }
    }

    public void stopRecord(){
        Log.e(TAG, "stopRecord: 结束录制");
        mRecorder.stop();
    }


}
