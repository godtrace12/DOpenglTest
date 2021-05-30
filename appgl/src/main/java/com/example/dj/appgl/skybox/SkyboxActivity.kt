package com.example.dj.appgl.skybox

import android.opengl.GLSurfaceView
import android.os.Bundle
import com.example.dj.appgl.base.AbsGLSurfaceActivity
import com.example.dj.appgl.model.ModelLoadRenderer

class SkyboxActivity: AbsGLSurfaceActivity() {

    override fun bindRenderer(): GLSurfaceView.Renderer? {
        return ModelLoadRenderer()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

}