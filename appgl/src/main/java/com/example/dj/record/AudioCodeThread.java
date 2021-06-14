package com.example.dj.record;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
/**
 *  音频采集，录制线程
 *
 * **/
public class AudioCodeThread  extends Thread{
    private static final String TAG = "MediaRecorder";
    private AudioRecord audioRecord;
    private MediaCodec mAudioCodec;
    private int minAudioBufferSize;
    private int audioTrack;
    private long mLastAudioTimeStamp;//音频pts
    private long audioPts;

    private MediaMuxer mMuxer;
    private boolean isStop;

    public AudioCodeThread(MediaMuxer muxer){
        this.mMuxer = muxer;
        this.isStop = true;
    }


    public void initAudioRecord(){
        try{
            MediaFormat mediaFormat = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC,
                    44100, 1);
            //编码规格，可以看成质量
            mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
            //码率
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 64_000);
            mAudioCodec =  MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC);
            mAudioCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

            // 最小缓冲区大小
            minAudioBufferSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
            audioRecord = new AudioRecord(android.media.MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, minAudioBufferSize);


        }catch(IOException e){
            e.printStackTrace();
            Log.e(TAG, "initAudioRecord: 音频类型无效");
        }

    }


    @Override
    public void run(){
        super.run();
        MediaCodecConstant.audioTrackIndex = -1;
        mAudioCodec.start();
        audioRecord.startRecording();
        codecAudio(false);

    }

    public void startCodec(){
        isStop = false;
        start();
    }

    // 这个函数不会被执行，因为codec一直在循环阻塞当中
    private void codecAudio(boolean endOfStream){
        //给个结束信号
        if (isStop) {
            mAudioCodec.signalEndOfInputStream();
        }

        while (true){
            Log.e(TAG, "codec: start code audio");
            if(isStop){
                break;
            }
            //1----------------------- 音频录制 获取音频数据  塞音频数据
            byte[] bufferAudio = new byte[minAudioBufferSize];
            int audioLen = audioRecord.read(bufferAudio,0,bufferAudio.length);
            Log.e(TAG, "codec: audioLen="+audioLen);
            //输入队列塞入数据
            int audioIndex = mAudioCodec.dequeueInputBuffer(0);
            if(audioIndex >= 0){
                ByteBuffer byteBufferAudio = mAudioCodec.getInputBuffer(audioIndex);
                byteBufferAudio.clear();
                // 把输入塞入容器
                byteBufferAudio.put(bufferAudio,0,audioLen);
                mAudioCodec.queueInputBuffer(audioIndex,0,audioLen,System.nanoTime() / 1000,0);
            }

            //2-------------- 音频数据编码
            // 从输出queue里拿出数据，进行编码
            //获得输出缓冲区 (编码后的数据从输出缓冲区获得)
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int encoderStatus = mAudioCodec.dequeueOutputBuffer(bufferInfo, 10_000);
            //需要更多数据
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                //如果是结束那直接退出，否则继续循环
                if (!endOfStream) {
                    break;
                }
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                Log.e(TAG, "codec:  add audio track");
                //输出格式发生改变  第一次总会调用所以在这里开启混合器
                MediaFormat newFormat = mAudioCodec.getOutputFormat();
                audioTrack = mMuxer.addTrack(newFormat);
                MediaCodecConstant.audioTrackIndex = audioTrack;
                Log.e(TAG, "run: audio formatchanged audioTrackIndex="+MediaCodecConstant.audioTrackIndex+" videoTrackIndex="+MediaCodecConstant.videoTrackIndex);
                if(MediaCodecConstant.videoTrackIndex != -1){
                    Log.e(TAG, "codec: start audio muxer");
                    mMuxer.start();
                    MediaCodecConstant.encodeStart = true;
                }
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                //可以忽略
            }else{
//                if(!MediaCodecConstant.encodeStart){
//                    SystemClock.sleep(10);
//                    continue;
//                }
//                ByteBuffer encodedData = mAudioCodec.getOutputBuffer(encoderStatus);
//                //如果当前的buffer是配置信息，不管它 不用写出去
//                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
//                    bufferInfo.size = 0;
//                }
//                if (bufferInfo.size != 0) {
//                    //调整时间戳
//                    if(audioPts ==0){
//                        audioPts = bufferInfo.presentationTimeUs;
//                    }
//                    mLastAudioTimeStamp = bufferInfo.presentationTimeUs - audioPts;
//                    bufferInfo.presentationTimeUs = mLastAudioTimeStamp;
//                    //设置从哪里开始读数据(读出来就是编码后的数据)
//                    encodedData.position(bufferInfo.offset);
//                    //设置能读数据的总长度
//                    encodedData.limit(bufferInfo.offset + bufferInfo.size);
//                    //写出为mp4
//                    mMuxer.writeSampleData(audioTrack, encodedData, bufferInfo);
//                }
//                // 释放这个缓冲区，后续可以存放新的编码后的数据啦
//                mAudioCodec.releaseOutputBuffer(encoderStatus, false);
//                // 如果给了结束信号 signalEndOfInputStream
//                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
//                    break;
//                }
            }
        }

    }

    public void stopCodec(){
        isStop = true;
        // 音频采集
        audioRecord.stop();
        audioRecord.release();
        audioRecord = null;
        // 音频编码
        mAudioCodec.stop();
        mAudioCodec.release();
        mAudioCodec = null;
    }




}
