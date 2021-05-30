package com.example.dj.appgl.skybox.base

import android.opengl.GLSurfaceView

abstract class AbsSensorRenderer: GLSurfaceView.Renderer {
    abstract fun rotation(rotationMatrix:FloatArray)
}