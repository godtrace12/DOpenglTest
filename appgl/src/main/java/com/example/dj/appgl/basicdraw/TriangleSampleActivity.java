package com.example.dj.appgl.basicdraw;

import android.opengl.GLSurfaceView;
import android.os.Bundle;

import com.example.dj.appgl.base.AbsGLSurfaceActivity;

public class TriangleSampleActivity extends AbsGLSurfaceActivity {

    @Override
    protected GLSurfaceView.Renderer bindRenderer() {
        return new TriangleTextureRenderer();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
