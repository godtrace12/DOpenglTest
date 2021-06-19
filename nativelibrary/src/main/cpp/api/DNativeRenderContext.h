//
// Created by daijun on 6/19/21.
//

#ifndef DOPENGL_DNATIVERENDERCONTEXT_H
#define DOPENGL_DNATIVERENDERCONTEXT_H

#include <GLES3/gl3.h>
#include "base/GLBaseRender.h"

class DNativeRenderContext{

    DNativeRenderContext();

    ~DNativeRenderContext();

private:
    static DNativeRenderContext *m_pContext;
    GLBaseRender *m_pCurSample;
    int mWidth;
    int mHeight;

public:
    void SetParamsInt(int paramType, int value0, int value1);
    void OnSurfaceCreated();

    void OnSurfaceChanged(int width, int height);

    void OnDrawFrame();
    static DNativeRenderContext* GetInstance();
    static void DestroyInstance();

};

#endif //DOPENGL_DNATIVERENDERCONTEXT_H
