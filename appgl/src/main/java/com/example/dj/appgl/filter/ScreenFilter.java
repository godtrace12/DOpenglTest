package com.example.dj.appgl.filter;

import com.example.dj.appgl.R;
import com.example.dj.appgl.filter.base.AbsRectangleTextureFilter;
import com.example.dj.appgl.filter.base.AbstractRect2DFilter;

public class ScreenFilter extends AbstractRect2DFilter {
    public ScreenFilter() {
        super(R.raw.vertex_base_texture, R.raw.fragment_base_texture_shade);
    }
}
