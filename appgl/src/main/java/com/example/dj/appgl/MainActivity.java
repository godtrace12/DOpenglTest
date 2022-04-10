package com.example.dj.appgl;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.dj.appgl.ball.EarthTextureActivity;
import com.example.dj.appgl.basicdraw.CubicSampleActivity;
import com.example.dj.appgl.basicdraw.TriangleSampleActivity;
import com.example.dj.appgl.basicdraw.axissample.CubixAxisSampleActivity;
import com.example.dj.appgl.camera.CameraActivity;
import com.example.dj.appgl.camera.CameraRecordActivity;
import com.example.dj.appgl.camera.quardraw.CameraDrawActivity;
import com.example.dj.appgl.camera3d.Camera3DActivity;
import com.example.dj.appgl.fbo.TriangleFboActivity;
import com.example.dj.appgl.fbo.RectFboTestActivity;
import com.example.dj.appgl.geometryshader.GeometryShaderActivity;
import com.example.dj.appgl.instance.InstancingSampleActivity;
import com.example.dj.appgl.light.FengLightActivity;
import com.example.dj.appgl.light.TextureLightActivity;
import com.example.dj.appgl.model.ModeBglLoadActivity;
import com.example.dj.appgl.model.ModelLoadActivity;
import com.example.dj.appgl.mrt.MrtRenderActivity;
import com.example.dj.appgl.nativegl.NativeMainActivity;
import com.example.dj.appgl.opencv.OpenCVActivity;
import com.example.dj.appgl.opencv.OpenCvCameraActivity;
import com.example.dj.appgl.particles.cubicexplose.CubicExploseParticlesActivity;
import com.example.dj.appgl.particles.fountain.FountainParticlesActivity;
import com.example.dj.appgl.particles.transformfeedback.TransformfeedbackActivity;
import com.example.dj.appgl.player.MediaPlayerActivity;
import com.example.dj.appgl.playercam.MediaPlayerCamActivity;
import com.example.dj.appgl.skybox.SkyboxActivity;
import com.example.dj.appgl.wave.WaveSampleActivity;
import com.example.dj.media.DPlayer;
import com.example.dj.appgl.nativegl.NativeGLActivity;
import com.tbruyelle.rxpermissions2.RxPermissions;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

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
    private Button btnMediaPlayerCam;
    private Button btnNativeCrash;
    private Button btnOpenCV;
    private Button btnWave;
    private Button btnCameraDraw;
    private Button btnEarthBall;
    private Button btnBasicPerspec;
    private Button btnMRTRender;
    private Button btnOpenCVCamera;
    private Button btnInstancing;
    private Button btnGeometry;
    private Button btnTransformFeedback;
    private Button btnCubicParticles;
    private Button btnFountainParticles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String natvieStr = DPlayer.getNativeString();
        Log.e("dj", "onCreate: "+natvieStr);
        findViews();
        requestPermission();
    }

    private void requestPermission(){
        String[] permissionsGroup = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA};
        RxPermissions rxPermissions = new RxPermissions(MainActivity.this);
        rxPermissions.request(permissionsGroup).subscribe(new Observer<Boolean>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull Boolean aBoolean) {
                if(aBoolean){
                    Toast.makeText(MainActivity.this,"已获取权限",Toast.LENGTH_SHORT).show();
                }else{
                    //只有用户拒绝开启权限，且选了不再提示时，才会走这里，否则会一直请求开启
                    Toast.makeText(MainActivity.this, "主人，我被禁止啦，去设置权限设置那把我打开哟", Toast.LENGTH_LONG)
                            .show();
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
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
        btnMediaPlayerCam = findViewById(R.id.btnMediaPlayerCam);
        btnMediaPlayerCam.setOnClickListener(this);
        btnNativeCrash = findViewById(R.id.btnNativeCrash);
        btnNativeCrash.setOnClickListener(this);
        btnOpenCV = findViewById(R.id.btnOpenCV);
        btnOpenCV.setOnClickListener(this);
        btnWave = findViewById(R.id.btnWave);
        btnWave.setOnClickListener(this);
        btnCameraDraw = findViewById(R.id.btnCameraDraw);
        btnCameraDraw.setOnClickListener(this);
        btnEarthBall = findViewById(R.id.btnEarthBall);
        btnEarthBall.setOnClickListener(this);
        btnBasicPerspec = findViewById(R.id.btnBasicPerspec);
        btnBasicPerspec.setOnClickListener(this);
        btnMRTRender = findViewById(R.id.btnMRTRender);
        btnMRTRender.setOnClickListener(this);
        btnOpenCVCamera = findViewById(R.id.btnOpenCVCamera);
        btnOpenCVCamera.setOnClickListener(this);
        btnInstancing = findViewById(R.id.btnInstancing);
        btnInstancing.setOnClickListener(this);
        btnGeometry = findViewById(R.id.btnGeometry);
        btnGeometry.setOnClickListener(this);
        btnTransformFeedback = findViewById(R.id.btnTransformFeedback);
        btnTransformFeedback.setOnClickListener(this);
        btnCubicParticles = findViewById(R.id.btnCubicParticles);
        btnCubicParticles.setOnClickListener(this);
        btnFountainParticles = findViewById(R.id.btnFountainParticles);
        btnFountainParticles.setOnClickListener(this);
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
        }else if(viewId == R.id.btnBasicPerspec){
            Intent  intent = new Intent(MainActivity.this, CubixAxisSampleActivity.class);
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
            Intent intent = new Intent(MainActivity.this, NativeMainActivity.class);
            startActivity(intent);
        }else if(viewId == R.id.btnMediaPlayer){
            Intent intent = new Intent(MainActivity.this, MediaPlayerActivity.class);
            startActivity(intent);
        }else if(viewId == R.id.btnMediaPlayerCam){
            Intent intent = new Intent(MainActivity.this, MediaPlayerCamActivity.class);
            startActivity(intent);
        }else if(viewId == R.id.btnNativeCrash){
//            CrashReport.testJavaCrash();
            DPlayer.nativeStringInit();
        }else if(viewId == R.id.btnOpenCV){
            Intent  intent = new Intent(MainActivity.this, OpenCVActivity.class);
            startActivity(intent);
        }else if(viewId == R.id.btnOpenCVCamera){
            Intent  intent = new Intent(MainActivity.this, OpenCvCameraActivity.class);
            startActivity(intent);
        }else if(viewId == R.id.btnWave){
            Intent  intent = new Intent(MainActivity.this, WaveSampleActivity.class);
            startActivity(intent);
        }else if(viewId == R.id.btnCameraDraw){
            Intent  intent = new Intent(MainActivity.this, CameraDrawActivity.class);
            startActivity(intent);
        }else if(viewId == R.id.btnEarthBall){
            Intent  intent = new Intent(MainActivity.this, EarthTextureActivity.class);
            startActivity(intent);
        }else if(viewId == R.id.btnMRTRender){
            Intent  intent = new Intent(MainActivity.this, MrtRenderActivity.class);
            startActivity(intent);
        }else if(viewId == R.id.btnInstancing){
            Intent  intent = new Intent(MainActivity.this, InstancingSampleActivity.class);
            startActivity(intent);
        }else if(viewId == R.id.btnGeometry){
            Intent  intent = new Intent(MainActivity.this, GeometryShaderActivity.class);
            startActivity(intent);
        }else if(viewId == R.id.btnTransformFeedback){
            Intent  intent = new Intent(MainActivity.this, TransformfeedbackActivity.class);
            startActivity(intent);
            Handler handler = new Handler();
        }else if(viewId == R.id.btnCubicParticles){
            Intent  intent = new Intent(MainActivity.this, CubicExploseParticlesActivity.class);
            startActivity(intent);
        }else if(viewId == R.id.btnFountainParticles){
            Intent  intent = new Intent(MainActivity.this, FountainParticlesActivity.class);
            startActivity(intent);
        }
    }
}
