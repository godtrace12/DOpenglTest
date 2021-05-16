package com.example.dj.appgl.filter.base;

public class FilterContext {

    public float[] cameraMtx; //摄像头转换矩阵
    public int width;
    public int height;

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void setTransformMatrix(float[] mtx) {
        this.cameraMtx = mtx;
    }

}
