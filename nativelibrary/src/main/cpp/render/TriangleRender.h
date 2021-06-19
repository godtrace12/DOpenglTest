//
// Created by daijun on 6/19/21.
//

#ifndef DOPENGL_TRIANGLERENDER_H
#define DOPENGL_TRIANGLERENDER_H

#include "base/GLBaseRender.h"

class TriangleRender: public GLBaseRender {

public:
    TriangleRender();
    //？为何也是虚函数
    virtual ~TriangleRender();

    virtual void Init();

    virtual void Draw(int screenWidth,int screenHeight);

    virtual void Destroy();
};


#endif //DOPENGL_TRIANGLERENDER_H
