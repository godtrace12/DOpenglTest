package com.example.dj.appgl.filter;

import android.content.Context;
import android.opengl.GLES20;

import com.example.dj.appgl.R;
import com.example.dj.appgl.filter.base.AbstractFboFilter2;
import com.example.dj.appgl.filter.base.FilterContext;

public class SoulFilter extends AbstractFboFilter2 {
    private int mixturePercent;
    private int scalePercent;

    float mix = 0.0f; //透明度，越大越透明
    float scale = 0.0f; //缩放，越大就放的越大

    public SoulFilter() {
        super(R.raw.vertex_base_shader, R.raw.fragment_soul_shader);
    }

    @Override
    public void initGL(int vertexShaderId, int fragmentShaderId) {
        super.initGL(vertexShaderId, fragmentShaderId);
        mixturePercent = GLES20.glGetUniformLocation(mProgram, "mixturePercent");
        scalePercent = GLES20.glGetUniformLocation(mProgram, "scalePercent");
    }


    @Override
    public void beforeDraw(FilterContext filterContext) {
        super.beforeDraw(filterContext);

        GLES20.glUniform1f(mixturePercent, 1.0f - mix);
        GLES20.glUniform1f(scalePercent, scale + 1.0f);


        mix += 0.08f;
        scale += 0.08f;
        if (mix >= 1.0) {
            mix = 0.0f;
        }
        if (scale >= 1.0) {
            scale = 0.0f;
        }
    }

}
