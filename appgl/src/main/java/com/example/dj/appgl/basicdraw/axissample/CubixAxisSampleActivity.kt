package com.example.dj.appgl.basicdraw.axissample

import android.app.Activity
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.widget.SeekBar
import com.example.dj.appgl.R
import com.example.dj.appgl.base.DBaseGLSurfaceView
import kotlinx.android.synthetic.main.activity_cubic_axis.*

/**
 * 创建日期：12/12/21 9:34 PM
 * @author daijun
 * @version 1.0
 * @des：
 */
class CubixAxisSampleActivity:Activity(){
    var mGLSurfaceView: GLSurfaceView? = null
    var renderer: CubicAxisSampleRenderer? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cubic_axis)
        setupViews()
        initViews()
    }

    private fun initViews() {
        pbLocZ.setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tv_EyeZ.text="eyeZ:$progress"
                renderer!!.updateCameraPosition(pbLocX!!.progress.toFloat(),pbLocY!!.progress.toFloat(),progress.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })

        pbLocX.setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tv_EyeX.text="eyeX:$progress"
                renderer!!.updateCameraPosition(pbLocX!!.progress.toFloat(),pbLocY!!.progress.toFloat(),progress.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })

        pbLocY.setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tv_EyeY.text="eyeY:$progress"
                renderer!!.updateCameraPosition(pbLocX!!.progress.toFloat(),pbLocY!!.progress.toFloat(),progress.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })

    }


    private fun setupViews(){
        mGLSurfaceView = gl_Surface
        mGLSurfaceView!!.setEGLContextClientVersion(3)
        renderer = CubicAxisSampleRenderer()
        mGLSurfaceView!!.setRenderer(renderer)
        mGLSurfaceView!!.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
    }
}