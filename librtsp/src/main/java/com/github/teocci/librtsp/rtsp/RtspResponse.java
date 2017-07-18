package com.github.teocci.librtsp.rtsp;

import com.github.teocci.utils.LogHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2017-Jul-12
 */

public class RtspResponse
{
    public static final String TAG = RtspResponse.class.getSimpleName();

    // Parses method & uri
    public static final Pattern regexStatus = Pattern.compile("RTSP/\\d.\\d (\\d+) (\\w+)", Pattern.CASE_INSENSITIVE);
    // Parses a request header
    public static final Pattern rexegHeader = Pattern.compile("(\\S+):(.+)", Pattern.CASE_INSENSITIVE);
    // Parses a WWW-Authenticate header
    public static final Pattern rexegAuthenticate = Pattern.compile("realm=\"(.+)\",\\s+nonce=\"(\\w+)\"", Pattern.CASE_INSENSITIVE);
    // Parses a Session header
    public static final Pattern rexegSession = Pattern.compile("(\\d+)", Pattern.CASE_INSENSITIVE);
    // Parses a Transport header
    public static final Pattern rexegTransport = Pattern.compile("client_port=(\\d+)-(\\d+).+server_port=(\\d+)-(\\d+)", Pattern.CASE_INSENSITIVE);


    public int status;
    public HashMap<String, String> headers = new HashMap<String, String>();

    /**
     * Parse the method, URI & headers of a RTSP request
     */
    public static RtspResponse parseResponse(BufferedReader input) throws IOException, IllegalStateException, SocketException
    {
        RtspResponse response = new RtspResponse();
        String line;
        Matcher matcher;
        // Parsing request method & URI
        if ((line = input.readLine()) == null) throw new SocketException("Connection lost");

        LogHelper.i(TAG, line);
        matcher = regexStatus.matcher(line);
        if (matcher.find())
            response.status = Integer.parseInt(matcher.group(1));

        // Parsing headers of the request
        while ((line = input.readLine()) != null) {
            //Log.e(TAG,"l: "+line.length()+", c: "+line);
            if (line.length() > 3) {
                matcher = rexegHeader.matcher(line);
                if (matcher.find())
                    response.headers.put(matcher.group(1), matcher.group(2));
            } else {
                break;
            }
        }
        if (line == null) throw new SocketException("Connection lost");

        LogHelper.d(TAG, "Response from server: " + response.status);

        return response;
    }
}
