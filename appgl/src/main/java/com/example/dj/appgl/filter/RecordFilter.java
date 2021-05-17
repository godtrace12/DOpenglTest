package com.example.dj.appgl.filter;

import com.example.dj.appgl.R;
import com.example.dj.appgl.filter.base.AbsRectangleTextureFilter;
import com.example.dj.appgl.filter.base.AbstractRect2DFilter;

public class RecordFilter extends AbstractRect2DFilter {
    public RecordFilter() {
        super(R.raw.vertex_base_shader, R.raw.fragment_base_shader);
    }
}
