#version 300 es
precision mediump float;// 数据精度
varying vec2 aCoord;
layout(location = 0) out vec4 outColor0;
layout(location = 1) out vec4 outColor1;
layout(location = 2) out vec4 outColor2;
layout(location = 3) out vec4 outColor3;

uniform sampler2D  vTexture;// samplerExternalOES: 图片， 采样器
// MRT多目标渲染

void main(){
    vec4 rgba = texture2D(vTexture, aCoord);//rgba
    outColor0 = rgba;
    outColor1 = vec4(rgba.r, 0.0, 0.0, 1.0);
    outColor2 = vec4(0.0, rgba.g, 0.0, 1.0);
    outColor3 = vec4(0.0, 0.0, rgba.b, 1.0);
}