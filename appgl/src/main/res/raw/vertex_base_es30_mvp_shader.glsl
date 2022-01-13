#version 300 es
in vec4 vPosition; // 顶点坐标

in vec2 vTextureCoord;  //纹理坐标

out vec2 aCoord;// es3.0使用in/out 替换了varying api
// mvp矩阵
uniform mat4 vMatrix;

void main(){
    //内置变量： 把坐标点赋值给gl_position 就Ok了。
    gl_Position =vMatrix * vPosition;
    aCoord = vTextureCoord;
}