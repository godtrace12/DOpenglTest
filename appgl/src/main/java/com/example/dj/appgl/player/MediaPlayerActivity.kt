package com.example.dj.appgl.player

import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.SurfaceHolder
import com.example.dj.appgl.R
import kotlinx.android.synthetic.main.activity_media_player.*

class MediaPlayerActivity : AppCompatActivity() {
    lateinit var mPlayer:MediaPlayer
    val videoUrl:String = "/storage/emulated/0/Android/data/aom.example.dj.appgl/files/big_buck_bunny.mp4"
    lateinit var holder:SurfaceHolder
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_player)
        mPlayer = MediaPlayer()

        initViews()
    }

    private fun initViews() {
        btn_play.setOnClickListener {
            startPlay()
        }
    }

    private fun startPlay(){
        mPlayer.reset()
        mPlayer.setDataSource(videoUrl)
        //安卓6.0以后
        if(Build.VERSION.SDK_INT >=23){
            //配置播放器
            var aa = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                    .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                    .build();
            mPlayer.setAudioAttributes(aa);
        }else{
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
        holder = sv_surface.holder
        mPlayer.setDisplay(holder)
        mPlayer.prepare()
        mPlayer.start()

    }






}