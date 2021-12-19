package com.example.dj.appgl.basicdraw;

import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.example.dj.appgl.R;
import com.example.dj.appgl.base.AppCore;
import com.example.dj.appgl.util.ResReadUtils;
import com.example.dj.appgl.util.ShaderUtils;
import com.example.dj.appgl.util.TextureUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


/** 等腰直角三角形+纹理贴图
 *
 * **/
public class TriangleTextureRenderer implements GLSurfaceView.Renderer{
    private static final String TAG = "TriangleTextureRenderer";
    private FloatBuffer vertexBuffer;
    private FloatBuffer textureBuffer;
    //渲染程序
    private int mProgram;

    //3个定点，等腰直角
    static float triangleCoords[] ={
            // 矩形全部点位
//            0.5f,0.5f,0.0f,
//            -0.5f, -0.5f, 0.0f,
//            -0.5f,0.5f,0.0f,

            0.5f,  0.5f, 0.0f, // top  0.5202312
            -0.5f, -0.5f, 0.0f, // bottom left
            0.5f, -0.5f, 0.0f  // bottom right
    };

    //纹理坐标1
//    private float textureVertex[] = {
//            0.75f, 0.25f,
//            0.25f, 0.75f,
//            0.75f, 0.75f,
//    };

    //纹理坐标2
    // 三角形3个定点对应在纹理坐标系中的坐标
    private float textureVertex[] = {
            // 矩形全部点位
//            1.0f,0.0f,
//            0.0f, 1.0f,
//            0.0f,0.0f,


            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
    };

    //相机矩阵
    private final float[] mViewMatrix = new float[16];
    //投影矩阵
    private final float[] mProjectMatrix = new float[16];
    //最终变换矩阵
    private final float[] mMVPMatrix = new float[16];


    //纹理id
    private int textureId;

//    //变换矩阵
//    private int uMatrixLocation;

    public TriangleTextureRenderer() {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(triangleCoords.length*4);
        byteBuffer.order(ByteOrder.nativeOrder());
        vertexBuffer = byteBuffer.asFloatBuffer();
        //把这门语法() 推送给GPU
        vertexBuffer.put(triangleCoords);
        vertexBuffer.position(0);

        textureBuffer = ByteBuffer.allocateDirect(textureVertex.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        //传入指定的数据
        textureBuffer.put(textureVertex);
        textureBuffer.position(0);

    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES30.glClearColor(0.5f,0.5f,0.5f,1.0f);
        //编译顶点着色程序
        String vertexShaderStr = ResReadUtils.readResource(R.raw.vertex_triangle_texture_shader);
        int vertexShaderId = ShaderUtils.compileVertexShader(vertexShaderStr);
        //编译片段着色程序
        String fragmentShaderStr = ResReadUtils.readResource(R.raw.fragment_triangle_texture_shader);
        int fragmentShaderId = ShaderUtils.compileFragmentShader(fragmentShaderStr);
        //连接程序
        mProgram = ShaderUtils.linkProgram(vertexShaderId, fragmentShaderId);
        //在OpenGLES环境中使用程序
        GLES30.glUseProgram(mProgram);
        //加载纹理
        textureId = TextureUtils.loadTexture(AppCore.getInstance().getContext(),R.drawable.world_map);

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES30.glViewport(0, 0, width, height);
        float ratio = (float) width/height;
        Log.e(TAG, "onSurfaceChanged: ratio="+ratio);
        //设置透视投影
        Matrix.frustumM(mProjectMatrix,0,-ratio,ratio,-1,1,3,7);
//        Matrix.orthoM(mProjectMatrix,0,-ratio,ratio,-1,1,3,7);
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix,0,0,0,3.0f,//摄像机坐标
                0f,0f,0f,//目标物的中心坐标
                0f,1.0f,0.0f);//相机方向
        //接着是摄像机顶部的方向了，如下图，很显然相机旋转，up的方向就会改变，这样就会会影响到绘制图像的角度。
        //例如设置up方向为y轴正方向，upx = 0,upy = 1,upz = 0。这是相机正对着目标图像
        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix,0,mProjectMatrix,0,mViewMatrix,0);

    }

    @Override
    public void onDrawFrame(GL10 gl) {

        //左乘矩阵
        int uMaxtrixLocation = GLES30.glGetUniformLocation(mProgram,"vMatrix");
        // 将前面计算得到的mMVPMatrix(frustumM setLookAtM 通过multiplyMM 相乘得到的矩阵) 传入vMatrix中，与顶点矩阵进行相乘
        GLES30.glUniformMatrix4fv(uMaxtrixLocation,1,false,mMVPMatrix,0);

        int aPositionLocation = GLES30.glGetAttribLocation(mProgram,"vPosition");
        GLES30.glEnableVertexAttribArray(aPositionLocation);
        //x y z 所以数据size 是3
        GLES30.glVertexAttribPointer(aPositionLocation,3,GLES30.GL_FLOAT,false,0,vertexBuffer);

        int aTextureLocation = GLES20.glGetAttribLocation(mProgram,"aTextureCoord");
        Log.e(TAG, "onDrawFrame: textureLocation="+aTextureLocation);
        //纹理坐标数据 x、y，所以数据size是 2
        GLES30.glVertexAttribPointer(aTextureLocation, 2, GLES30.GL_FLOAT, false, 0, textureBuffer);
        //启用顶点颜色句柄
        GLES30.glEnableVertexAttribArray(aTextureLocation);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        //绑定纹理
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,textureId);
        //三角形
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, triangleCoords.length/3);
        //矩形
//        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 6);

        //禁止顶点数组的句柄
        GLES30.glDisableVertexAttribArray(aPositionLocation);
        GLES30.glDisableVertexAttribArray(aTextureLocation);
    }
}
