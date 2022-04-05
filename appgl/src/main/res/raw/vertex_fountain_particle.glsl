// 支持实例化
#version 300 es
uniform float u_Time;

in vec3 a_Position;
in vec3 a_Color;
in vec3 a_Direction;
in float a_PatricleStartTime;

out vec3 v_Color;
out float v_ElapsedTime;

//对于匀变速直线运动：
//位移公式：s=v0*t+a*t*t/2
void main(){
    v_Color = a_Color;
    //粒子已经持续时间  当前时间-开始时间
    v_ElapsedTime = u_Time - a_PatricleStartTime;
    //重力或者阻力因子，随着时间的推移越来越大
    float gravityFactor = v_ElapsedTime * v_ElapsedTime / 9.8;
    //当前的运动到的位置 粒子起始位置+（运动矢量*持续时间）
    vec3 curPossition = a_Position + (a_Direction * v_ElapsedTime);
    //减去重力或阻力的影响
    curPossition.y -= gravityFactor;

    //把当前位置通过内置变量传给片元着色器
    gl_Position =  vec4(curPossition,1.0);
    gl_PointSize = 10.0;
}