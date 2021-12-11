package com.example.dj.appgl.ball;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.example.dj.appgl.R;
import com.example.dj.appgl.base.AppCore;
import com.example.dj.appgl.util.ResReadUtils;
import com.example.dj.appgl.util.ShaderUtils;
import com.example.dj.appgl.util.TextureUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.opengl.Matrix;


/**
 * 创建日期：12/11/21 8:04 PM
 *
 * @author daijun
 * @version 1.0
 * @des：
 */
public class EarthMapRenderer implements GLSurfaceView.Renderer{
    private static final String TAG = "EarthMapRenderer";
    private static final int BYTES_PER_FLOAT = 4;
    //顶点位置缓存
    private FloatBuffer vertexBuffer;
    //纹理顶点位置缓存
    private FloatBuffer mTexVertexBuffer;
    //渲染程序
    private int mProgram;

    //图片生成的位图
    private Bitmap mBitmap;
    //纹理id
    private int textureId;

    //向量个数
    private int vCount;

    //相关属性id
    private int mHProjMatrix;
    private int mHViewMatrix;
    private int mHModelMatrix;
    private int mHUTexture;
    private int mHPosition;
    private int mHCoordinate;

    //相机矩阵
    private final float[] mViewMatrix = new float[16];
    //投影矩阵
    private final float[] mProjectMatrix = new float[16];

    private final float[] mModelMatrix = new float[16];


    public EarthMapRenderer() {
        calculateAttribute();
    }

    //计算顶点坐标和纹理坐标
    private void calculateAttribute(){
        float radius = 1.0f; // 球的半径
        double angleSpan = Math.PI / 90f; // 将球进行单位切分的角度
        ArrayList<Float> alVertix = new ArrayList<>();
        ArrayList<Float> textureVertix = new ArrayList<>();
        for (double vAngle = 0; vAngle < Math.PI; vAngle = vAngle + angleSpan){

            for (double hAngle = 0; hAngle < 2*Math.PI; hAngle = hAngle + angleSpan){
                float x0 = (float) (radius* Math.sin(vAngle) * Math.cos(hAngle));
                float y0 = (float) (radius* Math.sin(vAngle) * Math.sin(hAngle));
                float z0 = (float) (radius * Math.cos((vAngle)));

                float x1 = (float) (radius* Math.sin(vAngle) * Math.cos(hAngle + angleSpan));
                float y1 = (float) (radius* Math.sin(vAngle) * Math.sin(hAngle + angleSpan));
                float z1 = (float) (radius * Math.cos(vAngle));

                float x2 = (float) (radius* Math.sin(vAngle + angleSpan) * Math.cos(hAngle + angleSpan));
                float y2 = (float) (radius* Math.sin(vAngle + angleSpan) * Math.sin(hAngle + angleSpan));
                float z2 = (float) (radius * Math.cos(vAngle + angleSpan));

                float x3 = (float) (radius* Math.sin(vAngle + angleSpan) * Math.cos(hAngle));
                float y3 = (float) (radius* Math.sin(vAngle + angleSpan) * Math.sin(hAngle));
                float z3 = (float) (radius * Math.cos(vAngle + angleSpan));


                float s0 = (float) (-hAngle / Math.PI/2);
                float s1 = (float) (-(hAngle + angleSpan)/Math.PI/2);

                float t0 = (float) (vAngle / Math.PI);
                float t1 = (float) ((vAngle + angleSpan) / Math.PI);

                alVertix.add(x1);
                alVertix.add(y1);
                alVertix.add(z1);
                alVertix.add(x0);
                alVertix.add(y0);
                alVertix.add(z0);
                alVertix.add(x3);
                alVertix.add(y3);
                alVertix.add(z3);

                textureVertix.add(s1);// x1 y1对应纹理坐标
                textureVertix.add(t0);
                textureVertix.add(s0);// x0 y0对应纹理坐标
                textureVertix.add(t0);
                textureVertix.add(s0);// x3 y3对应纹理坐标
                textureVertix.add(t1);

                alVertix.add(x1);
                alVertix.add(y1);
                alVertix.add(z1);
                alVertix.add(x3);
                alVertix.add(y3);
                alVertix.add(z3);
                alVertix.add(x2);
                alVertix.add(y2);
                alVertix.add(z2);

                textureVertix.add(s1);// x1 y1对应纹理坐标
                textureVertix.add(t0);
                textureVertix.add(s0);// x3 y3对应纹理坐标
                textureVertix.add(t1);
                textureVertix.add(s1);// x2 y3对应纹理坐标
                textureVertix.add(t1);
            }
        }
        vCount = alVertix.size() / 3;
        vertexBuffer = convertToFloatBuffer(alVertix);
        mTexVertexBuffer = convertToFloatBuffer(textureVertix);
    }

    //动态数组转FloatBuffer
    private FloatBuffer convertToFloatBuffer(ArrayList<Float> data){
        float[] d=new float[data.size()];
        for (int i=0;i<d.length;i++){
            d[i]=data.get(i);
        }

        ByteBuffer buffer= ByteBuffer.allocateDirect(data.size()*4);
        buffer.order(ByteOrder.nativeOrder());
        FloatBuffer ret=buffer.asFloatBuffer();
        ret.put(d);
        ret.position(0);
        return ret;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //将背景设置为灰色
        GLES30.glClearColor(0.5f,0.5f,0.5f,1.0f);

        //编译顶点着色程序
        String vertexShaderStr = ResReadUtils.readResource(R.raw.vertex_ball_shade);
        int vertexShaderId = ShaderUtils.compileVertexShader(vertexShaderStr);
        //编译片段着色程序
        String fragmentShaderStr = ResReadUtils.readResource(R.raw.fragment_ball_shade);
        int fragmentShaderId = ShaderUtils.compileFragmentShader(fragmentShaderStr);
        //连接程序
        mProgram = ShaderUtils.linkProgram(vertexShaderId, fragmentShaderId);
        //在OpenGLES环境中使用程序
        GLES30.glUseProgram(mProgram);

        //编译glprogram并获取控制句柄
        mHProjMatrix=GLES30.glGetUniformLocation(mProgram,"uProjMatrix");
        mHViewMatrix=GLES30.glGetUniformLocation(mProgram,"uViewMatrix");
        mHModelMatrix=GLES30.glGetUniformLocation(mProgram,"uModelMatrix");
        mHUTexture=GLES30.glGetUniformLocation(mProgram,"uTexture");
        mHPosition=GLES30.glGetAttribLocation(mProgram,"aPosition");
        mHCoordinate=GLES30.glGetAttribLocation(mProgram,"aCoordinate");

        //加载纹理
        textureId = TextureUtils.loadTexture(AppCore.getInstance().getContext(), R.drawable.world_map);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        //设置绘制窗口
        GLES30.glViewport(0, 0, width, height);

        setSize(width,height);
    }

    public void setSize(int width,int height){
        //计算宽高比
        float ratio=(float)width/height;
        //透视投影矩阵/视锥
        Matrix.perspectiveM(mProjectMatrix,0,60,ratio,1f,300f);
//        Matrix.frustumM(mProjectMatrix,0,-ratio,ratio,-1,1,3,100);
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0, 0f, 4f,1f, 0.0f, 0.0f,0f, 0f,1f, 0f);
        //模型矩阵
        Matrix.setIdentityM(mModelMatrix,0);

        //Matrix.rotateM(mViewMatrix,0,180,0,0,1);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        //把颜色缓冲区设置为我们预设的颜色
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);


        GLES30.glUniformMatrix4fv(mHProjMatrix,1,false,mProjectMatrix,0);
        GLES30.glUniformMatrix4fv(mHViewMatrix,1,false,mViewMatrix,0);
        GLES30.glUniformMatrix4fv(mHModelMatrix,1,false,mModelMatrix,0);

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,textureId);

        GLES30.glEnableVertexAttribArray(mHPosition);
        GLES30.glVertexAttribPointer(mHPosition,3,GLES30.GL_FLOAT,false,0,vertexBuffer);

        GLES30.glEnableVertexAttribArray(mHCoordinate);
        GLES30.glVertexAttribPointer(mHCoordinate,2,GLES30.GL_FLOAT,false,0,mTexVertexBuffer);

        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vCount);

        GLES30.glDisableVertexAttribArray(mHCoordinate);
        GLES30.glDisableVertexAttribArray(mHPosition);
    }

}
