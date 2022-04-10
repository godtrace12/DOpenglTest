package com.example.dj.appgl.base;

import android.opengl.GLSurfaceView;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dj.appgl.nativegl.NativeGLDrawType;

import static com.example.dj.appgl.nativegl.NativeGLDrawType.NativeGLDraw_SAMPLE_INDEX_TRIANGLE;

public abstract class AbsGLSurfaceActivity extends AppCompatActivity {

    protected DBaseGLSurfaceView mGLSurfaceView;

    protected abstract GLSurfaceView.Renderer bindRenderer();
    protected GLSurfaceView.Renderer renderer;
    // native绘制时使用到的参数，指明是哪个例子
    protected int nativeGLType = NativeGLDraw_SAMPLE_INDEX_TRIANGLE;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nativeGLType = getIntent().getIntExtra(NativeGLDrawType.NativeGLDraw_Type,NativeGLDraw_SAMPLE_INDEX_TRIANGLE);
        setupViews();
    }

    private void setupViews() {
        mGLSurfaceView = new DBaseGLSurfaceView(this, new DBaseGLSurfaceView.GestureListener() {
            @Override
            public void onUpdateTransformMatrix(float xAngle, float yAngle, float scale) {
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