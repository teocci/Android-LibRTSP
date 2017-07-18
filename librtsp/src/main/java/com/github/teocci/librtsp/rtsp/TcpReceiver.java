package com.github.teocci.librtsp.rtsp;

import com.github.teocci.librtsp.rtsp.interfaces.TcpDataHandler;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2017-Jul-12
 */

public class TcpReceiver
{
    private Socket socket;

    public TcpReceiver(Socket socket)
    {
        this.socket = socket;
    }

    public void run(TcpDataHandler handler) throws IOException
    {
        DataInputStream inputStream = new DataInputStream(socket.getInputStream());

        while (true) {
            if (36 != inputStream.read())
                throw new IOException("invalid magic number");

            int channel = inputStream.read();
            int dataSize = inputStream.readShort();
            byte[] data = new byte[dataSize];
            inputStream.readFully(data);

            handler.onData(channel, data, dataSize);
        }
    }
}
