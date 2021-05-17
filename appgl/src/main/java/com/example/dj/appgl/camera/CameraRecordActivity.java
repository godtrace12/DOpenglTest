package com.example.dj.appgl.camera;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dj.appgl.R;
import com.example.dj.appgl.camera.object.TrianCamColorRender;
import com.example.dj.appgl.camera.object.TrianCamTextureRender;

public class CameraRecordActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "CameraActivity";
    private CameraFilterGLSurface glSurface;
    private Button btnStart;
    private Button btnStop;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_camera);
        glSurface = findViewById(R.id.glSurface);
        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);
        btnStart.setOnClickListener(this);
        btnStop.setOnClickListener(this);
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
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if(viewId == R.id.btnStart){
            glSurface.startRecord();
        }else if(viewId == R.id.btnStop){
            glSurface.stopRecord();
        }
    }
}