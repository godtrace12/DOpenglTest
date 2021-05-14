attribute vec4 vPosition; // 顶点坐标

attribute vec2 vTextureCoord;  //纹理坐标

varying vec2 aCoord;

void main(){
    //内置变量： 把坐标点赋值给gl_position 就Ok了。
    gl_Position = vPosition;
    aCoord = vTextureCoord;
}