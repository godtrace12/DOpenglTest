package com.example.dj.appgl.camera3d;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.example.dj.appgl.camera.CameraQuarRender;
import com.example.dj.appgl.camera.base.AbsObjectRender;
import com.example.dj.appgl.camera.base.BaseCameraRenderer;
import com.example.dj.appgl.camera.object.TrianCamColorRender;
import com.example.dj.appgl.camera3d.obj.CubicIntancingRender;
import com.example.dj.appgl.camera3d.obj.CubicRender;

public class Camera3DGLSurface extends GLSurfaceView implements SurfaceTexture.OnFrameAvailableListener {

    private BaseCameraRenderer render;

    public Camera3DGLSurface(Context context) {
        super(context);
    }

    public Camera3DGLSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(3);
        render = new Camera3DRender(context,this);
        setRenderer(render);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
        //1-- 叠加单个立方体
//        render.setObjectRender(new CubicRender());
        //2-- 实例化多个立方体
        render.setObjectRender(new CubicIntancingRender());

    }

    public void setObjectRender(AbsObjectRender absObjectRender){
        if (render != null){
            render.setObjectRender(absObjectRender);
        }
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        requestRender();
    }


}
