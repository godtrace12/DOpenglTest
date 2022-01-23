#version 300 es
#extension GL_OES_EGL_image_external_essl3 : require
precision mediump float;
uniform samplerExternalOES videoTex;
in vec2 textureCoordinate;
out vec4 gl_FragColor;

void main() {
    vec4 tc = texture(videoTex, textureCoordinate);
    float color = tc.r * 0.3 + tc.g * 0.59 + tc.b * 0.11;
//    gl_FragColor = vec4(color,color,color,1.0);
    gl_FragColor = tc;
}
