package com.example.dj.appgl.filter;

import android.content.Context;

import com.example.dj.appgl.R;
import com.example.dj.appgl.filter.base.AbsRectangleTextureFilter;

public class CameraFilter extends AbsRectangleTextureFilter {
    public CameraFilter() {
        super(R.raw.vertex_common_camera_texture, R.raw.fragment_common_camera_shade);
    }
}
