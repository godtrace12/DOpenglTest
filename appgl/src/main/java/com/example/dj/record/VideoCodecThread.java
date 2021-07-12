package com.example.dj.record;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import android.view.Surface;

import com.example.dj.record.bean.MediaCodecState;

import java.io.IOException;
import java.nio.ByteBuffer;

//import static com.example.dj.record.MediaCodecConstant.audioTrackIndex;
//import static com.example.dj.record.MediaCodecConstant.videoTrackIndex;

/**
 * 视频写入thread,进行视频编码参数初始化以及编码
 */
public class VideoCodecThread extends Thread {

    private static final String TAG = "AudioRecorder";

    private MediaCodec mMediaCodec;
    private MediaCodec.BufferInfo bufferInfo;
    private MediaMuxer mediaMuxer;

    private boolean isStop;

    private long pts;

    private MediaMuxerChangeListener listener;
    // 视频画面长宽
    private int mWidth;
    private int mHeight;
    private Surface mSurface;
    private MediaCodecState mCodecState;

    public VideoCodecThread( MediaMuxer mediaMuxer,int width,int height,
                             MediaCodecState codecState, MediaMuxerChangeListener listener) {
        this.mediaMuxer = mediaMuxer;
        this.listener = listener;
        pts = 0;
        this.mCodecState = codecState;
        mCodecState.videoTrackIndex = -1;
        this.mWidth = width;
        this.mHeight = height;
    }


    public Surface getSurface(){
        return mSurface;
    }

    public Surface initVideoRecord() throws IOException {
        MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC,
                mWidth, mHeight);
        //颜色空间 从 surface当中获得
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities
                .COLOR_FormatSurface);
        int bitrate = mWidth *mHeight*2;
        //码率
        format.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);
        //帧率
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
        //关键帧间隔-每秒关键帧数
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            format.setInteger(MediaFormat.KEY_PROFILE, MediaCodecInfo.CodecProfileLevel.AVCProfileHigh);
            format.setInteger(MediaFormat.KEY_LEVEL, MediaCodecInfo.CodecProfileLevel.AVCLevel31);
        }
        //创建编码器
        mMediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
        //配置编码器
        mMediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        //这个surface显示的内容就是要编码的画面
        mSurface = mMediaCodec.createInputSurface();
        bufferInfo = new MediaCodec.BufferInfo();
        return mSurface;
    }


    @Override
    public void run() {
        super.run();
        isStop = false;
        mMediaCodec.start();
        while (true) {
            if (isStop) {
                mMediaCodec.stop();
                mMediaCodec.release();
                mMediaCodec = null;
                mCodecState.videoStop = true;

                    if (mCodecState.audioStop && mediaMuxer != null) {
                        Log.e(TAG, "codecVideo: stop mux");
                        if(mCodecState.muxStop){
//                            listener.onMediaMuxerChangeListener(MediaCodecState.MUXER_STOP);
                            break;
                        }
                        mediaMuxer.stop();
                        mCodecState.muxStop = true;
                        mediaMuxer.release();
                        mediaMuxer = null;
//                    listener.onMediaMuxerChangeListener(MediaCodecConstant.MUXER_STOP);
                        break;
                    }
            }

            if (mMediaCodec == null)
                break;
            int outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 0);
            if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                mCodecState.videoTrackIndex = mediaMuxer.addTrack(mMediaCodec.getOutputFormat());
                Log.e(TAG, "run: video formatchanged videoTrackIndex="+mCodecState.videoTrackIndex+" audioTrackIndex="+mCodecState.audioTrackIndex);
                if (mCodecState.audioTrackIndex != -1) {
                    Log.e(TAG, "run: video muxer start");
                    mediaMuxer.start();
                    //标识编码开始
                    mCodecState.encodeStart = true;
                    // 可以考虑不回调
                    listener.onMediaMuxerChangeListener(MediaCodecState.MUXER_START);
                }
            } else if(outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER){

            }else {
                while (outputBufferIndex >= 0) {
                    if (!mCodecState.encodeStart) {
                        Log.d(TAG, "run: 线程延迟");
                        SystemClock.sleep(10);
                        continue;
                    }

                    ByteBuffer outputBuffer = mMediaCodec.getOutputBuffer(outputBufferIndex);
                    outputBuffer.position(bufferInfo.offset);
                    outputBuffer.limit(bufferInfo.offset + bufferInfo.size);

                    if (pts == 0) {
                        pts = bufferInfo.presentationTimeUs;
                    }
                    bufferInfo.presentationTimeUs = bufferInfo.presentationTimeUs - pts;
                    mediaMuxer.writeSampleData(mCodecState.videoTrackIndex, outputBuffer, bufferInfo);
                    Log.d(TAG, "视频秒数时间戳 = " + bufferInfo.presentationTimeUs / 1000000.0f);
//                    if (bufferInfo != null)
//                        listener.onMediaInfoListener((int) (bufferInfo.presentationTimeUs / 1000000));

                    mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                    outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 0);
                    Log.e(TAG, "codecVideoMM: outputBufferIndex="+outputBufferIndex);

                }
            }
        }
    }

    public void startCodec(){
        isStop = false;
        start();
    }

    public void stopVideoCodec() {
        isStop = true;
        mCodecState.videoStop = true;
    }
}
