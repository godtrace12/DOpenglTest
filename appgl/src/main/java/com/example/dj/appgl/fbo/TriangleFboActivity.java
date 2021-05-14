package com.example.dj.appgl.fbo;

import android.opengl.GLSurfaceView;
import android.os.Bundle;

import com.example.dj.appgl.base.AbsGLSurfaceActivity;
import com.example.dj.appgl.basicdraw.TriangleTextureRenderer;
import com.example.dj.appgl.camera.base.BaseRectTextureRender;

public class TriangleFboActivity extends AbsGLSurfaceActivity {

    @Override
    protected GLSurfaceView.Renderer bindRenderer() {
        return new BaseRectTextureRender();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
