package com.example.dj.appgl.pbo;

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
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGL;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


/** FBO + ReadPixels
 *
 * **/
public class ReadPixelsFboRenderer implements GLSurfaceView.Renderer{
    private static final String TAG = "TriangleTextureRenderer";

    // -------------- 顶点和纹理坐标数据

    static float rectCoords[] ={

            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f

    };

    //纹理坐标2
    private float textureVertex[] = {

            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,

    };

    // ------------------- 原始纹理绘图 ------------------
    private FloatBuffer vertexBuffer;
    private FloatBuffer textureBuffer;
    //渲染程序
    private int mProgram;

    //纹理id
    private int textureId;
    int mWidth,mHeight;

    // ------------------------ FBO --------------------
    protected int[] frameBuffer;
    protected int[] frameTextures;
    // 控制是否使用fbo
    boolean isUseFbo = true;
    // 使用fbo的前提下，是否绘制到屏幕上
    boolean isDrawToScreen = true;
    //fbo是否已创建
    boolean isFboCreated = false;


    // ---------------------- filter -------------------
    protected FloatBuffer vertexFilterBuffer;
    protected FloatBuffer textureFilterBuffer;
    private int mProgramFilter; //滤镜program

    // -------------------- readpixels --------------------
    private IntBuffer pixelsBuffer;
    private boolean isSavedPixels = false;

    public ReadPixelsFboRenderer() {
        // 1---------- 原始纹理相关初始化
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(rectCoords.length*4);
        byteBuffer.order(ByteOrder.nativeOrder());
        vertexBuffer = byteBuffer.asFloatBuffer();
        //把这门语法() 推送给GPU
        vertexBuffer.put(rectCoords);
        vertexBuffer.position(0);

        textureBuffer = ByteBuffer.allocateDirect(textureVertex.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        //传入指定的数据
        textureBuffer.put(textureVertex);
        textureBuffer.position(0);

        // 2----------- filter滤镜相关初始化
        ByteBuffer byteBuffer2 = ByteBuffer.allocateDirect(rectCoords.length*4);
        byteBuffer2.order(ByteOrder.nativeOrder());
        vertexFilterBuffer = byteBuffer.asFloatBuffer();
        //把这门语法() 推送给GPU
        vertexFilterBuffer.put(rectCoords);
        vertexFilterBuffer.position(0);

        textureFilterBuffer = ByteBuffer.allocateDirect(textureVertex.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        //传入指定的数据
        textureFilterBuffer.put(textureVertex);
        textureFilterBuffer.position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES30.glClearColor(0.5f,0.5f,0.5f,1.0f);
        // 1--- 原始纹理相关初始化
        //编译顶点着色程序
        String vertexShaderStr = ResReadUtils.readResource(R.raw.vertex_base_shader);
        int vertexShaderId = ShaderUtils.compileVertexShader(vertexShaderStr);
        //编译片段着色程序
        String fragmentShaderStr = ResReadUtils.readResource(R.raw.fragment_base_shader);
        int fragmentShaderId = ShaderUtils.compileFragmentShader(fragmentShaderStr);
        //连接程序
        mProgram = ShaderUtils.linkProgram(vertexShaderId, fragmentShaderId);
        //加载纹理
        textureId = TextureUtils.loadTexture(AppCore.getInstance().getContext(),R.drawable.world_map);


        // 2--滤镜Filter相关初始化
        String vertexFilterShaderStr = ResReadUtils.readResource(R.raw.vertex_base_shader);
        int vertexFilterShaderId = ShaderUtils.compileVertexShader(vertexFilterShaderStr);
        //编译片段着色程序
        String fragmentShaderFilterStr = ResReadUtils.readResource(R.raw.fragment_edge_shader);
        int fragmentShaderFilterId = ShaderUtils.compileFragmentShader(fragmentShaderFilterStr);
        //连接程序
        mProgramFilter = ShaderUtils.linkProgram(vertexFilterShaderId, fragmentShaderFilterId);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mWidth = width;
        mHeight = height;
        float ratio = (float) width/height;
        //3-------------------- readpixels 相关初始化
        pixelsBuffer = ByteBuffer.allocate(width * height * 3)//注意3还是4，rgb rgba
                .order(ByteOrder.nativeOrder()).asIntBuffer();
        pixelsBuffer.clear();
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES30.glViewport(0, 0, mWidth, mHeight);

        if(isUseFbo){
            //创建FBO
            if(!isFboCreated){
                isFboCreated = true;
                createFBO(mWidth,mHeight);
            }
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER,frameBuffer[0]);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, frameTextures[0]);
        }
        // 1----------------------- 绘制原始纹理
        GLES30.glUseProgram(mProgram);
        int aPositionLocation = GLES30.glGetAttribLocation(mProgram,"vPosition");
        GLES30.glEnableVertexAttribArray(aPositionLocation);
        //x y z 所以数据size 是3
        GLES30.glVertexAttribPointer(aPositionLocation,2,GLES30.GL_FLOAT,false,0,vertexBuffer);

        int aTextureLocation = GLES20.glGetAttribLocation(mProgram,"vTextureCoord");
        Log.e(TAG, "onDrawFrame: textureLocation="+aTextureLocation);
        //纹理坐标数据 x、y，所以数据size是 2
        GLES30.glVertexAttribPointer(aTextureLocation, 2, GLES30.GL_FLOAT, false, 0, textureBuffer);
        int vTextureLoc = GLES20.glGetUniformLocation(mProgram, "vTexture");
        //启用顶点颜色句柄
        GLES30.glEnableVertexAttribArray(aTextureLocation);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        //绑定纹理
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,textureId);
        // GL_TEXTURE1 ， 1
        GLES30.glUniform1i(vTextureLoc,0);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP,0, rectCoords.length/2);
        //禁止顶点数组的句柄
        GLES30.glDisableVertexAttribArray(aPositionLocation);
        GLES30.glDisableVertexAttribArray(aTextureLocation);
        //解绑纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        // 关闭FBO
        if(isUseFbo){
            if (!isSavedPixels){
                isSavedPixels = true;
                //指定像素数据的格式。接受以下符号值：GL_ALPHA，GL_RGB和GL_RGBA
//                GLES30.glReadPixels(0, 0, mWidth, mHeight, GLES30.GL_RGB,GLES30.GL_UNSIGNED_BYTE,pixelsBuffer);
//                Log.e(TAG, "onDrawFrame: capcatiy="+pixelsBuffer.capacity());
//                StringBuilder str = new StringBuilder();
//                for(int i = 0; i < pixelsBuffer.capacity(); i++)
//                {
//                    str.append(pixelsBuffer.get());
//                }
//                Log.e(TAG, "onDrawFrame: pixelData="+pixelsBuffer.toString());
            }
            //解绑FBO
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
            GLES30.glDeleteFramebuffers(1,frameBuffer,0);
        }

        //
        // 2------------------------ 绘制filter滤镜
        if(isUseFbo && isDrawToScreen){
            GLES30.glUseProgram(mProgramFilter);
            int vPositionCoordLoc = GLES30.glGetAttribLocation(mProgramFilter,"vPosition");
            int vTextureCoordLoc = GLES20.glGetAttribLocation(mProgramFilter,"vTextureCoord");
            int vTextureFilterLoc = GLES20.glGetUniformLocation(mProgramFilter, "vTexture");
            //x y 所以数据size 是2
            GLES30.glVertexAttribPointer(vPositionCoordLoc,2,GLES30.GL_FLOAT,false,0,vertexFilterBuffer);
            GLES30.glEnableVertexAttribArray(vPositionCoordLoc);

            //纹理坐标是xy 所以数据size是 2
            GLES30.glVertexAttribPointer(vTextureCoordLoc, 2, GLES30.GL_FLOAT, false, 0, textureFilterBuffer);
            //启用顶点颜色句柄
            GLES30.glEnableVertexAttribArray(vTextureCoordLoc);

            GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
            //绑定纹理
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,frameTextures[0]);
            GLES30.glUniform1i(vTextureFilterLoc,0);

            GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP,0, rectCoords.length/2);

            //解绑纹理
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
            //禁止顶点数组的句柄
            GLES30.glDisableVertexAttribArray(vPositionCoordLoc);
            GLES30.glDisableVertexAttribArray(vTextureFilterLoc);
        }

    }



    public void createFBO(int width, int height) {
        if (frameTextures != null) {
            return;
        }
        // 創建FBO
        frameBuffer = new int[1];
        frameTextures = new int[1];
        GLES30.glGenFramebuffers(1, frameBuffer, 0);
        // 创建FBO纹理
        TextureUtils.glGenTextures(frameTextures);

        /**
         * 2、fbo与纹理关联
         */
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, frameTextures[0]);
        // 设置FBO分配内存大小
        GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, width, height, 0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE,
                null);
        //纹理关联 fbo
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, frameBuffer[0]);  //綁定FBO
        //将纹理附着到帧缓冲中
        // GL_COLOR_ATTACHMENT0、GL_DEPTH_ATTACHMENT、GL_STENCIL_ATTACHMENT分别对应颜色缓冲、深度缓冲和模板缓冲。
        GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D,
                frameTextures[0],
                0);
        // 检测fbo绑定是否成功
        if(GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER) != GLES30.GL_FRAMEBUFFER_COMPLETE){
            throw new RuntimeException("FBO附着异常");
        }

        //7. 解绑纹理和FBO
//        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
//        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);

    }

    private void releaseFbo() {
        if (frameTextures != null) {
            GLES30.glDeleteTextures(1, frameTextures, 0);
            frameTextures = null;
        }

        if (frameBuffer != null) {
            GLES30.glDeleteFramebuffers(1, frameBuffer, 0);
        }
    }


}
