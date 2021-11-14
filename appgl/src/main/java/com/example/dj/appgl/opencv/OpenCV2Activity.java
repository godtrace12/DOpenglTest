package com.example.dj.appgl.opencv;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.dj.appgl.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.samples.facedetect.DetectionBasedTracker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * 创建日期：11/14/21 11:54 AM
 *
 * @author daijun
 * @version 1.0
 * @des：
 */
public class OpenCV2Activity extends Activity {
    private static final String TAG = "OpenCV2Activity";
    private ImageView ivPic;
    private Button btnDetect;

    private File mCascadeFile;
    private CascadeClassifier mJavaDetector;
    private Bitmap srcPhotoBitmap = null;


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization
                    System.loadLibrary("detection_based_tracker");

                    try {
                        // load cascade file from application resources
                        InputStream is = getResources().openRawResource(org.opencv.samples.facedetect.R.raw.lbpcascade_frontalface);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        if (mJavaDetector.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier");
                            mJavaDetector = null;
                        } else
                            Log.e(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

                        cascadeDir.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }
                }

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_c_v);
        initViews();
    }

    private void initViews() {
        ivPic = findViewById(R.id.ivPic);
        btnDetect = findViewById(R.id.btnDetect);
        btnDetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDetect();
//                changePicToGray();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    // 进行人脸检测
    private void startDetect(){
        srcPhotoBitmap = ((BitmapDrawable)ivPic.getDrawable()).getBitmap();
        Bitmap faceBitmap = faceDetect(srcPhotoBitmap);
        ivPic.setImageBitmap(faceBitmap);
    }

    // 图片转换为灰度图片
    private void changePicToGray(){
        srcPhotoBitmap = ((BitmapDrawable)ivPic.getDrawable()).getBitmap();
        Bitmap grayBitmap = toGrayByOpencv(srcPhotoBitmap);
        ivPic.setImageBitmap(grayBitmap);
    }

    private Bitmap toGrayByOpencv(Bitmap srcBitmap){
        Mat mat = new Mat();
        Utils.bitmapToMat(srcBitmap,mat);
        Mat grayMat = new Mat();
        Imgproc.cvtColor(mat,grayMat,Imgproc.COLOR_BGR2GRAY,1);
        Utils.matToBitmap(grayMat,srcBitmap);
        return srcBitmap;
    }


    private Bitmap faceDetect(Bitmap photo){
        Mat matSrc = new Mat();
        Mat matDst = new Mat();
        Mat matGray = new Mat();
        Utils.bitmapToMat(photo,matSrc);
        Imgproc.cvtColor(matSrc,matGray,Imgproc.COLOR_BGRA2GRAY);
        MatOfRect faces = new MatOfRect();
        if(mJavaDetector != null){
            mJavaDetector.detectMultiScale(matGray,faces,1.05,3,0, new Size(30.0,30.0),new Size());
        }
        ArrayList<Rect> faceList = new ArrayList<>(faces.toList());
        matSrc.copyTo(matDst);
        if(faceList.size() >0){
            for (Rect rect:faceList) {
                Imgproc.rectangle(matDst,rect.tl(),rect.br(), new Scalar(255.0,0.0,0.0,255.0),4,8,0);
            }
        }
        Bitmap dstBitmap = Bitmap.createBitmap(photo.getWidth(),photo.getHeight(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(matDst,dstBitmap);
        matDst.release();
        matGray.release();
        matSrc.release();
        return dstBitmap;
    }

}
