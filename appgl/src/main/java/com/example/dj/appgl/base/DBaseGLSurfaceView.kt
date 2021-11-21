package com.example.dj.appgl.base

import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import android.view.ScaleGestureDetector

/**
 * 创建日期：11/21/21 10:09 PM
 * @author daijun
 * @version 1.0
 * @des：
 */
class DBaseGLSurfaceView(context:Context,gestureListener: GestureListener): GLSurfaceView(context),ScaleGestureDetector.OnScaleGestureListener {
    private var mContext: Context? = null
    private var mGestureListener:GestureListener? = null

    init {
        mContext = context
        mGestureListener = gestureListener
    }

    interface GestureListener{
        fun onClick(x:Float,y:Float)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        // 单点触控
        if (event!!.pointerCount == 1){
            dealClickEvent(event)
        }else{

        }
        return true
    }

    fun dealClickEvent(event: MotionEvent?){
        var touchX = -1.0f
        var touchY = -1.0f
        when(event!!.action){
            MotionEvent.ACTION_UP -> {
                touchX = event.x
                touchY = event.y
                mGestureListener!!.onClick(touchX,touchY)
            }
        }
    }


    override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
        TODO("Not yet implemented")
    }

    override fun onScaleEnd(detector: ScaleGestureDetector?) {
        TODO("Not yet implemented")
    }

    override fun onScale(detector: ScaleGestureDetector?): Boolean {
        TODO("Not yet implemented")
    }
}