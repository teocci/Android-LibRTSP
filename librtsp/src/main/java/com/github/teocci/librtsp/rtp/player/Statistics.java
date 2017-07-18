package com.github.teocci.librtsp.rtp.player;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2017-MAR-12
 */
public class Statistics
{
    public int videoFrameCount;
    public int audioFrameCount;
    public int syncFrameCount;
    public int renderDropFrameCount;
    public int transportDropPacketCount;
    public int playbackState;
}
