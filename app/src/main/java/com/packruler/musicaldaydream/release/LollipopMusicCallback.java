package com.packruler.musicaldaydream.release;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import java.util.Calendar;
import java.util.List;

/**
 * Created by packr_000 on 10/30/2014.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class LollipopMusicCallback extends MediaController.Callback {
    private final String TAG = this.getClass().getSimpleName();
    private int arrayPosition;

    protected boolean isPlaying;
    protected long minutes;
    protected long seconds;
    protected long durationMinutes;
    protected long durationSeconds;
    protected long currentDuration = 0;
    protected long localPosition;
    protected boolean updateDuration = true;
    protected int progressBarSize;
    protected SharedPreferences sharedPreferences;
    private MediaMetadata metadata;
    private long currentPos;
    private Calendar calendar;
    private PlaybackState playbackState;
    private long duration;
    private int progress;
    private boolean updatePos;
    private String durationString;
    private String currentPosString;
    private Context context;
    private Object sync = new Object();
    private String packageName;
    private List<String> ignoreTime;
    private MetadataContainer container;

    public LollipopMusicCallback(String pkg, MetadataContainer container) {
//        sharedPreferences = ListenerService.context.getSharedPreferences(ListenerService.context.getString(R.string.settings_string), 0);
        packageName = pkg;
//        ignoreTime = Arrays.asList(context.getResources().getStringArray(R.array.ignore_running_time));
        this.container = container;
        metadata = container.getMediaController().getMetadata();
        if (metadata != null) {
            container.updateMetadata(metadata);
            container.updateState(container.getMediaController().getPlaybackState());
        }
        Log.i(TAG, "Package: " + packageName);

    }

    @Override
    public void onPlaybackStateChanged(PlaybackState inState) {
        super.onPlaybackStateChanged(inState);
        Log.i(TAG, "PlaybackStateChanged");
        container.updateState(inState);

////        Log.i(TAG, "PlaybackState change " + inState.toString());
//        playbackState = inState;
//        if (playbackState.getState() == PlaybackState.STATE_PLAYING) {
////            Log.i(TAG, "Playing Music");
//            isPlaying = true;
//            context.sendBroadcast(MusicalDaydreamService.musicStarted);
//
//            if (sharedPreferences.getBoolean(context.getString(R.string.maintain_position), true)) {
//                if (!ignoreTime.contains(packageName)) {
//                    if (timeThread == null) {
////                        Log.i(TAG, "new TimeRunnable()");
//                        timeThread = new TimeThread();
//                        timeThread.start();
//                    } else if (timeThread.isInterrupted()) {
////                        Log.i(TAG, "new TimeThread()");
//                        timeThread = new TimeThread();
//                        timeThread.start();
//                    } else if (!timeThread.isAlive()) {
//                        timeThread = new TimeThread();
//                        timeThread.start();
//                    }
//                } else {
//                    duration = metadata.getLong("android.media.metadata.DURATION");
//                    setDurationString();
//                }
//            }
//        } else if (playbackState.getState() == PlaybackState.STATE_ERROR ||
//                playbackState.getState() == PlaybackState.STATE_PAUSED ||
//                playbackState.getState() == PlaybackState.STATE_STOPPED) {
////            Log.i(TAG, "No music playing");
//            isPlaying = false;
//            context.sendBroadcast(MusicalDaydreamService.musicStopped);
//
//            if (sharedPreferences.getBoolean(context.getString(R.string.maintain_position), true)) {
//                if (!ignoreTime.contains(packageName)) {
//                    if (timeThread != null) {
//                        if (timeThread.isAlive()) {
//                            timeThread.interrupt();
//                        }
//                    }
//                }
//            }
//        }
//        if (sharedPreferences.getBoolean(context.getString(R.string.maintain_position), true)) {
//            Intent intent = new Intent(ListenerService.RECEIVER_STRING);
//            intent.putExtra(ListenerService.COMMAND, ListenerService.UPDATE_POSITION_DURATION);
//            intent.putExtra(ListenerService.DURATION_STRING, durationString);
//            intent.putExtra(ListenerService.CURRENT_POSITION_STRING, currentPosString);
//            intent.putExtra(ListenerService.ARRAY_POSITION, arrayPosition);
//            intent.putExtra(ListenerService.POSITION_PERCENT, progress);
//            intent.putExtra(ListenerService.PLAY_STATE, playbackState.getState());
//            context.sendBroadcast(intent);
//        } else {
//            Intent intent = new Intent(ListenerService.RECEIVER_STRING);
//            intent.putExtra(ListenerService.COMMAND, ListenerService.PLAYSTATE_UPDATE);
//            intent.putExtra(ListenerService.PLAY_STATE, playbackState.getState());
//            context.sendBroadcast(intent);
//        }
    }

    @Override
    public void onMetadataChanged(MediaMetadata inMetadata) {
        Log.i(TAG, "Metadata changed");
        super.onMetadataChanged(metadata);
        container.updateMetadata(inMetadata);

//
//        Intent intent = new Intent(ListenerService.RECEIVER_STRING);
//        intent.putExtra(ListenerService.COMMAND, ListenerService.UPDATE_METADATA);
//        intent.putExtra(ListenerService.ARRAY_POSITION, arrayPosition);
//
//        if (metadata.getString(MediaMetadata.METADATA_KEY_ARTIST) != null) {
//            intent.putExtra(ListenerService.ARTIST_STRING, metadata.getString(MediaMetadata.METADATA_KEY_ARTIST));
//        } else {
//            intent.putExtra(ListenerService.ARTIST_STRING, metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST));
//        }
//        intent.putExtra(ListenerService.ALBUM_STRING, metadata.getString(MediaMetadata.METADATA_KEY_ALBUM));
//        intent.putExtra(ListenerService.TITLE_STRING, metadata.getString(MediaMetadata.METADATA_KEY_TITLE));
//
//        ListenerService.albumBitmap.setImage(metadata.getBitmap(MediaMetadata.METADATA_KEY_ART));
//        if (ListenerService.albumBitmap.isNull())
//            ListenerService.albumBitmap.setImage(metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART));
//        if (ListenerService.albumBitmap.isNull())
//            ListenerService.albumBitmap.setImage(metadata.getBitmap(MediaMetadata.METADATA_KEY_ART_URI));
//        if (ListenerService.albumBitmap.isNull())
//            ListenerService.albumBitmap.setImage(metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART_URI));
//
//        ListenerService.context.sendBroadcast(intent);
        //Send metadata content to daydream
//        updateMetadata();
    }

    @Override
    public void onQueueChanged(List<MediaSession.QueueItem> inQueue) {
        super.onQueueChanged(inQueue);
    }

    @Override
    public void onQueueTitleChanged(CharSequence title) {
        super.onQueueTitleChanged(title);
    }

    @Override
    public void onExtrasChanged(Bundle extras) {
        super.onExtrasChanged(extras);
    }
}
