package com.example.dj.appgl.base

/**
 * 创建日期：11/21/21 11:30 PM
 * @author daijun
 * @version 1.0
 * @des：
 */
interface IRenderGesture {
    var touchX:Float
    var touchY:Float

    fun setTouchLocation(x:Float,y:Float){
        this.touchX =x
        this.touchY =y
    }
}