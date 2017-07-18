package com.github.teocci.librtsp.rtp.interfaces;

import com.github.teocci.librtsp.rtp.player.Statistics;
import com.google.android.exoplayer.MediaFormat;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2017-MAR-12
 */

public interface Player
{
    void start();

    void stop();

    void setVideoFormat(MediaFormat format);

    void setAudioFormat(MediaFormat format);

    void addVideoPacket(byte[] data, int dataSize);

    void addAudioPacket(byte[] data, int dataSize);

    Statistics getStats();

    void setJitterBuffer(long timeUs);
}
