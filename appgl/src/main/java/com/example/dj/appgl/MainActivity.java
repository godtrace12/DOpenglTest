package com.example.dj.appgl;

import androidx.appcompat.app.AppCompatActivity;

import android.app.NativeActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.dj.appgl.basicdraw.CubicSampleActivity;
import com.example.dj.appgl.basicdraw.TriangleSampleActivity;
import com.example.dj.appgl.camera.CameraActivity;
import com.example.dj.appgl.camera.CameraRecordActivity;
import com.example.dj.appgl.camera3d.Camera3DActivity;
import com.example.dj.appgl.fbo.TriangleFboActivity;
import com.example.dj.appgl.fbo.RectFboTestActivity;
import com.example.dj.appgl.light.FengLightActivity;
import com.example.dj.appgl.light.TextureLightActivity;
import com.example.dj.appgl.model.ModeBglLoadActivity;
import com.example.dj.appgl.model.ModelLoadActivity;
import com.example.dj.appgl.player.MediaPlayerActivity;
import com.example.dj.appgl.skybox.SkyboxActivity;
import com.example.dj.media.DPlayer;
import com.example.dj.appgl.nativegl.NativeGLActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnBall;
    private Button btnBasic;
    private Button btnCamera;
    private Button btnRecordCamera;
    private Button btnFBO;
    private Button btnFBOTest;
    private Button btnModel;
    private Button btnModelBg;
    private Button btnSkybox;
    private Button btnCamera3D;
    private Button btnFengLight;
    private Button btnTextureLight;
    private Button btnNative;
    private Button btnMediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String natvieStr = DPlayer.getNativeString();
        Log.e("dj", "onCreate: "+natvieStr);
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
        btnFBO = findViewById(R.id.btnFBO);
        btnFBO.setOnClickListener(this);
        btnFBOTest = findViewById(R.id.btnFBOTest);
        btnFBOTest.setOnClickListener(this);
        btnModel = findViewById(R.id.btnModel);
        btnModel.setOnClickListener(this);
        btnModelBg = findViewById(R.id.btnModelBg);
        btnModelBg.setOnClickListener(this);
        btnSkybox = findViewById(R.id.btnSkybox);
        btnSkybox.setOnClickListener(this);
        btnCamera3D = findViewById(R.id.btnCamera3D);
        btnCamera3D.setOnClickListener(this);
        btnFengLight = findViewById(R.id.btnFengLight);
        btnFengLight.setOnClickListener(this);
        btnTextureLight = findViewById(R.id.btnTextureLight);
        btnTextureLight.setOnClickListener(this);
        btnNative = findViewById(R.id.btnNative);
        btnNative.setOnClickListener(this);
        btnMediaPlayer = findViewById(R.id.btnMediaPlayer);
        btnMediaPlayer.setOnClickListener(this);
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
        }else if(viewId == R.id.btnFBO){
            Intent intent = new Intent(MainActivity.this, TriangleFboActivity.class);
            startActivity(intent);
        }else if(viewId == R.id.btnFBOTest){
            Intent intent = new Intent(MainActivity.this, RectFboTestActivity.class);
            startActivity(intent);
        }else if(viewId == R.id.btnModel){
            Intent intent = new Intent(MainActivity.this, ModelLoadActivity.class);
            startActivity(intent);
        }else if(viewId == R.id.btnModelBg){
            Intent intent = new Intent(MainActivity.this, ModeBglLoadActivity.class);
            startActivity(intent);
        }else if(viewId == R.id.btnSkybox){
            Intent intent = new Intent(MainActivity.this, SkyboxActivity.class);
            startActivity(intent);
        }else if(viewId == R.id.btnCamera3D){
            Intent intent = new Intent(MainActivity.this, Camera3DActivity.class);
            startActivity(intent);
        }else if(viewId == R.id.btnFengLight){
            Intent intent = new Intent(MainActivity.this, FengLightActivity.class);
            startActivity(intent);
        }else if(viewId == R.id.btnTextureLight){
            Intent intent = new Intent(MainActivity.this, TextureLightActivity.class);
            startActivity(intent);
        }else if(viewId == R.id.btnNative){
            Intent intent = new Intent(MainActivity.this, NativeGLActivity.class);
            startActivity(intent);
        }else if(viewId == R.id.btnMediaPlayer){
            Intent intent = new Intent(MainActivity.this, MediaPlayerActivity.class);
            startActivity(intent);
        }
    }
}
