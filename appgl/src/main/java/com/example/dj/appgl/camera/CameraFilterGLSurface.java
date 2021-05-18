package com.example.dj.appgl.camera;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

/**
 *  可以添加各种滤镜
 * **/

public class CameraFilterGLSurface extends GLSurfaceView /*implements SurfaceTexture.OnFrameAvailableListener*/ {

    private CameraRender render;

    public CameraFilterGLSurface(Context context) {
        super(context);
    }

    public CameraFilterGLSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(3);
        render = new CameraRender(this,context);
        setRenderer(render);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
        render.onSurfaceDestroyed();
    }

    public void startRecord(){
        render.startRecord();
    }

    public void stopRecord(){
        render.stopRecord();
    }


//        @Override
//    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
//        requestRender();
//    }
}
