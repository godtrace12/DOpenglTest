package com.example.dj.appgl.playercam;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.example.dj.appgl.player.MediaGLRenderer;

public class PlayerCamGLSurface extends GLSurfaceView implements SurfaceTexture.OnFrameAvailableListener {

    private MediaGLRenderer render;

    public PlayerCamGLSurface(Context context) {
        super(context);
    }

    public PlayerCamGLSurface(Context context, AttributeSet attrs) {
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
