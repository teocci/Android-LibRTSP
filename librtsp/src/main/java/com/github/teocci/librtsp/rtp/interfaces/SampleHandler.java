package com.github.teocci.librtsp.rtp.interfaces;

import com.github.teocci.librtsp.rtp.receiver.Sample;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2017-MAR-17
 */

public interface SampleHandler
{
    void onSample(Sample sample);
}
