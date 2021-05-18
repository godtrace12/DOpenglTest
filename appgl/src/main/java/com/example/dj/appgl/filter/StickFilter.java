package com.example.dj.appgl.filter;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLES30;

import com.example.dj.appgl.R;
import com.example.dj.appgl.filter.base.AbstractFboFilter2;
import com.example.dj.appgl.filter.base.FilterChain;
import com.example.dj.appgl.filter.base.FilterContext;
import com.example.dj.appgl.util.TextureUtils;

public class StickFilter extends AbstractFboFilter2 {
    private Bitmap bizi;
    private int[] textures;

    public StickFilter(Context context) {
        super(R.raw.vertex_base_shader, R.raw.fragment_base_shader);
        textures = new int[1];
        textures[0] = TextureUtils.loadTexture(context,R.drawable.world_map);

    }


    @Override
    public void afterDraw() {
//        super.afterDraw();

        //开启混合模式
        GLES30.glEnable(GLES20.GL_BLEND);
        GLES30.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);


        GLES30.glViewport(500,500,200,200);
        GLES30.glUseProgram(mProgram);
        vertexBuffer.position(0);
        GLES30.glVertexAttribPointer(vPositionCoordLoc,2,GLES30.GL_FLOAT,false,0,vertexBuffer);
        GLES30.glEnableVertexAttribArray(vPositionCoordLoc);

        //纹理坐标是xy 所以数据size是 2
        textureBuffer.position(0);
        GLES30.glVertexAttribPointer(vTextureCoordLoc, 2, GLES30.GL_FLOAT, false, 0, textureBuffer);
        //启用顶点颜色句柄
        GLES30.glEnableVertexAttribArray(vTextureCoordLoc);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        //绑定纹理
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,textures[0]);
        // 0: 图层ID  GL_TEXTURE0
        // GL_TEXTURE1 ， 1
        GLES30.glUniform1i(vTextureLoc,0);

        //通知画画
        GLES30.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES30.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        //关闭混合模式
        GLES30.glDisable(GLES20.GL_BLEND);

    }

}
