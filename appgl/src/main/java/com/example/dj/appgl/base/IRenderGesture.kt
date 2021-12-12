package com.example.dj.appgl.base

/**
 * 创建日期：11/21/21 11:30 PM
 * @author daijun
 * @version 1.0
 * @des：
 */
interface IRenderGesture {


    fun setTouchLocation(x:Float,y:Float){

    }

    fun updateModelTransformMatrix(xAngle:Float,yAngle:Float,curScale:Float)
}