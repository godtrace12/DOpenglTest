package com.example.dj.appgl.skybox

import android.hardware.Sensor
import android.opengl.GLSurfaceView
import android.os.Bundle
import com.example.dj.appgl.base.AbsGLSurfaceActivity
import com.example.dj.appgl.base.AbsGLSurfaceSensorActivity
import com.example.dj.appgl.model.ModelLoadRenderer
import com.example.dj.appgl.skybox.base.AbsSensorRenderer

class SkyboxActivity: AbsGLSurfaceSensorActivity() {

    override fun bindRenderer(): AbsSensorRenderer? {
        return SkyboxRenderer()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

}