package com.github.teocci.librtsp.rtp.player;

import android.os.Handler;

import com.github.teocci.librtsp.rtp.interfaces.Player;
import com.github.teocci.librtsp.rtp.interfaces.SampleHandler;
import com.github.teocci.librtsp.rtp.receiver.RtpAacStream;
import com.github.teocci.librtsp.rtp.receiver.RtpAvcStream;
import com.github.teocci.librtsp.rtp.receiver.RtpStream;
import com.github.teocci.librtsp.rtp.receiver.Sample;
import com.github.teocci.librtsp.rtp.receiver.UdpServer;
import com.google.android.exoplayer.MediaFormat;
import com.google.android.exoplayer.VideoSurfaceView;

import java.net.SocketException;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2017-MAR-12
 */
public class UdpSinkPlayer implements Player
{

    //private final static String TAG = "UdpSinkPlayer";

    private int videoReceivePort;
    private int audioReceivePort;

    private UdpServer videoReceiver;
    private UdpServer audioReceiver;

    private RtpStream videoStream;
    private RtpStream audioStream;

    private SimplePlayer player;

    private int audioSampleRate;

    private int sourceCount;


    public UdpSinkPlayer(int videoReceivePort, int audioReceivePort, VideoSurfaceView view, Handler handler, int sourceCount)
    {
        player = new SimplePlayer(view, handler, sourceCount);

        this.videoReceivePort = videoReceivePort;
        this.audioReceivePort = audioReceivePort;
        this.sourceCount = sourceCount;
    }


    public void setVideoFormat(MediaFormat format)
    {
        player.setVideoFormat(format);
    }


    public void setAudioFormat(MediaFormat format)
    {
        player.setAudioFormat(format);
        if (format != null)
            audioSampleRate = format.sampleRate;
    }

    public void start()
    {
        player.start();
        try {
            startRtp();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void stop()
    {

        stopRtp();

        player.stop();
        player = null;
    }

    void startRtp() throws SocketException
    {
        videoStream = new RtpAvcStream(new SampleHandler()
        {
            @Override
            public void onSample(Sample sample)
            {
                player.onVideoSample(sample);
            }
        }, getStats());
        videoReceiver = new UdpServer(videoReceivePort, videoStream);
        videoReceiver.open();

        if (sourceCount > 1) {
            audioStream = new RtpAacStream(audioSampleRate, new SampleHandler()
            {
                @Override
                public void onSample(Sample sample)
                {
                    player.onAudioSample(sample);
                }
            }, getStats());
            audioReceiver = new UdpServer(audioReceivePort, audioStream);
            audioReceiver.open();
        }
    }

    void stopRtp()
    {
        if (videoStream != null) {
            videoStream.close();
            videoStream = null;
        }
        if (videoReceiver != null) {
            videoReceiver.close();
            videoReceiver = null;
        }

        if (audioStream != null) {
            audioStream.close();
            audioStream = null;
        }
        if (audioReceiver != null) {
            audioReceiver.close();
            audioReceiver = null;
        }
    }


    @Override
    public void addVideoPacket(byte[] data, int dataSize) {}

    @Override
    public void addAudioPacket(byte[] data, int dataSize) {}

    @Override
    public Statistics getStats()
    {
        return player.stats;
    }

    @Override
    public void setJitterBuffer(long timeUs)
    {
        player.setJitterBuffer(timeUs);
    }

}
