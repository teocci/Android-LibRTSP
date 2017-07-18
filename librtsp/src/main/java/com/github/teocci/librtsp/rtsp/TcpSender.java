package com.github.teocci.librtsp.rtsp;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2017-Jul-12
 */

public class TcpSender
{
    private Socket socket;

    public TcpSender(Socket socket)
    {
        this.socket = socket;
    }

    public synchronized void sendData(int channel, byte[] data, int offset, int size) throws IOException
    {
        DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());

        outputStream.write(36);
        outputStream.write(channel);
        outputStream.writeShort(size);
        outputStream.write(data, offset, size);
    }

    public void close()
    {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}





