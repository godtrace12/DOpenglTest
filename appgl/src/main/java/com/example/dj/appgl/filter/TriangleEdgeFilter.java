package com.example.dj.appgl.filter;

import com.example.dj.appgl.R;
import com.example.dj.appgl.filter.base.AbstractFboFilter;
import com.example.dj.appgl.filter.base.AbstractFboFilter2;
import com.example.dj.appgl.filter.base.AbstractRect2DFilter;

public class TriangleEdgeFilter extends AbstractFboFilter2 {
    public TriangleEdgeFilter() {
        super(R.raw.vertex_base_shader, R.raw.fragment_edge_shader);
    }
}
