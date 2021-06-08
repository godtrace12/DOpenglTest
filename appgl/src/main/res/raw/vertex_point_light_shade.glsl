//#version 300 es
uniform mat4 uMVPMatrix;
attribute vec3 aPosition;
void main() {
 gl_Position = uMVPMatrix * vec4(aPosition, 1.0);
 gl_PointSize = 25.0;
}