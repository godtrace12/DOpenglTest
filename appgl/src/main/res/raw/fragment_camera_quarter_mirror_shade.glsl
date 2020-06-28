#extension GL_OES_EGL_image_external : require
precision mediump float;
uniform samplerExternalOES videoTex;
varying vec2 textureCoordinate;

void main() {
    vec2 uv = textureCoordinate;
    if (uv.x <= 0.5) {
        uv.x = uv.x * 2.0;
    } else {
        uv.x = (uv.x - 0.5) * 2.0;
    }
    if (uv.y <= 0.5) {
        uv.y = uv.y * 2.0;
    } else {
        uv.y = (uv.y - 0.5) * 2.0;
    }
    vec4 tc = texture2D(videoTex, fract(uv));
    float color = tc.r * 0.3 + tc.g * 0.59 + tc.b * 0.11;
    gl_FragColor = vec4(tc.r,tc.g,tc.b,1.0);
}
