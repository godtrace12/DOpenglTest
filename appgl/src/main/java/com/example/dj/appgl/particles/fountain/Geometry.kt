package com.example.dj.appgl.particles.fountain

/**
 * 创建日期：3/27/22 6:43 AM
 * @author daijun
 * @version 1.0
 * @des：
 */
class Geometry {

    class Point(var xparam:Float,var yparam:Float,var zparam:Float){
        public var x:Float = 0.0f
        public var y:Float = 0.0f
        public var z:Float = 0.0f

        init {
            x = xparam
            y = yparam
            z = zparam
        }

    }

    class Vector(var xparam:Float,var yparam:Float,var zparam:Float){
        public var x:Float = 0.0f
        public var y:Float = 0.0f
        public var z:Float = 0.0f

        init {
            x = xparam
            y = yparam
            z = zparam
        }
    }
}