package com.packruler.musicaldaydream.release;


import android.annotation.TargetApi;
import android.app.Instrumentation;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.RemoteController;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.KeyEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by Packruler on 4/8/2014.
 */
public class ListenerService extends android.service.notification.NotificationListenerService implements RemoteController.OnClientUpdateListener, MetadataCallback {
    private final String TAG = this.getClass().getSimpleName();

    protected static final String RECEIVER_STRING = "com.packruler.MusicalDaydream.NOTIFICATION_LISTENER_SERVICE";
    protected static final String CLEAR_NOTIFICATION = "CLEAR_NOTIFICATION";
    protected static final String NOTIFICATION_PACKAGE = "NOTIFICATION_PACKAGE";
    protected static final String NOTIFICATION_TAG = "NOTIFICATION_TAG";
    protected static final String NOTIFICATION_ID = "NOTIFICATION_ID";
    protected static final String ARRAY_POSITION = "ARRAY_POSITION";
    protected static final String UPDATE_METADATA = "UPDATE_METADATA";
    protected static final String INITIAL_METADATA = "INITIAL_METADATA";
    protected static final String INITIAL_NOTIFICATION_SET = "INITIAL_NOTIFICATION_SET";
    protected static final String ARTIST_STRING = "ARTIST_STRING";
    protected static final String ALBUM_STRING = "ALBUM_STRING";
    protected static final String TITLE_STRING = "TITLE_STRING";
    protected static final String UPDATE_POSITION_DURATION = "UPDATE_POSITION_DURATION";
    protected static final String UPDATE_DURATION = "UPDATE_DURATION";
    protected static final String UPDATE_POSITION = "UPDATE_POSITION";
    protected static final String POSITION_PERCENT = "POSITION_PERCENT";
    protected static final String CURRENT_POSITION_STRING = "CURRENT_POSITION_STRING";
    protected static final String DURATION_STRING = "DURATION_STRING";
    protected static final String PLAY_STATE = "PLAY_STATE";
    protected static final String MUSIC_PLAYING = "MUSIC_PLAYING";
    protected static final String MUSIC_STOPPED = "MUSIC_STOPPED";
    protected static final String START_TIMEOUT = "START_TIMEOUT";
    protected static final String STOP_TIMEOUT = "STOP_TIMEOUT";
    protected static final String COMMAND = "command";
    protected static final String PLAY_PAUSE = "PLAY_PAUSE";
    protected static final String PLAY = "PLAY";
    protected static final String PAUSE = "PAUSE";
    protected static final String SKIP_FORWARD = "SKIP_FORWARD";
    protected static final String SKIP_BACK = "SKIP_BACK";
    protected static final String LAUNCH_PLAYER = "LAUNCH_PLAYER";
    protected static final String PLAYSTATE_UPDATE = "PLAYSTATE_UPDATE";
    protected static final String UPDATED_IMAGE = "UPDATED_IMAGE";

    private int activePlayer = 0;

    protected boolean serviceRunning = false;

    private ServiceReceiver serviceReceiver;
    private RemoteController remoteController;
    private Context mContext;
    protected boolean isPlaying = false;

    protected Set<String> notificationPackagesSet = new HashSet<String>();
    protected Set<String> notificationPackageLabelSet = new HashSet<String>();
    protected static ArrayList<StatusBarNotification> statusBarNotificationArrayList = new ArrayList<StatusBarNotification>();

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor preferenceEditor;

    private NotificationDisplayUpdateThread notificationDisplayUpdateThread;
    private Thread getActiveNotificationsThread;


    protected Context context;

    private PackageManager packageManager;

    private OnCreateThread onCreateThread;
    protected ArrayList<MetadataContainer> metadataContainers = new ArrayList<>();
    private ListenerBinder mBinder = new ListenerBinder();

    protected MetadataContainer kitKatMetadataContainer;
    private ArrayList<MetadataCallback> metadataCallbacks = new ArrayList<>();
    protected static boolean started = false;
    private Handler backgroundHandler;

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        Log.i(TAG, "Class Name: " + getClass().getName());
        mContext = getApplicationContext();
        onCreateThread = new OnCreateThread();
        onCreateThread.start();
        Log.i(TAG, "onCreate");
        HandlerThread thread = new HandlerThread(getPackageName() + ".ListenerService.background");
        thread.start();
        backgroundHandler = new Handler(thread.getLooper());

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            remoteController = new RemoteController(mContext, this);
            boolean controllerInitialized = audioManager.registerRemoteController(remoteController);
            if (!controllerInitialized) {
                //handle registration failure
                Log.i(TAG, "Remote Not Initialized");
            } else {
                remoteController.setArtworkConfiguration(3000, 3000);
                Log.i(TAG, "Remote Initialized");
            }
        } else {
            MediaSessionManager mediaSessionManager = (MediaSessionManager) getSystemService(MEDIA_SESSION_SERVICE);
            List<MediaController> mediaControllers = mediaSessionManager.getActiveSessions(new ComponentName(getPackageName(), ListenerService.class.getName()));
            for (MediaController controller : mediaControllers) {
                metadataContainers.add(new MetadataContainer(this, controller, this));
                Log.i(TAG, controller.getPackageName() + "Controller Found");
            }
            if (metadataContainers.size() > 0) {
                activeContainer = metadataContainers.get(0);
            }

            mediaSessionManager.addOnActiveSessionsChangedListener(new ActiveControllerListener(), new ComponentName(getPackageName(), ListenerService.class.getName()));
        }
        started = true;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onDestroy() {
        super.onDestroy();
        serviceRunning = false;
        unregisterReceiver(serviceReceiver);
        ((AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE)).unregisterRemoteController(remoteController);
        if (getActiveNotificationsThread != null) {
            if (getActiveNotificationsThread.isAlive()) {
                getActiveNotificationsThread.interrupt();
            }
        }

        for (MetadataContainer container : metadataContainers) {
            container.endContainer();
        }
        if (kitKatMetadataContainer != null) {
            kitKatMetadataContainer.endContainer();
        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            if (mediaControllers != null && mediaControllers.size() > 0) {
//                for (int x = 0; x < mediaControllers.size(); x++) {
//                    mediaControllers.get(x).unregisterCallback(callbacks.get(x));
//                }
//            }
//        }
        started = false;
        Log.i(TAG, "Service Ended");
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (!sharedPreferences.getStringSet(getString(R.string.blacklist), new HashSet<String>()).contains(sbn.getPackageName())) {
            getNotifications();
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        if (!sharedPreferences.getStringSet(getString(R.string.blacklist), new HashSet<String>()).contains(sbn.getPackageName())) {
            getNotifications();
        }
    }

    /**
     * Called whenever all information, previously received through the other
     * methods of the listener, is no longer valid and is about to be refreshed.
     * This is typically called whenever a new {@link  android.media.RemoteControlClient} has been
     * selected
     * by the system to have its media information published.
     *
     * @param clearing
     *         true if there is no selected RemoteControlClient and no information
     *         is available.
     */
    @Override
    public void onClientChange(boolean clearing) {

    }

    /**
     * Called whenever the playback state has changed.
     * It is called when no information is known about the playback progress in the media and
     * the playback speed.
     *
     * @param state
     *         one of the playback states authorized
     *         in {@link android.media.RemoteControlClient#setPlaybackState(int)}.
     */
    @Override
    public void onClientPlaybackStateUpdate(int state) {
    }


    @Override
    public void onClientPlaybackStateUpdate(int state, long stateChangeTimeMs, long currentPosMs, float speed) {
        Log.i(TAG, "Playback state update");
        if (kitKatMetadataContainer != null) {
            Log.i(TAG, "Container ready");
            kitKatMetadataContainer.updateState(state, stateChangeTimeMs, currentPosMs);
            activeContainer = kitKatMetadataContainer;
        }
    }

    /**
     * Called whenever the transport control flags have changed.
     *
     * @param transportControlFlags
     *         one of the flags authorized
     *         in {@link android.media.RemoteControlClient#setTransportControlFlags(int)}.
     */
    @Override
    public void onClientTransportControlUpdate(int transportControlFlags) {
    }

    /**
     * Called whenever new metadata is available.
     * Used to set metadata display variables.
     * See the {@link android.media.MediaMetadataEditor#putLong(int, long)},
     * {@link  android.media.MediaMetadataEditor#putString(int, String)},
     * {@link  android.media.MediaMetadataEditor#putBitmap(int, Bitmap)}, and
     * {@link  android.media.MediaMetadataEditor#putObject(int, Object)} methods for the various
     * keys that
     * can be queried.
     *
     * @param metadataEditor
     *         the container of the new metadata.
     */
    @Override
    public void onClientMetadataUpdate(RemoteController.MetadataEditor metadataEditor) {
        if (kitKatMetadataContainer == null) {
            kitKatMetadataContainer = new MetadataContainer(this, metadataEditor, this);
            activeContainer = kitKatMetadataContainer;
        } else {
            kitKatMetadataContainer.updateMetadata(metadataEditor);
            activeContainer = kitKatMetadataContainer;
        }

//        //Update metadata strings
//        if (!metadataEditor.getString(MediaMetadataRetriever.METADATA_KEY_ARTIST, "").isEmpty()) {
//            artistString = metadataEditor.getString(MediaMetadataRetriever.METADATA_KEY_ARTIST, "");
//        } else {
//            artistString = metadataEditor.getString(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST, "");
//        }
//        albumString = metadataEditor.getString(MediaMetadataRetriever.METADATA_KEY_ALBUM, "");
//        titleString = metadataEditor.getString(MediaMetadataRetriever.METADATA_KEY_TITLE, "");
//
//        //Update albumBitmap the post to artView ImageView
//        albumBitmap.setImage(metadataEditor.getBitmap(MediaMetadataEditor.BITMAP_KEY_ARTWORK, albumBitmap.getImage()));
//
//        duration = metadataEditor.getLong(MediaMetadataRetriever.METADATA_KEY_DURATION, duration);
//
//        //Send metadata content to daydream
//        updateMetadata();
    }

    public void getNotifications() {
        try {
            final StatusBarNotification[] activeNotifications = getActiveNotifications();
            if (getActiveNotificationsThread == null) {
                getActiveNotificationsThread = new Thread();
            }
            if (!getActiveNotificationsThread.isAlive() && activeNotifications != null) {
                getActiveNotificationsThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //initializeNotificationArray();
//                        Log.i(TAG, "Setting Notification array");
                        statusBarNotificationArrayList.clear();
                        if (activeNotifications.length > 0) {
                            for (StatusBarNotification sbn : activeNotifications) {
                                if (!sharedPreferences.getStringSet(getString(R.string.blacklist), new HashSet<String>()).contains(sbn.getPackageName())) {
                                    statusBarNotificationArrayList.add(sbn);
                                }
                            }
                        }
                        boolean restart = true;
//                        String arrayPackages = "Notification Array Includes:";
                        while (restart) {
                            boolean foundRemoval = false;
                            synchronized (statusBarNotificationArrayList) {
                                if (statusBarNotificationArrayList.size() > 0) {
                                    for (int x = 0; x < statusBarNotificationArrayList.size(); x++) {
                                        if (!notificationPackagesSet.contains(statusBarNotificationArrayList.get(x).getPackageName())) {
                                            notificationPackagesSet.add(statusBarNotificationArrayList.get(x).getPackageName());
                                            preferenceEditor.putStringSet(getString(R.string.notification_packages), notificationPackagesSet);
                                            preferenceEditor.commit();
                                        }
                                        if (!foundRemoval && statusBarNotificationArrayList.size() >= 1) {
                                            if (sharedPreferences.getStringSet(getString(R.string.blacklist), new HashSet<String>()).contains(statusBarNotificationArrayList.get(x).getPackageName())) {
                                                Log.i(TAG, statusBarNotificationArrayList.get(x).getPackageName() + " removed from array");
                                                statusBarNotificationArrayList.remove(x);
                                                foundRemoval = true;
                                                break;
                                            } else {
//                                                arrayPackages = arrayPackages + "\n" + statusBarNotificationArrayList.get(x).getPackageName();
                                            }
                                        }
                                    }
                                }
                                if (!foundRemoval) {
                                    restart = false;
                                }
                            }
                        }
//                        Log.i(TAG, arrayPackages);
//                        setMusicActivity();

                        notificationDisplayUpdateThread = new NotificationDisplayUpdateThread();
                        notificationDisplayUpdateThread.start();

                        new VerifyInstall().start();
                    }
                });
                getActiveNotificationsThread.start();
            }
        } catch (Exception e) {
            if (e instanceof NullPointerException) {
                Log.i(TAG, "Not ready to get notifications");
            } else {
                e.printStackTrace();
            }
        }
    }

    private MetadataContainer activeContainer;

    @Override
    public void updateMetadata(MetadataContainer container) {
        if (container != null) {
            int position = metadataContainers.indexOf(container);
            Log.i(TAG, "updateMetadata() Container pkg: " + container.getPackageName() + " index: " + position + " isPlaying: " + container.isPlaying());

            if (activeContainer != null) {
                if (!activeContainer.equals(container)) {
                    if (kitKatMetadataContainer == null) {
                        if (position == 0) {
                            activeContainer = container;
                            pushMetadata();
                        } else if (position > 0 && container.isPlaying()) {
                            Log.i(TAG, "Moved to top");
                            Collections.swap(metadataContainers, position, 0);
                            activeContainer = container;
                            pushMetadata();
                        }
                    } else if (activeContainer == null) {
                        activeContainer = container;
                        pushMetadata();
                    }

                } else {
                    pushMetadata();
                }
            } else {
                activeContainer = container;
                pushMetadata();
            }
        }
    }

    private void pushMetadata() {
        for (MetadataCallback callback : metadataCallbacks) {
            callback.updateMetadata(activeContainer);
        }
    }

    @Override
    public void updateState(MetadataContainer container) {
        if (container != null) {
            int position = metadataContainers.indexOf(container);
            Log.i(TAG, "updateState() Container pkg: " + container.getPackageName() + " index: " + position + " isPlaying: " + container.isPlaying());

            if (activeContainer != null) {
                if (!activeContainer.equals(container)) {
                    if (kitKatMetadataContainer == null) {
                        if (position == 0) {
                            activeContainer = container;
                            pushState();
                        } else if (position > 0 && container.isPlaying()) {
                            Log.i(TAG, "Moved to top");
                            Collections.swap(metadataContainers, position, 0);
                            activeContainer = container;
                            pushState();
                        }
                    } else if (activeContainer == null) {
                        activeContainer = container;
                        pushState();
                    }

                } else {
                    pushState();
                }
            } else {
                activeContainer = container;
                pushState();
            }
        }
    }

    private void pushState() {
        for (MetadataCallback callback : metadataCallbacks) {
            callback.updateState(activeContainer);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private class ServiceReceiver extends BroadcastReceiver {
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onReceive(Context context, Intent intent) {
//            try {
//                if (intent.getStringExtra(COMMAND) != null) {
////                    Log.i(TAG, "Command " + intent.getStringExtra(COMMAND));
//                    if (intent.getStringExtra(COMMAND).equals(UPDATE_METADATA)) {
//                        Log.i(TAG, UPDATE_METADATA);
//                        updateMetadata(intent.getStringExtra(ARTIST_STRING),
//                                intent.getStringExtra(ALBUM_STRING),
//                                intent.getStringExtra(TITLE_STRING));
//                    } else if (intent.getStringExtra(COMMAND).equals(INITIAL_METADATA)) {
//                        Log.i(TAG, INITIAL_METADATA);
//                        if (metadataContainers != null) {
//                            if (metadataContainers.size() > activePlayer) {
//                                if (metadataContainers.get(activePlayer).getMediaController().getMetadata() == null) {
//                                    for (int x = 0; x < metadataContainers.size(); x++) {
//                                        if (metadataContainers.get(x).getMediaController().getMetadata() != null) {
//                                            activePlayer = x;
//                                            x = metadataContainers.size() + 1;
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                        updateMetadata();
//                        updateMediaPosition();
//                    } else if (intent.getStringExtra(COMMAND).equals(UPDATE_POSITION_DURATION)) {
////                        Log.i(TAG, UPDATE_POSITION_DURATION);
//                        if (intent.getIntExtra(PLAY_STATE, 0) == PlaybackState.STATE_PLAYING) {
//                            isPlaying = true;
//                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                                activePlayer = intent.getIntExtra(ARRAY_POSITION, 0);
//                                MediaMetadata metadata = metadataContainers.get(activePlayer).getMediaController().getMetadata();
//                                int pos = 0;
//                                while (metadata == null && pos < metadataContainers.size()) {
//                                    metadata = metadataContainers.get(pos).getMediaController().getMetadata();
//                                    if (metadata != null) {
//                                        activePlayer = pos;
//                                    }
//                                    pos++;
//                                }
//                                if (metadata != null) {
//                                    if (metadata.getString(MediaMetadata.METADATA_KEY_ARTIST) != null) {
//                                        artistString = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST);
//                                    } else {
//                                        artistString = metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST);
//                                    }
//                                    albumString = metadata.getString(MediaMetadata.METADATA_KEY_ALBUM);
//                                    titleString = metadata.getString(MediaMetadata.METADATA_KEY_TITLE);
//
////                                    albumBitmap.setImage(metadata.getBitmap(MediaMetadata.METADATA_KEY_ART));
////                                    if (albumBitmap.isNull()) {
////                                        albumBitmap.setImage(metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART));
////                                    }
////                                    if (albumBitmap.isNull()) {
////                                        albumBitmap.setImage(metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART_URI));
////                                    }
//                                }
//                                updateMediaPosition();
//                            }
//                        } else {
//                            isPlaying = false;
//                        }
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                            updateMediaPosition(intent.getStringExtra(CURRENT_POSITION_STRING),
//                                    intent.getStringExtra(DURATION_STRING),
//                                    intent.getIntExtra(PLAY_STATE, playState),
//                                    intent.getIntExtra(POSITION_PERCENT, 0));
//                        } else {
//                            updateMediaPosition(intent.getStringExtra(CURRENT_POSITION_STRING),
//                                    intent.getStringExtra(DURATION_STRING),
//                                    intent.getIntExtra(POSITION_PERCENT, 0));
//                        }
//                    } else if (intent.getStringExtra(COMMAND).equals(PLAYSTATE_UPDATE)) {
//                        playState = intent.getIntExtra(PLAY_STATE, 0);
//                        if (playState == PlaybackState.STATE_PLAYING) {
//                            isPlaying = true;
//                        } else {
//                            isPlaying = false;
//                        }
//                        Intent i = new Intent(MusicalDaydreamService.MAIN_RECEIVER);
//                        i.putExtra(MusicalDaydreamService.COMMAND, MusicalDaydreamService.PLAYSTATE_UPDATE);
//                        i.putExtra(MusicalDaydreamService.PLAY_STATE, playState);
//                        sendBroadcast(i);
//                    }
//                    //Initialize NotificationArrayList
//                    if (intent.getStringExtra(COMMAND).equals(INITIAL_NOTIFICATION_SET)) {
//                        getNotifications();
//                    } else if (intent.getStringExtra(COMMAND).equals(PLAY_PAUSE)) {
//                        playPause();
//                    } else if (intent.getStringExtra(COMMAND).equals(PLAY)) {
//                        play();
//                    } else if (intent.getStringExtra(COMMAND).equals(PAUSE)) {
//                        pause();
//                    } else if (intent.getStringExtra(COMMAND).equals(SKIP_BACK)) {
//                        skipBack();
//                    } else if (intent.getStringExtra(COMMAND).equals(SKIP_FORWARD)) {
//                        skipForward();
//                    } else if (intent.getStringExtra(COMMAND).equals(CLEAR_NOTIFICATION)) {
//                        String pkg = intent.getStringExtra(NOTIFICATION_PACKAGE);
//                        String tag = intent.getStringExtra(NOTIFICATION_TAG);
//                        int id = intent.getIntExtra(NOTIFICATION_ID, 0);
//                        clearNotification(pkg, tag, id);
//                    } else if (intent.getStringExtra(COMMAND).equals(LAUNCH_PLAYER)) {

//                    } else if (intent.getStringExtra(COMMAND).equals(UPDATED_IMAGE)) {
//                        intent = new Intent(MusicalDaydreamService.MAIN_RECEIVER);
//                        intent.putExtra(MusicalDaydreamService.COMMAND, MusicalDaydreamService.UPDATED_IMAGE);
//                        sendBroadcast(intent);
//                    }
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
        }
    }

    private class OnCreateThread extends Thread {
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void run() {
            Looper.prepare();

//        context = getApplicationContext();
//        thisListener = this;
            sharedPreferences = getSharedPreferences(getString(R.string.settings_string), MODE_MULTI_PROCESS);
            packageManager = getPackageManager();
            preferenceEditor = sharedPreferences.edit();
            if (sharedPreferences.getStringSet(getString(R.string.notification_packages), notificationPackagesSet) != null) {
                notificationPackagesSet = sharedPreferences.getStringSet(getString(R.string.notification_packages), notificationPackagesSet);
            }

            serviceRunning = true;
            isPlaying = false;

            HandlerThread thread = new HandlerThread("MyHandlerThread");
            thread.start();
            Handler threadHandler = new Handler(thread.getLooper());
            serviceReceiver = new ServiceReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(UPDATE_METADATA);
            filter.addAction(RECEIVER_STRING);
            registerReceiver(serviceReceiver, filter, null, threadHandler);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    getNotifications();
                }
            }, 1000);
        }
    }

    private class VerifyInstall extends Thread {
        @Override
        public void run() {
            ArrayList<String> removals = new ArrayList<String>();

            for (String packages : notificationPackagesSet) {
                try {
                    packageManager.getApplicationInfo(packages, 0);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                    removals.add(packages);
                }
            }
            if (!removals.isEmpty()) {
                for (int x = 0; x < removals.size(); x++) {
                    Log.i(TAG, "Remove " + removals.get(x) + " " + notificationPackagesSet.remove(removals.get(x)));
                    notificationPackagesSet.remove(removals.get(x));
                }
            }
            preferenceEditor.putStringSet(getString(R.string.notification_packages), notificationPackagesSet);
            preferenceEditor.commit();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public class ActiveControllerListener implements MediaSessionManager.OnActiveSessionsChangedListener {
        @Override
        public void onActiveSessionsChanged(List<MediaController> controllers) {
            Log.i(TAG, "ActiveSessionsChanged");

            for (MediaController controller : controllers) {
                boolean found = false;
                for (MetadataContainer container : metadataContainers) {
                    if (container.getPackageName().equals(controller.getPackageName())) {
                        found = true;
                    }
                }
                if (!found) {
                    Log.i(TAG, "Adding: " + controller.getPackageName());
                    metadataContainers.add(new MetadataContainer(ListenerService.this, controller, ListenerService.this));

                }
            }
            if (activeContainer == null && metadataContainers.size() > 0) {
                activeContainer = metadataContainers.get(0);
            }
            Iterator<MetadataContainer> iterator = metadataContainers.iterator();
            while (iterator.hasNext()) {
                try {
                    boolean found = false;
                    MetadataContainer container = iterator.next();
                    for (int x = 0; x < controllers.size(); x++) {
                        if (controllers.get(x).getPackageName().equals(container.getPackageName())) {
                            found = true;
                        }
                    }
                    if (!found) {
                        Log.i(TAG, "Removed: " + container.getPackageName());
                        container.endContainer();
                        if (activeContainer.equals(container)) {
                            activeContainer = null;
                            pushState();
                        }
                        iterator.remove();
                        activePlayer = 0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void playPause() {
        Log.i(TAG, PLAY_PAUSE + " KitKat: " + (kitKatMetadataContainer != null));
        //Receive play/pause command and send command to device
        if (kitKatMetadataContainer != null || metadataContainers.size() == 0) {
            Log.i(TAG, PLAY_PAUSE + "Send button press");
//            KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
            backgroundHandler.post(new Runnable() {
                @Override
                public void run() {
                    new Instrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
                }
            });
//            keyEvent = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
//            boolean second = remoteController.sendMediaKeyEvent(keyEvent);
//            if (!first || !second) {
//                Log.i(TAG, "Remote Initialization Error" + "\n" + "Action Down = " + first + " | Action Up = " + second);
//            }
        } else {
            Log.i(TAG, PLAY_PAUSE + " Send transport press");
            if (metadataContainers.get(0).isPlaying()) {
                metadataContainers.get(0).getMediaController().getTransportControls().pause();
            } else {
                metadataContainers.get(0).getMediaController().getTransportControls().play();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void play() {
        if (kitKatMetadataContainer != null || metadataContainers.size() == 0) {
            playPause();
        } else {
            Log.i(TAG, PLAY_PAUSE + " Send transport press");
            metadataContainers.get(0).getMediaController().getTransportControls().play();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void pause() {
        if (kitKatMetadataContainer != null || metadataContainers.size() == 0) {
            playPause();
        } else {
            Log.i(TAG, PLAY_PAUSE + " Send transport press");
            metadataContainers.get(0).getMediaController().getTransportControls().pause();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void skipBack() {
        Log.i(TAG, SKIP_BACK);
        //Receive skip back command and send command to device
        if (kitKatMetadataContainer != null || metadataContainers.size() == 0) {
            Log.i(TAG, PLAY_PAUSE + " Send button press");
            backgroundHandler.post(new Runnable() {
                @Override
                public void run() {
                    new Instrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
                }
            });
        } else {
            Log.i(TAG, PLAY_PAUSE + " Send transport press");
            metadataContainers.get(0).getMediaController().getTransportControls().skipToPrevious();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void skipForward() {
        Log.i(TAG, SKIP_FORWARD);
        //Receive skip forward command and send command to device
        if (kitKatMetadataContainer != null || metadataContainers.size() == 0) {
            Log.i(TAG, PLAY_PAUSE + " Send button press");
            backgroundHandler.post(new Runnable() {
                @Override
                public void run() {
                    new Instrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_MEDIA_NEXT);
                }
            });
        } else {
            Log.i(TAG, PLAY_PAUSE + " Send transport press");
            metadataContainers.get(0).getMediaController().getTransportControls().skipToNext();
        }
    }

    protected class ListenerBinder extends Binder {
        public ListenerService getService() {
            return ListenerService.this;
        }

    }

    protected void addCallback(MetadataCallback callback) {
        Log.i(TAG, "New Callback");
        metadataCallbacks.add(callback);
        callback.updateMetadata(activeContainer);
        callback.updateState(activeContainer);
    }

    protected void removeCallback(MetadataCallback callback) {
        Log.i(TAG, "Remove Callback: " + metadataCallbacks.remove(callback));
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void seekTo(int progress) {
        if (remoteController != null) {
            remoteController.seekTo(progress);
        } else if (metadataContainers.size() > 0) {
            metadataContainers.get(0).getMediaController().getTransportControls().seekTo(progress);
        }
    }
}
