package com.example.dj.record;

import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import com.example.dj.record.bean.MediaCodecState;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *  音频录制编码线程
 *
 * **/
public class AudioCodeThread extends Thread{
    private static final String TAG = "AudioRecorder";
    private MediaCodec mAudioCodec;
    private int audioTrack;

    private MediaMuxer mMuxer;
    private boolean isStop;
    // 音视频合成准备完毕会调
    private MediaMuxerChangeListener muxListener;

    // 音频参数---------
    private int sampleRate = 44100;
    int channelCount = 2;
    int audioFormat = 16;//AudioFormat.ENCODING_PCM_16BIT ，此处应传入16bit，

    //时间戳
    private long presentationTimeUs;
    private long pts;
    private MediaCodec.BufferInfo bufferInfo;//缓冲区
    private MediaCodecState mCodecState;


    public AudioCodeThread(MediaMuxer muxer,MediaCodecState codecState, MediaMuxerChangeListener muxerChangeListener){
        this.mMuxer = muxer;
        this.isStop = true;
        this.muxListener = muxerChangeListener;
        pts = 0;
        this.mCodecState = codecState;
    }


    // 初始化音频编码器参数
    public void initAudioRecord(){
        try{
            MediaFormat mediaFormat = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC,
                    sampleRate, channelCount);
            //编码规格，可以看成质量
            mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
            //码率
            int BIT_RATE = 96000;
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE);
            int MAX_INOUT_SIZE = 8192;
            mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE,MAX_INOUT_SIZE);

            mAudioCodec =  MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC);
            mAudioCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

            // 最小缓冲区大小
//            minAudioBufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO,
//                    AudioFormat.ENCODING_PCM_16BIT);
//            audioRecord = new AudioRecord(android.media.MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_MONO,
//                    AudioFormat.ENCODING_PCM_16BIT, minAudioBufferSize);
            bufferInfo = new MediaCodec.BufferInfo();

        }catch(IOException e){
            e.printStackTrace();
            Log.e(TAG, "initAudioRecord: 音频类型无效");
        }

    }


    // 往音频编码器中塞入数据
    public void setPcmSource(byte[] pcmBuffer, int buffSize){
        if(mAudioCodec == null){
            return;
        }
        try {

            int buffIndex = mAudioCodec.dequeueInputBuffer(0);
            if (buffIndex < 0) {
                return;
            }
            ByteBuffer byteBuffer;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                byteBuffer = mAudioCodec.getInputBuffer(buffIndex);
            } else {
                byteBuffer = mAudioCodec.getInputBuffers()[buffIndex];
            }
            if (byteBuffer == null) {
                return;
            }
            byteBuffer.clear();
            byteBuffer.put(pcmBuffer);
            //presentationTimeUs = 1000000L * (buffSize / 2) / sampleRate
            //一帧音频帧大小 int size = 采样率 x 位宽 x 采样时间 x 通道数
            // 1s时间戳计算公式  presentationTimeUs = 1000000L * (totalBytes / sampleRate/ audioFormat / channelCount / 8 )
            //totalBytes : 传入编码器的总大小
            //1000 000L : 单位为 微秒，换算后 = 1s,
            //除以8     : pcm原始单位是bit, 1 byte = 8 bit, 1 short = 16 bit, 用 Byte[]、Short[] 承载则需要进行换算
            presentationTimeUs += (long) (1.0 * buffSize / (sampleRate * channelCount * (audioFormat / 8)) * 1000000.0);
            Log.d(TAG, "pcm一帧时间戳 = " + presentationTimeUs / 1000000.0f);
            mAudioCodec.queueInputBuffer(buffIndex, 0, buffSize, presentationTimeUs, 0);
        } catch (IllegalStateException e) {
            //audioCodec 线程对象已释放MediaCodec对象
            Log.d(TAG, "setPcmSource: " + "MediaCodec对象已释放");
        }

    }


    @Override
    public void run(){
        super.run();
        codecAudioMM();

    }

    private void codecAudioMM(){
//        Log.e(TAG, "codecAudioMM: ");
        isStop = false;
        mAudioCodec.start();
        while (true) {
            if (isStop) {
//                Log.e(TAG, "codecAudioMM: stop audio codec");
                mAudioCodec.stop();
                mAudioCodec.release();
                mAudioCodec = null;
                mCodecState.audioStop = true;

                    if (mCodecState.videoStop && mMuxer != null) {
                        Log.e(TAG, "codecAudioMM: stop mux");
                        if(mCodecState.muxStop){
                            muxListener.onMediaMuxerChangeListener(MediaCodecState.MUXER_STOP);
                            break;
                        }
                        mMuxer.stop();
                        mCodecState.muxStop = true;
                        mMuxer.release();
                        mMuxer = null;
                        muxListener.onMediaMuxerChangeListener(MediaCodecState.MUXER_STOP);
                        break;
                    }
            }

            if (mAudioCodec == null){
                Log.e(TAG, "codecAudioMM: exit loop");
                break;
            }
            int outputBufferIndex = mAudioCodec.dequeueOutputBuffer(bufferInfo, 0);
            if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                Log.e(TAG, "run: audio formatchanged");
                mCodecState.audioTrackIndex = mMuxer.addTrack(mAudioCodec.getOutputFormat());
                Log.e(TAG, "run: audio formatchanged audioTrackIndex="+mCodecState.audioTrackIndex+" videoTrackIndex="+mCodecState.videoTrackIndex);
                if (mCodecState.videoTrackIndex != -1) {
                Log.e(TAG, "run: audio muxer start");
                mMuxer.start();
                //标识编码开始
                mCodecState.encodeStart = true;
                muxListener.onMediaMuxerChangeListener(MediaCodecState.MUXER_START);
                }
            } else {
//                Log.e(TAG, "codecAudioMM: wrapper outIndex="+outputBufferIndex);
                while (outputBufferIndex >= 0) {
                    if (!mCodecState.encodeStart) {
                        Log.d(TAG, "run: 线程延迟 bfIndex="+outputBufferIndex);
                        SystemClock.sleep(10);
                        continue;
                    }

                    ByteBuffer outputBuffer = mAudioCodec.getOutputBuffer(outputBufferIndex);
                    outputBuffer.position(bufferInfo.offset);
                    outputBuffer.limit(bufferInfo.offset + bufferInfo.size);

                    if (pts == 0) {
                        pts = bufferInfo.presentationTimeUs;
                    }
                    bufferInfo.presentationTimeUs = bufferInfo.presentationTimeUs - pts;
                    mMuxer.writeSampleData(mCodecState.audioTrackIndex, outputBuffer, bufferInfo);

                    mAudioCodec.releaseOutputBuffer(outputBufferIndex, false);
                    outputBufferIndex = mAudioCodec.dequeueOutputBuffer(bufferInfo, 0);
                    Log.e(TAG, "codecAudioMM: outputBufferIndex="+outputBufferIndex);
                }
            }
        }
    }

    public void startCodec(){
        isStop = false;
        start();
    }

    public void stopCodec(){
        Log.e(TAG, "stopAudioCodec: ");
        isStop = true;
        // 音频编码

        //线程循环里检测到之后，自然会进行释放
//        mAudioCodec.stop();
//        mAudioCodec.release();
//        mAudioCodec = null;
    }




}
