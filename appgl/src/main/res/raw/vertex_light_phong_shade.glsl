uniform mat4 uMVMatrix;
uniform mat4 uMVPMatrix;

attribute vec4 aPosition;
// 法向量
attribute vec3 aNormal;
attribute vec3 objectColor;

varying vec3 fragPos;
varying vec3 norm;

// 纹理坐标
attribute vec2 aTexCoords;
varying vec2 TexCoord;

void main() {
    // 乘以model view矩阵(uMVMatrix)，转换到观察空间
    fragPos = vec3(uMVMatrix * aPosition);
    norm = normalize(vec3(uMVMatrix * vec4(aNormal, 0.0)));
    gl_Position = uMVPMatrix * aPosition;
    // 纹理坐标
    TexCoord = aTexCoords;
}