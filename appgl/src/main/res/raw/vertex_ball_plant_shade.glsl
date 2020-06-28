#version 300 es
in vec4 vPosition;
uniform mat4 u_Matrix;
out vec4 vColor;
void main(){
     gl_Position=u_Matrix*vPosition;
     float color;
     if(vPosition.z>0.0){
          color=vPosition.z;
     }else{
          color=-vPosition.z;
     }
     vColor=vec4(color,color,color,1.0);
}