package com.example.dj.record;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.opengl.EGLContext;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MediaRecorder {
    private static final String TAG = "MediaRecorder";
    private final int mWidth;
    private final int mHeight;
    private final String mPath;
    private final Context mContext;

    private MediaCodec mMediaCodec;
    private Surface mSurface;
    private EGLContext mGlContext;
    private MediaMuxer mMuxer;
    private Handler mHandler;
    private boolean isStart;
    private int track;
    private float mSpeed;
    private long mLastTimeStamp;
    private EGLEnv eglEnv;
    //2------------- 音频录制相关  --------------
    private AudioCodeThread2 audioCodeThread;
    //3----------- 音频采集 ------------
    private AudioCapture audioCapture;


    public MediaRecorder(Context context, String path, EGLContext glContext, int width, int
            height) {
        mContext = context.getApplicationContext();
        mPath = path;
        Log.e(TAG, "MediaRecorder: mPath="+mPath);
        mWidth = width;
        mHeight = height;
        mGlContext = glContext;
    }

    public void start(float speed) throws IOException {
        mSpeed = speed;
        MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC,
                mWidth, mHeight);
        //颜色空间 从 surface当中获得
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities
                .COLOR_FormatSurface);
        int bitrate = 1080 *2340*4;
        //码率
        format.setInteger(MediaFormat.KEY_BIT_RATE, 1500_000);
        //帧率
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
        //关键帧间隔
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

        //混合器 (复用器) 将编码的h.264封装为mp4
        mMuxer = new MediaMuxer(mPath,
                MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

        startAudioCaptureCodec();

        //开启编码
        mMediaCodec.start();
//        mAudioCodec.start();
//        audioRecord.startRecording();

        //創建OpenGL 的 環境
        HandlerThread handlerThread = new HandlerThread("codec-gl");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                // 创建EGL环境
                eglEnv = new EGLEnv(mContext,mGlContext, mSurface,mWidth, mHeight);
                isStart = true;
            }
        });

    }

    // 音频采集及录制
    private void startAudioCaptureCodec(){
        // 音频采集
        audioCapture = new AudioCapture();
        audioCapture.start();

        MediaCodecConstant.audioStop = false;
        MediaCodecConstant.videoStop = false;

        audioCodeThread = new AudioCodeThread2(mMuxer, new MediaMuxerChangeListener() {
            @Override
            public void onMediaMuxerChangeListener(int type) {
                Log.e(TAG, "onMediaMuxerChangeListener: ");
                // TODO: 6/14/21  开始进行音频采集
                if(type == MediaCodecConstant.MUXER_START){
                    if(audioCapture.getCaptureListener() == null){
                        audioCapture.setCaptureListener(new AudioCapture.AudioCaptureListener() {
                            @Override
                            public void onCaptureListener(byte[] audioSource, int audioReadSize) {
//                            Log.e(TAG, "onCaptureListener: ");
                                // 塞给音频编码线程
                                // TODO: 6/14/21 判断状态
                                if (MediaCodecConstant.audioStop || MediaCodecConstant.videoStop) {
                                    return;
                                }
                                audioCodeThread.setPcmSource(audioSource,audioReadSize);
                            }
                        });
                    }
                }
            }
        });
        audioCodeThread.initAudioRecord();

        // 配置声音解码及播放
//        initAudioRecord();
        MediaCodecConstant.videoTrackIndex = -1;
        MediaCodecConstant.audioTrackIndex = -1;
        MediaCodecConstant.encodeStart = false;
        audioCodeThread.startCodec();
    }

    private void onMediaMuxerChangeListener(int type){
        if(type == MediaCodecConstant.MUXER_START){
            if(audioCapture.getCaptureListener() == null){
                audioCapture.setCaptureListener(new AudioCapture.AudioCaptureListener() {
                    @Override
                    public void onCaptureListener(byte[] audioSource, int audioReadSize) {
//                            Log.e(TAG, "onCaptureListener: ");
                        // 塞给音频编码线程
                        // TODO: 6/14/21 判断状态
                        if (MediaCodecConstant.audioStop || MediaCodecConstant.videoStop) {
                            return;
                        }
                        audioCodeThread.setPcmSource(audioSource,audioReadSize);
                    }
                });
            }
        }
    }


    public void fireFrame(final int textureId, final long timestamp) {
//        Log.e(TAG, "fireFrame: ");
        if (!isStart) {
//            Log.e(TAG, "fireFrame: not start");
            return;
        }
        //录制用的opengl已经和handler的线程绑定了 ，所以需要在这个线程中使用录制的opengl
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                //画画
                eglEnv.draw(textureId,timestamp);
                codec(false);
//                codecAudio(false);
            }
        });
    }


    private void codec(boolean endOfStream) {
//        Log.e(TAG, "codec: run codec");
        //给个结束信号
        if (endOfStream) {
            Log.e(TAG, "codec: endOfStream");
            mMediaCodec.signalEndOfInputStream();
        }
        while (true) {
            Log.e(TAG, "codec: in the while loop--------");
            //获得输出缓冲区 (编码后的数据从输出缓冲区获得)
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int encoderStatus = mMediaCodec.dequeueOutputBuffer(bufferInfo, 10_000);

            //需要更多数据
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                Log.e(TAG, "codec: video tray again later");
                //如果是结束那直接退出，否则继续循环
                if (!endOfStream) {
                    break;
                }
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                Log.e(TAG, "codec: add video track");
                //输出格式发生改变  第一次总会调用所以在这里开启混合器
                MediaFormat newFormat = mMediaCodec.getOutputFormat();
                track = mMuxer.addTrack(newFormat);
                MediaCodecConstant.videoTrackIndex = track;
                Log.e(TAG, "run: video formatchanged videoTrackIndex="+MediaCodecConstant.videoTrackIndex+" audioTrackIndex="+MediaCodecConstant.audioTrackIndex);
                if(MediaCodecConstant.audioTrackIndex != -1){
                    Log.e(TAG, "codec: start video muxer");
                    mMuxer.start();
                    MediaCodecConstant.encodeStart = true;
                    onMediaMuxerChangeListener(MediaCodecConstant.MUXER_START);
                }
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                Log.e(TAG, "codec: video ignore buffer change");
                //可以忽略
            } else {
                Log.e(TAG, "codec: read code video before");
                if(!MediaCodecConstant.encodeStart){
                    SystemClock.sleep(10);
                    continue;
                }
                Log.e(TAG, "codec: read code video");
                //调整时间戳
                bufferInfo.presentationTimeUs = (long) (bufferInfo.presentationTimeUs / mSpeed);
                //有时候会出现异常 ： timestampUs xxx < lastTimestampUs yyy for Video track
                if (bufferInfo.presentationTimeUs <= mLastTimeStamp) {
                    bufferInfo.presentationTimeUs = (long) (mLastTimeStamp + 1_000_000 / 25 / mSpeed);
                }
                mLastTimeStamp = bufferInfo.presentationTimeUs;

                //正常则 encoderStatus 获得缓冲区下标
                ByteBuffer encodedData = mMediaCodec.getOutputBuffer(encoderStatus);
                //如果当前的buffer是配置信息，不管它 不用写出去
                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    bufferInfo.size = 0;
                }
                if (bufferInfo.size != 0) {
                    //设置从哪里开始读数据(读出来就是编码后的数据)
                    encodedData.position(bufferInfo.offset);
                    //设置能读数据的总长度
                    encodedData.limit(bufferInfo.offset + bufferInfo.size);
                    //写出为mp4
                    mMuxer.writeSampleData(track, encodedData, bufferInfo);
                }
                // 释放这个缓冲区，后续可以存放新的编码后的数据啦
                mMediaCodec.releaseOutputBuffer(encoderStatus, false);
                // 如果给了结束信号 signalEndOfInputStream
                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    Log.e(TAG, "codec: break encode");
                    break;
                }
            }
        }
    }


    public void stop() {
        // 释放
        isStart = false;
        MediaCodecConstant.encodeStart = false;
        MediaCodecConstant.videoStop = true;
//        mMediaCodec.stop();
//        mMediaCodec.release();
//        mMediaCodec = null;
//        MediaCodecConstant.videoStop = true;
        // 音频采集
        audioCapture.stop();
        audioCodeThread.stopCodec();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mMediaCodec.stop();
            mMediaCodec.release();
            mMediaCodec = null;
            MediaCodecConstant.videoStop = true;

/*                MediaCodecConstant.encodeStart = false;
//                codec(true);
//                codecAudio(true);

                mMediaCodec.stop();
                mMediaCodec.release();
                mMediaCodec = null;
                MediaCodecConstant.videoStop = true;
                // 音频采集
//                audioRecord.stop();
//                audioRecord.release();
//                audioRecord = null;
//                // 音频编码
//                mAudioCodec.stop();
//                mAudioCodec.release();
//                mAudioCodec = null;
                audioCapture.stop();
                audioCodeThread.stopCodec();*/
                // 混频器关闭
//                mMuxer.stop();
//                mMuxer.release();
                eglEnv.release();
                eglEnv = null;
//                mMuxer = null;
                mSurface = null;
                mHandler.getLooper().quitSafely();
                mHandler = null;
            }
        });
    }

}
