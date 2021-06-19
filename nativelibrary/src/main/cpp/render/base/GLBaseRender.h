//
// Created by daijun on 6/19/21.
//

#ifndef DOPENGL_GLBASERENDER_H
#define DOPENGL_GLBASERENDER_H

#include "stdint.h"
#include <GLES3/gl3.h>


class GLBaseRender {

protected:
    GLuint mVertexShader;
    GLuint mFragmentShader;
    GLuint mProgramObj;
    int mWidth;
    int mHeight;

public:
    GLBaseRender(){
        mVertexShader = 0;
        mFragmentShader = 0;
        mProgramObj = 0;
        mWidth = 0;
        mHeight = 0;
    }

    virtual ~GLBaseRender(){

    }

    virtual void Init() = 0;
    virtual void Draw(int screenW, int screenH) = 0;

    virtual void Destroy() = 0;


};


#endif //DOPENGL_GLBASERENDER_H
