package com.github.teocci.librtsp.rtp.receiver;

import com.github.teocci.BuildConfig;
import com.github.teocci.librtsp.rtp.interfaces.SampleHandler;
import com.github.teocci.librtsp.rtp.player.Statistics;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2017-MAR-17
 */

public class RtpAacStream extends RtpStream
{
    private int sampleRate;
    protected SampleHandler handler;

    public RtpAacStream(int sampleRate, SampleHandler handler, Statistics stats)
    {
        super(stats);
        this.sampleRate = sampleRate;
        this.handler = handler;
    }

    @Override
    protected boolean decodePayload(DataInputStream reader) throws IOException
    {
        int auHeadersLength = reader.readShort();
        auHeadersLength /= 8;


        if (BuildConfig.DEBUG && auHeadersLength != 2)
            throw new RuntimeException();

        int auTotalSize = 0;


        for (int i = 0; i < auHeadersLength / 2; i++) {
            long auHeader = reader.readShort() & ((-1) >>> 32);

            int auSize = (int) (auHeader >>> 3);
            //int auIndexDelta = (int)(auHeader & 0x07);

            auTotalSize += auSize;
        }

        if (BuildConfig.DEBUG && auTotalSize != reader.available())
            throw new RuntimeException();

        Sample sample = new Sample();
        sample.keyframe = true;
        sample.timestampUs = this.timestamp * 1000000 / sampleRate;
        sample.data = new byte[reader.available()];
        reader.readFully(sample.data);

        if (handler != null)
            handler.onSample(sample);

        if (stats != null)
            stats.audioFrameCount++;

        return true;
    }
}
