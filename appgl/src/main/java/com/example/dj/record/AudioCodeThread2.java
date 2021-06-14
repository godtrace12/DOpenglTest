package com.example.dj.record;

import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import static com.example.dj.record.MediaCodecConstant.audioTrackIndex;
import static com.example.dj.record.MediaCodecConstant.videoTrackIndex;

/**
 *  音频采集，录制线程
 *
 * **/
public class AudioCodeThread2 extends Thread{
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


    public AudioCodeThread2(MediaMuxer muxer, MediaMuxerChangeListener muxerChangeListener){
        this.mMuxer = muxer;
        this.isStop = true;
        this.muxListener = muxerChangeListener;
        pts = 0;
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
//        MediaCodecConstant.audioTrackIndex = -1;
//        mAudioCodec.start();
//        audioRecord.startRecording();
//        codecAudio(false);
        codecAudioMM();

    }

    private void codecAudioMM(){
        Log.e(TAG, "codecAudioMM: ");
        isStop = false;
        mAudioCodec.start();
        while (true) {
            if (isStop) {
                Log.e(TAG, "codecAudioMM: stop audio codec");
                mAudioCodec.stop();
                mAudioCodec.release();
                mAudioCodec = null;
                MediaCodecConstant.audioStop = true;

                if (MediaCodecConstant.videoStop) {
                    Log.e(TAG, "codecAudioMM: stop mux");
                    mMuxer.stop();
                    mMuxer.release();
                    mMuxer = null;
                    muxListener.onMediaMuxerChangeListener(MediaCodecConstant.MUXER_STOP);
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
                audioTrackIndex = mMuxer.addTrack(mAudioCodec.getOutputFormat());
                Log.e(TAG, "run: audio formatchanged audioTrackIndex="+audioTrackIndex+" videoTrackIndex="+videoTrackIndex);
                if (videoTrackIndex != -1) {
                Log.e(TAG, "run: audio muxer start");
                mMuxer.start();
                //标识编码开始
                MediaCodecConstant.encodeStart = true;
                muxListener.onMediaMuxerChangeListener(MediaCodecConstant.MUXER_START);
                }
            } else {
//                Log.e(TAG, "codecAudioMM: wrapper outIndex="+outputBufferIndex);
                while (outputBufferIndex >= 0) {
                    if (!MediaCodecConstant.encodeStart) {
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
                    mMuxer.writeSampleData(audioTrackIndex, outputBuffer, bufferInfo);

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

    // 这个函数不会被执行，因为codec一直在循环阻塞当中
//    private void codecAudio(boolean endOfStream){
//        //给个结束信号
//        if (isStop) {
//            mAudioCodec.signalEndOfInputStream();
//        }
//
//        while (true){
//            Log.e(TAG, "codec: start code audio");
//            if(isStop){
//                break;
//            }
//            //1----------------------- 音频录制 获取音频数据  塞音频数据
//            byte[] bufferAudio = new byte[minAudioBufferSize];
//            int audioLen = audioRecord.read(bufferAudio,0,bufferAudio.length);
//            Log.e(TAG, "codec: audioLen="+audioLen);
//            //输入队列塞入数据
//            int audioIndex = mAudioCodec.dequeueInputBuffer(0);
//            if(audioIndex >= 0){
//                ByteBuffer byteBufferAudio = mAudioCodec.getInputBuffer(audioIndex);
//                byteBufferAudio.clear();
//                // 把输入塞入容器
//                byteBufferAudio.put(bufferAudio,0,audioLen);
//                mAudioCodec.queueInputBuffer(audioIndex,0,audioLen,System.nanoTime() / 1000,0);
//            }
//
//            //2-------------- 音频数据编码
//            // 从输出queue里拿出数据，进行编码
//            //获得输出缓冲区 (编码后的数据从输出缓冲区获得)
//            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
//            int encoderStatus = mAudioCodec.dequeueOutputBuffer(bufferInfo, 10_000);
//            //需要更多数据
//            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
//                //如果是结束那直接退出，否则继续循环
//                if (!endOfStream) {
//                    break;
//                }
//            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
//                Log.e(TAG, "codec:  add audio track");
//                //输出格式发生改变  第一次总会调用所以在这里开启混合器
//                MediaFormat newFormat = mAudioCodec.getOutputFormat();
//                audioTrack = mMuxer.addTrack(newFormat);
//                MediaCodecConstant.audioTrackIndex = audioTrack;
//                Log.e(TAG, "run: audio formatchanged audioTrackIndex="+MediaCodecConstant.audioTrackIndex+" videoTrackIndex="+MediaCodecConstant.videoTrackIndex);
//                if(MediaCodecConstant.videoTrackIndex != -1){
//                    Log.e(TAG, "codec: start audio muxer");
//                    mMuxer.start();
//                    MediaCodecConstant.encodeStart = true;
//                    if(muxListener != null){
//                        muxListener.onMediaMuxerChangeListener(MediaCodecConstant.MUXER_START);
//                    }
//                }
//            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
//                //可以忽略
//            }else{
////                if(!MediaCodecConstant.encodeStart){
////                    SystemClock.sleep(10);
////                    continue;
////                }
////                ByteBuffer encodedData = mAudioCodec.getOutputBuffer(encoderStatus);
////                //如果当前的buffer是配置信息，不管它 不用写出去
////                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
////                    bufferInfo.size = 0;
////                }
////                if (bufferInfo.size != 0) {
////                    //调整时间戳
////                    if(audioPts ==0){
////                        audioPts = bufferInfo.presentationTimeUs;
////                    }
////                    mLastAudioTimeStamp = bufferInfo.presentationTimeUs - audioPts;
////                    bufferInfo.presentationTimeUs = mLastAudioTimeStamp;
////                    //设置从哪里开始读数据(读出来就是编码后的数据)
////                    encodedData.position(bufferInfo.offset);
////                    //设置能读数据的总长度
////                    encodedData.limit(bufferInfo.offset + bufferInfo.size);
////                    //写出为mp4
////                    mMuxer.writeSampleData(audioTrack, encodedData, bufferInfo);
////                }
////                // 释放这个缓冲区，后续可以存放新的编码后的数据啦
////                mAudioCodec.releaseOutputBuffer(encoderStatus, false);
////                // 如果给了结束信号 signalEndOfInputStream
////                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
////                    break;
////                }
//            }
//        }
//
//    }

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
