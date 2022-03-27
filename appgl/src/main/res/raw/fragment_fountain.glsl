#version 300 es
precision mediump float;

in vec3 v_Color;
in float v_ElapsedTime;
out vec4 gl_FragColor;

void main(){
    //粒子颜色随着颜色的推移变化
    gl_FragColor = vec4(v_Color/v_ElapsedTime, 1.0);
}