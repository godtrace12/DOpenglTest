#extension GL_OES_EGL_image_external : require
precision mediump float;
uniform samplerExternalOES videoTex;
varying vec2 textureCoordinate;

void main() {
    vec2 uv = textureCoordinate;
    // 法1，右上角是原图
//    if(uv.x <= 0.5 && uv.y <= 0.5){
//        uv.x = uv.x * 2.0;
//        uv.y = uv.y * 2.0;
//    }
//    else if(uv.x >=0.5 && uv.y <=0.5){
//        uv.x = 1.0 -(uv.x - 0.5) * 2.0;
//        uv.y = uv.y * 2.0;
//    }else if(uv.x <= 0.5 && uv.y >=0.5){
//        uv.x = uv.x * 2.0;
//        uv.y = 1.0 -(uv.y - 0.5) * 2.0;
//    }else if(uv.x >= 0.5 && uv.y >= 0.5){
//        uv.x = 1.0 -(uv.x - 0.5) * 2.0;
//        uv.y = 1.0 -(uv.y - 0.5) * 2.0;
//    }

    // 法2，左上角是原图-- 如何理解？将一个矩形分成4小块，每个小块先移到右上方的坐标原点，然后再进行放大
    if(uv.x <= 0.5 && uv.y >=0.5){ //左上
        uv.x = (uv.x) * 2.0;
        uv.y = (uv.y - 0.5) * 2.0;//左上方块整体沿y轴向右平移0.5，即可到达坐标原点
    }
    else if(uv.x <= 0.5 && uv.y <= 0.5){ //右上 - y轴镜像
        uv.x = uv.x * 2.0;
        uv.y = 1.0 - uv.y *2.0;
    }

    else if(uv.x >= 0.5 && uv.y >= 0.5){ // 左下 -x轴镜像
        uv.x = 1.0-(uv.x - 0.5) * 2.0;
        uv.y = (uv.y - 0.5) * 2.0;
    }
    else if(uv.x >=0.5 && uv.x <= 1.0 && uv.y <=0.5){ //右下 -x轴，y轴都做镜像
        uv.x = 1.0-(uv.x-0.5) *2.0;
        uv.y = 1.0-uv.y * 2.0;
    }

    vec4 tc = texture2D(videoTex, fract(uv));
    gl_FragColor = vec4(tc.r,tc.g,tc.b,1.0);
}
