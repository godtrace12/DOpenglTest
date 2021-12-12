package com.example.dj.appgl.base

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.Log
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
    private var mScaleGestureDetector:ScaleGestureDetector? = null
    init {
        mContext = context
        mGestureListener = gestureListener
        mScaleGestureDetector = ScaleGestureDetector(mContext,this)
    }

    interface GestureListener{
        fun onClick(x:Float,y:Float)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        Log.e(TAG, "onTouchEvent: " )
        // 单点触控
        if (event!!.pointerCount == 1){
            dealClickEvent(event)
        }else{
            mScaleGestureDetector!!.onTouchEvent(event)
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
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector?) {

    }

    override fun onScale(detector: ScaleGestureDetector?): Boolean {
        Log.e(Companion.TAG, "onScale:  scaleFactor=${detector!!.scaleFactor} preSpan=${detector!!.previousSpan} curSpan=${detector!!.currentSpan}")
        return false
    }

    companion object {
        private const val TAG = "DBaseGLSurfaceView"
    }
}