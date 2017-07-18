package com.github.teocci.librtsp.rtp.interfaces;

import java.io.IOException;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2017-MAR-17
 */

public interface RtpSocket
{
    void sendPacket(byte[] data, int offset, int size) throws IOException;

    void close();
}
