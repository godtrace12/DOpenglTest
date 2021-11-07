package com.example.dj.appgl.opencv

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.dj.appgl.R
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.opencv.imgproc.Imgproc.COLOR_BGRA2GRAY
import org.opencv.objdetect.CascadeClassifier
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class OpenCVActivity : AppCompatActivity() {
    var ivPic: ImageView? = null
    var btnDetect: Button? = null
    var srcPhotoBitmap:Bitmap? = null

    var mLoadCallback:BaseLoaderCallback? = null
    var mCascadeFile: File? = null
    private var mJavaDetector: CascadeClassifier? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_c_v)
        initViews()
        initOpenCV()
    }

    private fun initViews() {
        ivPic = findViewById(R.id.ivPic)
        btnDetect = findViewById(R.id.btnDetect)
        btnDetect!!.setOnClickListener {
            startDetect()
        }
    }

    private fun initOpenCV(){
        mLoadCallback = object:BaseLoaderCallback(this){
            override fun onManagerConnected(status: Int) {
                super.onManagerConnected(status)
                when(status){
                    LoaderCallbackInterface.SUCCESS ->
                        // Load native library after(!) OpenCV initialization
                    {
                        try {

//                            System.loadLibrary("detection_based_tracker")
                            var isStream:InputStream = resources.openRawResource(R.raw.lbpcascade_frontalface)
                            val cascadeDir = getDir("cascade", Context.MODE_PRIVATE)
                            mCascadeFile = File(cascadeDir,"lbpcascade_frontalface.xml")
                            var os:FileOutputStream = FileOutputStream(mCascadeFile)

                            val buffer = ByteArray(4096)
                            var bytesRead: Int
                            bytesRead = isStream.read(buffer)
                            while (isStream.read(buffer).also { bytesRead = it } != -1){
                                os.write(buffer,0,bytesRead)
                            }
                            isStream.close()
                            os.close()
                            var fileAbsolutePath = "/data/user/0/aom.example.dj.appgl/app_cascade/lbpcascade_frontalface.xml"
                            mJavaDetector = CascadeClassifier(mCascadeFile!!.absolutePath)
//                            mJavaDetector = CascadeClassifier(fileAbsolutePath)
                            if(mJavaDetector!!.empty()){
                                mJavaDetector = null
                            }

                        }catch (e:IOException){
                            Log.e(Companion.TAG, "onManagerConnected: "+e)
                        }
                    }
                }
            }
        }

//        CascadeClassifier.
    }

    private fun startDetect(){
        srcPhotoBitmap = ((ivPic!!.drawable) as BitmapDrawable).bitmap
        faceDetect(srcPhotoBitmap!!)
    }

    private fun faceDetect(photo:Bitmap):Bitmap{
        var matSrc = Mat()
        var matDst = Mat()
        var matGray = Mat()
        Utils.bitmapToMat(photo,matSrc)//转换后已经变成了BGRA？
        Imgproc.cvtColor(matSrc,matGray, COLOR_BGRA2GRAY)
        var faces = MatOfRect()
        mJavaDetector!!.detectMultiScale(matGray,faces,1.1,2,2,Size(30.0, 30.0),Size())
        var faceList:ArrayList<Rect> = faces.toList() as ArrayList<Rect>
        matSrc.copyTo(matDst)
        if(faceList.size >0){
            for (rect in faceList){
                Imgproc.rectangle(matDst,rect.tl(),rect.br(), Scalar(255.0,0.0,0.0,255.0),4,8,0)
            }
        }
        var dstBitmap = Bitmap.createBitmap(photo.width,photo.height,Bitmap.Config.ARGB_8888)
        return dstBitmap
    }

    override fun onResume() {
        super.onResume()
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization")
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoadCallback)
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!")
            mLoadCallback!!.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }

    companion object {
        private const val TAG = "OpenCVActivity"
    }

}