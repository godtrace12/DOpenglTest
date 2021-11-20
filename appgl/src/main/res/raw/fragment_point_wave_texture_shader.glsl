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
     // ratio 先乘，最后结果再除
     //设定固定常量
     float u_Boundary = 0.1;
     vec2 u_TouchXY = vec2(0.5, 0.5);
     vec2 textCoord = vTexCoord;
     vec2 touchXY = u_TouchXY;
     float distance = distance(vTexCoord,touchXY);
     if((u_time - u_Boundary >0.0) && (distance <=(u_time + u_Boundary))
     && (distance >= (u_time - u_Boundary))){
          float diff = (distance - u_time);
          float moveDis = 20.0*diff*(diff - u_Boundary)*(diff + u_Boundary);//采样坐标移动距离
//          vec2 unitDirectionVec = normalize(texCoord - touchXY);//单位方向向量
//          textCoord = textCoord + (unitDirectionVec * moveDis);
          textCoord = textCoord + moveDis;

     }

     vFragColor = texture(uTextureUnit,textCoord);

}