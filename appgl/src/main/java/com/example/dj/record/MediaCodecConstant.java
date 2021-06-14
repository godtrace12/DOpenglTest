package com.example.dj.record;

public class MediaCodecConstant {
    //标记track下标
    public static int audioTrackIndex;
    public static int videoTrackIndex;
    //标记MediaMuxer是否开启
    public static boolean encodeStart;
    //开始合成
    public static final int MUXER_START = 1;
    //结束合成
    public static final int MUXER_STOP = 2;

    //标记MediaCodec -- audioCodec 对象是否退出释放
    public static boolean audioStop;
    //标记MediaCodec -- videoCodec 对象是否退出释放
    public static boolean videoStop;

    //标记MediaMuxer是否开启
}
