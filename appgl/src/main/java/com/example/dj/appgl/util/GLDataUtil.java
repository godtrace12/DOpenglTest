package com.example.dj.appgl.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

public class GLDataUtil {
    public static final int SIZEOF_FLOAT = 4;
    public static final int SIZEOF_INT = 4;
    public static final int SIZEOF_SHORT = 2;

    public static FloatBuffer createFloatBuffer(ArrayList<Float> data) {
        float[] aar = new float[data.size()];
        for (int i = 0; i < aar.length; i++) {
            aar[i] = data.get(i);
        }
        return createFloatBuffer(aar);
    }

    public static FloatBuffer createFloatBuffer(float[] arr) {
        ByteBuffer bb = ByteBuffer.allocateDirect(arr.length * SIZEOF_FLOAT);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(arr);
        fb.position(0);
        return fb;
    }

    public static ShortBuffer createShortBuffer(short[] arr) {
        ByteBuffer bb = ByteBuffer.allocateDirect(arr.length * SIZEOF_INT);
        bb.order(ByteOrder.nativeOrder());
        ShortBuffer sb = bb.asShortBuffer();
        sb.put(arr);
        sb.position(0);
        return sb;
    }
}
