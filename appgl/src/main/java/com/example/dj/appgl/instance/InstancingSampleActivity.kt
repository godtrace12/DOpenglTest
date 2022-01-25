package com.example.dj.appgl.instance

import android.app.Activity
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import com.example.dj.appgl.R
import kotlinx.android.synthetic.main.activity_cubic_axis.*

/**
 * 创建日期：12/12/21 9:34 PM
 * @author daijun
 * @version 1.0
 * @des：
 */
class InstancingSampleActivity:Activity(){
    var mGLSurfaceView: GLSurfaceView? = null
    var renderer: InstancingSampleRenderer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instance_sample)
        setupViews()
    }




    private fun setupViews(){
        mGLSurfaceView = gl_Surface
        mGLSurfaceView!!.setEGLContextClientVersion(3)
        renderer = InstancingSampleRenderer()
        mGLSurfaceView!!.setRenderer(renderer)
        mGLSurfaceView!!.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
    }
}