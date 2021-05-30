uniform mat4 uMVPMatrix;
attribute vec3 aPosition;
varying vec3 TexCoord;
void main() {
    TexCoord = aPosition;

    vec4 pos = uMVPMatrix * vec4(aPosition, 1.0);
    // 原来的z用w分量代替，保证背景的深度为1，深度最深，不会覆盖任何前景物体
    gl_Position = pos.xyww;
}