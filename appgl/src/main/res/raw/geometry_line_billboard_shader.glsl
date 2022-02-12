#version 320 es
layout (points) in;
layout (triangle_strip, max_vertices = 4) out;
uniform mat4 vMatrix; //vp矩阵相乘结果
uniform vec3 gCameraPos;

out vec2 TexCoord;

void main() {
    // 计算x乘结果矢量
    vec3 Pos = gl_in[0].gl_Position.xyz;
    vec3 toCamera = normalize(gCameraPos - Pos);
    vec3 up = vec3(0.0, 1.0, 0.0);
//    vec3 right = cross(toCamera, up);
    vec3 right = cross(up, toCamera);

    //billboard
    mat4 gVP = vMatrix;
    Pos -= (right * 0.5);//左下角
    gl_Position = gVP * vec4(Pos, 1.0);
    TexCoord = vec2(0.0, 1.0);
    EmitVertex();
    Pos.y += 1.0;//左上角
    gl_Position = gVP * vec4(Pos, 1.0);
    TexCoord = vec2(0.0, 0.0);
    EmitVertex();
    Pos.y -= 1.0;
    Pos += right;//右下角
    gl_Position = gVP * vec4(Pos, 1.0);
    TexCoord = vec2(1.0, 1.0);
    EmitVertex();
    Pos.y += 1.0;//右上角
    gl_Position = gVP * vec4(Pos, 1.0);
    TexCoord = vec2(1.0, 0.0);
    EmitVertex();
    EndPrimitive();
}

