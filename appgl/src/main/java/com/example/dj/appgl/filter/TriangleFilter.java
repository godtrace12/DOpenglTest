package com.example.dj.appgl.filter;

import com.example.dj.appgl.R;
import com.example.dj.appgl.filter.base.AbstractFboFilter2;
import com.example.dj.appgl.filter.base.AbstractRect2DFilter;

public class TriangleFilter extends AbstractFboFilter2 {
    public TriangleFilter() {
        super(R.raw.vertex_base_shader, R.raw.fragment_base_shader);
    }
}
