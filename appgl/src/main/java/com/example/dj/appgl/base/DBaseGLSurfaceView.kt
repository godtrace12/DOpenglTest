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
    // 上一次手势完成后的缩放比例
    private var mPreScale:Float = 1.0F
    // 当前缩放比例
    private var mCurScale:Float = 1.0F
    // 上一次单点触摸坐标
    private var mPreviousY:Float = 0.0F
    private var mPreviousX:Float = 0.0F
    private var mLastMultiTouchTime = 0.0F
    private val TOUCH_SCALE_FACTOR = 180.0f / 320
    // 旋转角度
    private var mYAngle:Float = 0.0F
    private var mXAngle:Float = 0.0F

    init {
        mContext = context
        mGestureListener = gestureListener
        mScaleGestureDetector = ScaleGestureDetector(mContext,this)
    }

    interface GestureListener{
        fun onClick(x:Float,y:Float)
        fun onUpdateTransformMatrix(xAngle:Float,yAngle:Float,scale:Float)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        // 单点触控
        if (event!!.pointerCount == 1){
            dealClickEvent(event)
            var curTime = System.currentTimeMillis()
            if(curTime - mLastMultiTouchTime >200){
//                Log.e(TAG, "onTouchEvent: lastMultiTouchTime= $mLastMultiTouchTime action=${event.action}")
                var y = event.y
                var x = event.x
                when(event.action){
                    MotionEvent.ACTION_MOVE ->{
                        val dy = y - mPreviousY
                        val dx = x - mPreviousX
                        mYAngle += dx * TOUCH_SCALE_FACTOR
                        mXAngle += dy * TOUCH_SCALE_FACTOR
//                        Log.e(TAG, "onTouchEvent: $dx $dy $mYAngle $mXAngle")
                    }
                }
                mPreviousY = y
                mPreviousX = x
                mGestureListener!!.onUpdateTransformMatrix(mXAngle,mYAngle,mCurScale)
            }
        }else{  // 多点触控
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
        mPreScale = mCurScale
        mLastMultiTouchTime = System.currentTimeMillis().toFloat()

    }

    override fun onScale(detector: ScaleGestureDetector?): Boolean {
//        Log.e(Companion.TAG, "onScale:  scaleFactor=${detector!!.scaleFactor} preSpan=${detector!!.previousSpan} curSpan=${detector!!.currentSpan}")
        val preSpan = detector!!.previousSpan
        val curSpan = detector!!.currentSpan
        if (curSpan < preSpan) {
            mCurScale = mPreScale - (preSpan - curSpan) / 250
        } else {
            mCurScale = mPreScale + (curSpan - preSpan) / 250
        }
//        Log.e(TAG, "onScale: mPreScale=$mPreScale  mCurScale=$mCurScale")
        mCurScale = Math.max(0.05f, Math.min(mCurScale, 80.0f))

        mGestureListener!!.onUpdateTransformMatrix(mXAngle,mYAngle,mCurScale)
        return false
    }

    companion object {
        private const val TAG = "DBaseGLSurfaceView"
    }
}