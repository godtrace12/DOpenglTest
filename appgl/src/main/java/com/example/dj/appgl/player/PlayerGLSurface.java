package com.example.dj.appgl.player;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.example.dj.appgl.camera.base.AbsObjectRender;
import com.example.dj.appgl.camera.base.BaseCameraRenderer;
import com.example.dj.appgl.camera3d.Camera3DRender;
import com.example.dj.appgl.camera3d.obj.CubicRender;

public class PlayerGLSurface extends GLSurfaceView implements SurfaceTexture.OnFrameAvailableListener {

    private MediaGLRenderer render;

    public PlayerGLSurface(Context context) {
        super(context);
    }

    public PlayerGLSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(3);
        render = new MediaGLRenderer(context,this);
        setRenderer(render);
        setRenderMode(RENDERMODE_CONTINUOUSLY);
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        requestRender();
    }


}
