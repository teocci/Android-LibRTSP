package com.github.teocci.librtsp.rtsp;

import com.github.teocci.librtsp.rtp.interfaces.RtpSocket;

import java.io.IOException;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2017-Jul-12
 */

public class TcpChannel implements RtpSocket
{
    private int channelNum;
    private TcpSender sender;

    public TcpChannel(TcpSender sender, int channelNum)
    {
        this.sender = sender;
        this.channelNum = channelNum;
    }

    @Override
    public void sendPacket(byte[] data, int offset, int size) throws IOException
    {
        sender.sendData(channelNum, data, offset, size);
    }

    public void close()
    {

    }
}