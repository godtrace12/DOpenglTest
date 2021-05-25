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
import com.example.dj.appgl.util.OpenGLUtil;
import com.example.dj.appgl.util.ResReadUtils;
import com.example.dj.appgl.util.ShaderUtils;
import com.example.dj.appgl.util.TextureUtils;
import com.example.dj.appgl.util.model.LoadObjectUtil;
import com.example.dj.appgl.util.model.bean.ObjectBean;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


/** 等腰直角三角形+纹理贴图
 *
 * **/
public class ModelLoadRenderer2 implements GLSurfaceView.Renderer{
    private static final String TAG = "TriangleTextureRenderer";
    int mWidth,mHeight;
    // --------------- 1 绘制模型
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

    //---------------- 2 绘制平面背景
    private float[] planeVertices = {
            // positions          // texture Coords (note we set these higher than 1 (together with GL_REPEAT as texture wrapping mode). this will cause the floor texture to repeat)
            5.0f, -0.5f, 5.0f, 2.0f, 0.0f,
            -5.0f, -0.5f, 5.0f, 0.0f, 0.0f,
            -5.0f, -0.5f, -5.0f, 0.0f, 2.0f,

            5.0f, -0.5f, 5.0f, 2.0f, 0.0f,
            -5.0f, -0.5f, -5.0f, 0.0f, 2.0f,
            5.0f, -0.5f, -5.0f, 2.0f, 2.0f
    };
//    private float[] planeVertices = {
//            // positions          // texture Coords (note we set these higher than 1 (together with GL_REPEAT as texture wrapping mode). this will cause the floor texture to repeat)
//            -1f, -0.5f, -1f, 1.0f, 0.0f,
//            -1f, -0.5f, -1f, 0.0f, 0.0f,
//            -1f, -0.5f, -1f, 0.0f, 1.0f,
//
//            1f, -0.5f, -1f, 1.0f, 0.0f,
//            -1f, -0.5f, -1f, 0.0f, 1.0f,
//            1f, -0.5f, -1f, 1.0f, 1.0f
//    };
    private int mProgramBg;
    private FloatBuffer planVertexBuffer;
    private int textureFloor;
    //最终变换矩阵
    private final float[] mMVPMatrixFloor = new float[16];
    private final float[] modelMatrix = new float[16];

    public ModelLoadRenderer2() {
        list = LoadObjectUtil.loadObject(rockDir + "/rock.obj",
                AppCore.getInstance().getResources(), rockDir);

    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES30.glClearColor(0.5f,0.5f,0.5f,1.0f);
        mProgram = generateProgram();
        //在OpenGLES环境中使用程序
        GLES30.glUseProgram(mProgram);

        mProgramBg = generateProgram();
        GLES30.glUseProgram(mProgramBg);
        // 传入顶点坐标
        planVertexBuffer = GLDataUtil.createFloatBuffer(planeVertices);
        textureFloor = TextureUtils.loadTextureNormal(AppCore.getInstance().getContext(), R.drawable.ic_depth_testing_metal);

    }

    int generateProgram(){
        String vertexShaderStr = ResReadUtils.readResource(R.raw.vertex_base_mvp_shader);
//        String vertexShaderStr = ResReadUtils.readResource(R.raw.advanced_opengl_depth_testing_vertex);
        int vertexShaderId = ShaderUtils.compileVertexShader(vertexShaderStr);
        //编译片段着色程序
        String fragmentShaderStr = ResReadUtils.readResource(R.raw.fragment_base_shader);
//        String fragmentShaderStr = ResReadUtils.readResource(R.raw.advanced_opengl_depth_testing_fragment);
        int fragmentShaderId = ShaderUtils.compileFragmentShader(fragmentShaderStr);
        //连接程序
        int program = ShaderUtils.linkProgram(vertexShaderId, fragmentShaderId);
//        int program = OpenGLUtil.createProgram(vertexShaderStr,fragmentShaderStr);
        return program;
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        mWidth = width;
        mHeight = height;
        float ratio = (float) width/height;
        //设置透视投影
        Matrix.frustumM(mProjectMatrix,0,-ratio,ratio,-1,1,1,10);
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix,0,0,0,4.5f,//摄像机坐标
                0f,0f,0f,//目标物的中心坐标
                0f,1.0f,0.0f);//相机方向
        //接着是摄像机顶部的方向了，如下图，很显然相机旋转，up的方向就会改变，这样就会会影响到绘制图像的角度。
        //例如设置up方向为y轴正方向，upx = 0,upy = 1,upz = 0。这是相机正对着目标图像
        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix,0,mProjectMatrix,0,mViewMatrix,0);

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // 设置显示范围
        GLES30.glViewport(0, 0, mWidth, mHeight);
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT| GLES30.GL_DEPTH_BUFFER_BIT);
        //开启深度测试

        //左乘矩阵
        int uMaxtrixLocation = GLES30.glGetUniformLocation(mProgram,"vMatrix");
        // 将前面计算得到的mMVPMatrix(frustumM setLookAtM 通过multiplyMM 相乘得到的矩阵) 传入vMatrix中，与顶点矩阵进行相乘
        GLES30.glUniformMatrix4fv(uMaxtrixLocation,1,false,mMVPMatrix,0);

        int aPositionLocation = GLES30.glGetAttribLocation(mProgram,"vPosition");
        GLES30.glEnableVertexAttribArray(aPositionLocation);

        int aTextureLocation = GLES20.glGetAttribLocation(mProgram,"vTextureCoord");
        int vTextureFilterLoc = GLES20.glGetUniformLocation(mProgram, "vTexture");
        //启用顶点颜色句柄
        GLES30.glEnableVertexAttribArray(aTextureLocation);

        drawModel(aPositionLocation,aTextureLocation,vTextureFilterLoc);

        //禁止顶点数组的句柄
        GLES30.glDisableVertexAttribArray(aPositionLocation);
        GLES30.glDisableVertexAttribArray(aTextureLocation);
//        drawBackFloor();

        drawBackFloor2();

//        drawFloor();

//        drawFloor2();
    }


    // 参数顶点坐标handle位置，纹理坐标handle位置，纹理位置
    void drawModel(int vertexPosLoc,int textPosLoc,int textureLoc){
        //绘制模型
        if (list != null && !list.isEmpty()) {
            for (ObjectBean item : list) {
                if (item != null) {
                    /// 数据如何排列？？？！！！
                    GLES30.glVertexAttribPointer(vertexPosLoc, 3, GLES20.GL_FLOAT,
                            false, 3 * 4, GLDataUtil.createFloatBuffer(item.aVertices));
                    GLES30.glVertexAttribPointer(textPosLoc, 2, GLES20.GL_FLOAT,
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

                        GLES30.glActiveTexture(GLES20.GL_TEXTURE0);
                        GLES30.glBindTexture(GLES20.GL_TEXTURE_2D, item.diffuse);
                        GLES30.glUniform1i(textureLoc, 0);
                    }

                    // 绘制顶点
                    GLES30.glDrawArrays(GLES20.GL_TRIANGLES, 0, item.aVertices.length / 3);
                }
            }
        }
    }

    // 参数顶点坐标handle位置，纹理坐标handle位置，纹理位置
    void drawBackFloor(){
        int textureId = textureFloor;
        Matrix.setIdentityM(modelMatrix, 0);

        Matrix.multiplyMM(mMVPMatrixFloor, 0, mViewMatrix, 0, modelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrixFloor, 0, mProjectMatrix, 0, mMVPMatrixFloor, 0);

        //是否需要2个program?
        //左乘矩阵
        int uMaxtrixLocation = GLES30.glGetUniformLocation(mProgramBg,"vMatrix");
        // 将前面计算得到的mMVPMatrix(frustumM setLookAtM 通过multiplyMM 相乘得到的矩阵) 传入vMatrix中，与顶点矩阵进行相乘
        GLES30.glUniformMatrix4fv(uMaxtrixLocation,1,false,mMVPMatrixFloor,0);

        int vertexPosLoc = GLES30.glGetAttribLocation(mProgramBg,"vPosition");
        GLES30.glEnableVertexAttribArray(vertexPosLoc);

        int textPosLoc = GLES30.glGetAttribLocation(mProgramBg,"vTextureCoord");
        int vTextureFilterLoc = GLES30.glGetUniformLocation(mProgramBg, "vTexture");
        //启用顶点颜色句柄
        GLES30.glEnableVertexAttribArray(textPosLoc);

        // 传入顶点坐标
//        FloatBuffer planVertexBuffer = GLDataUtil.createFloatBuffer(planeVertices);
        GLES30.glVertexAttribPointer(vertexPosLoc, 3, GLES20.GL_FLOAT,
                false, 5 * 4, planVertexBuffer);
        // 纹理坐标
        planVertexBuffer.position(3);
        GLES30.glVertexAttribPointer(textPosLoc, 2, GLES20.GL_FLOAT,
                false, 5 * 4, planVertexBuffer);
        GLES30.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES30.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES30.glUniform1i(vTextureFilterLoc, 0);

        // 绘制顶点
        GLES30.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);

        GLES30.glDisableVertexAttribArray(vertexPosLoc);
        GLES30.glDisableVertexAttribArray(textPosLoc);

    }

    // 参数顶点坐标handle位置，纹理坐标handle位置，纹理位置
    void drawBackFloor2(){
        int textureId = textureFloor;
        Matrix.setIdentityM(modelMatrix, 0);

        Matrix.multiplyMM(mMVPMatrixFloor, 0, mViewMatrix, 0, modelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrixFloor, 0, mProjectMatrix, 0, mMVPMatrixFloor, 0);

        //是否需要2个program?


        int vertexPosLoc = GLES30.glGetAttribLocation(mProgramBg,"vPosition");
        GLES30.glEnableVertexAttribArray(vertexPosLoc);

        int textPosLoc = GLES30.glGetAttribLocation(mProgramBg,"vTextureCoord");
        GLES30.glEnableVertexAttribArray(textPosLoc);
        int vTextureFilterLoc = GLES30.glGetUniformLocation(mProgramBg, "vTexture");


        // 传入顶点坐标
//        FloatBuffer planVertexBuffer = GLDataUtil.createFloatBuffer(planeVertices);
        planVertexBuffer.position(0);
        GLES30.glVertexAttribPointer(vertexPosLoc, 3, GLES20.GL_FLOAT,
                false, 5 * 4, planVertexBuffer);
        // 纹理坐标
        planVertexBuffer.position(3);
        GLES30.glVertexAttribPointer(textPosLoc, 2, GLES20.GL_FLOAT,
                false, 5 * 4, planVertexBuffer);


        //左乘矩阵
        int uMaxtrixLocation = GLES30.glGetUniformLocation(mProgramBg,"vMatrix");
        // 将前面计算得到的mMVPMatrix(frustumM setLookAtM 通过multiplyMM 相乘得到的矩阵) 传入vMatrix中，与顶点矩阵进行相乘
        GLES30.glUniformMatrix4fv(uMaxtrixLocation,1,false,mMVPMatrixFloor,0);

//        //启用顶点颜色句柄
//        GLES30.glEnableVertexAttribArray(textPosLoc);

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId);
        GLES30.glUniform1i(vTextureFilterLoc, 0);

        // 绘制顶点
        GLES30.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);

        GLES30.glDisableVertexAttribArray(vertexPosLoc);
        GLES30.glDisableVertexAttribArray(textPosLoc);

    }


    private void drawFloor(){
//        int shaderProgram = generateProgram();
        int shaderProgram = mProgramBg;
        GLES30.glUseProgram(shaderProgram);
        // 传入顶点坐标
        int positionHandle = GLES30.glGetAttribLocation(shaderProgram, "vPosition");
        GLES30.glEnableVertexAttribArray(positionHandle);
        // 纹理坐标
        int textHandle = GLES30.glGetAttribLocation(shaderProgram, "vTextureCoord");
        GLES30.glEnableVertexAttribArray(textHandle);

        // 传入顶点坐标
        FloatBuffer planVertexBuffer = GLDataUtil.createFloatBuffer(planeVertices);
        GLES30.glVertexAttribPointer(positionHandle, 3, GLES30.GL_FLOAT,
                false, 5 * 4, planVertexBuffer);
        // 纹理坐标
        planVertexBuffer.position(3);
        GLES30.glVertexAttribPointer(textHandle, 2, GLES30.GL_FLOAT,
                false, 5 * 4, planVertexBuffer);

        int mMVPMatrixHandle = GLES30.glGetUniformLocation(shaderProgram, "vMatrix");
        Matrix.setIdentityM(modelMatrix, 0);

        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, modelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mMVPMatrix, 0);
        GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        int texturePosHandle = GLES30.glGetUniformLocation(shaderProgram, "vTexture");

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureFloor);
        GLES30.glUniform1i(texturePosHandle, 0);

        // 绘制顶点
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 6);

        GLES30.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(textHandle);
    }


    private void drawFloor2(){
        int type = 0;
        int shaderProgram = mProgramBg;
        GLES30.glUseProgram(shaderProgram);
        // 传入顶点坐标
        int positionHandle = GLES30.glGetAttribLocation(shaderProgram, "aPosition");
        GLES30.glEnableVertexAttribArray(positionHandle);
        // 纹理坐标
        int textHandle = GLES30.glGetAttribLocation(shaderProgram, "aTexCoords");
        GLES30.glEnableVertexAttribArray(textHandle);

        // 传入顶点坐标
        FloatBuffer planVertexBuffer = GLDataUtil.createFloatBuffer(planeVertices);
        GLES30.glVertexAttribPointer(positionHandle, 3, GLES30.GL_FLOAT,
                false, 5 * 4, planVertexBuffer);
        // 纹理坐标
        planVertexBuffer.position(3);
        GLES30.glVertexAttribPointer(textHandle, 2, GLES30.GL_FLOAT,
                false, 5 * 4, planVertexBuffer);

        int mMVPMatrixHandle = GLES30.glGetUniformLocation(shaderProgram, "uMVPMatrix");
        int typeHandle = GLES30.glGetUniformLocation(shaderProgram, "type");
        GLES30.glUniform1i(typeHandle, type);
        Matrix.setIdentityM(modelMatrix, 0);
        //Matrix.translateM(modelMatrix, 0, 2.0f, 0.0f, -4.0f);
        //Matrix.rotateM(modelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f);

        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, modelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mMVPMatrix, 0);
        GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        int texturePosHandle = GLES30.glGetUniformLocation(shaderProgram, "texture");
//        OpenGLUtil.bindTexture(texturePosHandle, floorTexture, 0);

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureFloor);
        GLES30.glUniform1i(texturePosHandle, 0);

        // 绘制顶点
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 6);

        GLES30.glDisableVertexAttribArray(positionHandle);
        GLES30.glDisableVertexAttribArray(textHandle);
    }



}
