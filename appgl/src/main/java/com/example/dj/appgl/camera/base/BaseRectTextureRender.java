package com.example.dj.appgl.camera.base;

import android.opengl.GLSurfaceView;

import com.example.dj.appgl.R;
import com.example.dj.appgl.base.AppCore;
import com.example.dj.appgl.filter.TriangleFilter;
import com.example.dj.appgl.util.TextureUtils;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 *  通用矩型纹理render基类
 *
 * **/

public class BaseRectTextureRender implements GLSurfaceView.Renderer{

    TriangleFilter triangleFilter;
    //纹理id
    private int textureId;

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        triangleFilter = new TriangleFilter();
        //加载纹理
        textureId = TextureUtils.loadTexture(AppCore.getInstance().getContext(), R.drawable.world_map);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        triangleFilter.setSize(width,height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        triangleFilter.onDraw(textureId);
    }
}
