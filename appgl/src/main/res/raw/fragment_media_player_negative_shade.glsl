#extension GL_OES_EGL_image_external : require
precision mediump float;
uniform samplerExternalOES videoTex;
varying vec2 textureCoordinate;

void main() {
    vec4 tc = texture2D(videoTex, textureCoordinate);
//    float color = tc.r * 0.3 + tc.g * 0.59 + tc.b * 0.11;
    float r = 1.0 - tc.r;
    float g = 1.0 - tc.g;
    float b = 1.0 - tc.b;
    gl_FragColor = vec4(r,g,b,1.0);
}
