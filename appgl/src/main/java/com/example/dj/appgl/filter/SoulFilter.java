package com.example.dj.appgl.filter;

import com.example.dj.appgl.R;
import com.example.dj.appgl.filter.base.AbstractFboFilter2;

public class SoulFilter extends AbstractFboFilter2 {
    public SoulFilter() {
        super(R.raw.vertex_base_shader, R.raw.fragment_soul_shader);
    }
}
