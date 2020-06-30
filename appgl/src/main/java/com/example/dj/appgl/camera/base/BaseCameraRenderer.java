package com.example.dj.appgl.camera.base;

import android.opengl.GLSurfaceView;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 【说明】：摄像头预览Render基类，主要便于在预览画面上叠加其他渲染对象
 *
 * @author daijun
 * @version 2.0
 * @date 2020/6/30 15:32
 */
public abstract class BaseCameraRenderer implements GLSurfaceView.Renderer {
    private static final String TAG = "BaseCameraRenderer";
    private List<AbsObjectRender> objectRenders = new ArrayList<>();
    protected float[] mProjectMatrix = new float[16];
    protected float[] mCameraMatrix  = new float[16];

    public void setObjectRender(AbsObjectRender absObjectRender){
        objectRenders.clear();
        objectRenders.add(absObjectRender);
    }

    public void setObjectRenders(List<AbsObjectRender> absObjectRenders){
        objectRenders.clear();
        objectRenders.addAll(absObjectRenders);
    }


    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        for (AbsObjectRender objRender:objectRenders){
            objRender.initProgram();
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int i, int i1) {
        for (AbsObjectRender objRender:objectRenders){
            objRender.setProjAndCamMatrix(mProjectMatrix,mCameraMatrix);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
//        Log.e(TAG, "onDrawFrame: onDrawFrame");
        for (AbsObjectRender objRender:objectRenders){
            if (!objRender.isAlreadyInited()){   //初始化不成功，先进行初始化
                Log.e(TAG, "onDrawFrame: 初始化不成功，重新初始化");
                objRender.initProgram();
                objRender.setProjAndCamMatrix(mProjectMatrix,mCameraMatrix);
            }
            objRender.onDrawFrame();
        }
    }
}
