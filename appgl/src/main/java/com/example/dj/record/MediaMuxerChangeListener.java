package com.example.dj.record;

public interface MediaMuxerChangeListener {
    /**
     * 音视频合成状态回调 开始 -- 停止
     *
     * @param type int
     */
    void onMediaMuxerChangeListener(int type);
}
