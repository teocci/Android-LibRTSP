package com.github.teocci.librtsp.rtp.sender;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;

import com.github.teocci.librtsp.rtp.interfaces.RtpSocket;
import com.github.teocci.utils.LogHelper;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2017-MAR-17
 */
public class MicStream extends RtpAacStream
{
    private static String TAG = LogHelper.makeLogTag(MicStream.class);
    private AudioRecord record;
    private MediaCodec mediaCodec;
    private int sampleRate;
    private int channelNum;
    private boolean hasStopped;
    private Thread thread;
    private byte[] audioConfig;
    private long baseTimeUs;

    public MicStream(long baseTimeUs, int sampleRate, int channelNum, RtpSocket socket)
    {
        super(sampleRate, socket);
        this.baseTimeUs = baseTimeUs;
        this.sampleRate = sampleRate;
        this.channelNum = channelNum;
    }

    public void start() throws IOException
    {
        hasStopped = false;

        int channel = channelNum > 1 ? AudioFormat.CHANNEL_IN_STEREO : AudioFormat.CHANNEL_IN_MONO;
        int format = AudioFormat.ENCODING_PCM_16BIT;
        final int bufSize = AudioRecord.getMinBufferSize(sampleRate, channel, format);

        MediaFormat mAudioFormat = MediaFormat.createAudioFormat("audio/mp4a-latm", sampleRate, channelNum);
        mAudioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        mAudioFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 16384);
        mAudioFormat.setInteger(MediaFormat.KEY_BIT_RATE, 128000);

        mediaCodec = MediaCodec.createEncoderByType("audio/mp4a-latm");
        mediaCodec.configure(mAudioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mediaCodec.start();

        record = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channel, format, bufSize);
        record.startRecording();

        thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                ByteBuffer data = ByteBuffer.allocateDirect(bufSize);

                ByteBuffer[] inBuffers = mediaCodec.getInputBuffers();
                ByteBuffer[] outBuffers = mediaCodec.getOutputBuffers();
                BufferInfo info = new BufferInfo();

                while (!hasStopped) {
                    data.clear();
                    int readSize = record.read(data, data.capacity());
                    //Log.i("Audio", "readSize:" + readSize);

                    if (readSize > 0) {
                        int inBufferIndex = mediaCodec.dequeueInputBuffer(100 * 1000);
                        if (inBufferIndex >= 0) {

                            long ptsUs = System.nanoTime() / 1000 - baseTimeUs;

                            ByteBuffer buf = inBuffers[inBufferIndex];
                            buf.clear();
                            buf.put(data);

                            mediaCodec.queueInputBuffer(inBufferIndex, 0, readSize, ptsUs, 0);
                        }
                    }

                    while (!hasStopped) {
                        int outBufferIndex = mediaCodec.dequeueOutputBuffer(info, 0);
                        if (outBufferIndex >= 0) {
                            ByteBuffer buf = outBuffers[outBufferIndex];

                            if (info.flags != MediaCodec.BUFFER_FLAG_CODEC_CONFIG) {
                                buf.position(info.offset);
                                buf.limit(info.offset + info.size);

                                try {
                                    addAU(buf, info.size, info.presentationTimeUs);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }

                            mediaCodec.releaseOutputBuffer(outBufferIndex, false);
                        } else if (outBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                            MediaFormat format = mediaCodec.getOutputFormat();
                            ByteBuffer buf = format.getByteBuffer("csd-0");
                            audioConfig = new byte[buf.capacity()];
                            buf.get(audioConfig);

                            LogHelper.i(TAG, audioConfig.toString());

                        } else if (outBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                            outBuffers = mediaCodec.getOutputBuffers();
                        } else {
                            break;
                        }
                    }
                }
            }

        });

        thread.start();
    }

    public void stop()
    {
        hasStopped = true;

        try {
            thread.join(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mediaCodec.stop();
        mediaCodec.release();
        mediaCodec = null;

        record.stop();
        record.release();
        record = null;
    }

    public byte[] getAudioConfig()
    {
        return audioConfig;
    }
}
