package com.example.dj.appgl.camera.quardraw;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.example.dj.appgl.camera.CameraQuarRender;
import com.example.dj.appgl.camera.base.AbsObjectRender;
import com.example.dj.appgl.camera.base.BaseCameraRenderer;
import com.example.dj.appgl.camera.object.TrianCamColorRender;

public class CameraDrawGLSurface extends GLSurfaceView implements SurfaceTexture.OnFrameAvailableListener {

    private BaseCameraRenderer render;

    public CameraDrawGLSurface(Context context) {
        super(context);
    }

    public CameraDrawGLSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(3);
        render = new CameraQuarDrawRender(context,this);
        setRenderer(render);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
//        render.setObjectRender(new TrianCamColorRender());
    }

    public void setObjectRender(AbsObjectRender absObjectRender){
        if (render != null){
//            render.setObjectRender(absObjectRender);
        }
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        requestRender();
    }
}
