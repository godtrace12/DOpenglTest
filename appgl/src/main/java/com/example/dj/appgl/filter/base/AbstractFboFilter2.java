package com.example.dj.appgl.filter.base;

import android.opengl.GLES30;

import com.example.dj.appgl.util.TextureUtils;

public class AbstractFboFilter2 extends AbstractRect2DFilter {
    public AbstractFboFilter2(int vertexResId, int fragmentResId) {
        super(vertexResId, fragmentResId);
    }

    protected int[] frameBuffer;
    protected int[] frameTextures;


    public void createFrame(int width, int height) {
        if (frameTextures != null) {
            return;
        }
        //創建FBO
        /**
         * 1、创建FBO + FBO中的纹理
         */
        frameBuffer = new int[1];
        frameTextures = new int[1];
        GLES30.glGenFramebuffers(1, frameBuffer, 0);
        TextureUtils.glGenTextures(frameTextures);

        /**
         * 2、fbo与纹理关联
         */
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, frameTextures[0]);
        GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, width, height, 0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE,
                null);
        //纹理关联 fbo
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, frameBuffer[0]);  //綁定FBO
        GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D,
                frameTextures[0],
                0);

        /**
         * 3、解除绑定
         */
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);

    }

    @Override
    public int onDraw(int textureId,FilterChain filterChain) {
        FilterContext filterContext = filterChain.filterContext;
        createFrame(filterContext.width,filterContext.height);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, frameBuffer[0]);
        super.onDraw(textureId,filterChain);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
        //!! 返回的是FBO创建的纹理frameTextures[0]
        return filterChain.proceed(frameTextures[0]);
    }

    @Override
    public void release() {
        super.release();
        releaseFrame();
    }

    private void releaseFrame() {
        if (frameTextures != null) {
            GLES30.glDeleteTextures(1, frameTextures, 0);
            frameTextures = null;
        }

        if (frameBuffer != null) {
            GLES30.glDeleteFramebuffers(1, frameBuffer, 0);
        }
    }


}
