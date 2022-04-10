//
// Created by daijun on 6/19/21.
//

#include "RectangleRender.h"
#include "../util/GLUtils.h"

RectangleRender::RectangleRender() {

}

RectangleRender::~RectangleRender() {

}

void RectangleRender::Init() {
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
            "   fragColor = vec4 ( 1.0, 0.5, 0.0, 1.0 );  \n"
            "}                                            \n";
    mProgramObj = GLUtils::CreateProgram(vShaderStr, fShaderStr, mVertexShader, mFragmentShader);
    GLfloat vVertices[] = {
            -0.5f,  0.5f, 0.0f,
            -0.5f, -0.5f, 0.0f,
            0.5f, -0.5f, 0.0f,

            -0.5f, 0.5f, 0.0f,
            0.5f, -0.5f, 0.0f,
            0.5f, 0.5f, 0.0f,
    };
    int size2 = sizeof(vVertices);
    createVAO_VBO(vVertices,size2);
}

void RectangleRender::Draw(int screenWidth, int screenHeight) {
    int VERTEX_STRIDE = 3;
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    glClearColor(1.0,1.0,1.0,1.0);
    glUseProgram(mProgramObj);

    glBindVertexArray(mVAOId);

    glDrawArrays(GL_TRIANGLES,0,6);
    glBindVertexArray(GL_NONE);

    glUseProgram(GL_NONE);
}

void RectangleRender::createVAO_VBO(GLfloat vVertices[],int sizeCount) {
    // 生成VBO
    glGenBuffers(1,mVbOIds);
    glBindBuffer(GL_ARRAY_BUFFER,mVbOIds[0]);
    int size = sizeof(vVertices);
    // 此处使用sizeof(指针)得到的是指针的大小，而不是指针指向的内存空间大小
    glBufferData(GL_ARRAY_BUFFER,sizeof(vVertices),vVertices,GL_STATIC_DRAW);

    glBufferData(GL_ARRAY_BUFFER,sizeCount,vVertices,GL_STATIC_DRAW);

    // 生成VAO
    glGenVertexArrays(1,&mVAOId);
    glBindVertexArray(mVAOId);

    glBindBuffer(GL_ARRAY_BUFFER,mVbOIds[0]);
    // 顶点坐标索引 根据顶点着色器layout(location = 0) in vec4 vPosition 知道
    int VERTEX_POS_INDEX = 0;
    glEnableVertexAttribArray(VERTEX_POS_INDEX);
    glVertexAttribPointer(VERTEX_POS_INDEX, 3, GL_FLOAT, GL_FALSE, 3 * sizeof(GLfloat), (const void *)0);
    glBindBuffer(GL_ARRAY_BUFFER, GL_NONE);

    glBindVertexArray(GL_NONE);

}

void RectangleRender::Destroy() {
    if(mProgramObj){
        glDeleteProgram(mProgramObj);
        mProgramObj = GL_NONE;
    }
}