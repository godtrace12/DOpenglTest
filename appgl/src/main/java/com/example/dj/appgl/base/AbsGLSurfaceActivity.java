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

    protected DBaseGLSurfaceView mGLSurfaceView;

    protected abstract GLSurfaceView.Renderer bindRenderer();
    protected GLSurfaceView.Renderer renderer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupViews();
    }

    private void setupViews() {
        mGLSurfaceView = new DBaseGLSurfaceView(this, new DBaseGLSurfaceView.GestureListener() {
            @Override
            public void onUpdateScale(float xAngle, float yAngle, float scale) {
                if(renderer instanceof IRenderGesture){
                    ((IRenderGesture)renderer).updateModelTransformMatrix(xAngle,yAngle,scale);
                }
            }

            @Override
            public void onClick(float x, float y) {
                if(renderer instanceof IRenderGesture){
                    ((IRenderGesture)renderer).setTouchLocation(x,y);
                }
            }
        });
        setContentView(mGLSurfaceView);
        //设置版本
        mGLSurfaceView.setEGLContextClientVersion(3);
        renderer = bindRenderer();
        mGLSurfaceView.setRenderer(renderer);
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

}