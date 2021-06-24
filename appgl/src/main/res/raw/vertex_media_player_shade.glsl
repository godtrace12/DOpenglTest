uniform mat4 textureTransform;
attribute vec4 inputTextureCoordinate;
attribute vec3 position;            //NDK坐标点
varying   vec2 textureCoordinate; //纹理坐标点变换后输出
uniform mat4 uTextRotateMatrix;

 void main() {
//     gl_Position = position;

     vec4 pos = vec4(position, 1.0);

     gl_Position = pos.xyww;
//     textureCoordinate = inputTextureCoordinate;
     //?? 错误原因？？
//     textureCoordinate = vec2((uTextRotateMatrix * vec4(inputTextureCoordinate,0,0)));
     textureCoordinate = (uTextRotateMatrix * inputTextureCoordinate).xy;
 }