#version 300 es
precision mediump float;
uniform sampler2D texture;
in vec2 TexCoord;
out vec4 gl_FragColor;

void main() {
    gl_FragColor = texture(texture, TexCoord);
}
