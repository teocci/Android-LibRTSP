package com.github.teocci.librtsp.rtp.receiver;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2017-MAR-17
 */

public class Sample
{
    public boolean isVideo;
    public long timestampUs;
    public boolean keyframe;
    public byte[] data;
}
