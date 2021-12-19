package com.example.dj.appgl.basicdraw.axissample

import android.app.Activity
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.View
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
    var mCurDegree:Float = 0.0f

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
        // 保存camera up 参数
        btn_SaveCamUp.setOnClickListener(View.OnClickListener {
            renderer!!.updateCameraUpParams(et_camUpX.text.toString().toFloat(),et_camUpY.text.toString().toFloat(),et_camUpZ.text.toString().toFloat())
        })

        pb_RotateModel.setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                mCurDegree = seekBar!!.progress.toFloat()
                btn_PlusDegree.setText("360:$mCurDegree")
                renderer!!.updateRotateAngle(mCurDegree)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

//        btn_MinusDegree.setOnClickListener(View.OnClickListener {
//            if(mCurDegree >=0 && mCurDegree <= 180){
//                mCurDegree -= 1.0f
//                et_Degree.setText("$mCurDegree")
//                renderer!!.updateRotateAngle(mCurDegree)
//            }
//        })
//        btn_PlusDegree.setOnClickListener(View.OnClickListener {
//            if(mCurDegree >=0 && mCurDegree <= 180){
//                mCurDegree += 1.0f
//                et_Degree.setText("$mCurDegree")
//                renderer!!.updateRotateAngle(mCurDegree)
//            }
//        })

    }


    private fun setupViews(){
        mGLSurfaceView = gl_Surface
        mGLSurfaceView!!.setEGLContextClientVersion(3)
        renderer = CubicAxisSampleRenderer()
        mGLSurfaceView!!.setRenderer(renderer)
        mGLSurfaceView!!.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
    }
}