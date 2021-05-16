#version 300 es
precision mediump float;
uniform sampler2D uTextureUnit;
varying vec2 textureCoordinate;

void main() {
    vec4 tc = texture2D(uTextureUnit, textureCoordinate);
    gl_FragColor = tc;
}
