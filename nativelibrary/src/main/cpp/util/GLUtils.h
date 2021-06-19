//
// Created by daijun on 6/19/21.
//

#ifndef DOPENGL_GLUTILS_H
#define DOPENGL_GLUTILS_H

#include <string>
#include <GLES3/gl3.h>

class GLUtils {
public:
    static GLuint LoadShader(GLenum shaderType,const char *pSource);
    static GLuint CreateProgram(const char *pVertexShaderSource,const char *pFragShaderSource,
                                GLuint &vertexShaderHandle,GLuint &fragShaderHandle);
    static GLuint CreateProgram(const char *pVertexShaderSource, const char *pFragShaderSource);
    static void DeleteProgram(GLuint &program);
    static void CheckGLError(const char *pGLOperation);

};


#endif //DOPENGL_GLUTILS_H
