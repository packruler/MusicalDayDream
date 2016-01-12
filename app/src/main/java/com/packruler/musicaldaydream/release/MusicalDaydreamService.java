package com.packruler.musicaldaydream.release;

import android.animation.TimeInterpolator;
import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.PlaybackState;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.service.dreams.DreamService;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.List;


@TargetApi(Build.VERSION_CODES.KITKAT)
public class MusicalDaydreamService extends DreamService implements MetadataCallback {

    private final String TAG = this.getClass().getSimpleName();
    protected static final String MAIN_RECEIVER = "com.packruler.MusicalDaydream.MAIN_RECEIVER";
    protected static final String COMMAND = "command";
    protected static final String UPDATE_METADATA = "UPDATE_METADATA";
    protected static final String ARTIST_STRING = "ARTIST_STRING";
    protected static final String ALBUM_STRING = "ALBUM_STRING";
    protected static final String TITLE_STRING = "TITLE_STRING";
    protected static final String UPDATE_POSITION_DURATION = "UPDATE_POSITION_DURATION";
    protected static final String UPDATE_DURATION = "UPDATE_DURATION";
    protected static final String UPDATE_POSITION = "UPDATE_POSITION";
    protected static final String CURRENT_POSITION_STRING = "CURRENT_POSITION_STRING";
    protected static final String DURATION_STRING = "DURATION_STRING";
    protected static final String POSITION_PERCENT = "POSITION_PERCENT";
    protected static final String PLAY_STATE = "PLAY_STATE";
    protected static final String MUSIC_PLAYING = "MUSIC_PLAYING";
    protected static final String MUSIC_STOPPED = "MUSIC_STOPPED";
    protected static final String START_TIMEOUT = "START_TIMEOUT";
    protected static final String STOP_TIMEOUT = "STOP_TIMEOUT";
    protected static final String SHUTDOWN = "SHUTDOWN";
    protected static final String PLAYSTATE_UPDATE = "PLAYSTATE_UPDATE";
    protected static final String UPDATED_IMAGE = "UPDATED_IMAGE";

    protected static Handler uiHandler = new Handler(Looper.getMainLooper());
    private HandlerThread backgroundThread;
    private Handler backgroundHandler;
    private HandlerThread backgroundThreadAux;
    private Handler backgroundHandlerAux;


    protected static Context context;

    protected RelativeLayout daydreamFrame;
    private FrameLayout artFrameLayout;

    protected CharSequence noMusicAppMsg = "No Active Music Application";
    protected Toast noMusicAppToast;

    protected RelativeLayout mediaLayout;

    protected RelativeLayout metadataBackground;
    protected TextView artistText;
    protected TextView titleText;
    protected TextView albumText;
    protected TextView batteryInfo;
    protected ImageView artView;
    protected FrameLayout albumArtOverlay;

    protected RelativeLayout statusControl;
    protected SeekBar progressBar;
    protected PositionClock durationClock;
    protected PositionClock positionClock;

    protected String artistString = "";
    protected String albumString = "";
    protected String titleString = "";
    protected String batteryString = "";
    private Bitmap albumBitmap;
    private MediaMetadata mediaMetadata;
    private long currentPositionLong;
    private long durationLong;

    protected ImageView playButton;
    protected ImageView pauseButton;
    protected ImageView skipForwardButton;
    protected ImageView skipBackButton;

    protected static boolean isActive = false;
    protected boolean isDreaming = false;

    protected static TextClock clock;

    protected static RelativeLayout largeNotificationDisplay;
    protected ImageView notificationLargeIcon;
    protected static TextView notificationSummary;
    protected static TextView notificationTitle;

    protected long animationDuration = 12000;

    protected static LinearLayout notificationPanel;

    protected final TimeInterpolator sInterpolator = new LinearInterpolator();

    private boolean inPortrait;
    private boolean portraitSet = false;
    private int screenWidth;
    private int artFrameScaleSide;

    protected ViewPropertyAnimator mAnimator;
    private ViewPropertyAnimator clockFader;

    protected MainReceiver mainReceiver;

    private SharedPreferences sharedPreferences;
    private Intent listenerServiceIntent;
    private ListenerService listenerService;
    private ServiceConnection serviceConnection;
    private boolean serviceConnected;

    private int dimension;
    private boolean artScrolling = false;
    private FrameLayout.LayoutParams layoutParams;

    private Display display;
    private Point displayPoint;

    private final ScreenTimeout screenTimeout = new ScreenTimeout();

    protected int fadeOutTime = 1000;
    protected int fadeInTime = 500;

    protected static final Intent musicStopped = new Intent(MAIN_RECEIVER).putExtra(COMMAND, MUSIC_STOPPED);
    protected static final Intent musicStarted = new Intent(MAIN_RECEIVER).putExtra(COMMAND, MUSIC_PLAYING);
    protected static final Intent startTimeout = new Intent(MAIN_RECEIVER).putExtra(COMMAND, START_TIMEOUT);
    protected static final Intent stopTimeout = new Intent(MAIN_RECEIVER).putExtra(COMMAND, STOP_TIMEOUT);

    private BatteryStatusReceiver batteryStatusReceiver;

    private SetDisplayVariablesRunnable setDisplayVariablesRunnable = new SetDisplayVariablesRunnable();
    private List<MediaController> mediaControllers;
    private boolean isPlaying;
    private MetadataContainer activeContainer;
    private int animationRatio;
    private RelativeLayout currentMetadata;
    private ImageView lastMetadata;
    private RelativeLayout lastMetadataBg;
    private ProgressBar batteryBar;


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        backgroundThread = new HandlerThread("MusicalDaydreamBackground");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
        backgroundThreadAux = new HandlerThread("MusicalDaydreamBackground 2");
        backgroundThreadAux.start();
        backgroundHandlerAux = new Handler(backgroundThreadAux.getLooper());


        sharedPreferences = getSharedPreferences(getString(R.string.settings_string), 0);
        animationDuration = sharedPreferences.getInt(getString(R.string.art_scroll_duration), 12) * 1000;

        String timeoutSelection = sharedPreferences.getString(getString(R.string.screen_timeout_pause), "0");
        if (timeoutSelection.equals("15 seconds")) {
            timeout = 15000;
        } else if (timeoutSelection.equals("30 seconds")) {
            timeout = 30000;
        } else if (timeoutSelection.equals("45 seconds")) {
            timeout = 45000;
        } else if (timeoutSelection.equals("1 minute")) {
            timeout = 60000;
        } else if (timeoutSelection.equals("2 minute")) {
            timeout = 120000;
        } else if (timeoutSelection.equals("5 minute")) {
            timeout = 300000;
        }

        context = getApplicationContext();

        initializeOnAttached();

        ContentResolver contentResolver = getContentResolver();
        String enabledNotificationListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners");
        String packageName = getPackageName();
        display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();

        //License Check
        if (sharedPreferences.getBoolean("first_run", true) || !sharedPreferences.getBoolean("licensed", false)) {
            setContentView(R.layout.first_run_daydream);
        } else if (enabledNotificationListeners == null || !enabledNotificationListeners.contains(packageName)) {
            // check to see if the enabledNotificationListeners String contains our package name
            setContentView(R.layout.first_run_daydream);
            TextView status = (TextView) findViewById(R.id.dream_setup_info);
            status.setText("Notification Listening Service Not Enabled.\nPlease run settings app to enable.");
        } else {
            serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    serviceConnected = true;
                    listenerService = ((ListenerService.ListenerBinder) service).getService();
                    listenerService.addCallback(MusicalDaydreamService.this);
                    Log.i(TAG, "ComponentName: " + name.toString() + "\n IBinder: " + service.toString());
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    Log.i(TAG, "Service Disconnected");
                    serviceConnected = false;
//                startService(listenerServiceIntent);
                }
            };
            listenerServiceIntent = new Intent(this, ListenerService.class);
            if (!ListenerService.started) {
                Log.i(TAG, "Restarting Listener Service");
                startService(listenerServiceIntent);
            }

            setContentView(R.layout.musical_daydream);
            initializeAllTheViews();

            uiHandler.postAtFrontOfQueue(new Runnable() {
                @Override
                public void run() {
                    if (isActive) {
                        findViewById(R.id.load_screen).setVisibility(View.VISIBLE);
                    }
                }
            });

//Initializes receiver to receive commands to update metadata from ListenerService
            HandlerThread thread = new HandlerThread("Musical Daydream Receiver");
            thread.start();
            Handler threadHandler = new Handler(thread.getLooper());
            mainReceiver = new MainReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(MAIN_RECEIVER);
            registerReceiver(mainReceiver, filter, null, threadHandler);

            if (sharedPreferences.getBoolean(getString(R.string.display_battery), true)) {
                batteryStatusReceiver = new BatteryStatusReceiver();
                IntentFilter batteryFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                registerReceiver(batteryStatusReceiver, batteryFilter);
            }

            Intent i = new Intent(ListenerService.RECEIVER_STRING);
            i.putExtra(COMMAND, ListenerService.INITIAL_NOTIFICATION_SET);
            sendBroadcast(i);

//            i = new Intent(ListenerService.RECEIVER_STRING);
//            i.putExtra(COMMAND, ListenerService.INITIAL_METADATA);
//            sendBroadcast(i);

            RelativeLayout swipePanel = (RelativeLayout) findViewById(R.id.swipe_panel);
            MusicSwipeListener musicSwipeListener = new MusicSwipeListener(this);
            swipePanel.setOnTouchListener(musicSwipeListener);


            listenerServiceIntent = new Intent(this, ListenerService.class);

            try {
                int timeout = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT);
                Log.i(TAG, "Timeout: " + timeout);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDreamingStarted() {
        super.onDreamingStarted();
        isDreaming = true;
    }

    @Override
    public void onDreamingStopped() {
        super.onDreamingStopped();

        isDreaming = false;

        //ListenerService.setMusicActivity();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (isActive) {
            isActive = false;
            backgroundThread.quitSafely();
            backgroundThreadAux.quitSafely();
            unregisterReceiver(mainReceiver);
            if (mAnimator != null) {
                mAnimator.cancel();
            }
            if (sharedPreferences.getBoolean(getString(R.string.display_battery), true)) {
                unregisterReceiver(batteryStatusReceiver);
            }

        }

        listenerService.removeCallback(this);
        unbindService(serviceConnection);
    }

    /**
     * General launch setup
     */
    private void initializeOnAttached() {
        Log.i("Licensed: ", sharedPreferences.getBoolean("licensed", false) + "");

        // Exit dream upon user touch?
        setInteractive(true);

        // Hide system UI?
        setFullscreen(true);

        if (sharedPreferences.getBoolean(getString(R.string.dim_screen), true)) {
            setScreenBright(false);
        } else {
            setScreenBright(true);
        }

        animationRatio = sharedPreferences.getInt(getString(R.string.art_scroll_duration), 12) * 1000;
    }

    /**
     * Sets all views to corresponding variables
     */
    private void initializeAllTheViews() {
        backgroundHandlerAux.post(new SetDisplayVariablesRunnable());
    }

    /**
     * Enacts user preferences related to display
     */
    private void setDisplayPreferences() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);


        if (sharedPreferences.getBoolean(getString(R.string.display_song_times), false)) {
            positionClock.setValue(0);
            durationClock.setValue(0);
        } else {
            positionClock.setVisibility(View.INVISIBLE);
            durationClock.setVisibility(View.INVISIBLE);
        }


        if (sharedPreferences.getBoolean(getString(R.string.display_album_title), false)) {
            albumText.setVisibility(View.VISIBLE);
        } else {
            albumText.setVisibility(View.GONE);
        }

        if (sharedPreferences.getBoolean(getString(R.string.display_battery), true)) {
            batteryInfo.setVisibility(View.VISIBLE);
        } else {
            batteryInfo.setVisibility(View.GONE);
        }

        if (sharedPreferences.getBoolean(getString(R.string.immersive_mode), true)) {
            daydreamFrame.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            if (!sharedPreferences.getBoolean(getString(R.string.stable_bottom), true)) {
                mediaLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            }
        }
//        setAlbumArtDimensions();

        setClockTheme();
        setMetadataTheme();
        setProgressBarTheme();
        setControlTheme();
        setTextSizes();
        setBatteryTheme();
    }

    /**
     * Set clock text color to user preference
     */
    private void setClockTheme() {
        if (!sharedPreferences.getBoolean(getString(R.string.clock_color_framework), true) && !sharedPreferences.getBoolean(getString(R.string.clock_color_palette), false)) {
            clock.setTextColor(sharedPreferences.getInt(getString(R.string.clock_color_custom), Color.WHITE));
        }
    }

    /**
     * Set metadata text color to user preference
     */
    private void setMetadataTheme() {
        if (!sharedPreferences.getBoolean(getString(R.string.metadata_color_framework), true) && !sharedPreferences.getBoolean(getString(R.string.metadata_color_palette), false)) {
            titleText.setTextColor(sharedPreferences.getInt(getString(R.string.metadata_color_custom), Color.WHITE));
            artistText.setTextColor(sharedPreferences.getInt(getString(R.string.metadata_color_custom), Color.WHITE));
            albumText.setTextColor(sharedPreferences.getInt(getString(R.string.metadata_color_custom), Color.WHITE));
        }
    }

    /**
     * Set battery text color to user preference
     */
    private void setBatteryTheme() {
        if (!sharedPreferences.getBoolean(getString(R.string.battery_color_framework), true) && !sharedPreferences.getBoolean(getString(R.string.battery_color_palette), false)) {
            batteryInfo.setTextColor(sharedPreferences.getInt(getString(R.string.battery_color_custom), Color.WHITE));
        }
    }

    /**
     * Set progress bar layout to corresponding layout style
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setProgressBarTheme() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (!sharedPreferences.getBoolean(getString(R.string.progress_color_framework), false) && !sharedPreferences.getBoolean(getString(R.string.progress_color_palette), true)) {
                PorterDuff.Mode mode = PorterDuff.Mode.SRC_ATOP;
                ColorStateList colorState = new ColorStateList(new int[][]{
                        new int[]{-android.R.attr.enabled}},
                        new int[]{sharedPreferences.getInt(getString(R.string.progress_color_custom), Color.WHITE)});
                progressBar.setProgressTintList(colorState);
                progressBar.setThumbTintList(colorState);
                progressBar.setProgressTintMode(mode);
            }
        }
    }

    /**
     * Set media controls layout to corresponding layout style
     */
    private void setControlTheme() {
        if (!sharedPreferences.getBoolean(getString(R.string.control_color_framework), true) && !sharedPreferences.getBoolean(getString(R.string.control_color_palette), false)) {
            controlColor = new PorterDuffColorFilter(sharedPreferences.getInt(getString(R.string.control_color_custom), Color.WHITE), PorterDuff.Mode.SRC_ATOP);
            playButton.setColorFilter(controlColor);
            pauseButton.setColorFilter(controlColor);
            skipForwardButton.setColorFilter(controlColor);
            skipBackButton.setColorFilter(controlColor);
            if (sharedPreferences.getBoolean(getString(R.string.display_song_times), false)) {
                positionClock.setTextColor(sharedPreferences.getInt(getString(R.string.metadata_color_custom), Color.WHITE));
                durationClock.setTextColor(sharedPreferences.getInt(getString(R.string.metadata_color_custom), Color.WHITE));
            }
        }
    }

    /**
     * Set text sizes to user preference
     */
    private void setTextSizes() {
        if (sharedPreferences.getInt(getString(R.string.clock_text_size), 0) != 0) {
            clock.setTextSize(TypedValue.COMPLEX_UNIT_SP, sharedPreferences.getInt(getString(R.string.clock_text_size), 0));
        }
        if (sharedPreferences.getInt(getString(R.string.artist_name_text_size), 0) != 0) {
            artistText.setTextSize(TypedValue.COMPLEX_UNIT_SP, sharedPreferences.getInt(getString(R.string.artist_name_text_size), 0));
        }
        if (sharedPreferences.getInt(getString(R.string.track_title_text_size), 0) != 0) {
            titleText.setTextSize(TypedValue.COMPLEX_UNIT_SP, sharedPreferences.getInt(getString(R.string.track_title_text_size), 0));
        }
        if (sharedPreferences.getInt(getString(R.string.album_name_text_size), 0) != 0) {
            albumText.setTextSize(TypedValue.COMPLEX_UNIT_SP, sharedPreferences.getInt(getString(R.string.album_name_text_size), 0));
        }
        if (sharedPreferences.getInt(getString(R.string.battery_info_text_size), 0) != 0) {
            batteryInfo.setTextSize(TypedValue.COMPLEX_UNIT_SP, sharedPreferences.getInt(getString(R.string.battery_info_text_size), 0));
        }
    }

    private boolean squareArt = true;

    private RelativeLayout.LayoutParams defaultParams;

    private float artViewHeight;
    private float artViewWidth;
    private float scaleRatio;
    private float artHeightRatio;
    private float artWidthRatio;
    private boolean artPortrait;
    private boolean aboveRemoved = false;
    private Bitmap lastDataBitmap;
    private boolean doAgain;

    /**
     * Resize album art FrameLayout and ImageView and initialize scroll animation as needed
     */
    private void setAlbumArtDimensions(boolean resize) {
        Log.i(TAG, "setAlbumArtDimensions");
        doAgain = false;
        if (resize) {
            if (!sharedPreferences.getBoolean(getString(R.string.fit_art), false)) {
                artViewHeight = artworkBitmap.getHeight();
                artViewWidth = artworkBitmap.getWidth();
                artPortrait = artViewHeight > artViewWidth;
                Log.i(TAG, "Art Height: " + artViewHeight + " Art Width: " + artViewWidth);
                artHeightRatio = artViewHeight / artViewWidth;
                artWidthRatio = artViewWidth / artViewHeight;
                float frameHeightRatio = artFrameLayout.getHeight() / artFrameLayout.getWidth();
                artPortrait = artHeightRatio > frameHeightRatio;
                squareArt = artViewHeight == artViewWidth;
                Log.i(TAG, "Frame Height: " + artFrameLayout.getHeight() + " Frame Width: " + artFrameLayout.getWidth());
                Log.i(TAG, "Height Ratio: " + artHeightRatio + " Width Ratio: " + artWidthRatio);


                if (artPortrait) {
                    artFrameScaleSide = artFrameLayout.getWidth();
                    scaleRatio = artFrameScaleSide / artViewWidth;
                } else {
                    artFrameScaleSide = artFrameLayout.getHeight();
                    scaleRatio = artFrameScaleSide / artViewHeight;
                }
                Log.i(TAG, "Scale: " + scaleRatio + " Portrait: " + artPortrait);
                artViewHeight = (scaleRatio * artViewHeight);
                artViewWidth = (scaleRatio * artViewWidth);
                Log.i(TAG, "Height: " + artViewHeight + " Width: " + artViewWidth);

                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (sharedPreferences.getBoolean(getString(R.string.fullscreen_art), true)) {
                            if (!aboveRemoved) {
                                RelativeLayout.LayoutParams currentMetadataLayout = (RelativeLayout.LayoutParams) currentMetadata.getLayoutParams();
                                currentMetadataLayout.removeRule(RelativeLayout.ABOVE);
                                currentMetadata.setLayoutParams(currentMetadataLayout);
                                aboveRemoved = true;
                            }

                            artFrameLayout.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                            artView.setScaleType(ImageView.ScaleType.FIT_XY);

                            if (displayPoint.x <= displayPoint.y) {
                                artFrameScaleSide = displayPoint.y;
                            } else {
                                artFrameScaleSide = displayPoint.x;
                            }

                            if (artPortrait) {
                                scaleRatio = artFrameScaleSide / artViewHeight;
                                Log.i(TAG, "Scale: " + scaleRatio + " Portrait: " + artPortrait);
                                artViewHeight = (scaleRatio * artViewHeight);
                                artViewWidth = (scaleRatio * artViewWidth);
                            } else {
                                scaleRatio = artFrameScaleSide / artViewWidth;
                                Log.i(TAG, "Scale: " + scaleRatio + " Portrait: " + artPortrait);
                                artViewHeight = (scaleRatio * artViewHeight);
                                artViewWidth = (scaleRatio * artViewWidth);
                            }
                            Log.i(TAG, "Height: " + artViewHeight + " Width: " + artViewWidth);

                        }
                        if (sharedPreferences.getBoolean(getString(R.string.scroll_art), true)) {

                            layoutParams = new FrameLayout.LayoutParams((int) artViewWidth, (int) artViewHeight);

                            artView.setLayoutParams(layoutParams);
                            artView.setScaleType(ImageView.ScaleType.FIT_XY);
                            initializeAlbumArtScroll();
                        } else {
                            layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                            artView.setLayoutParams(layoutParams);
                            artView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        }
                        Log.i(TAG, "X: " + artView.getX() + " Y: " + artView.getY());

                        doAgain = (artFrameLayout.getWidth() < artViewWidth && artFrameLayout.getHeight() < artViewHeight);
                        if (doAgain) {
                            Log.i(TAG, "Resize Again");
                            backgroundHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    setAlbumArtDimensions(true);
                                }
                            });
                        }
                    }
                });
            }
        }
    }

    private Bitmap artworkBitmap;

    @Override
    public void updateMetadata(final MetadataContainer container) {
        if (container == null) {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    artView.setImageBitmap(null);
                    titleText.setText("No active player");
                    artistText.setText("");
                    albumText.setText("");
                    setAlbumArtDimensions(false);
                    updateAlbumArtColors();
                    progressBar.setProgress(0);
                    progressBar.setMax(0);
                    positionClock.setValue(0);
                    durationClock.setValue(0);
                }
            });
        } else {
            backgroundHandlerAux.post(new Runnable() {
                @Override
                public void run() {
                    final String[] metadata = container.getMetadataStrings();
                    Log.i(TAG, "Active Container: " + container.getPackageName());
                    activeContainer = container;
                    Log.i(TAG, "Metadata: ");
                    for (String data : metadata) {
                        if (data == null)
                            data = "";
                        Log.i(TAG, data);
                    }
                    if (container.getArtworkBitmap() != null) {
                        boolean resetAlbumArtSize;
                        if (artworkBitmap != null) {
                            resetAlbumArtSize = artworkBitmap.getWidth() != container.getArtworkBitmap().getWidth() || artworkBitmap.getHeight() != container.getArtworkBitmap().getHeight();
                        } else {
                            Log.i(TAG, "Initial setup for artwork");
                            resetAlbumArtSize = true;
                        }
                        artworkBitmap = container.getArtworkBitmap();
//                updateAlbumArtColors();

                        setAlbumArtDimensions(resetAlbumArtSize);
                    } else {
                        artworkBitmap = container.getArtworkBitmap();
                    }

                    lastDataBitmap = Bitmap.createBitmap(currentMetadata.getWidth(),
                            currentMetadata.getHeight(), Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(lastDataBitmap);
//                clock.setAlpha(0);
//                batteryInfo.setAlpha(0);
                    currentMetadata.draw(canvas);
//                clock.setAlpha(1);
//                batteryInfo.setAlpha(1);
                    durationLong = container.getDuration();
                    updateDuration();
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (!(titleText.getText().equals(metadata[0]) && artistText.getText().equals(metadata[1]) && albumText.getText().equals(metadata[2]))) {
                                titleText.setText(metadata[0]);
                                artistText.setText(metadata[1]);
                                albumText.setText(metadata[2]);
                                lastMetadataBg.setBackgroundColor(currentBG);
                                lastMetadata.setImageBitmap(lastDataBitmap);
                                lastMetadataBg.setX(0);
                                lastMetadataBg.animate().setDuration(1000).setInterpolator(new LinearInterpolator()).x(-lastDataBitmap.getWidth()).start();
                                artView.setImageBitmap(artworkBitmap);
                                updateAlbumArtColors();
                            }
                        }
                    });
                }
            });
        }
    }

    public void updateDuration() {
        if (sharedPreferences.getBoolean(getString(R.string.song_time_left), true)) {
            durationClock.setValue(currentPositionLong - durationLong);
        } else {
            durationClock.setValue(durationLong);
        }
        progressBar.setProgress(0);
        progressBar.setMax((int) durationLong);
        positionClock.setValue(0);
    }

    private void updatePosition() {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                if (activeContainer.isAvoidTiming()) {
                    positionClock.setVisibility(View.GONE);
                    positionClock.setValue(currentPositionLong);
                    if (sharedPreferences.getBoolean(getString(R.string.song_time_left), true)) {
                        durationClock.setValue(durationLong);
                    }
                    positionClock.setValue(currentPositionLong);
                    progressBar.setProgress(0);
                } else if (!seeking) {
                    positionClock.setVisibility(postionViewInts);
                    if (sharedPreferences.getBoolean(getString(R.string.song_time_left), true)) {
                        durationClock.setValue(currentPositionLong - durationLong);
                    }
                    positionClock.setValue(currentPositionLong);
                    progressBar.setProgress((int) currentPositionLong);
                }
            }
        });
    }

    @Override
    public void updateState(MetadataContainer container) {
        activeContainer = container;
        if (container != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                long actions = container.getMediaController().getPlaybackState().getActions();
                if (0 == (actions & PlaybackState.ACTION_SKIP_TO_PREVIOUS)) {
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            skipBackButton.setVisibility(View.GONE);
                        }
                    });
                } else if (skipBackButton.getVisibility() == View.GONE) {
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            skipBackButton.setVisibility(View.VISIBLE);
                        }
                    });
                }

                if (0 == (actions & PlaybackState.ACTION_SKIP_TO_NEXT)) {
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            skipForwardButton.setVisibility(View.GONE);
                        }
                    });
                } else if (skipForwardButton.getVisibility() == View.GONE) {
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            skipForwardButton.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }

            if (!container.isPlaying() && this.isPlaying) {
                this.isPlaying = false;

                if (timeout != 0) {
                    backgroundHandlerAux.removeCallbacks(screenTimeout);
                    backgroundHandlerAux.postDelayed(screenTimeout, timeout);
                }
                if (sharedPreferences.getBoolean(getString(R.string.fade_music_controls), true)) {
                    uiHandler.post(new FadeControlOut());
                }

                if (sharedPreferences.getBoolean(getString(R.string.fade_metadata), true)) {
                    uiHandler.post(new FadeMetadataOut());
                }

                if (sharedPreferences.getBoolean(getString(R.string.fade_clock), true)) {
                    uiHandler.post(new FadeClockOut());
                }

                if (sharedPreferences.getBoolean(getString(R.string.fade_notification_icons), true)) {
                    uiHandler.post(new FadeIconsOut());
                }
            } else if (container.isPlaying() && !this.isPlaying) {
                this.isPlaying = true;

                if (timeout != 0) {
                    backgroundHandlerAux.removeCallbacks(screenTimeout);
                }
                if (sharedPreferences.getBoolean(getString(R.string.fade_music_controls), true)) {
                    uiHandler.post(new FadeControlIn());
                }

                if (sharedPreferences.getBoolean(getString(R.string.fade_metadata), true)) {
                    uiHandler.post(new FadeMetadataIn());
                }

                if (sharedPreferences.getBoolean(getString(R.string.fade_clock), true)) {
                    uiHandler.post(new FadeClockIn());
                }

                if (sharedPreferences.getBoolean(getString(R.string.fade_notification_icons), true)) {
                    uiHandler.post(new FadeIconsIn());
                }
            }
            currentPositionLong = container.getCurrentPosition();
            updatePosition();
        }
    }

    /**
     * Begins album art scrolling
     */
    private void initializeAlbumArtScroll() {
        artScrolling = true;
        Log.i(TAG, "initializeAlbumArtScroll");

        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                mAnimator = artView.animate().x(0)
                        .y(0)
                        .setDuration(10)
                        .setStartDelay(500)
                        .setInterpolator(sInterpolator);

                if (!artPortrait) {
                    Log.i(TAG, "X Scroll");
                    mAnimator.withEndAction(new XArtScrollFirst(500));

                    float ratio = (artViewWidth / (float) artFrameLayout.getWidth());
//                    animationDuration = (long) (ratio * 10000);
                    Log.i(TAG, "Animation Duration: " + animationDuration + " Ratio: " + ratio);
                } else {
                    Log.i(TAG, "Y Scroll");
                    mAnimator.withEndAction(new YArtScrollFirst(500));

                    float ratio = (artViewHeight / (float) artFrameLayout.getHeight());
//                    animationDuration = (long) (ratio * 10000);
                    Log.i(TAG, "Animation Duration: " + animationDuration + " Ratio: " + ratio);
                }
                mAnimator.start();
            }
        });
    }

    /**
     * Album art scroll from the right of the screen to the left
     */
    private class XArtScrollFirst implements Runnable {
        private int delay;

        public XArtScrollFirst(int inDelay) {
            delay = inDelay;
        }

        @Override
        public void run() {
            artView.setX(0);
            artView.setY(0);

            Log.i(TAG, "Scroll Left");

            mAnimator = artView.animate().x(artFrameLayout.getWidth() - artView.getWidth())
                    .y(0)
                    .setDuration(animationDuration)
                    .setStartDelay(delay)
                    .setInterpolator(sInterpolator)
                    .withEndAction(new XArtScrollSecond(500));

            // Start the animation
            mAnimator.start();
        }

    }

    /**
     * Album art scroll from the left of the screen to the right
     */
    private class XArtScrollSecond implements Runnable {
        private int delay;

        public XArtScrollSecond(int inDelay) {
            delay = inDelay;
        }

        @Override
        public void run() {

            artView.setX(-artView.getWidth() + artFrameLayout.getWidth());
            artView.setY(0);

            Log.i(TAG, "Scroll Right");

            mAnimator = artView.animate().x(0)
                    .y(0)
                    .setDuration(animationDuration)
                    .setStartDelay(delay)
                    .setInterpolator(sInterpolator)
                    .withEndAction(new XArtScrollFirst(500));

            // Start the animation
            mAnimator.start();
        }
    }

    /**
     * Album art scroll from the bottom of the screen to the top
     */
    private class YArtScrollFirst implements Runnable {
        private int delay;

        public YArtScrollFirst(int inDelay) {
            delay = inDelay;
        }

        @Override
        public void run() {
            artView.setY(0);
            artView.setX(0);

//            Log.i(TAG, "Scroll Up");

            mAnimator = artView.animate().y(artFrameLayout.getHeight() - artView.getHeight())
                    .x(0)
                    .setDuration(animationDuration)
                    .setStartDelay(delay)
                    .setInterpolator(sInterpolator)
                    .withEndAction(new YArtScrollSecond(500));

            // Start the animation
            mAnimator.start();
        }
    }

    /**
     * Album art scroll from the top of the screen to the bottom
     */
    private class YArtScrollSecond implements Runnable {
        private int delay;

        public YArtScrollSecond(int inDelay) {
            delay = inDelay;
        }

        @Override
        public void run() {
            artView.setY(artFrameLayout.getHeight() - artView.getHeight());
            artView.setX(0);

//            Log.i(TAG, "Scroll Down");

            mAnimator = artView.animate().y(0)
                    .x(0)
                    .setDuration(animationDuration)
                    .setStartDelay(delay)
                    .setInterpolator(sInterpolator)
                    .withEndAction(new YArtScrollFirst(500));

            // Start the animation
            mAnimator.start();
        }
    }

    /**
     * Send play command to {@link com.packruler.musicaldaydream.release.ListenerService}
     *
     * @param v
     */
    public void play(View v) {
//        Log.i(TAG, "Sending playPause");
        listenerService.play();
    }

    public void pause(View v) {
//        Log.i(TAG, "Sending playPause");
        listenerService.pause();
    }

    /**
     * Send skip back command to {@link com.packruler.musicaldaydream.release.ListenerService}
     *
     * @param v
     */
    public void skipBack(View v) {
//        Log.i(TAG, "Sending skipBack");
        listenerService.skipBack();
    }

    /**
     * Send skip forward command to {@link com.packruler.musicaldaydream.release.ListenerService}
     *
     * @param v
     */
    public void skipForward(View v) {
//        Log.i(TAG, "Sending skipForward");
        listenerService.skipForward();
    }

    public void updateScreenDim() {
        Object sync = new Object();
        synchronized (sync) {
            Log.i(TAG, "Dim screen set to: " + sharedPreferences.getBoolean(getString(R.string.dim_screen), true));
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (sharedPreferences.getBoolean(getString(R.string.dim_screen), true)) {
                        setScreenBright(false);
                    } else {
                        setScreenBright(true);
                    }

                    largeNotificationDisplay.setBackgroundColor(getResources().getColor(R.color.notification_display_background));
                    notificationTitle.setVisibility(View.VISIBLE);

                    notificationTitle.setText("Dim screen set to: " + sharedPreferences.getBoolean(getString(R.string.dim_screen), true));
                }
            });
            uiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    notificationTitle.setText("");
                    notificationSummary.setText("");
                    largeNotificationDisplay.setBackgroundColor(View.INVISIBLE);
                }
            }, 1000);
        }
    }

    /**
     * End daydream
     */
    private void shutdown() {
        this.finish();
    }

    private class MainReceiver extends BroadcastReceiver {
        /**
         * Only command that can be received is "shutdown" to end daydream
         *
         * @param context
         * @param intent
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive: " + intent.getAction());
            if (intent.getStringExtra(COMMAND) != null) {
                if (intent.getStringExtra(COMMAND).equals(SHUTDOWN)) {
                    shutdown();
                } else if (intent.getStringExtra(COMMAND).equals("launchMusic")) {
                    Log.i(TAG, "LaunchMusic");
                    intent = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_MUSIC);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    shutdown();
                } else if (intent.getStringExtra(COMMAND).equals("updateScreenDim")) {
                    updateScreenDim();
                } else if (intent.getStringExtra(COMMAND).equals(START_TIMEOUT)) {
                    if (timeout != 0) {
                        backgroundHandlerAux.removeCallbacks(screenTimeout);
                        backgroundHandlerAux.postDelayed(screenTimeout, timeout);
                    }
                } else if (intent.getStringExtra(COMMAND).equals(STOP_TIMEOUT)) {
                    if (timeout != 0) {
                        backgroundHandlerAux.removeCallbacks(screenTimeout);
                    }
                } else if (intent.getStringExtra(COMMAND).equals("battery")) {
                    backgroundHandlerAux.post(new UpdateBattery());
                }
            }
        }
    }

    private class FadeMetadataOut implements Runnable {
        float alpha;

        public FadeMetadataOut() {
            alpha = sharedPreferences.getInt(getString(R.string.metadata_alpha_paused), 50);
            alpha = alpha / 100;
        }

        @Override
        public void run() {
            titleText.animate().alpha(alpha).setDuration(fadeOutTime).start();
            artistText.animate().alpha(alpha).setDuration(fadeOutTime).start();
            albumText.animate().alpha(alpha).setDuration(fadeOutTime).start();
            batteryInfo.animate().alpha(alpha).setDuration(fadeOutTime).start();
        }
    }

    private class FadeMetadataIn implements Runnable {
        float alpha;

        public FadeMetadataIn() {
            alpha = sharedPreferences.getInt(getString(R.string.metadata_alpha_playing), 100);
            alpha = alpha / 100;
        }

        @Override
        public void run() {
            titleText.animate().alpha(alpha).setDuration(fadeInTime).start();
            artistText.animate().alpha(alpha).setDuration(fadeInTime).start();
            albumText.animate().alpha(alpha).setDuration(fadeInTime).start();
            batteryInfo.animate().alpha(alpha).setDuration(fadeInTime).start();
        }
    }

    private class FadeControlOut implements Runnable {
        float alpha;

        public FadeControlOut() {
            alpha = sharedPreferences.getInt(getString(R.string.control_alpha_paused), 50);
            alpha = alpha / 100;
        }

        @Override
        public void run() {
            pauseButton.setVisibility(View.GONE);
            playButton.setVisibility(View.VISIBLE);
            statusControl.animate().alpha(alpha).setDuration(fadeOutTime).start();
            positionClock.animate().alpha(alpha).setDuration(fadeOutTime).start();
            durationClock.animate().alpha(alpha).setDuration(fadeOutTime).start();
        }
    }

    private class FadeControlIn implements Runnable {
        float alpha;

        public FadeControlIn() {
            alpha = sharedPreferences.getInt(getString(R.string.control_alpha_playing), 100);
            alpha = alpha / 100;
        }

        @Override
        public void run() {
            pauseButton.setVisibility(View.VISIBLE);
            playButton.setVisibility(View.GONE);
            statusControl.animate().alpha(alpha).setDuration(fadeInTime).start();
            positionClock.animate().alpha(alpha).setDuration(fadeInTime).start();
            durationClock.animate().alpha(alpha).setDuration(fadeInTime).start();
        }
    }

    private class FadeClockOut implements Runnable {
        float alpha;

        public FadeClockOut() {
            alpha = sharedPreferences.getInt(getString(R.string.clock_alpha_paused), 50);
            alpha = alpha / 100;
        }

        @Override
        public void run() {
            if (!isPlaying) {
                clock.animate().alpha(alpha).setDuration(fadeOutTime).start();
            }
        }
    }

    private class FadeClockIn implements Runnable {
        float alpha;

        public FadeClockIn() {
            alpha = sharedPreferences.getInt(getString(R.string.clock_alpha_playing), 100);
            alpha = alpha / 100;
        }

        @Override
        public void run() {
            clock.animate().alpha(alpha).setDuration(fadeInTime).start();
        }
    }

    private class FadeIconsOut implements Runnable {
        float alpha;

        public FadeIconsOut() {
            alpha = sharedPreferences.getInt(getString(R.string.notification_alpha_paused), 50);
            alpha = alpha / 100;
        }

        @Override
        public void run() {
            notificationPanel.animate().alpha(alpha).setDuration(fadeOutTime).start();
        }
    }

    private class FadeIconsIn implements Runnable {
        float alpha;

        public FadeIconsIn() {
            alpha = sharedPreferences.getInt(getString(R.string.notification_alpha_playing), 100);
            alpha = alpha / 100;
        }

        @Override
        public void run() {
            notificationPanel.animate().alpha(alpha).setDuration(fadeInTime).start();
        }
    }

    private class SetupLayout extends Thread {
        @Override
        public void run() {
//            Looper.prepare();
            daydreamFrame = (RelativeLayout) findViewById(R.id.daydream_frame);
            display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
            displayPoint = new Point();
            if (sharedPreferences.getBoolean(getString(R.string.immersive_mode), true)) {
                display.getRealSize(displayPoint);
            } else {
                display.getSize(displayPoint);
            }

//            Log.i(TAG, "Width: " + displayPoint.x + " Height: " + displayPoint.y);

            if (sharedPreferences.getBoolean(getString(R.string.scroll_art), true) /*&& sharedPreferences.getBoolean(getString(R.string.fullscreen_art), true)*/) {
                daydreamFrame.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
//                        Log.i(TAG, "Rotation: " + display.getRotation());
                        if ((display.getRotation() == Surface.ROTATION_0 || display.getRotation() == Surface.ROTATION_180) && !inPortrait) {
                            if (daydreamFrame.getWidth() < daydreamFrame.getHeight()) {
//                                Log.i(TAG, "Now in portrait");
                                if (mAnimator != null) {
                                    mAnimator.cancel();
                                }
                                inPortrait = true;

                                if (artScrolling)
                                    setAlbumArtDimensions(true);
                            }
//                            else {
////                                Log.i(TAG, "Thinks portrait but layout landscape");
//                            }
                        } else if ((display.getRotation() == Surface.ROTATION_270 || display.getRotation() == Surface.ROTATION_90) && inPortrait) {
                            if (daydreamFrame.getWidth() > daydreamFrame.getHeight()) {
//                                Log.i(TAG, "Now in landscape");
                                if (mAnimator != null) {
                                    mAnimator.cancel();
                                }
                                inPortrait = false;

                                if (artScrolling)
                                    setAlbumArtDimensions(true);
                            }
//                            else {
//                                Log.i(TAG, "Thinks landscape but layout portrait");
//                            }
                        }
                    }
                });
//                Log.i(TAG, "Rotation: " + display.getRotation());
                if ((display.getRotation() == Surface.ROTATION_0 || display.getRotation() == Surface.ROTATION_180)) {
                    if (daydreamFrame.getWidth() < daydreamFrame.getHeight()) {
                        inPortrait = true;
                    } else {
//                        Log.i(TAG, "In portrait?");
                        inPortrait = false;
                    }
                } else {
                    if (daydreamFrame.getWidth() > daydreamFrame.getHeight()) {
                        inPortrait = false;
                    } else {
//                        Log.i(TAG, "In landscape?");
                        inPortrait = true;
                    }
                }
                portraitSet = true;
            }
            if (timeout != 0) {
                backgroundHandlerAux.removeCallbacks(screenTimeout);
                backgroundHandlerAux.postDelayed(screenTimeout, timeout);
            }

            bindService(listenerServiceIntent, serviceConnection, BIND_AUTO_CREATE);
        }
    }

    private int postionViewInts;

    private class SetDisplayVariablesRunnable implements Runnable {
        public void run() {
//            Looper.prepare();
            mediaLayout = (RelativeLayout) findViewById(R.id.media_layout);
            //Initialize metadata TextView fields
            metadataBackground = (RelativeLayout) findViewById(R.id.metadata_background);

            artFrameLayout = (FrameLayout) findViewById(R.id.art_frame);

            float textSize = getResources().getDimensionPixelSize(R.dimen.metadata_text_size);
            Log.i(TAG, "Text Size: " + textSize);

            clock = (TextClock) findViewById(R.id.text_clock);

            currentMetadata = (RelativeLayout) findViewById(R.id.current_metadata);
            lastMetadataBg = (RelativeLayout) findViewById(R.id.last_metadata_bg);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                currentMetadata.setZ(0);
                lastMetadataBg.setElevation(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()));
                lastMetadataBg.setTranslationZ(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics()));
            }
            lastMetadata = (ImageView) findViewById(R.id.last_metadata);

            artistText = (TextView) findViewById(R.id.Artist_TextView);
            artistText.setOnClickListener(new OnClickEllipsize());

            albumText = (TextView) findViewById(R.id.Album_TextView);
            albumText.setOnClickListener(new OnClickEllipsize());

            titleText = (TextView) findViewById(R.id.Title_TextView);
            titleText.setOnClickListener(new OnClickEllipsize());

            batteryInfo = (TextView) findViewById(R.id.Battery_Info);

            artView = (ImageView) findViewById(R.id.Album_Artwork);

            albumArtOverlay = (FrameLayout) findViewById(R.id.album_art_overlay);

            statusControl = (RelativeLayout) findViewById(R.id.status_control);
            progressBar = (SeekBar) findViewById(R.id.progress_bar);
            progressBar.setOnSeekBarChangeListener(new SeekBarListener());

            durationClock = (PositionClock) findViewById(R.id.duration);
            positionClock = (PositionClock) findViewById(R.id.current_position);

            playButton = (ImageView) findViewById(R.id.Play_Button);
            pauseButton = (ImageView) findViewById(R.id.Pause_Button);
            skipBackButton = (ImageView) findViewById(R.id.Skip_Back_Button);
            skipForwardButton = (ImageView) findViewById(R.id.Skip_Forward_Button);

            notificationPanel = (LinearLayout) findViewById(R.id.notification_panel);

            largeNotificationDisplay = (RelativeLayout) findViewById(R.id.notification_display);
            notificationLargeIcon = (ImageView) findViewById(R.id.notification_large_icon);
            notificationSummary = (TextView) findViewById(R.id.notification_summary);
            notificationTitle = (TextView) findViewById(R.id.notification_title);

            //Set isActive so metadata updates are enabled in updateMetadata function

            uiHandler.postAtFrontOfQueue(new Runnable() {
                @Override
                public void run() {
                    notificationSummary.setText("");
                    notificationTitle.setText("");
                    setDisplayPreferences();
                }
            });

            if (sharedPreferences.getBoolean(getString(R.string.limit_marquee), false)) {
                titleText.setMarqueeRepeatLimit(1);
                artistText.setMarqueeRepeatLimit(1);
                albumText.setMarqueeRepeatLimit(1);
            }

            if (sharedPreferences.getBoolean(getString(R.string.display_song_times), true)) {
                postionViewInts = View.VISIBLE;
                durationClock.setVisibility(postionViewInts);
                positionClock.setVisibility(postionViewInts);
            } else {
                postionViewInts = View.GONE;
                durationClock.setVisibility(postionViewInts);
                positionClock.setVisibility(postionViewInts);
            }

            if (sharedPreferences.getBoolean(getString(R.string.display_battery), true)) {
                backgroundHandlerAux.post(new UpdateBattery());
            }

            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (sharedPreferences.getBoolean(getString(R.string.fade_clock), true)) {
                        if (isPlaying) {
                            new FadeClockIn().run();
                        } else {
                            new FadeClockOut().run();
                        }
                    } else {
                        new FadeClockIn().run();
                    }

                    if (sharedPreferences.getBoolean(getString(R.string.fade_music_controls), true)) {
                        if (isPlaying) {
                            new FadeControlIn().run();
                        } else {
                            new FadeControlOut().run();
                        }
                    } else {
                        new FadeControlIn().run();
                    }

                    if (sharedPreferences.getBoolean(getString(R.string.fade_metadata), true)) {
                        if (isPlaying) {
                            new FadeMetadataIn().run();
                        } else {
                            new FadeMetadataOut().run();
                        }
                    } else {
                        new FadeMetadataIn().run();
                    }

                    if (sharedPreferences.getBoolean(getString(R.string.fade_notification_icons), true)) {
                        if (isPlaying) {
                            new FadeIconsIn().run();
                        } else {
                            new FadeIconsOut().run();
                        }
                    } else {
                        new FadeIconsIn().run();
                    }
                }
            });

            uiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isActive) {
                        findViewById(R.id.load_screen).setVisibility(View.GONE);
                    } else {
                        uiHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (isActive) {
                                    findViewById(R.id.load_screen).setVisibility(View.GONE);
                                }
                            }
                        }, 500);
                    }
                }
            }, 1500);

            backgroundHandlerAux.post(new SetupLayout());
            backgroundHandler.post(new SetFont());
//            Intent intent = new Intent(ListenerService.RECEIVER_STRING);
//            intent.putExtra(ListenerService.COMMAND, ListenerService.INITIAL_METADATA);
//            sendBroadcast(intent);
        }
    }

    private int timeout = 0;

    private class ScreenTimeout implements Runnable {
        @Override
        public void run() {
            if (!isPlaying)
                shutdown();
        }
    }

    private class UpdateBattery implements Runnable {
        @Override
        public void run() {
//            Log.i(TAG, "Charging: " + batteryStatusReceiver.isCharging + " Level: " + batteryStatusReceiver.batteryPct);
            try {
                final boolean hideStatus = batteryStatusReceiver.batteryPct == 100 && batteryStatusReceiver.isCharging && sharedPreferences.getBoolean(getString(R.string.hide_battery_full), false);

                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!hideStatus) {
                            if (!batteryStatusReceiver.isCharging) {
                                batteryString = batteryStatusReceiver.batteryPct + "%";
                            } else if (batteryStatusReceiver.usbCharge) {
                                batteryString = "(USB) " + batteryStatusReceiver.batteryPct + "%";
                            } else if (batteryStatusReceiver.acCharge) {
                                batteryString = "(AC) " + batteryStatusReceiver.batteryPct + "%";
                            } else if (batteryStatusReceiver.wirelessCharging) {
                                batteryString = "(Qi) " + batteryStatusReceiver.batteryPct + "%";
                            }
                            batteryInfo.setVisibility(View.VISIBLE);
                        }
                        if (hideStatus) {
                            batteryInfo.setVisibility(View.GONE);
                        } else {
                            batteryInfo.setVisibility(View.VISIBLE);
                        }
                        batteryInfo.setText(batteryString);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class SetFont implements Runnable {
        @Override
        public void run() {
            if (!sharedPreferences.getBoolean(getString(R.string.framework_font), true)) {
                File fileDir = getFilesDir();
                File[] files = fileDir.listFiles();
                File font = null;
                String name = null;
                if (sharedPreferences.getString(getString(R.string.sans_serif), null) != null) {
                    name = sharedPreferences.getString(getString(R.string.sans_serif), null);
                } else if (sharedPreferences.getString(getString(R.string.serif), null) != null) {
                    name = sharedPreferences.getString(getString(R.string.serif), null);
                } else if (sharedPreferences.getString(getString(R.string.font_display), null) != null) {
                    name = sharedPreferences.getString(getString(R.string.font_display), null);
                } else if (sharedPreferences.getString(getString(R.string.handwriting), null) != null) {
                    name = sharedPreferences.getString(getString(R.string.handwriting), null);
                } else if (sharedPreferences.getString(getString(R.string.monospace), null) != null) {
                    name = sharedPreferences.getString(getString(R.string.monospace), null);
                }
                if (name != null) {
                    name += ".ttf";
                    for (int x = 0; x < files.length; x++) {
                        if (files[x].getName().equals(name)) {
                            font = files[x];
                            break;
                        }
                    }
                }
                if (font != null) {
                    Typeface tf = Typeface.createFromFile(font);
                    clock.setTypeface(tf);
                    titleText.setTypeface(tf);
                    albumText.setTypeface(tf);
                    artistText.setTypeface(tf);
                    batteryInfo.setTypeface(tf);
                    durationClock.setTypeface(tf);
                    positionClock.setTypeface(tf);
                    notificationTitle.setTypeface(tf);
                    notificationSummary.setTypeface(tf);
                }
            }

            isActive = true;
        }
    }

    private Palette palette;
    private int currentText;
    private int currentBG;
    private PorterDuffColorFilter controlColor = new PorterDuffColorFilter(currentText, PorterDuff.Mode.SRC_ATOP);

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void updateAlbumArtColors() {
        if (sharedPreferences.getBoolean(getString(R.string.background_palette), true)) {
            if (artworkBitmap != null) {
                backgroundHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        palette = Palette.generate(artworkBitmap);
//                    uiHandler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            artView.setImageBitmap(artworkBitmap);
//                        }
//                    });
                        try {
                            currentBG = palette.getDarkMutedColor(Color.BLACK);
                            if (currentBG == Color.BLACK) {
                                currentBG = palette.getDarkVibrantColor(Color.BLACK);
                            }
                            currentText = palette.getLightVibrantColor(Color.WHITE);
                            if (currentText == Color.WHITE) {
                                currentText = palette.getLightMutedColor(Color.WHITE);
                            }
                            controlColor = new PorterDuffColorFilter(currentText, PorterDuff.Mode.SRC_ATOP);
                            uiHandler.postAtFrontOfQueue(new Runnable() {
                                @Override
                                public void run() {
//                                metadataBackground.setBackgroundColor(currentBG);
//                                metadataBackground.setAlpha(0.5f);
                                    if (sharedPreferences.getBoolean(getString(R.string.clock_color_palette), false)) {
                                        clock.setTextColor(currentText);
                                    }
                                    if (sharedPreferences.getBoolean(getString(R.string.metadata_color_palette), false)) {
                                        albumText.setTextColor(currentText);
                                        artistText.setTextColor(currentText);
                                        titleText.setTextColor(currentText);
                                    }
                                    if (sharedPreferences.getBoolean(getString(R.string.clock_color_palette), false)) {
                                        batteryInfo.setTextColor(currentText);
                                    }
                                    if (sharedPreferences.getBoolean(getString(R.string.clock_color_palette), false)) {
                                        positionClock.setTextColor(currentText);
                                        durationClock.setTextColor(currentText);
                                        playButton.setColorFilter(controlColor);
                                        pauseButton.setColorFilter(controlColor);
                                        skipForwardButton.setColorFilter(controlColor);
                                        skipBackButton.setColorFilter(controlColor);
                                    }
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && sharedPreferences.getBoolean(getString(R.string.progress_color_palette), true)) {
                                        ColorStateList colorState = new ColorStateList(new int[][]{
                                                new int[]{-android.R.attr.enabled}},
                                                new int[]{currentText});
                                        progressBar.setProgressTintList(colorState);
                                        progressBar.setThumbTintList(colorState);
                                    }
                                    if (sharedPreferences.getBoolean(getString(R.string.background_palette), true)) {
                                        daydreamFrame.setBackgroundColor(currentBG);
                                    }
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else {
                backgroundHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        currentBG = Color.BLACK;
                        currentText = Color.WHITE;
                        controlColor = new PorterDuffColorFilter(currentText, PorterDuff.Mode.SRC_ATOP);

                        uiHandler.postAtFrontOfQueue(new Runnable() {
                            @Override
                            public void run() {
//                                metadataBackground.setBackgroundColor(currentBG);
//                                metadataBackground.setAlpha(0.5f);
                                if (sharedPreferences.getBoolean(getString(R.string.clock_color_palette), false)) {
                                    clock.setTextColor(currentText);
                                }
                                if (sharedPreferences.getBoolean(getString(R.string.metadata_color_palette), false)) {
                                    albumText.setTextColor(currentText);
                                    artistText.setTextColor(currentText);
                                    titleText.setTextColor(currentText);
                                }
                                if (sharedPreferences.getBoolean(getString(R.string.clock_color_palette), false)) {
                                    batteryInfo.setTextColor(currentText);
                                }
                                if (sharedPreferences.getBoolean(getString(R.string.clock_color_palette), false)) {
                                    positionClock.setTextColor(currentText);
                                    durationClock.setTextColor(currentText);
                                    playButton.setColorFilter(controlColor);
                                    pauseButton.setColorFilter(controlColor);
                                    skipForwardButton.setColorFilter(controlColor);
                                    skipBackButton.setColorFilter(controlColor);
                                }
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && sharedPreferences.getBoolean(getString(R.string.progress_color_palette), true)) {
                                    ColorStateList colorState = new ColorStateList(new int[][]{
                                            new int[]{-android.R.attr.enabled}},
                                            new int[]{currentText});
                                    progressBar.setProgressTintList(colorState);
                                    progressBar.setThumbTintList(colorState);
                                }
                                if (sharedPreferences.getBoolean(getString(R.string.background_palette), true)) {
                                    daydreamFrame.setBackgroundColor(currentBG);
                                }
                            }
                        });
                    }
                });
            }
        } else if (sharedPreferences.getBoolean(getString(R.string.background_black), false)) {
            currentBG = Color.BLACK;
        }
    }

    private class OnClickEllipsize implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            v.setSelected(!v.isSelected());
        }
    }

    private boolean seeking = false;

    private class SeekBarListener implements SeekBar.OnSeekBarChangeListener {
        private int seekingProgress;

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            seekingProgress = progress;
            positionClock.setVisibility(postionViewInts);
            if (sharedPreferences.getBoolean(getString(R.string.song_time_left), true)) {
                durationClock.setValue(seekingProgress - durationLong);
            }
            positionClock.setValue(seekingProgress);
            progressBar.setProgress((int) seekingProgress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            seeking = true;
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            seeking = false;
            listenerService.seekTo(seekingProgress);
        }
    }

    private class MusicSwipeListener implements View.OnTouchListener {

        private final GestureDetector gestureDetector;
        private Context context;
        private SharedPreferences.Editor editor = sharedPreferences.edit();

        public MusicSwipeListener(Context ctx) {
            context = ctx;
            gestureDetector = new GestureDetector(context, new GestureListener());
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP){
                sendBroadcast(MusicalDaydreamService.startTimeout);
            }
            return gestureDetector.onTouchEvent(event);
        }


        private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

            private static final int SWIPE_THRESHOLD = 300;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//            Log.i("onFling", "YES");
                boolean result = false;
                try {
                    float diffY = e2.getY() - e1.getY();
                    float diffX = e2.getX() - e1.getX();
                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffX > 0) {
                                onSwipeRight();
                            } else {
                                onSwipeLeft();
                            }
                        }
                    } else {
                        if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffY > 0) {
                                onSwipeBottom();
                            } else {
                                onSwipeTop();
                            }
                        }
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                return result;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (sharedPreferences.getString(getString(R.string.double_tap_options), "Stop Dreaming").equals("Stop Dreaming")) {
                    Intent i = new Intent(MusicalDaydreamService.MAIN_RECEIVER);
                    i.putExtra(MusicalDaydreamService.COMMAND, MusicalDaydreamService.SHUTDOWN);
                    sendBroadcast(i);
                } else {
                    if (sharedPreferences.getBoolean(getString(R.string.dim_screen), true)) {
                        editor.putBoolean(getString(R.string.dim_screen), false);
                    } else {
                        editor.putBoolean(getString(R.string.dim_screen), true);
                    }
                    editor.apply();
                    Intent i = new Intent(MusicalDaydreamService.MAIN_RECEIVER);
                    i.putExtra(MusicalDaydreamService.COMMAND, "updateScreenDim");
                    sendBroadcast(i);
                }
                return super.onDoubleTap(e);
            }
        }

        public void onSwipeTop() {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                Intent intent = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_MUSIC);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                shutdown();
            }else {
                try {
                    if (activeContainer.getMediaController().getSessionActivity() != null) {
                        activeContainer.getMediaController().getSessionActivity().send();
                        shutdown();
                    } else {
                        Log.i(TAG, "null activity");
                        Intent intent = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_MUSIC);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        shutdown();
                    }
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                    Intent intent = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_MUSIC);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    shutdown();
                }
            }
        }

        public void onSwipeRight() {
            if (sharedPreferences.getBoolean(getString(R.string.swipe_inversion), true)) {
                if (listenerService != null){
                    listenerService.skipBack();
                }
            } else {
                if (listenerService != null){
                    listenerService.skipForward();
                }
            }
        }

        public void onSwipeLeft() {
            Log.i("Swipe", "Left");
            if (sharedPreferences.getBoolean(getString(R.string.swipe_inversion), true)) {
                if (listenerService != null){
                    listenerService.skipForward();
                }
            } else {
                if (listenerService != null){
                    listenerService.skipBack();
                }
            }
        }

        public void onSwipeBottom() {
            if (listenerService != null){
                listenerService.playPause();
            }
        }
    }
}
