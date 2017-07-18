package com.github.teocci.librtsp.rtp.receiver;

import com.github.teocci.librtsp.rtp.interfaces.RtpSink;
import com.github.teocci.utils.LogHelper;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2017-MAR-17
 */

public class UdpServer implements Runnable
{
    private static String TAG = LogHelper.makeLogTag(UdpServer.class);

    private DatagramSocket datagramSocket;
    private int port;
    protected RtpSink sink;
    private Thread thread;

    public UdpServer(int port, RtpSink sink)
    {
        this.port = port;
        this.sink = sink;
    }

    @Override
    public void run()
    {
        byte[] buf = new byte[1500];

        while (true) {
            try {
                DatagramPacket pack = new DatagramPacket(buf, buf.length);
                datagramSocket.receive(pack);

                if (sink != null)
                    sink.onRtp(pack.getData(), pack.getLength());

            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    public void open() throws SocketException
    {
        LogHelper.i(TAG, "listen at " + port);
        datagramSocket = new DatagramSocket(port);

        thread = new Thread(this);
        thread.start();
    }

    public void close()
    {
        datagramSocket.close();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
