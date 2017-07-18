package com.github.teocci.librtsp.rtp.player;

import android.media.MediaCodec;
import android.media.MediaCodec.CryptoException;
import android.os.Handler;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.ExoPlayer.ExoPlayerComponent;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecTrackRenderer.DecoderInitializationException;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.TrackRenderer;
import com.google.android.exoplayer.VideoSurfaceView;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2017-MAR-12
 */
public class RtpPlayer implements SurfaceHolder.Callback, ExoPlayer.Listener, MediaCodecVideoTrackRenderer.EventListener
{
    private VideoSurfaceView surfaceView;
    private ExoPlayer player;
    private TrackRenderer[] renders;
    private ExoPlayerComponent videoRenderer;
    private Handler handler;
    private Statistics stats;

    public RtpPlayer(int sourceCount, VideoSurfaceView view, Handler handler, Statistics stats)
    {
        this.renders = new TrackRenderer[sourceCount];
        this.surfaceView = view;
        this.handler = handler;
        this.stats = stats;
        this.player = ExoPlayer.Factory.newInstance(sourceCount, 100, 100);
        player.addListener(this);

        surfaceView.getHolder().addCallback(this);
    }

    public void prepare(RtpSampleSource... sources)
    {
        for (int i = 0; i < sources.length; i++) {
            RtpSampleSource source = sources[i];
            if (source.getFormat().mimeType.startsWith("video/")) {
                renders[i] = new MediaCodecVideoTrackRenderer(source, MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING,
                        0, handler, this, 10);
                videoRenderer = renders[i];
            } else if (source.getFormat().mimeType.startsWith("audio/")) {
                renders[i] = new MediaCodecAudioTrackRenderer(source, null, true);
            } else {
                throw new RuntimeException();
            }
        }

        player.prepare(renders);

        setSurface();
    }

    public void play()
    {
        player.setPlayWhenReady(true);
    }

    public void close()
    {
        player.stop();
        player.release();
        player = null;
    }

    private void setSurface()
    {
        Surface surface = surfaceView.getHolder().getSurface();
        if (videoRenderer == null || surface == null || !surface.isValid()) {
            return;
        }
        player.sendMessage(videoRenderer, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, surface);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        setSurface();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        if (videoRenderer != null) {
            player.blockingSendMessage(videoRenderer, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, null);
        }
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState)
    {
        stats.playbackState = playbackState;
    }

    @Override
    public void onPlayWhenReadyCommitted() {}

    @Override
    public void onPlayerError(ExoPlaybackException error) {}

    @Override
    public void onDecoderInitializationError(DecoderInitializationException e) {}

    @Override
    public void onCryptoError(CryptoException e) {}

    @Override
    public void onDecoderInitialized(String decoderName, long elapsedRealtimeMs, long initializationDurationMs) {}

    @Override
    public void onDroppedFrames(int count, long elapsed)
    {
        stats.renderDropFrameCount += count;
    }

    @Override
    public void onVideoSizeChanged(int width, int height, float pixelWidthHeightRatio) {}

    @Override
    public void onDrawnToSurface(Surface surface) {}
}
