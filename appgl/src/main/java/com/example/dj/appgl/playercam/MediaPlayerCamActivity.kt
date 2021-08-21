package com.example.dj.appgl.playercam

import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.view.SurfaceHolder
import androidx.appcompat.app.AppCompatActivity
import com.example.dj.appgl.R
import kotlinx.android.synthetic.main.activity_media_player.*

class MediaPlayerCamActivity : AppCompatActivity() {
    private val videoUrl:String = "/storage/emulated/0/Android/data/aom.example.dj.appgl/files/big_buck_bunny.mp4"
    //-------------------- opengl 渲染相关 -----------
    protected var renderer: MediaCamGLRenderer? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_player_cam)
        setupViews()
        initViews()
    }

    private fun setupViews() {
//        gv_surface.setEGLContextClientVersion(3)

    }

    private fun initViews() {
        btn_glplay.setOnClickListener {

        }
    }






}