package com.example.dj.appgl.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import android.os.Build;
import android.util.Log;

import javax.microedition.khronos.opengles.GL10;

/**
 * @anchor: andy
 * @date: 18-11-10
 */

public class TextureUtils {

    private static final String TAG = "TextureUtils";

    public static int loadTexture(Context context, int resourceId) {
        final int[] textureIds = new int[1];
        GLES30.glGenTextures(1, textureIds, 0);
        if (textureIds[0] == 0) {
            Log.e(TAG, "Could not generate a new OpenGL textureId object.");
            return 0;
        }
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);
        if (bitmap == null) {
            Log.e(TAG, "Resource ID " + resourceId + " could not be decoded.");
            GLES30.glDeleteTextures(1, textureIds, 0);
            return 0;
        }
        // 绑定纹理到OpenGL
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureIds[0]);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                 GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_REPEAT);

        // 加载bitmap到纹理中
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0);

        // 生成MIP贴图
//        GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D);

        // 数据如果已经被加载进OpenGL,则可以回收该bitmap
        bitmap.recycle();

        // 取消绑定纹理
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);

        return textureIds[0];
    }

    public static int loadTextureNormal(Context context, int resourceId) {
        final int[] textureIds = new int[1];
        GLES30.glGenTextures(1, textureIds, 0);
        if (textureIds[0] == 0) {
            Log.e(TAG, "Could not generate a new OpenGL textureId object.");
            return 0;
        }
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);
        if (bitmap == null) {
            Log.e(TAG, "Resource ID " + resourceId + " could not be decoded.");
            GLES30.glDeleteTextures(1, textureIds, 0);
            return 0;
        }
        // 绑定纹理到OpenGL
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureIds[0]);

        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                 GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                 GLES20.GL_REPEAT);
        // 加载bitmap到纹理中
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0);

        // 生成MIP贴图
//        GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D);

        // 数据如果已经被加载进OpenGL,则可以回收该bitmap
        bitmap.recycle();

        // 取消绑定纹理
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);

        return textureIds[0];
    }

    /**
     * 加载OES Texture
     *
     * @return
     */
    public static int loadOESTexture() {
        int[] textureIds = new int[1];
        GLES20.glGenTextures(1, textureIds, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureIds[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        return textureIds[0];
    }


    // [mod dj 2020-06-21] 逻辑待完善
    public static int[] loadTextures(Context context, int[] resourceIds) {
        int count = resourceIds.length;
        final int[] textureIds = new int[count];
        GLES30.glGenTextures(count, textureIds, 0);
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;

        for (int i = 0; i < count; i++) {
            final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceIds[i], options);
            if (bitmap == null) {
                Log.e(TAG, "Resource ID " + resourceIds[i] + " could not be decoded.");
                GLES30.glDeleteTextures(1, textureIds, 0);
            }
            // 绑定纹理到OpenGL
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureIds[i]);

            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_LINEAR);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);

            // 加载bitmap到纹理中
            GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0);

            // 生成MIP贴图
            GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D);

            // 数据如果已经被加载进OpenGL,则可以回收该bitmap
            bitmap.recycle();

            // 取消绑定纹理
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureIds[i]);
        }
        return textureIds;

    }



    public static void glGenTextures(int[] textures) {
        GLES30.glGenTextures(textures.length, textures, 0);
        for (int i = 0; i < textures.length; i++) {
            //绑定纹理，后续配置纹理
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textures[i]);


            /**
             *  必须：设置纹理过滤参数设置
             */
            /*设置纹理缩放过滤*/
            // GL_NEAREST: 使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
            // GL_LINEAR:  使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
            // 后者速度较慢，但视觉效果好
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER,
                    GLES30.GL_LINEAR);//放大过滤
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER,
                    GLES30.GL_LINEAR);//缩小过滤

            /**
             * 可选：设置纹理环绕方向
             */
            //纹理坐标的范围是0-1。超出这一范围的坐标将被OpenGL根据GL_TEXTURE_WRAP参数的值进行处理
            //GL_TEXTURE_WRAP_S, GL_TEXTURE_WRAP_T 分别为x，y方向。
            //GL_REPEAT:平铺
            //GL_MIRRORED_REPEAT: 纹理坐标是奇数时使用镜像平铺
            //GL_CLAMP_TO_EDGE: 坐标超出部分被截取成0、1，边缘拉伸
//            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S,
//                    GLES30.GL_CLAMP_TO_EDGE);
//            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T,
//                    GLES30.GL_CLAMP_TO_EDGE);
            GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
            GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,0);
        }
    }


    public static int createTextureWithBitmap(Bitmap bitmap) {
        final int[] textureIds = new int[1];
        GLES30.glGenTextures(1, textureIds, 0);
        if (textureIds[0] == 0) {
            Log.e(TAG, "Could not generate a new OpenGL textureId object.");
            return 0;
        }
        if (bitmap == null) {
            Log.e(TAG, "Resource ID bitmap could not be decoded.");
            GLES30.glDeleteTextures(1, textureIds, 0);
            return 0;
        }
        // 绑定纹理到OpenGL
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureIds[0]);

        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        // 加载bitmap到纹理中
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0);

        // 生成MIP贴图
        GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D);

        // 数据如果已经被加载进OpenGL,则可以回收该bitmap
        bitmap.recycle();

        // 取消绑定纹理
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);

        return textureIds[0];
    }

    /**
     * 立方体贴图
     * @param context context
     * @param resIds  贴图集合，顺序是：
     *                <ul><li>右{@link GLES20#GL_TEXTURE_CUBE_MAP_POSITIVE_X}</li>
     *                <li>左{@link GLES20#GL_TEXTURE_CUBE_MAP_NEGATIVE_X}</li>
     *                <li>上{@link GLES20#GL_TEXTURE_CUBE_MAP_POSITIVE_Y}</li>
     *                <li>下{@link GLES20#GL_TEXTURE_CUBE_MAP_NEGATIVE_Y}</li>
     *                <li>后{@link GLES20#GL_TEXTURE_CUBE_MAP_POSITIVE_Z}</li>
     *                <li>前{@link GLES20#GL_TEXTURE_CUBE_MAP_NEGATIVE_Z}</li></ul>
     * @return int
     */
    public static int createTextureCube(Context context, int[] resIds) {
        if (resIds != null && resIds.length >= 6) {
            int[] texture = new int[1];
            //生成纹理
            GLES30.glGenTextures(1, texture, 0);
            //生成纹理
            GLES30.glBindTexture(GLES30.GL_TEXTURE_CUBE_MAP, texture[0]);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
//            if (OpenGLVersion > 2 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
//                GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GLES30.GL_TEXTURE_WRAP_R, GLES30.GL_CLAMP_TO_EDGE);
//            }

            Bitmap bitmap;
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;
            for (int i = 0; i < resIds.length; i++) {
                bitmap = BitmapFactory.decodeResource(context.getResources(),
                        resIds[i], options);
                if (bitmap == null) {
                    GLES30.glDeleteTextures(1, texture, 0);
                    return 0;
                }
                GLUtils.texImage2D(GLES30.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, bitmap, 0);
                bitmap.recycle();
            }
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
            return texture[0];
        }
        return 0;
    }

    /**
     * 绑定纹理
     * @param location 句柄
     * @param texture  纹理id
     * @param index    索引
     */
    public static void bindTexture(int location, int texture, int index) {
        bindTexture(location, texture, index, GLES20.GL_TEXTURE_2D);
    }

    /**
     * 绑定纹理
     * @param location    句柄
     * @param texture     纹理值
     * @param index       绑定的位置
     * @param textureType 纹理类型
     */
    public static void bindTexture(int location, int texture, int index, int textureType) {
        // 最多支持绑定32个纹理
        if (index > 31) {
            throw new IllegalArgumentException("index must be no more than 31!");
        }
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + index);
        GLES20.glBindTexture(textureType, texture);
        GLES20.glUniform1i(location, index);
    }


}
