package com.example.dj.appgl.light;

import android.opengl.GLSurfaceView;
import android.os.Bundle;

import com.example.dj.appgl.base.AbsGLSurfaceActivity;

public class TextureLightActivity extends AbsGLSurfaceActivity {

    @Override
    protected GLSurfaceView.Renderer bindRenderer() {
        return new TextureMapLightRenderer();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

}
