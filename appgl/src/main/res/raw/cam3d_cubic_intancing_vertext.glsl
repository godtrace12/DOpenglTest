// 支持实例化
#version 300 es
uniform mat4 uMVPMatrix;
in vec3 aPosition;
in vec2 aTexCoords;
out vec2 TexCoord;
uniform vec2 offsets[100];


void main() {

//    vec3 posOffset = aPosition + vec3(offsets[gl_InstanceID],0);
    vec4 glPosTmp = vec4(aPosition, 1.0);
//    vec4 glPosTmp = vec4(posOffset, 1.0);

//    glPosTmp.x = aPosition.x + offsets[gl_InstanceID].x;
//    glPosTmp.y = aPosition.y + offsets[gl_InstanceID].y;


    gl_Position = uMVPMatrix * glPosTmp;
    TexCoord = aTexCoords;


    // 发2
//    vec2 offset = offsets[gl_InstanceID];
//    vec3 posOffset = aPosition + vec3(offset,0);
//    vec4 glPosTmp = vec4(posOffset, 1.0);
//    gl_Position = uMVPMatrix * glPosTmp;
//    TexCoord = aTexCoords;
}