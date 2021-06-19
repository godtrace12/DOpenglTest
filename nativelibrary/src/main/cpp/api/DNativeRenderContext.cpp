//
// Created by daijun on 6/19/21.
//

#include "DNativeRenderContext.h"
#include "../render/TriangleRender.h"

//？？？ 为何要这样引用
DNativeRenderContext* DNativeRenderContext::m_pContext = nullptr;

DNativeRenderContext::DNativeRenderContext() {

}

DNativeRenderContext::~DNativeRenderContext() {

}

void DNativeRenderContext::OnSurfaceCreated()
{
    glClearColor(1.0f,1.0f,1.0f, 1.0f);
}

void DNativeRenderContext::OnSurfaceChanged(int width, int height) {
    mWidth = width;
    mHeight = height;
    glViewport(0,0,width,height);
}

void DNativeRenderContext::OnDrawFrame()
{
    glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);
    if(m_pCurSample){
        m_pCurSample->Init();
        m_pCurSample->Draw(mWidth,mHeight);
    }
}

void DNativeRenderContext::SetParamsInt(int paramType, int value0, int value1) {
    m_pCurSample = new TriangleRender();
}

DNativeRenderContext * DNativeRenderContext::GetInstance() {
    if(m_pContext == nullptr){
        m_pContext = new DNativeRenderContext();
    }
    return m_pContext;
}

void DNativeRenderContext::DestroyInstance() {
    if(m_pContext){
        delete m_pContext;
        m_pContext = nullptr;
    }
}