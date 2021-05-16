package com.example.dj.appgl.filter;

import com.example.dj.appgl.R;
import com.example.dj.appgl.filter.base.AbstractRect2DFilter;

public class ScreenFilter2 extends AbstractRect2DFilter {
    public ScreenFilter2() {
        super(R.raw.vertex_base_shader, R.raw.fragment_base_shader);
    }
}
