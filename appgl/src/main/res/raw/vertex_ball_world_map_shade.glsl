#version 300 es
in vec4 aPosition;
in vec2 aCoordinate;
//顶点着色器
uniform mat4 uProjMatrix;
uniform mat4 uViewMatrix;
uniform mat4 uModelMatrix;
uniform mat4 uRotateMatrix;
out vec2 vCoordinate;

void main(){
     gl_Position=uProjMatrix*uViewMatrix*uModelMatrix*aPosition;
     vCoordinate=aCoordinate;
}