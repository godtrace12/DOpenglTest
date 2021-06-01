package com.example.dj.appgl.camera3d

import android.opengl.GLSurfaceView
import com.example.dj.appgl.base.AbsGLSurfaceActivity

class Camera3DActivity: AbsGLSurfaceActivity() {
    override fun bindRenderer(): GLSurfaceView.Renderer? {

        return Camera3DRender(mGLSurfaceView)
    }
}