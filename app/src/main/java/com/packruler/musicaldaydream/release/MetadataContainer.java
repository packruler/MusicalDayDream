package com.packruler.musicaldaydream.release;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaDescription;
import android.media.MediaMetadata;
import android.media.MediaMetadataRetriever;
import android.media.RemoteControlClient;
import android.media.RemoteController;
import android.media.session.MediaController;
import android.media.session.PlaybackState;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.util.Log;

/**
 * Created by packr_000 on 12/17/2014.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MetadataContainer {
    private final String TAG = this.getClass().getSimpleName();

    private MediaController mediaController;
    private String packageName;
    private LollipopMusicCallback mediaCallback;
    private MetadataCallback metadataCallback;
    private MediaMetadata mediaMetadata;
    private PlaybackState playbackState;
    private RemoteController.MetadataEditor metadataEditor;
    private int state;

    private boolean isPlaying;

    private final String[] metadataStrings = new String[3];
    private int getTitleInt = MediaMetadataRetriever.METADATA_KEY_TITLE;
    private int getArtistInt;
    private int getAlbumInt = MediaMetadataRetriever.METADATA_KEY_ALBUM;
    private Bitmap artworkBitmap;
    private String getArtworkString = "";
    private int getArtworkInt = RemoteControlClient.MetadataEditor.BITMAP_KEY_ARTWORK;

    private long duration;
    private long currentPosition;
    private boolean getVariablesSet = false;
    private boolean avoidTiming = false;

    private Context context;
    private Handler timingHandler;

    public MetadataContainer(Context contextIn, MediaController controller, MetadataCallback callbackIn) {
        context = contextIn;
        Log.i(TAG, "New MetadataContainer");

        //LOLLIPOP BONUS FEATURES
        mediaController = controller;
        playbackState = mediaController.getPlaybackState();
        mediaMetadata = mediaController.getMetadata();
        packageName = controller.getPackageName();

        //SET HANDLER THREADS
        HandlerThread thread = new HandlerThread(controller.getPackageName() + ".packruler.callback");
        thread.start();
        Handler handler = new Handler(thread.getLooper());
        thread = new HandlerThread(controller.getPackageName() + ".packruler.timing");
        thread.start();
        timingHandler = new Handler(thread.getLooper());

        //SET MEDIA_CALLBACK
        mediaCallback = new LollipopMusicCallback(mediaController.getPackageName(), this);
        mediaController.registerCallback(mediaCallback, handler);

        try {
            metadataStrings[0] = mediaMetadata.getDescription().getTitle().toString();
        } catch (Exception e) {
            metadataStrings[0] = "";
        }

        try {
            metadataStrings[1] = mediaMetadata.getDescription().getSubtitle().toString();
        } catch (Exception e) {
            metadataStrings[1] = "";
        }

        try {
            if (metadataStrings[1].equals("")) {
                metadataStrings[1] = mediaMetadata.getDescription().getDescription().toString();
            } else {
                metadataStrings[2] = mediaMetadata.getDescription().getDescription().toString();
            }
        } catch (Exception e) {
            metadataStrings[2] = "";
        }

        Log.i(TAG, "getArtworkString: " + getArtworkString);
        try {
            artworkBitmap = mediaMetadata.getDescription().getIconBitmap();
        } catch (Exception e) {
            e.printStackTrace();
        }
        getVariablesSet = true;

        newCallback(callbackIn);
        checkAvoidTiming();
    }

    public MetadataContainer(Context contextIn, RemoteController.MetadataEditor metadataEditorIn, MetadataCallback callbackIn) {
        metadataEditor = metadataEditorIn;
        context = contextIn;
        packageName = "KitKat";

        //SET HANDLER THREADS
        HandlerThread thread = new HandlerThread(getPackageName() + ".packruler.callback");
        thread.start();
        Handler handler = new Handler(thread.getLooper());
        thread = new HandlerThread(getPackageName() + ".packruler.timing");
        thread.start();
        timingHandler = new Handler(thread.getLooper());

        //SET TITLE
        metadataStrings[0] = metadataEditor.getString(getTitleInt, "");

        //SET ARTIST
        if (metadataEditor.getString(MediaMetadataRetriever.METADATA_KEY_ARTIST, null) != null) {
            getArtistInt = MediaMetadataRetriever.METADATA_KEY_ARTIST;
        } else if (metadataEditor.getString(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST, null) != null) {
            getArtistInt = MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST;
        } else {
            getArtistInt = MediaMetadataRetriever.METADATA_KEY_WRITER;
        }
        metadataStrings[1] = metadataEditor.getString(getArtistInt, "");

        //SET ALBUM
        metadataStrings[2] = metadataEditor.getString(getAlbumInt, "");

        //SET ART
        artworkBitmap = metadataEditor.getBitmap(getArtworkInt, artworkBitmap);

        newCallback(callbackIn);
    }

    @Override
    public boolean equals(Object o) {
        try {
            if (((MetadataContainer) o).getPackageName().equals(getPackageName())) {
                return true;
            }
        } catch (Exception e) {
        }

        return false;
    }

    private void checkAvoidTiming() {
//        if (mediaController != null) {
//            packageName = mediaController.getPackageName();
//            SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.settings_string), Context.MODE_MULTI_PROCESS);
//            if (sharedPreferences.getBoolean(context.getString(R.string.maintain_position), true)) {
//                String[] avoidStrings = context.getResources().getStringArray(R.array.ignore_running_time);
//                for (String avoid : avoidStrings) {
//                    if (avoid.equals(packageName)) {
//                        avoidTiming = true;
//                    }
//                }
//            } else {
//                avoidTiming = true;
//            }
//        }
//        Log.i(TAG, "Package: " + getPackageName() + " Avoid timing: " + avoidTiming);
    }

    private final Object metadataObject = new Object();

    public void updateMetadata(MediaMetadata metadata) {
        Log.i(TAG, "updateMetadata");
        mediaMetadata = metadata;
        if (mediaMetadata != null) {
            MediaDescription description = mediaMetadata.getDescription();
            Log.i(TAG, packageName + " Metadata Keys size: " + mediaMetadata.keySet().size());
            if (mediaMetadata.keySet().size() > 1) {

                Log.i(TAG, "Artwork String: " + getArtworkString);

                synchronized (metadataStrings) {
                    try {
                        metadataStrings[0] = mediaMetadata.getDescription().getTitle().toString();
                    } catch (Exception e) {
                        metadataStrings[0] = "";
                    }

                    try {
                        metadataStrings[1] = mediaMetadata.getDescription().getSubtitle().toString();
                    } catch (Exception e) {
                        metadataStrings[1] = "";
                    }

                    try {
                        if (metadataStrings[1].equals("")) {
                            metadataStrings[1] = mediaMetadata.getDescription().getDescription().toString();
                        } else {
                            metadataStrings[2] = mediaMetadata.getDescription().getDescription().toString();
                        }
                    } catch (Exception e) {
                        metadataStrings[2] = "";
                    }

                    duration = mediaMetadata.getLong(MediaMetadata.METADATA_KEY_DURATION);

                    try {
                        artworkBitmap = mediaMetadata.getDescription().getIconBitmap();
                    } catch (NullPointerException e) {
                        artworkBitmap = null;
                    }
                    if (metadataCallback != null) {
                        metadataCallback.updateMetadata(this);
                    }
                }
            } else {
                Log.i(TAG, "KeySize <= 1");
            }
        }
    }

    public void updateMetadata(RemoteController.MetadataEditor editor) {
        Log.i(TAG, "updateMetadata");
        metadataEditor = editor;


        synchronized (metadataObject) {
            metadataStrings[0] = metadataEditor.getString(getTitleInt, "");
            metadataStrings[1] = metadataEditor.getString(getArtistInt, "");
            metadataStrings[2] = metadataEditor.getString(getAlbumInt, "");

            artworkBitmap = metadataEditor.getBitmap(getArtworkInt, artworkBitmap);
            Log.i(TAG, "Bitmap null: " + (artworkBitmap == null));

            duration = metadataEditor.getLong(MediaMetadataRetriever.METADATA_KEY_DURATION, 0);

            if (metadataCallback != null) {
                metadataCallback.updateMetadata(this);
            }
        }
    }

    private final Object stateObject = new Object();

    public void updateState(int state, long stateChangeTimeMs, long currentPosMs) {
        Log.i(TAG, "updateState");
        synchronized (stateObject) {
            this.state = state;
            positionSetTime = stateChangeTimeMs;
            metadataPosition = currentPosMs;
            if (metadataPosition == -1) {
                avoidTiming = true;
                Log.i(TAG, "Auto avoid timing");
                currentPosition = 0;
            } else {
                currentPosition = metadataPosition + (SystemClock.elapsedRealtime() - positionSetTime);
            }
            if (state == RemoteControlClient.PLAYSTATE_PLAYING && !isPlaying) {
                isPlaying = true;
                updateMusicTimer();
            } else if (state == RemoteControlClient.PLAYSTATE_PAUSED ||
                    state == RemoteControlClient.PLAYSTATE_ERROR ||
                    state == RemoteControlClient.PLAYSTATE_STOPPED) {
                isPlaying = false;
            }

            if (metadataCallback != null) {
                metadataCallback.updateState(this);
            }
        }

    }

    public void updateState(PlaybackState inState) {
        Log.i(TAG, "updateState: " + inState.getState());
        Log.i(TAG, "Position: " + inState.getPosition() + " Last updated: " + inState.getLastPositionUpdateTime());
        synchronized (stateObject) {
            playbackState = inState;
            metadataPosition = inState.getPosition();
            positionSetTime = inState.getLastPositionUpdateTime();
            if (metadataPosition == -1) {
                avoidTiming = true;
                Log.i(TAG, "Auto avoid timing");
                currentPosition = 0;
            } else {
                currentPosition = metadataPosition + (SystemClock.elapsedRealtime() - positionSetTime);
            }


            if (playbackState.getState() == PlaybackState.STATE_PLAYING) {
                isPlaying = true;
            } else if (playbackState.getState() == PlaybackState.STATE_SKIPPING_TO_NEXT ||
                    playbackState.getState() == PlaybackState.STATE_SKIPPING_TO_PREVIOUS ||
                    playbackState.getState() == PlaybackState.STATE_SKIPPING_TO_QUEUE_ITEM) {
                Log.i(TAG, "isPlaying do not change");
            } else {
                isPlaying = false;
            }

            if (metadataCallback != null) {
                metadataCallback.updateState(this);
            }
        }

        updateMusicTimer();
    }

    public void newCallback(MetadataCallback callback) {
        metadataCallback = callback;
        metadataCallback.updateMetadata(this);
        metadataCallback.updateState(this);
    }

    public String getPackageName() {
        return packageName;
    }

    public LollipopMusicCallback getMediaCallback() {
        return mediaCallback;
    }

    public MediaController getMediaController() {
        return mediaController;
    }

    private MusicTimer musicTimer = new MusicTimer();

    public void updateMusicTimer() {
        timingHandler.removeCallbacks(musicTimer);
        if (isPlaying && !avoidTiming) {
            timingHandler.postDelayed(musicTimer, (1000 - (currentPosition % 1000)));
//            Log.i(TAG, "Handler Delay: " + (1000 - (currentPosition % 1000)));
        }
    }

    private long metadataPosition;
    private long positionSetTime;

    private class MusicTimer implements Runnable {
        @Override
        public void run() {
            if (isPlaying) {
//                Log.i(TAG, "Title: " + metadataStrings[0] + " Position: " +  (metadataPosition + (SystemClock.elapsedRealtime() - positionSetTime)));
                synchronized (stateObject) {
                    currentPosition = metadataPosition + (SystemClock.elapsedRealtime() - positionSetTime);
                    if (metadataCallback != null) {
                        metadataCallback.updateState(MetadataContainer.this);
                    }
                }
                updateMusicTimer();
            }
        }
    }

    public boolean isAvoidTiming() {
        return avoidTiming;
    }

    public void endContainer() {
        timingHandler.getLooper().quitSafely();
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public String[] getMetadataStrings() {
        return metadataStrings;
    }

    public long getDuration() {
        return duration;
    }

    public long getCurrentPosition() {
        return currentPosition;
    }

    public Bitmap getArtworkBitmap() {
        return artworkBitmap;
    }
}
