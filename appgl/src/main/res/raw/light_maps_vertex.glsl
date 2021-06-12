uniform mat4 uMVMatrix;
uniform mat4 uMVPMatrix;
uniform mat4 normalMatrix;

attribute vec4 aPosition;
// 法向量
attribute vec3 aNormal;
attribute vec2 aTexCoords;

varying vec3 fragPos;
varying vec3 norm;
varying vec2 TexCoord;

void main() {
    fragPos = vec3(uMVMatrix * aPosition);
//    norm = normalize(mat3(transpose(inverse(uMVMatrix))) * aNormal);
    // mat3作用：把被处理过的矩阵强制转换为3×3矩阵，来保证它失去了位移属性以及能够乘以vec3的法向量
    norm = normalize(mat3(normalMatrix) * aNormal);
    TexCoord = aTexCoords;
    gl_Position = uMVPMatrix * aPosition;
}