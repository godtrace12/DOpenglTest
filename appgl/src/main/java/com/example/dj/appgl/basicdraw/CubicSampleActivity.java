package com.example.dj.appgl.basicdraw;

import android.opengl.GLSurfaceView;
import android.os.Bundle;

import com.example.dj.appgl.base.AbsGLSurfaceActivity;

public class CubicSampleActivity extends AbsGLSurfaceActivity {

    @Override
    protected GLSurfaceView.Renderer bindRenderer() {
        return new CubicMultiSixTextureRotateRenderer();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
