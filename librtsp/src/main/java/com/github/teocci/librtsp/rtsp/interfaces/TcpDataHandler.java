package com.github.teocci.librtsp.rtsp.interfaces;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2017-Jul-12
 */

public interface TcpDataHandler
{
    void onData(int channel, byte[] data, int dataSize);
}