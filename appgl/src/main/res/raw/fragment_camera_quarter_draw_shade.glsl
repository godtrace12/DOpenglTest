#extension GL_OES_EGL_image_external : require
precision mediump float;
uniform samplerExternalOES videoTex;
varying vec2 textureCoordinate;

void main() {
    vec2 uv = textureCoordinate;
    // 法1，右上角是原图
    if(uv.x <= 0.5 && uv.y <= 0.5){
        uv.x = uv.x * 2.0;
        uv.y = uv.y * 2.0;
    }
    else if(uv.x >=0.5 && uv.y <=0.5){
        uv.x = 1.0 -(uv.x - 0.5) * 2.0;
        uv.y = uv.y * 2.0;
    }else if(uv.x <= 0.5 && uv.y >=0.5){
        uv.x = uv.x * 2.0;
        uv.y = 1.0 -(uv.y - 0.5) * 2.0;
    }else if(uv.x >= 0.5 && uv.y >= 0.5){
        uv.x = 1.0 -(uv.x - 0.5) * 2.0;
        uv.y = 1.0 -(uv.y - 0.5) * 2.0;
    }

    // 法2，左上角是原图
//    if(uv.x <= 0.5 && uv.y <= 0.5){ //右上
//        uv.x = uv.x * 2.0;
//        uv.y = uv.y * 2.0;
//        uv.y = 1-(0.5-uv.y) * 2.0;
//    }
//    else if(uv.x >=0.5 && uv.y <=0.5){
//        uv.x = 1.0 -(uv.x - 0.5) * 2.0;
//        uv.y = uv.y * 2.0;
//    }
//    else if(uv.x <= 0.5 && uv.y >=0.5){ //左上
//        uv.x = uv.x * 2.0;
//        uv.y = (uv.y - 0.5) * 2.0;
//    }
//    else if(uv.x >= 0.5 && uv.y >= 0.5){
//        uv.x = 1.0 -(uv.x - 0.5) * 2.0;
//        uv.y = 1.0 -(uv.y - 0.5) * 2.0;
//    }

    vec4 tc = texture2D(videoTex, fract(uv));
////    float color = tc.r * 0.3 + tc.g * 0.59 + tc.b * 0.11;
//    vec4 tc = texture2D(videoTex, uv);

    gl_FragColor = vec4(tc.r,tc.g,tc.b,1.0);
}
