package com.example.dj.record.bean

/**
 * 创建日期：6/19/21 11:25 AM
 * @author daijun
 * @version 1.0
 * @des：编码录制相关状态
 */
class MediaCodecState {
    // 视频轨
    @kotlin.jvm.JvmField
    @Volatile
    var videoTrackIndex: Int

    //音频轨标记track下标
    @kotlin.jvm.JvmField
    @Volatile var audioTrackIndex = 0

    //标记MediaMuxer是否开启
    @kotlin.jvm.JvmField
    @Volatile var encodeStart = false

    //标记MediaCodec -- audioCodec 对象是否退出释放
    @kotlin.jvm.JvmField
    @Volatile var audioStop = false

    //标记MediaCodec -- videoCodec 对象是否退出释放
    @kotlin.jvm.JvmField
    @Volatile var videoStop = false


    init {
        audioTrackIndex = 0
        videoTrackIndex = 0
    }


    companion object{
        @kotlin.jvm.JvmField
        val MUXER_START = 1
        //结束合成
        @kotlin.jvm.JvmField
        val MUXER_STOP = 2
    }

}