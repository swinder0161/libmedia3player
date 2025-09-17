package com.swinder.android.media3player;

import android.util.Log;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.DeviceInfo;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Metadata;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.PlaybackParameters;
import androidx.media3.common.Player;
import androidx.media3.common.Timeline;
import androidx.media3.common.TrackSelectionParameters;
import androidx.media3.common.Tracks;
import androidx.media3.common.VideoSize;
import androidx.media3.common.text.CueGroup;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.ui.AspectRatioFrameLayout;

import java.util.ArrayList;
import java.util.List;

@UnstableApi public class Media3PlayerListener implements Player.Listener {
    private final Media3Player mPlayer;
    public Media3PlayerListener(Media3Player player) {
        super();
        mPlayer = player;
    }
    @Override
    public void onEvents(@NonNull Player player, @NonNull Player.Events events) {
        Player.Listener.super.onEvents(player, events);
    }

    @Override
    public void onTimelineChanged(@NonNull Timeline timeline, int reason) {
        Player.Listener.super.onTimelineChanged(timeline, reason);
    }

    @Override
    public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
        Player.Listener.super.onMediaItemTransition(mediaItem, reason);
    }
    @Override
    public void onTracksChanged(@NonNull Tracks tracks) {
        Log.i(Media3Player.TAG, "> Media3PlayerListener onTracksChanged()");
        for(int type : mPlayer.mTracks.keySet()) {
            List<Tracks.Group> tg = mPlayer.mTracks.get(type);
            if (null != tg) tg.clear();
        }
        mPlayer.mTracks.clear();
        for (Tracks.Group g : tracks.getGroups()) {
            int type = g.getType();
            List<Tracks.Group> list= mPlayer.mTracks.get(type);
            if(list == null) {
                list = new ArrayList<>();
            }
            Log.i(Media3Player.TAG, ". Media3PlayerListener onTracksChanged() type: " + mPlayer.getTrackType(type));
            Log.i(Media3Player.TAG, ". Media3PlayerListener onTracksChanged() selected: " + g.isSelected());
            list.add(g);
            for(int i=0; i<g.length; i++) {
                Log.i(Media3Player.TAG, ". Media3PlayerListener onTracksChanged() g format: " + g.getTrackFormat(i));
            }
            mPlayer.mTracks.put(type, list);
        }
        mPlayer.processPrefs();
        Log.i(Media3Player.TAG, "< Media3PlayerListener onTracksChanged(): " + mPlayer.mTracks);
    }

    @Override
    public void onMediaMetadataChanged(@NonNull MediaMetadata mediaMetadata) {
        Log.i(Media3Player.TAG, ". Media3PlayerListener onMediaMetadataChanged()");
    }

    @Override
    public void onPlaylistMetadataChanged(@NonNull MediaMetadata mediaMetadata) {
        Log.i(Media3Player.TAG, ". Media3PlayerListener onPlaylistMetadataChanged()");
    }

    @Override
    public void onIsLoadingChanged(boolean isLoading) {
        Player.Listener.super.onIsLoadingChanged(isLoading);
    }

    @Override
    public void onAvailableCommandsChanged(@NonNull Player.Commands availableCommands) {
        Player.Listener.super.onAvailableCommandsChanged(availableCommands);
    }

    @Override
    public void onTrackSelectionParametersChanged(@NonNull TrackSelectionParameters parameters) {
        Log.i(Media3Player.TAG, ". Media3PlayerListener onTrackSelectionParametersChanged(): " + parameters);
    }

    @Override
    public void onPlaybackStateChanged(int playbackState) {
        Player.Listener.super.onPlaybackStateChanged(playbackState);
    }

    @Override
    public void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
        Player.Listener.super.onPlayWhenReadyChanged(playWhenReady, reason);
    }

    @Override
    public void onPlaybackSuppressionReasonChanged(int playbackSuppressionReason) {
        Player.Listener.super.onPlaybackSuppressionReasonChanged(playbackSuppressionReason);
    }

    @Override
    public void onIsPlayingChanged(boolean isPlaying) {
        Player.Listener.super.onIsPlayingChanged(isPlaying);
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {
        Player.Listener.super.onRepeatModeChanged(repeatMode);
    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
        Player.Listener.super.onShuffleModeEnabledChanged(shuffleModeEnabled);
    }

    @Override
    public void onPlayerError(@NonNull PlaybackException error) {
        Log.i(Media3Player.TAG, "> Media3PlayerListener onPlayerError() error: " + error.getMessage());
        if (null != mPlayer.mCallBack) mPlayer.mCallBack.resetPlayer();
        Log.i(Media3Player.TAG, "< Media3PlayerListener onPlayerError()");
    }

    @Override
    public void onPlayerErrorChanged(@Nullable PlaybackException error) {
        Player.Listener.super.onPlayerErrorChanged(error);
    }

    @Override
    public void onPositionDiscontinuity(@NonNull Player.PositionInfo oldPosition, @NonNull Player.PositionInfo newPosition, int reason) {
        Player.Listener.super.onPositionDiscontinuity(oldPosition, newPosition, reason);
    }

    @Override
    public void onPlaybackParametersChanged(@NonNull PlaybackParameters playbackParameters) {
        Player.Listener.super.onPlaybackParametersChanged(playbackParameters);
    }

    @Override
    public void onSeekBackIncrementChanged(long seekBackIncrementMs) {
        Player.Listener.super.onSeekBackIncrementChanged(seekBackIncrementMs);
    }

    @Override
    public void onSeekForwardIncrementChanged(long seekForwardIncrementMs) {
        Player.Listener.super.onSeekForwardIncrementChanged(seekForwardIncrementMs);
    }

    @Override
    public void onMaxSeekToPreviousPositionChanged(long maxSeekToPreviousPositionMs) {
        Player.Listener.super.onMaxSeekToPreviousPositionChanged(maxSeekToPreviousPositionMs);
    }

    @Override
    public void onAudioSessionIdChanged(int audioSessionId) {
        Player.Listener.super.onAudioSessionIdChanged(audioSessionId);
    }

    @Override
    public void onAudioAttributesChanged(@NonNull AudioAttributes audioAttributes) {
        Player.Listener.super.onAudioAttributesChanged(audioAttributes);
    }

    @Override
    public void onVolumeChanged(float volume) {
        Player.Listener.super.onVolumeChanged(volume);
    }

    @Override
    public void onSkipSilenceEnabledChanged(boolean skipSilenceEnabled) {
        Player.Listener.super.onSkipSilenceEnabledChanged(skipSilenceEnabled);
    }

    @Override
    public void onDeviceInfoChanged(@NonNull DeviceInfo deviceInfo) {
        Player.Listener.super.onDeviceInfoChanged(deviceInfo);
    }

    @Override
    public void onDeviceVolumeChanged(int volume, boolean muted) {
        Player.Listener.super.onDeviceVolumeChanged(volume, muted);
    }

    @Override
    public void onVideoSizeChanged(@NonNull VideoSize videoSize) {
        Log.i(Media3Player.TAG, "> Media3PlayerListener onVideoSizeChanged(): " + videoSize.width + "x" + videoSize.height);
        mPlayer.mVideoSize = new Size(videoSize.width, videoSize.height);
        int video_width = videoSize.width;
        int video_height = videoSize.height;
        int view_width = mPlayer.mView.getWidth();
        int view_height = mPlayer.mView.getHeight();
        if(video_width == 0 || video_height == 0 || view_width == 0 || view_height == 0) {
            return;
        }
        if((video_width*100)/video_height <= (view_width*100)/view_height && mPlayer.mSDFullEnabled) {
            Log.i(Media3Player.TAG, ". Media3PlayerListener onVideoSizeChanged() RESIZE_MODE_FILL");
            mPlayer.mView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
        } else {
            Log.i(Media3Player.TAG, ". Media3PlayerListener onVideoSizeChanged() RESIZE_MODE_FIT");
            mPlayer.mView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
        }
        Log.i(Media3Player.TAG, "< Media3PlayerListener onVideoSizeChanged()");
    }

    @Override
    public void onSurfaceSizeChanged(int width, int height) {
        Player.Listener.super.onSurfaceSizeChanged(width, height);
    }

    @Override
    public void onRenderedFirstFrame() {
        Log.i(Media3Player.TAG, ". Media3PlayerListener onRenderedFirstFrame()");
    }

    @Override
    public void onCues(@NonNull CueGroup cueGroup) {
        Log.i(Media3Player.TAG, ". Media3PlayerListener onCues(): " + cueGroup);
    }

    @Override
    public void onMetadata(@NonNull Metadata metadata) {
        Log.i(Media3Player.TAG, ". Media3PlayerListener onMetadata()");
    }
}
