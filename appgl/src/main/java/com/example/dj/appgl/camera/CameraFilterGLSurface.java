package com.example.dj.appgl.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.example.dj.appgl.camera.base.AbsObjectRender;
import com.example.dj.appgl.camera.base.BaseCameraRenderer;
import com.example.dj.appgl.camera.object.TrianCamColorRender;

/**
 *  可以添加各种滤镜
 * **/

public class CameraFilterGLSurface extends GLSurfaceView implements SurfaceTexture.OnFrameAvailableListener {

    private CameraRender render;

    public CameraFilterGLSurface(Context context) {
        super(context);
    }

    public CameraFilterGLSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(3);
        render = new CameraRender(context,this);
        setRenderer(render);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }


    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        requestRender();
    }
}
