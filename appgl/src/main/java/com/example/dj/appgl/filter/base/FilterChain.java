package com.example.dj.appgl.filter.base;

import java.util.List;

public class FilterChain {
    private List<AbstractRect2DFilter> filters;
    private int index;
    public FilterContext filterContext;

    public FilterChain(List<AbstractRect2DFilter> filters,int index,FilterContext filterContext){
        this.filters = filters;
        this.index = index;
        this.filterContext = filterContext;
    }

    public int proceed(int textureId){
        if(index >= filters.size()){
            return textureId;
        }
        FilterChain nextFilterChain = new FilterChain(filters,index+1,filterContext);
        AbstractRect2DFilter abstractRect2DFilter = filters.get(index);
        return abstractRect2DFilter.onDraw(textureId,nextFilterChain);
    }

    public void setTransformMatrix(float[] mtx) {
        filterContext.setTransformMatrix(mtx);
    }


    public void setSize(int width, int height) {
        filterContext.setSize(width, height);
    }

    public void release(){
        for (AbstractRect2DFilter filter : filters) {
            filter.release();
        }
    }

}
