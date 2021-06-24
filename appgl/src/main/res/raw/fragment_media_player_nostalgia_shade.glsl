#extension GL_OES_EGL_image_external : require
precision mediump float;
uniform samplerExternalOES videoTex;
varying vec2 textureCoordinate;

void main() {
    vec4 tc = texture2D(videoTex, textureCoordinate);
//    float color = tc.r * 0.3 + tc.g * 0.59 + tc.b * 0.11;
    float r = tc.r;
    float g = tc.g;
    float b = tc.b;
    r = 0.393* r + 0.769 * g + 0.189* b;
    g = 0.349 * r + 0.686 * g + 0.168 * b;
    b = 0.272 * r + 0.534 * g + 0.131 * b;
    gl_FragColor = vec4(r,g,b,1.0);
}
