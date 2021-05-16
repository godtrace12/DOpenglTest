package com.example.dj.appgl.camera.base;

import android.opengl.GLSurfaceView;

import com.example.dj.appgl.R;
import com.example.dj.appgl.base.AppCore;
import com.example.dj.appgl.filter.ScreenFilter2;
import com.example.dj.appgl.filter.TriangleEdgeFilter;
import com.example.dj.appgl.filter.TriangleFilter;
import com.example.dj.appgl.filter.base.AbstractRect2DFilter;
import com.example.dj.appgl.filter.base.FilterChain;
import com.example.dj.appgl.filter.base.FilterContext;
import com.example.dj.appgl.util.TextureUtils;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 *  通用矩型纹理render基类
 *
 * **/

public class BaseRectTextureRender implements GLSurfaceView.Renderer{

    TriangleFilter triangleFilter;
    TriangleEdgeFilter triangleEdgeFilter;
    ScreenFilter2 screenFilter2;
    //纹理id
    private int textureId;
    List<AbstractRect2DFilter> filters = new ArrayList<>();
    FilterChain filterChain;

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //加载纹理
        textureId = TextureUtils.loadTexture(AppCore.getInstance().getContext(), R.drawable.world_map);
        triangleFilter = new TriangleFilter();
        triangleEdgeFilter = new TriangleEdgeFilter();
        screenFilter2 = new ScreenFilter2();
        filters.add(triangleFilter);
        filters.add(triangleEdgeFilter);
        filters.add(screenFilter2);
        filterChain = new FilterChain(filters,0,new FilterContext());
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        triangleFilter.setSize(width,height);
        triangleEdgeFilter.setSize(width,height);
        screenFilter2.setSize(width,height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
//        triangleFilter.onDraw(textureId,filterChain);
        filterChain.proceed(textureId);

    }
}
