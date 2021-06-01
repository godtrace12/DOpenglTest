package com.example.dj.appgl.camera3d;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.example.dj.appgl.camera.CameraQuarRender;
import com.example.dj.appgl.camera.base.AbsObjectRender;
import com.example.dj.appgl.camera.base.BaseCameraRenderer;
import com.example.dj.appgl.camera.object.TrianCamColorRender;

public class Camera3DGLSurface extends GLSurfaceView/* implements SurfaceTexture.OnFrameAvailableListener */{

    private Camera3DRender render;

    public Camera3DGLSurface(Context context) {
        super(context);
    }

    public Camera3DGLSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(3);
        render = new Camera3DRender(this);
        setRenderer(render);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

//    @Override
//    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
//        requestRender();
//    }


}
