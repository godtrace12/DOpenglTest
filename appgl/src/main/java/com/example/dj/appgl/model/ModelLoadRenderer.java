package com.example.dj.appgl.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.text.TextUtils;
import android.util.Log;

import com.example.dj.appgl.R;
import com.example.dj.appgl.base.AppCore;
import com.example.dj.appgl.util.GLDataUtil;
import com.example.dj.appgl.util.ResReadUtils;
import com.example.dj.appgl.util.ShaderUtils;
import com.example.dj.appgl.util.TextureUtils;
import com.example.dj.appgl.util.model.LoadObjectUtil;
import com.example.dj.appgl.util.model.bean.ObjectBean;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


/** 等腰直角三角形+纹理贴图
 *
 * **/
public class ModelLoadRenderer implements GLSurfaceView.Renderer{
    private static final String TAG = "TriangleTextureRenderer";
    //渲染程序
    private int mProgram;

    //相机矩阵
    private final float[] mViewMatrix = new float[16];
    //投影矩阵
    private final float[] mProjectMatrix = new float[16];
    //最终变换矩阵
    private final float[] mMVPMatrix = new float[16];


    private List<ObjectBean> list;
    public static final String planetDir = "planet", rockDir = "rock";

    public ModelLoadRenderer() {
        list = LoadObjectUtil.loadObject(rockDir + "/rock.obj",
                AppCore.getInstance().getResources(), rockDir);

    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
//        GLES30.glClearColor(0.5f,0.5f,0.5f,1.0f);
        //编译顶点着色程序
        String vertexShaderStr = ResReadUtils.readResource(R.raw.vertex_base_mvp_shader);
        int vertexShaderId = ShaderUtils.compileVertexShader(vertexShaderStr);
        //编译片段着色程序
        String fragmentShaderStr = ResReadUtils.readResource(R.raw.fragment_base_shader);
        int fragmentShaderId = ShaderUtils.compileFragmentShader(fragmentShaderStr);
        //连接程序
        mProgram = ShaderUtils.linkProgram(vertexShaderId, fragmentShaderId);
        //在OpenGLES环境中使用程序
        GLES30.glUseProgram(mProgram);

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES30.glViewport(0, 0, width, height);
        float ratio = (float) width/height;
        //设置透视投影
        Matrix.frustumM(mProjectMatrix,0,-ratio,ratio,-1,1,1,1000);
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix,0,0,0,10f,//摄像机坐标
                0f,0f,0f,//目标物的中心坐标
                0f,1.0f,0.0f);//相机方向
        //接着是摄像机顶部的方向了，如下图，很显然相机旋转，up的方向就会改变，这样就会会影响到绘制图像的角度。
        //例如设置up方向为y轴正方向，upx = 0,upy = 1,upz = 0。这是相机正对着目标图像
        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix,0,mProjectMatrix,0,mViewMatrix,0);

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT| GLES30.GL_DEPTH_BUFFER_BIT);
        //开启深度测试
        GLES30.glEnable(GLES20.GL_DEPTH_TEST);
        //左乘矩阵
        int uMaxtrixLocation = GLES30.glGetUniformLocation(mProgram,"vMatrix");
        // 将前面计算得到的mMVPMatrix(frustumM setLookAtM 通过multiplyMM 相乘得到的矩阵) 传入vMatrix中，与顶点矩阵进行相乘
        GLES30.glUniformMatrix4fv(uMaxtrixLocation,1,false,mMVPMatrix,0);

        int aPositionLocation = GLES30.glGetAttribLocation(mProgram,"vPosition");
        GLES30.glEnableVertexAttribArray(aPositionLocation);

        int aTextureLocation = GLES20.glGetAttribLocation(mProgram,"vTextureCoord");
        int vTextureFilterLoc = GLES20.glGetUniformLocation(mProgram, "vTexture");
        Log.e(TAG, "onDrawFrame: textureLocation="+aTextureLocation);
        //启用顶点颜色句柄
        GLES30.glEnableVertexAttribArray(aTextureLocation);

        drawModel(aPositionLocation,aTextureLocation,vTextureFilterLoc);

        //禁止顶点数组的句柄
        GLES30.glDisableVertexAttribArray(aPositionLocation);
        GLES30.glDisableVertexAttribArray(aTextureLocation);
    }

    // 参数顶点坐标handle位置，纹理坐标handle位置，纹理位置
    void drawModel(int vertexPosLoc,int textPosLoc,int textureLoc){
        //绘制模型
        if (list != null && !list.isEmpty()) {
            for (ObjectBean item : list) {
                if (item != null) {
                    /// 数据如何排列？？？！！！
                    GLES20.glVertexAttribPointer(vertexPosLoc, 3, GLES20.GL_FLOAT,
                            false, 3 * 4, GLDataUtil.createFloatBuffer(item.aVertices));
                    GLES20.glVertexAttribPointer(textPosLoc, 2, GLES20.GL_FLOAT,
                            false, 2 * 4, GLDataUtil.createFloatBuffer(item.aTexCoords));

                    if (item.mtl != null) {
                        if (!TextUtils.isEmpty(item.mtl.Kd_Texture)) {
                            if (item.diffuse < 0) {
                                try {
                                    Bitmap bitmap = BitmapFactory.decodeStream(AppCore.getInstance().getContext().getAssets().open(
                                            rockDir + "/" + item.mtl.Kd_Texture));
                                    item.diffuse = TextureUtils.createTextureWithBitmap(bitmap);
                                    bitmap.recycle();
                                } catch (IOException e) {
                                    Log.e(TAG, "onDrawFrame: "+e);
                                }
                            }
                        } else {
                            if (item.diffuse < 0) {
                                item.diffuse = TextureUtils.loadTexture(AppCore.getInstance().getContext(), R.drawable.ic_launcher_background);
                            }
                        }

                        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, item.diffuse);
                        GLES20.glUniform1i(textureLoc, 0);
                    }

                    // 绘制顶点
                    GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, item.aVertices.length / 3);
                }
            }
        }
    }

}
