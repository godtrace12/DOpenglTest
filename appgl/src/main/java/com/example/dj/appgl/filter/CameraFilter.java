package com.example.dj.appgl.filter;

import android.content.Context;
import android.opengl.GLES20;

import com.example.dj.appgl.R;
import com.example.dj.appgl.filter.base.AbstractFboFilter2;
import com.example.dj.appgl.filter.base.FilterChain;
import com.example.dj.appgl.filter.base.FilterContext;

public class CameraFilter extends AbstractFboFilter2 {
    private int vMatrix;

    public CameraFilter() {
        super(R.raw.vertex_common_camera_texture, R.raw.fragment_common_camera_shade);
    }

    @Override
    public void initGL(int vertexShaderId, int fragmentShaderId) {
        super.initGL(vertexShaderId, fragmentShaderId);
        vMatrix = GLES20.glGetUniformLocation(mProgram, "vMatrix");
    }

    @Override
    public int onDraw(int texture, FilterChain filterChain) {
        return super.onDraw(texture, filterChain);
    }

    @Override
    public void beforeDraw(FilterContext filterContext) {
        super.beforeDraw(filterContext);
//        GLES20.glUniformMatrix4fv(vMatrix, 1, false, filterContext.cameraMtx, 0);
    }



}
