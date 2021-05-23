package com.example.dj.appgl.model;

import android.opengl.GLSurfaceView;
import android.os.Bundle;

import com.example.dj.appgl.base.AbsGLSurfaceActivity;
import com.example.dj.appgl.basicdraw.RectTextureFboRenderer;

public class ModelLoadActivity extends AbsGLSurfaceActivity {

    @Override
    protected GLSurfaceView.Renderer bindRenderer() {
        return new ModelLoadRenderer();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

}
