#version 300 es
precision mediump float;// 数据精度
varying vec2 aCoord;

uniform sampler2D  vTexture0;// samplerExternalOES: 图片， 采样器
uniform sampler2D  vTexture1;// samplerExternalOES: 图片， 采样器
uniform sampler2D  vTexture2;// samplerExternalOES: 图片， 采样器
uniform sampler2D  vTexture3;// samplerExternalOES: 图片， 采样器

// 多目标渲染

void main(){
    if(aCoord.x < 0.5 && aCoord.y < 0.5){
        gl_FragColor = texture2D(vTexture0, aCoord);
    }else if(aCoord.x > 0.5 && aCoord.y < 0.5){
        gl_FragColor = texture2D(vTexture1, aCoord);
    }else if(aCoord.x < 0.5 && aCoord.y > 0.5){
        gl_FragColor = texture2D(vTexture2, aCoord);
    }else{
        gl_FragColor = texture2D(vTexture3, aCoord);
    }

}