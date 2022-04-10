//
// Created by daijun on 4/10/22.
//

#ifndef DOPENGL_RECTANGLERENDER_H
#define DOPENGL_RECTANGLERENDER_H

#include "base/GLBaseRender.h"
class RectangleRender: public GLBaseRender{
    public:
    RectangleRender();
    virtual ~RectangleRender();
    virtual void Init();
    virtual void Draw(int screenWidth,int screenHeight);
    virtual void Destroy();

private:
    void createVAO_VBO(GLfloat vVertices[],int sizeCount);
    GLuint mVAOId;
    GLuint mVbOIds[1] = {GL_NONE};
};

#endif //DOPENGL_RECTANGLERENDER_H
