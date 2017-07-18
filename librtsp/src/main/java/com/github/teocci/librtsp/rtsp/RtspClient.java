package com.github.teocci.librtsp.rtsp;

import android.net.Uri;
import android.util.Base64;

import com.github.teocci.librtsp.rtsp.interfaces.RtspClientListener;
import com.github.teocci.utils.LogHelper;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2017-Jul-12
 */

public class RtspClient implements Runnable
{
    public static String TAG = LogHelper.makeLogTag(RtspClient.class);

    private RtspClientListener listener;

    private Socket socket;
    private String session;
    private String videoTrackUrl;
    private String audioTrackUrl;

    public int sampleRate;
    public List<byte[]> sps_pps;
    public byte[] audioConfig;
    public int channel;

    private String rtspUrl;
    private boolean playVideo;
    private int receivePort;

    private boolean overTcp;

    private BufferedReader reader;

    private OutputStream writer;

    Thread thread;

    public RtspClient(String rtspUrl, boolean playVideo, int receivePort, RtspClientListener listener)
    {
        this.rtspUrl = rtspUrl;
        this.playVideo = playVideo;
        this.receivePort = receivePort;
        this.listener = listener;

        this.overTcp = receivePort == 0;
    }

    public void start()
    {
        thread = new Thread(this);
        thread.start();
    }

    public void stop()
    {
        thread.interrupt();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run()
    {
        try {
            rtspPlay();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static byte[] hexStringToByteArray(String s)
    {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    private void request(String content) throws UnknownHostException, IOException
    {
        LogHelper.i(TAG, content);

        writer.write(content.getBytes("UTF-8"));
        writer.flush();

        RtspResponse response = RtspResponse.parseResponse(reader);

        if (response.headers.containsKey("server")) {
            LogHelper.v(TAG, "RTSP server name:" + response.headers.get("server"));
        } else {
            LogHelper.v(TAG, "RTSP server name unknown");
        }

        int contentLength = 0;
        String content_base = "";

        if (response.headers.containsKey("session")) {
            try {
                Matcher m = RtspResponse.rexegSession.matcher(response.headers.get("session"));
                m.find();
                session = m.group(1);
            } catch (Exception e) {
                throw new IOException("Invalid response from server. Session id: " + session);
            }

//            session = session.trim();
        }

        if (response.headers.containsKey("Content-Length")) {
            contentLength = Integer.parseInt(response.headers.get("Content-Length").trim());
        }

        if (response.headers.containsKey("Content-Base")) {
            content_base = response.headers.get("Content-Base").trim();
        }

        if (contentLength > 0)
        {
            char[] buf = new char[contentLength];
            int offset = 0;
            while (offset < contentLength) {
                offset += reader.read(buf, offset, contentLength - offset);
            }

            String body = new String(buf);
            LogHelper.i(TAG, body);

            if (body.indexOf("m=video") > 0) {
                int start = body.indexOf(":", body.indexOf("a=control", body.indexOf("m=video")));
                videoTrackUrl = body.substring(start + 1, body.indexOf("\n", start)).trim();
                if (!videoTrackUrl.startsWith("rtsp:"))
                    videoTrackUrl = content_base + videoTrackUrl;

                start = body.indexOf("=", body.indexOf("sprop-parameter-sets"));
                int end = body.indexOf(",", start);

                sps_pps = new ArrayList<>();

                if (start > 0 && end > 0) {
                    sps_pps.add(Base64.decode(body.substring(start + 1, end), 0));
                    int xend = body.indexOf(";", end);
                    if (xend < 0)
                        xend = body.indexOf("\n", end);

                    sps_pps.add(Base64.decode(body.substring(end + 1, xend), 0));

                }
            }

            if (body.indexOf("m=audio") > 0) {
                int start = body.indexOf(":", body.indexOf("a=control", body.indexOf("m=audio")));
                audioTrackUrl = body.substring(start + 1, body.indexOf("\n", start)).trim();
                if (!videoTrackUrl.startsWith("rtsp:"))
                    audioTrackUrl = content_base + audioTrackUrl;

                start = body.indexOf("/", body.indexOf("rtpmap", body.indexOf("m=audio")));

                int end = body.indexOf("/", start + 1);
                if (end - start < 10) {
                    sampleRate = Integer.parseInt(body.substring(start + 1, end));
                    channel = Integer.parseInt(body.substring(end + 1, body.indexOf("\r\n", end + 1)));
                } else {
                    end = body.indexOf("\r\n", start + 1);
                    sampleRate = Integer.parseInt(body.substring(start + 1, end));
                    channel = 1;
                }

                start = body.indexOf("=", body.indexOf("config", body.indexOf("m=audio")));
                String config = body.substring(start + 1, body.indexOf(";", start));

                audioConfig = hexStringToByteArray(config);
            }
        }
    }

    private String getTrackUrl()
    {
        return playVideo ? videoTrackUrl : audioTrackUrl;
    }

    private void rtspPlay() throws UnknownHostException, IOException, InterruptedException
    {
        Uri uri = Uri.parse(rtspUrl);

        socket = new Socket(uri.getHost(), uri.getPort());
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new BufferedOutputStream(socket.getOutputStream());

        int CSeq = 1;

        String content;

        LogHelper.e(TAG, "getTrackUrl()|" + getTrackUrl());

        content = "OPTIONS " + rtspUrl + " RTSP/1.0\r\n"
                + "CSeq: " + CSeq++ + "\r\n"
                + "User-Agent: CustomRtsp\r\n"
                + "\r\n";

        request(content);

        content = "DESCRIBE " + rtspUrl + " RTSP/1.0\r\n"
                + "CSeq: " + CSeq++ + "\r\n"
                + "User-Agent: CustomRtsp\r\n"
                + "\r\n";

        request(content);

        content = "SETUP " + getTrackUrl() + " RTSP/1.0\r\n"
                + "CSeq: " + CSeq++ + "\r\n"
                + "User-Agent: CustomRtsp\r\n";
        if (overTcp) {
            content += "Transport: RTP/AVP/TCP;unicast;interleaved=0-1\r\n";
        } else {
            content += "Transport: RTP/AVP;unicast;client_port=" + receivePort + "-" + (receivePort + 1) + "\r\n";
        }
        content += "\n";

        request(content);

        content = "PLAY " + getTrackUrl() + " RTSP/1.0\r\n"
                + "CSeq: " + CSeq++ + "\r\n"
                + "User-Agent: CustomRtsp\r\n"
                + "Session: " + session + "\r\n"
                + "\r\n";

        request(content);

        if (listener != null)
            listener.onReady(this);

        while (true) {
            if (thread.isInterrupted())
                break;

            LogHelper.e(TAG, "waiting");
            if (overTcp) {
                LogHelper.e(TAG, "before DataInputStream");
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                int magicNum = dis.readByte();
                int channelNum = dis.readByte();

                LogHelper.e(TAG, "magicNum " + magicNum);

                if (magicNum != 36)
                    throw new RuntimeException();

                int dataSize = dis.readShort();
                byte[] data = new byte[dataSize];
                dis.readFully(data);

                if (channelNum == 0) {
                    if (listener != null)
                        listener.onRtpPacket(data, dataSize);
                } else {
                    LogHelper.i(TAG, new String(data));
                }
            } else {
                Thread.sleep(30 * 1000);

                content = "GET_PARAMETER " + getTrackUrl() + " RTSP/1.0\r\n"
                        + "CSeq: " + CSeq++ + "\r\n"
                        + "User-Agent: CustomRtsp\r\n"
                        + "Session: " + session + "\r\n"
                        + "\r\n";

                request(content);
            }
        }
//        socket.close();
    }
}
