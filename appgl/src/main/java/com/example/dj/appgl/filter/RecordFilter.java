package com.example.dj.appgl.filter;

import com.example.dj.appgl.R;
import com.example.dj.appgl.filter.base.AbsRectangleTextureFilter;

public class RecordFilter extends AbsRectangleTextureFilter {
    public RecordFilter(int vertexResId, int fragmentResId) {
        super(R.raw.vertex_base_texture, R.raw.fragment_base_texture_shade);
    }
}
