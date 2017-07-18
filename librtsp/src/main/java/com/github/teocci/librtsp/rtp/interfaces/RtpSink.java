package com.github.teocci.librtsp.rtp.interfaces;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2017-MAR-17
 */

public interface RtpSink
{
    void onRtp(byte[] data, int dataSize);
}
