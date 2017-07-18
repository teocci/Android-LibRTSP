package com.github.teocci.librtsp.rtsp.interfaces;

import com.github.teocci.librtsp.rtsp.RtspClient;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2017-Jul-12
 */

public interface RtspClientListener
{
    void onReady(RtspClient client);

    void onRtpPacket(byte[] data, int dataSize);
}
