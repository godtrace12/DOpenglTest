package com.example.dj.record;

import android.content.Context;
import android.media.MediaMuxer;
import android.opengl.EGLContext;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;

import com.example.dj.record.bean.MediaCodecState;

import java.io.IOException;

public class MediaRecorder {
    private static final String TAG = "MediaRecorder";
    private final int mWidth;
    private final int mHeight;
    private final String mPath;
    private final Context mContext;

    private Surface mSurface;
    private EGLContext mGlContext;
    private MediaMuxer mMuxer;
    private Handler mHandler;
    private boolean isStart;
    private EGLEnv eglEnv;
    // 1------------- 视频录制相关(放到单独线程)
    private VideoCodecThread videoCodecThread;
    //2------------- 音频录制相关  --------------
    private AudioCodeThread audioCodeThread;
    //3----------- 音频采集 ------------
    private AudioCapture audioCapture;
    // 录制状态相关
    private MediaCodecState mediaCodecState;


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
        //混合器 (复用器) 将编码的h.264封装为mp4
        mMuxer = new MediaMuxer(mPath,
                MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        mediaCodecState = new MediaCodecState();
        //开启视频编码
        mSurface = initVideoCodec();
        startVideoCodec();
        //开启音频采集和编码
        startAudioCaptureCodec();

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

    private Surface initVideoCodec(){
        videoCodecThread = new VideoCodecThread(mMuxer, mWidth, mHeight,mediaCodecState, new MediaMuxerChangeListener() {
            @Override
            public void onMediaMuxerChangeListener(int type) {
                Log.e(TAG, "onMediaMuxerChangeListener: type");
                onHandleMediaMuxerChange(type);
            }
        });
        try {
            return videoCodecThread.initVideoRecord();
        } catch (IOException e) {
            Log.e(TAG, "startVideoCodec: 视频编码开启异常");
            e.printStackTrace();
        }
        return null;
    }

    // 开始进行视频编码
    private void startVideoCodec(){
        videoCodecThread.startCodec();
    }

    // 音频采集及录制
    private void startAudioCaptureCodec(){
        // 音频采集
        audioCapture = new AudioCapture();
        audioCapture.start();

        mediaCodecState.audioStop = false;
        mediaCodecState.videoStop = false;

        audioCodeThread = new AudioCodeThread(mMuxer, mediaCodecState,new MediaMuxerChangeListener() {
            @Override
            public void onMediaMuxerChangeListener(int type) {
                Log.e(TAG, "onMediaMuxerChangeListener: ");
                onHandleMediaMuxerChange(type);
            }
        });
        audioCodeThread.initAudioRecord();

        // 配置声音解码及播放
//        initAudioRecord();
        mediaCodecState.videoTrackIndex = -1;
        mediaCodecState.audioTrackIndex = -1;
        mediaCodecState.encodeStart = false;
        audioCodeThread.startCodec();
    }

    private void onHandleMediaMuxerChange(int type){
        if(type == MediaCodecState.MUXER_START){
            if(audioCapture.getCaptureListener() == null){
                audioCapture.setCaptureListener(new AudioCapture.AudioCaptureListener() {
                    @Override
                    public void onCaptureListener(byte[] audioSource, int audioReadSize) {
//                            Log.e(TAG, "onCaptureListener: ");
                        // 塞给音频编码线程
                        // TODO: 6/14/21 判断状态
                        if (mediaCodecState.audioStop || mediaCodecState.videoStop) {
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
//                codec(false);
//                codecAudio(false);
            }
        });
    }


//    private void codec(boolean endOfStream) {
////        Log.e(TAG, "codec: run codec");
//        //给个结束信号
//        if (endOfStream) {
//            Log.e(TAG, "codec: endOfStream");
//            mMediaCodec.signalEndOfInputStream();
//        }
//        while (true) {
//            Log.e(TAG, "codec: in the while loop--------");
//            //获得输出缓冲区 (编码后的数据从输出缓冲区获得)
//            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
//            int encoderStatus = mMediaCodec.dequeueOutputBuffer(bufferInfo, 10_000);
//
//            //需要更多数据
//            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
//                Log.e(TAG, "codec: video tray again later");
//                //如果是结束那直接退出，否则继续循环
//                if (!endOfStream) {
//                    break;
//                }
//            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
//                Log.e(TAG, "codec: add video track");
//                //输出格式发生改变  第一次总会调用所以在这里开启混合器
//                MediaFormat newFormat = mMediaCodec.getOutputFormat();
//                track = mMuxer.addTrack(newFormat);
//                MediaCodecConstant.videoTrackIndex = track;
//                Log.e(TAG, "run: video formatchanged videoTrackIndex="+MediaCodecConstant.videoTrackIndex+" audioTrackIndex="+MediaCodecConstant.audioTrackIndex);
//                if(MediaCodecConstant.audioTrackIndex != -1){
//                    Log.e(TAG, "codec: start video muxer");
//                    mMuxer.start();
//                    MediaCodecConstant.encodeStart = true;
//                    onHandleMediaMuxerChange(MediaCodecConstant.MUXER_START);
//                }
//            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
//                Log.e(TAG, "codec: video ignore buffer change");
//                //可以忽略
//            } else {
//                Log.e(TAG, "codec: read code video before");
//                if(!MediaCodecConstant.encodeStart){
//                    SystemClock.sleep(10);
//                    continue;
//                }
//                Log.e(TAG, "codec: read code video");
//                //调整时间戳
//                bufferInfo.presentationTimeUs = (long) (bufferInfo.presentationTimeUs);
//                if(mLastTimeStamp == 0){
//                    mLastTimeStamp = bufferInfo.presentationTimeUs;
//                }
//                bufferInfo.presentationTimeUs = bufferInfo.presentationTimeUs - mLastTimeStamp;
//
//                //有时候会出现异常 ： timestampUs xxx < lastTimestampUs yyy for Video track
////                if (bufferInfo.presentationTimeUs <= mLastTimeStamp) {
////                    bufferInfo.presentationTimeUs = (long) (mLastTimeStamp + 1_000_000 / 25 / mSpeed);
////                }
////                mLastTimeStamp = bufferInfo.presentationTimeUs;
//
//                //正常则 encoderStatus 获得缓冲区下标
//                ByteBuffer encodedData = mMediaCodec.getOutputBuffer(encoderStatus);
//                //如果当前的buffer是配置信息，不管它 不用写出去
//                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
//                    bufferInfo.size = 0;
//                }
//                if (bufferInfo.size != 0) {
//                    //设置从哪里开始读数据(读出来就是编码后的数据)
//                    encodedData.position(bufferInfo.offset);
//                    //设置能读数据的总长度
//                    encodedData.limit(bufferInfo.offset + bufferInfo.size);
//                    //写出为mp4
//                    mMuxer.writeSampleData(track, encodedData, bufferInfo);
//                }
//                // 释放这个缓冲区，后续可以存放新的编码后的数据啦
//                mMediaCodec.releaseOutputBuffer(encoderStatus, false);
//                // 如果给了结束信号 signalEndOfInputStream
//                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
//                    Log.e(TAG, "codec: break encode");
//                    break;
//                }
//            }
//        }
//    }


    public void stop() {
        // 释放
        isStart = false;
        mediaCodecState.encodeStart = false;
        mediaCodecState.videoStop = true;
        // 音频采集
        audioCapture.stop();
        audioCodeThread.stopCodec();
        videoCodecThread.stopVideoCodec();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
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
