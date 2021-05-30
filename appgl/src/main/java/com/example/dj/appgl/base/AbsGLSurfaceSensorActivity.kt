package com.example.dj.appgl.base

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.dj.appgl.skybox.base.AbsSensorRenderer

abstract class AbsGLSurfaceSensorActivity: AppCompatActivity(), SensorEventListener {
    private var mGLSurfaceView:GLSurfaceView? = null
    protected abstract fun bindRenderer(): AbsSensorRenderer?
    protected var renderer: AbsSensorRenderer? = null
    private var sensorManager:SensorManager? = null
    private var rotationSensor: Sensor? = null
    // 旋转矩阵
    private val rotationMatrix = FloatArray(16)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorInit(this)
        setupViews()
    }

    fun sensorInit(context: Context) {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager?
        if(sensorManager != null){
            rotationSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        }
        Matrix.setIdentityM(rotationMatrix, 0)

    }

     open fun setupViews() {
        mGLSurfaceView = GLSurfaceView(this)
        setContentView(mGLSurfaceView)
        //设置版本
        mGLSurfaceView!!.setEGLContextClientVersion(3)
        renderer = bindRenderer()
        mGLSurfaceView!!.setRenderer(renderer)
    }

    override fun onPause() {
        super.onPause()
        if (sensorManager != null) {
            sensorManager!!.unregisterListener(this)
        }
    }

    override fun onResume() {
        super.onResume()
        if (sensorManager != null && rotationSensor != null){
            sensorManager!!.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
        mGLSurfaceView!!.queueEvent(Runnable {
            if (renderer != null) {
                renderer!!.rotation(rotationMatrix)
            }
        })
    }



}