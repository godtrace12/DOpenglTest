package com.example.dj.appgl.camera.quardraw;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dj.appgl.R;
import com.example.dj.appgl.camera.CameraGLSurface;
import com.example.dj.appgl.camera.object.TrianCamColorRender;
import com.example.dj.appgl.camera.object.TrianCamTextureRender;

public class CameraDrawActivity extends AppCompatActivity {
    private static final String TAG = "CameraActivity";
    private CameraDrawGLSurface glSurface;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_draw);
        glSurface = findViewById(R.id.glSurface);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(1,1,1,"颜色三角形");
        menu.add(1,2,2,"纹理三角形");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Log.e(TAG, "onOptionsItemSelected: itemId="+item.getItemId());
        switch (item.getItemId()){
            case 1:
                glSurface.setObjectRender(new TrianCamColorRender());
                break;
            case 2:
                glSurface.setObjectRender(new TrianCamTextureRender());
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}