package com.example.dj.appgl;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.dj.appgl.basicdraw.CubicSampleActivity;
import com.example.dj.appgl.basicdraw.TriangleSampleActivity;
import com.example.dj.appgl.camera.CameraActivity;
import com.example.dj.appgl.camera.CameraRecordActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnBall;
    private Button btnBasic;
    private Button btnCamera;
    private Button btnRecordCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
    }

    private void findViews() {
        btnBall = findViewById(R.id.btnBall);
        btnBall.setOnClickListener(this);
        btnBasic = findViewById(R.id.btnBasic);
        btnBasic.setOnClickListener(this);
        btnCamera = findViewById(R.id.btnCamera);
        btnCamera.setOnClickListener(this);
        btnRecordCamera = findViewById(R.id.btnRecordCamera);
        btnRecordCamera.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.btnBall){
            Intent intent = new Intent(MainActivity.this, CubicSampleActivity.class);
            startActivity(intent);
        }else if (viewId == R.id.btnBasic){
            Intent intent = new Intent(MainActivity.this, TriangleSampleActivity.class);
            startActivity(intent);
        }else if (viewId == R.id.btnCamera){
            Intent intent = new Intent(MainActivity.this, CameraActivity.class);
            startActivity(intent);
        }else if(viewId == R.id.btnRecordCamera){
            Intent intent = new Intent(MainActivity.this, CameraRecordActivity.class);
            startActivity(intent);
        }
    }
}
