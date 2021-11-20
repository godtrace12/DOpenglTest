#version 300 es
precision mediump float;
uniform sampler2D uTextureUnit;
in vec2 vTexCoord;
out vec4 vFragColor;
uniform vec2 u_resolution; //画布分辨率
uniform float u_time; //默认的时间，动态改变
//uniform int axis; //旋转轴如：X轴 Y轴 Z轴

void main() {
//     vFragColor = texture(uTextureUnit,vTexCoord);
//     vec2 vUv = vTexCoord.xy / u_resolution.xy;
     vec2 vUv = vTexCoord.xy;

     // 振幅（控制波浪顶端和底端的高度）
     float amplitude = 0.15;

     // 角速度（控制波浪的周期）
     float angularVelocity = 10.0;

     // 频率（控制波浪移动的速度）
     float frequency = 10.0;

     // 偏距（波浪垂直偏移量）
     float offset = 0.0;

     // 初相位（正值表现为向左移动，负值则表现为向右移动）
     float initialPhase = frequency * u_time;

     // 代入正弦曲线公式计算 y 值
     // y = Asin(ωx ± φt) + k
     float y = amplitude * sin((angularVelocity * vUv.x) + initialPhase) + offset;

//     vUv.y += y / 20.0;
     vUv.y += y / 10.0;

     vec4 color = texture(uTextureUnit,vUv);

     vFragColor = color;

}