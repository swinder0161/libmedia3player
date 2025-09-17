package com.swinder.android.media3player;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.TrackSelectionOverride;
import androidx.media3.common.Tracks;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.dash.DashChunkSource;
import androidx.media3.exoplayer.dash.DashMediaSource;
import androidx.media3.exoplayer.dash.DefaultDashChunkSource;
import androidx.media3.exoplayer.drm.DefaultDrmSessionManager;
import androidx.media3.exoplayer.drm.DrmSessionManager;
import androidx.media3.exoplayer.drm.DrmSessionManagerProvider;
import androidx.media3.exoplayer.drm.FrameworkMediaDrm;
import androidx.media3.exoplayer.drm.LocalMediaDrmCallback;
import androidx.media3.exoplayer.hls.HlsMediaSource;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Media3Player {
    public static String TAG = "Media3Player";

    public interface Callback {
        void resetPlayer();
    }

    public static final int SOURCE_TYPE_MPEG_DASH = 0;
    public static final int SOURCE_TYPE_HLS = 1;
    public static final int SOURCE_TYPE_SS = 2;
    public static final int SOURCE_TYPE_HTTP_PROGRESSIVE = 3;


    public static final int TRACK_TYPE_VIDEO = C.TRACK_TYPE_VIDEO;
    public static final int TRACK_TYPE_AUDIO = C.TRACK_TYPE_AUDIO;
    public static final int TRACK_TYPE_TEXT = C.TRACK_TYPE_TEXT;
    final PlayerView mView;
    private final Context mContext;
    public final ExoPlayer mPlayer;
    private final String mStreamUrl;
    private final String mLicense;
    private final Map<String, String> httpHeader;
    final Map<Integer, List<Tracks.Group>> mTracks;
    Size mVideoSize;

    boolean mSDFullEnabled;

    private Size mPrefLimitVideo;
    private String mPrefAudio;
    private String mPrefSubs;
    final Callback mCallBack;

    @UnstableApi
    public Media3Player(Context context, PlayerView view, Callback callback, int contentType,
                        String streamUrl, String license, Map<String, String> header, boolean sdFullEnabled,
                        Size prefLimitVideo, String prefAudio, String prefSubs) {
        Log.i(TAG, "> Media3Player init() contentType: " + contentType + ", streamUrl: " + streamUrl +
                ", license: " + license + ", header: " + header);
        mTracks = new LinkedHashMap<>();
        mContext = context;
        mView = view;
        mCallBack = callback;
        mStreamUrl = streamUrl;
        mLicense = license;
        //for testing only - HLS
        //mStreamUrl = "https://bitmovin-a.akamaihd.net/content/sintel/hls/playlist.m3u8";
        //mLicense = null;
        //contentType = SOURCE_TYPE_HLS;
        //for testing only - MPEG DASH
        //streamUrl = "https://dash.akamaized.net/dash264/TestCasesHD/2b/qualcomm/1/MultiResMPEG2.mpd";
        //licenseUrl = null;
        //contentType = SOURCE_TYPE_MPEG_DASH;
        httpHeader = header;
        mSDFullEnabled = sdFullEnabled;
        mPrefLimitVideo = prefLimitVideo;
        mPrefAudio = prefAudio;
        mPrefSubs = prefSubs;
        mPlayer = new ExoPlayer.Builder(mContext).build();
        mPlayer.addListener(new Media3PlayerListener(this));
        mPlayer.setPlayWhenReady(true);
        mView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
        mView.setPlayer(mPlayer);
        MediaSource mediaSource = getMediaSource(contentType);
        if (null != mediaSource) {
            mPlayer.setMediaSource(mediaSource, true);
            mPlayer.prepare();
        }
        Log.i(TAG, "< Media3Player init()");
    }

    private MediaItem.DrmConfiguration getDrmConfiguration() {
        Log.i(TAG, "> Media3Player getDrmConfiguration() mLicense: " + mLicense);
        if (null != mLicense) {
            MediaItem.DrmConfiguration.Builder builder;
            if (mLicense.contains("\"keys\":")) {
                Log.i(TAG, ". Media3Player getDrmConfiguration() CLEAR KEY UUID");
                builder = new MediaItem.DrmConfiguration.Builder(C.CLEARKEY_UUID);
            } else {
                Log.i(TAG, ". Media3Player getDrmConfiguration() WIDEVINE UUID");
                builder = new MediaItem.DrmConfiguration.Builder(C.WIDEVINE_UUID)
                        .setLicenseUri(mLicense);
            }
            Log.i(TAG, "< Media3Player getDrmConfiguration()");
            return builder.build();
        }
        Log.i(TAG, "< Media3Player getDrmConfiguration() null");
        return new MediaItem.DrmConfiguration.Builder(C.WIDEVINE_UUID).build();
    }

    @UnstableApi
    private MediaSource getMediaSource(int contentType) {
        Log.i(TAG, "> Media3Player getMediaSource() contentType: " + contentType);
        String user_agent = "tpma/16.4 (Linux;Android 14) AndroidXMedia3/1.1.1";
        if(null != httpHeader) {
            if(httpHeader.containsKey("user-agent")) {
                user_agent = httpHeader.get("user-agent");
            }
        }
        Log.i(TAG, ". Media3Player getMediaSource() user_agent: " + user_agent);

        DefaultHttpDataSource.Factory defaultHttpDataSourceFactory = new DefaultHttpDataSource.Factory()
                .setUserAgent(user_agent)
                .setTransferListener(
                        new DefaultBandwidthMeter.Builder(mContext)
                                .setResetOnNetworkTypeChange(false)
                                .build()
                );

        DefaultHttpDataSource.Factory manifestDataSourceFactory = new DefaultHttpDataSource.Factory().setUserAgent(user_agent);

        if(null != httpHeader) {
            defaultHttpDataSourceFactory.setDefaultRequestProperties(httpHeader);
            manifestDataSourceFactory.setDefaultRequestProperties(httpHeader);
        }

        MediaSource mediaSource;
        switch (contentType) {
            case SOURCE_TYPE_MPEG_DASH: {
                Log.i(TAG, ". Media3Player getMediaSource() SOURCE_TYPE_MPEG_DASH");
                DashChunkSource.Factory dashChunkSourceFactory = new DefaultDashChunkSource.Factory(defaultHttpDataSourceFactory);
                DashMediaSource.Factory dashMediaSourceFactory = new DashMediaSource.Factory(dashChunkSourceFactory, manifestDataSourceFactory);
                if (null != mLicense) {
                    LocalMediaDrmCallback drmCallback = new LocalMediaDrmCallback(mLicense.getBytes());
                    DefaultDrmSessionManager drmSessionManager = new DefaultDrmSessionManager.Builder()
                            .setPlayClearSamplesWithoutKeys(true)
                            .setMultiSession(false)
                            .setKeyRequestParameters(httpHeader)
                            .setUuidAndExoMediaDrmProvider(C.CLEARKEY_UUID, FrameworkMediaDrm.DEFAULT_PROVIDER)
                            .build(drmCallback);
                    dashMediaSourceFactory.setDrmSessionManagerProvider(new DrmSessionManagerProvider() {
                        @NonNull
                        @Override
                        public DrmSessionManager get(@NonNull MediaItem mediaItem) {
                            return drmSessionManager;
                        }
                    });
                }
                mediaSource = dashMediaSourceFactory.createMediaSource(
                        new MediaItem.Builder()
                                .setUri(Uri.parse(mStreamUrl))
                                .setDrmConfiguration(getDrmConfiguration())
                                .setMimeType(MimeTypes.APPLICATION_MPD)
                                .setTag(null)
                                .build());
            } break;
            case SOURCE_TYPE_SS: {
                Log.i(TAG, ". Media3Player getMediaSource() SOURCE_TYPE_SS");
                mediaSource = null;
            } break;
            case SOURCE_TYPE_HLS: {
                Log.i(TAG, ". Media3Player getMediaSource() SOURCE_TYPE_HLS");
                mediaSource = new HlsMediaSource.Factory(manifestDataSourceFactory)
                        .createMediaSource(
                                new MediaItem.Builder()
                                        .setUri(Uri.parse(mStreamUrl))
                                        .setDrmConfiguration(getDrmConfiguration())
                                        .setMimeType(MimeTypes.APPLICATION_M3U8)
                                        .setTag(null)
                                        .build());
            } break;
            case SOURCE_TYPE_HTTP_PROGRESSIVE: {
                Log.i(TAG, ". Media3Player getMediaSource() SOURCE_TYPE_HTTP_PROGRESSIVE");
                mediaSource = null;
            } break;
            default: {
                mediaSource = null;
            }
        }
        Log.i(TAG, "< Media3Player getMediaSource() mediaSource: " + mediaSource);
        return mediaSource;
    }

    public void setPlayWhenReady(boolean b) {
        mPlayer.setPlayWhenReady(b);
    }

    public void release() {
        mPlayer.stop();
        mPlayer.release();
    }

    String getTrackType(int trackType) {
        switch(trackType) {
            case TRACK_TYPE_VIDEO: return "TRACK_TYPE_VIDEO";
            case TRACK_TYPE_AUDIO: return "TRACK_TYPE_AUDIO";
            case TRACK_TYPE_TEXT: return "TRACK_TYPE_TEXT";
        }
        return "TRACK_TYPE_" + trackType;
    }

    public List<Tracks.Group> getTracks(int type) {
        return mTracks.get(type);
    }

    public Size getMaxVideoSize() {
        int width=0, height=0;
        List<Tracks.Group> list = getTracks(TRACK_TYPE_VIDEO);
        if(list != null) {
            for(Tracks.Group g:list) {
                for(int i=0; i<g.length; i++) {
                    if(width < g.getTrackFormat(i).width)
                        width = g.getTrackFormat(i).width;
                    if(height < g.getTrackFormat(i).height)
                        height = g.getTrackFormat(i).height;
                }
            }
        }
        return new Size(width, height);
    }

    public void limitVideoSize(Size sz) {
        Log.i(TAG, "> Media3Player limitVideoSize(): " + sz.getWidth() + "x" + sz.getHeight());
        mPlayer.setTrackSelectionParameters(mPlayer.getTrackSelectionParameters().buildUpon()
                .setMaxVideoSize(sz.getWidth(), sz.getHeight())
                .build());
        Log.i(TAG, "< Media3Player limitVideoSize()");
    }

    public void selectAudioTrack(String fmt) {
        Log.i(TAG, "> Media3Player selectAudioTrack() fmt: " + fmt);
        if(fmt == null) return;
        List<Tracks.Group> audioTracks = mTracks.get(TRACK_TYPE_AUDIO);
        int ii=-1,jj=-1;
        if(audioTracks != null) {
            for (int i = 0; i < audioTracks.size(); i++) {
                Tracks.Group g = audioTracks.get(i);
                for (int j = 0; j < g.length; j++) {
                    if (fmt.equalsIgnoreCase(g.getTrackFormat(j).toString())) {
                        ii = i;
                        jj = j;
                        break;
                    }
                }
            }
        }
        if(ii == -1 || jj == -1) return;
        Log.i(TAG, ". Media3Player selectAudioTrack() select: " + ii + "-" + jj);
        mPlayer.setTrackSelectionParameters(mPlayer.getTrackSelectionParameters().buildUpon()
                .setTrackTypeDisabled(TRACK_TYPE_AUDIO, false)
                .setOverrideForType(new TrackSelectionOverride(audioTracks.get(ii).getMediaTrackGroup(), jj))
                .build());
        Log.i(TAG, "< Media3Player selectAudioTrack()");
    }

    public void selectSubtitlesTrack(String fmt) {
        Log.i(TAG, "> Media3Player selectSubtitlesTrack() fmt: " + fmt);
        List<Tracks.Group> subsTracks = mTracks.get(TRACK_TYPE_TEXT);
        int ii=-1,jj=-1;
        if (fmt != null && subsTracks != null) {
            for (int i = 0; i < subsTracks.size(); i++) {
                Tracks.Group g = subsTracks.get(i);
                for (int j = 0; j < g.length; j++) {
                    if (fmt.equalsIgnoreCase(g.getTrackFormat(j).toString())) {
                        ii = i;
                        jj = j;
                        break;
                    }
                }
            }
        }
        if(ii == -1 || jj == -1) {
            Log.i(TAG, ". Media3Player selectSubtitlesTrack() disable");
            mPlayer.setTrackSelectionParameters(mPlayer.getTrackSelectionParameters().buildUpon()
                    .setTrackTypeDisabled(TRACK_TYPE_TEXT, true)
                    .build());
        } else {
            Log.i(TAG, ". Media3Player selectSubtitlesTrack() enable: " + ii + "-" + jj);
            mPlayer.setTrackSelectionParameters(mPlayer.getTrackSelectionParameters().buildUpon()
                    .setTrackTypeDisabled(TRACK_TYPE_TEXT, false)
                    .setOverrideForType(new TrackSelectionOverride(subsTracks.get(ii).getMediaTrackGroup(), jj))
                    .setSelectUndeterminedTextLanguage(true)
                    .build());
        }
        Log.i(TAG, "< Media3Player selectSubtitlesTrack()");
    }

    void processPrefs() {
        if (null != mPrefLimitVideo) limitVideoSize(mPrefLimitVideo);
        if (null != mPrefAudio) selectAudioTrack(mPrefAudio);
        if (null != mPrefSubs) selectSubtitlesTrack(mPrefSubs);
        mPrefLimitVideo = null;
        mPrefAudio = null;
        mPrefSubs = null;
    }

    public Size getVideoSize() {
        return mVideoSize;
    }

    @UnstableApi
    public int getAudioChannels() {
        int channels = -1;
        if(mPlayer != null && mPlayer.getAudioFormat() != null)
            channels = mPlayer.getAudioFormat().channelCount;
        return channels;
    }

    public boolean getSubtitles() {
        return mTracks.get(TRACK_TYPE_TEXT) != null;
    }
}
