package com.example.dj.appgl.camera.base;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;

import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

import java.io.IOException;

import static android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK;

/**
 * Created by Administrator on 2017-07-13.
 */

public class CameraManegerXx implements LifecycleObserver,ImageAnalysis.Analyzer {

    private Camera mCamera;
    private Preview.OnPreviewOutputUpdateListener listener;
    private CameraX.LensFacing currentFacing = CameraX.LensFacing.BACK;
    private HandlerThread handlerThread;

    public CameraManegerXx(){

    }

    public CameraManegerXx(LifecycleOwner lifecycleOwner, Preview.OnPreviewOutputUpdateListener listener){
        this.listener = listener;
        handlerThread = new HandlerThread("Analyze-thread");
        handlerThread.start();
        lifecycleOwner.getLifecycle().addObserver(this);
        CameraX.bindToLifecycle(lifecycleOwner, getPreView(), getImageAnalysis());
    }

    private ImageAnalysis getImageAnalysis() {
        ImageAnalysisConfig imageAnalysisConfig = new ImageAnalysisConfig.Builder()
                .setCallbackHandler(new Handler(handlerThread.getLooper()))
                .setLensFacing(currentFacing).setTargetResolution(new Size(640, 480))
                .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
                .build();
        ImageAnalysis imageAnalysis = new ImageAnalysis(imageAnalysisConfig);
        imageAnalysis.setAnalyzer(this);
        return imageAnalysis;
    }

    private Preview getPreView() {
        // 分辨率并不是最终的分辨率，CameraX会自动根据设备的支持情况，结合你的参数，设置一个最为接近的分辨率
        PreviewConfig previewConfig = new PreviewConfig.Builder()
                .setTargetResolution(new Size(640, 480))
                .setLensFacing(currentFacing) //前置或者后置摄像头
                .build();
        Preview preview = new Preview(previewConfig);
        preview.setOnPreviewOutputUpdateListener(listener);
        return preview;
    }

    @Override
    public void analyze(ImageProxy image, int rotationDegrees) {
//        byte[] bytes = ImageUtils.getBytes(image);
//        synchronized (this) {
//            face = faceTracker.detect(bytes, image.getWidth(), image.getHeight(), rotationDegrees);
//        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy(LifecycleOwner owner) {
        owner.getLifecycle().removeObserver(this);
    }

    public void OpenCamera(SurfaceTexture surfaceTexture) {
        try {
            mCamera = Camera.open(CAMERA_FACING_BACK);
//            mCamera = Camera.open(CAMERA_FACING_BACK);
            mCamera.setPreviewTexture(surfaceTexture);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
