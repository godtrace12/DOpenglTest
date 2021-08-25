#extension GL_OES_EGL_image_external : require
precision mediump float;
uniform samplerExternalOES videoTex;
varying vec2 textureCoordinate;
uniform vec2 cetenterVec;

void main() {
    vec4 tc = texture2D(videoTex, textureCoordinate);
    float color = tc.r * 0.3 + tc.g * 0.59 + tc.b * 0.11;
    // 纹理坐标范围在(0,1)之间
    vec2 centerVec = vec2(0.5,0.5);
    float dis = distance(centerVec,textureCoordinate);
    if(dis <0.4){
        gl_FragColor = tc;
    } else{
        vec4 colorResult = vec4(0.0,0.0,0.0,0.0);
        gl_FragColor = colorResult;
//        float u_offset = 0.2;
//        float Y = 0.299 * tc.r + 0.587 * tc.g + 0.114 * tc.b;//RGB 灰度化
//        vec4 grayColor = vec4(vec3(Y), 1);
//        gl_FragColor = mix(grayColor, tc, u_offset);//混合渐变
    }

//    gl_FragColor = tc;
}
