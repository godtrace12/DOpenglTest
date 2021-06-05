package com.example.dj.appgl.base;

import android.opengl.GLSurfaceView;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

/**
 * @anchor: andy
 * @date: 2018-11-02
 * @description:
 */
public abstract class AbsGLSurfaceActivity extends AppCompatActivity {

    protected GLSurfaceView mGLSurfaceView;

    protected abstract GLSurfaceView.Renderer bindRenderer();
    protected GLSurfaceView.Renderer renderer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupViews();
    }

    private void setupViews() {
        mGLSurfaceView = new GLSurfaceView(this);
        setContentView(mGLSurfaceView);
        //设置版本
        mGLSurfaceView.setEGLContextClientVersion(3);
        renderer = bindRenderer();
        mGLSurfaceView.setRenderer(renderer);
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

}