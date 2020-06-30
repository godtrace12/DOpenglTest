package com.example.dj.appgl.camera.base;

import java.nio.FloatBuffer;

/**
 * 【说明】：增加摄像头预览添加其他绘制元素时的抽象类
 *
 * @author daijun
 * @version 2.0
 * @date 2020/6/30 14:04
 */
public abstract class AbsObjectRender {

    //投影矩阵
    protected float[] projectMatrix = new float[16];
    //相机矩阵
    protected float[] cameraMatrix  = new float[16];
    //顶点数组buffer
    private FloatBuffer vertexBuffer;
    //颜色数组buffer
    private FloatBuffer colorBuffer;
    //渲染程序
    public int mProgram = 0;
    /**
     *【说明】： 在onSurfaceCreated中调用,program要在onSurfaceCreated中调用才能成功
     *@author daijun
     *@date 2020/6/30 13:57
     *@param
     *@return
     */
    abstract public void initProgram();

    /**
     *【说明】：在onSurfaceChanged中调用，保存投影矩阵和相机矩阵
     *@author daijun
     *@date 2020/6/30 13:57
     *@param
     *@return
     */
    public void setProjAndCamMatrix(float[] projectMatrix,float[] cameraMatrix){
        this.projectMatrix = projectMatrix;
        this.cameraMatrix = cameraMatrix;
    }

    public boolean isAlreadyInited(){
        return !(mProgram == 0);
    }

    /**
    *【说明】：在onDrawFrame中调用
    *@author daijun
    *@date 2020/6/30 14:23
    *@param
    *@return
    */
    abstract public void onDrawFrame();
}
