//
// Created by daijun on 6/19/21.
//

#include "TriangleRender.h"
#include "../util/GLUtils.h"

TriangleRender::TriangleRender() {

}

TriangleRender::~TriangleRender() {

}

void TriangleRender::Init() {
    if(mProgramObj != 0){
        return;
    }
    char vShaderStr[] = "#version 300 es                          \n"
                        "layout(location = 0) in vec4 vPosition;  \n"
                        "void main()                              \n"
                        "{                                        \n"
                        "   gl_Position = vPosition;              \n"
                        "}                                        \n";

    char fShaderStr[] =
            "#version 300 es                              \n"
            "precision mediump float;                     \n"
            "out vec4 fragColor;                          \n"
            "void main()                                  \n"
            "{                                            \n"
            "   fragColor = vec4 ( 1.0, 0.0, 0.0, 1.0 );  \n"
            "}                                            \n";
    mProgramObj = GLUtils::CreateProgram(vShaderStr, fShaderStr, mVertexShader, mFragmentShader);
}

void TriangleRender::Draw(int screenWidth, int screenHeight) {
    GLfloat vVertices[] = {
            0.0f,  0.5f, 0.0f,
            -0.5f, -0.5f, 0.0f,
            0.5f, -0.5f, 0.0f,
    };
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    glClearColor(1.0,1.0,1.0,1.0);
    glUseProgram(mProgramObj);

    glVertexAttribPointer(0,3,GL_FLOAT,GL_FALSE,0,vVertices);
    glEnableVertexAttribArray(0);

    glDrawArrays(GL_TRIANGLES,0,3);
    glUseProgram(GL_NONE);
}

void TriangleRender::Destroy() {
    if(mProgramObj){
        glDeleteProgram(mProgramObj);
        mProgramObj = GL_NONE;
    }
}