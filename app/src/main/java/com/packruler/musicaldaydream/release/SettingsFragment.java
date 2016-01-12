package com.packruler.musicaldaydream.release;

import android.app.Activity;
import android.app.Dialog;
import android.app.backup.BackupManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Parcelable;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.MediaStore;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.chiralcode.colorpicker.ColorPickerPreference;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.webfonts.Webfonts;
import com.google.api.services.webfonts.WebfontsRequestInitializer;
import com.google.api.services.webfonts.model.Webfont;
import com.google.api.services.webfonts.model.WebfontList;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Packruler on 5/5/2014.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private final String TAG = this.getClass().getSimpleName();
    protected final String GOOGLE_API = "AIzaSyBnW08IYnAQxffnBP6F_4JIFrOvVF5-nWM";

    private Handler handlerUI = new Handler(Looper.getMainLooper());
    private static final int SELECT_PICTURE = 1;
    private String selectedImagePath;
    private String filemanagerstring;

    protected SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private CheckBoxPreference scrollArt;
    private CheckBoxPreference swipeInversion;
    private CheckBoxPreference dimScreen;
    private CheckBoxPreference albumTitle;
    private CheckBoxPreference immersiveMode;
    private CheckBoxPreference fullscreenArt;
    private CheckBoxPreference stableControls;
    private NumberPickerDialogPreference scrollDuration;
    private CheckBoxPreference displaySongTimes;
    private CheckBoxPreference songTimeLeft;
    private CheckBoxPreference limitMarquee;
    private CheckBoxPreference displayBattery;
    private CheckBoxPreference hideBatteryFull;

    private ColorPickerPreference fullThemeCustom;
    private ColorPickerPreference metadataThemeCustom;
    private ColorPickerPreference clockThemeCustom;
    private ColorPickerPreference progressBarThemeCustom;
    private ColorPickerPreference controlThemeCustom;
    private ColorPickerPreference batteryThemeCustom;

    private NumberPickerDialogPreference notificationIconSize;
    private NumberPickerDialogPreference clockTextSize;
    private NumberPickerDialogPreference artistTextSize;
    private NumberPickerDialogPreference trackTextSize;
    private NumberPickerDialogPreference albumTextSize;


    private CheckBoxPreference fadeAll;
    private CheckBoxPreference fadeClock;
    private CheckBoxPreference fadeMetadata;
    private CheckBoxPreference fadeNotification;
    private CheckBoxPreference fadeMusicControls;

    private NumberPickerDialogPreference allAlphaPlaying;
    private NumberPickerDialogPreference allAlphaPaused;
    private NumberPickerDialogPreference clockAlphaPlaying;
    private NumberPickerDialogPreference clockAlphaPaused;
    private NumberPickerDialogPreference metadataAlphaPlaying;
    private NumberPickerDialogPreference metadataAlphaPaused;
    private NumberPickerDialogPreference notificationIconAlphaPlaying;
    private NumberPickerDialogPreference notificationIconAlphaPaused;
    private NumberPickerDialogPreference controlAlphaPlaying;
    private NumberPickerDialogPreference controlAlphaPaused;

    private static ArrayList<Webfont> sansSerif = new ArrayList<Webfont>();
    private static ArrayList<Webfont> serif = new ArrayList<Webfont>();
    private static ArrayList<Webfont> display = new ArrayList<Webfont>();
    private static ArrayList<Webfont> handwriting = new ArrayList<Webfont>();
    private static ArrayList<Webfont> monospace = new ArrayList<Webfont>();

    private Webfonts.WebfontsOperations.List list;
    private WebfontList fontList;
    private List<Webfont> webfontList;

    private MultiSelectListPreference deleteMultFonts;
    private ListPreference deleteAllFonts;
    private ListPreference downloadAllSansSerifPref;
    private ListPreference downloadAllSerifPref;
    private ListPreference downloadAllDisplayPref;
    private ListPreference downloadAllHandwritingPref;
    private ListPreference downloadAllMonospacePref;
    private ListPreference downloadAllPref;

    private ListPreference sortOrder;
    private FontPreference sansSerifFont;
    private FontPreference serifFont;
    private FontPreference displayFont;
    private FontPreference handwritingFont;
    private FontPreference monospaceFont;
    private CheckBoxPreference defaultFont;

    protected static File fontDir;
    protected static File[] fileList;
    private TextView testFont;
    private PreferenceScreen fontPrefScreen;
    private FontPreference fontPreferenceList;

    private PreferenceScreen themeSettings;

    private ListPreference screenTimeout;
    private Preference folderSelection;

    private MultiSelectListPreference blackListPreference;
    private CharSequence[] allPackages;
    private CharSequence[] allNames;

    private GetBlacklistArray getBlacklistArray = new GetBlacklistArray();
    private SetSansSerifArray setSansSerifArray;
    private SetSerifArray setSerifArray;
    private SetDisplayArray setDisplayArray;
    private SetHandwritingArray setHandwritingArray;
    private SetMonospaceArray setMonospaceArray;
    private GetFonts getFonts;
    private GetDownloadedFontList getDownloadedFontList;
    private SetAlphaPreferences setAlphaPreferences = new SetAlphaPreferences();
    private InitializeVariables initializeVariables = new InitializeVariables();

    private Toast toast;

    private DownloadAllSansSerif downloadAllSansSerif;
    private DownloadAllSerif downloadAllSerif;
    private DownloadAllDisplay downloadAllDisplay;
    private DownloadAllHandwriting downloadAllHandwriting;
    private DownloadAllMonospace downloadAllMonospace;
    private DownloadAllFonts downloadAllFonts;


    private NetHttpTransport netHttpTransport;
    private AndroidJsonFactory jsonFactory;
    private WebfontsRequestInitializer requestInitializer;
    private Webfonts.Builder builder;
    private Webfonts webfonts;
    private Webfonts.WebfontsOperations operations;
    private String lastSort = "";

    private ListPreference doubleTapAction;
    private PreferenceScreen themePrefScreen;
    private CheckBoxPreference progressBarThemeDefault;
    private CheckBoxPreference fullThemeDefault;
    private CheckBoxPreference metadataThemeDefault;
    private CheckBoxPreference batteryThemeDefault;
    private CheckBoxPreference clockThemeDefault;
    private CheckBoxPreference controlThemeDefault;
    private CheckBoxPreference fullThemePalette;
    private CheckBoxPreference metadataThemePalette;
    private CheckBoxPreference batteryThemePalette;
    private CheckBoxPreference clockThemePalette;
    private CheckBoxPreference controlThemePalette;
    private CheckBoxPreference progressBarThemePalette;
    private CheckBoxPreference displayNotifications;
    private ColorPickerPreference backgroundColorCustom;
    private CheckBoxPreference backgroundColorBlack;
    private CheckBoxPreference backgroundColorPalette;
    private CheckBoxPreference maintainPosition;
    private CheckBoxPreference fullProgressBar;
    private CheckBoxPreference fitArt;
    private CheckBoxPreference hideStartDreaming;
    private HandlerThread backgroundThread;
    private Handler backgroundHandler;
    private HandlerThread backgroundThread1;
    private Handler backgroundHandler1;
    private HandlerThread backgroundThread2;
    private Handler backgroundHandler2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fontDir = getActivity().getFilesDir();

        backgroundThread = new HandlerThread("Musical Daydream Settings Background 0");
        backgroundThread.start();
        backgroundThread1 = new HandlerThread("Musical Daydream Settings Background 1");
        backgroundThread1.start();
        backgroundThread2 = new HandlerThread("Musical Daydream Settings Background 2");
        backgroundThread2.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
        backgroundHandler1 = new Handler(backgroundThread1.getLooper());
        backgroundHandler2 = new Handler(backgroundThread2.getLooper());

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preference);

        getPreferenceManager().setSharedPreferencesName(getString(R.string.settings_string));

        sharedPreferences = getPreferenceManager().getSharedPreferences();
        editor = sharedPreferences.edit();

        backgroundHandler.post(initializeVariables);
        backgroundHandler1.post(setAlphaPreferences);

        blackListPreference = (MultiSelectListPreference) findPreference(getString(R.string.blacklist));

        backgroundHandler2.post(getBlacklistArray);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onStop() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        try {
            backgroundThread.quitSafely();
            backgroundThread1.quitSafely();
            backgroundThread2.quitSafely();
            stopUpdateFonts();

            if (getDownloadedFontList.isAlive()) {
                getDownloadedFontList.interrupt();
            }
        } catch (NullPointerException e) {
            Log.i(TAG, e.toString());
        }
        super.onStop();
    }

    public void updateToggles() {
        backgroundHandler1.post(new Runnable() {
            @Override
            public void run() {
                fullscreenArt.setChecked(sharedPreferences.getBoolean(getString(R.string.fullscreen_art), true));
                fitArt.setChecked(sharedPreferences.getBoolean(getString(R.string.fit_art), false));
                fitArt.setEnabled(!fullscreenArt.isChecked());
                scrollArt.setChecked(sharedPreferences.getBoolean(getString(R.string.scroll_art), true));
                scrollArt.setEnabled(!fitArt.isChecked());

                dimScreen.setChecked(sharedPreferences.getBoolean(getString(R.string.dim_screen), true));
                swipeInversion.setChecked(sharedPreferences.getBoolean(getString(R.string.swipe_inversion), true));
                albumTitle.setChecked(sharedPreferences.getBoolean(getString(R.string.display_album_title), true));
                immersiveMode.setChecked(sharedPreferences.getBoolean(getString(R.string.immersive_mode), true));
                stableControls.setChecked(sharedPreferences.getBoolean(getString(R.string.stable_bottom), true));
                limitMarquee.setChecked(sharedPreferences.getBoolean(getString(R.string.limit_marquee), false));
                displayBattery.setChecked(sharedPreferences.getBoolean(getString(R.string.display_battery), true));
                hideBatteryFull.setChecked(sharedPreferences.getBoolean(getString(R.string.hide_battery_full), false));

                stableControls.setEnabled(sharedPreferences.getBoolean(getString(R.string.immersive_mode), true));

                scrollDuration.setValue(sharedPreferences.getInt(getString(R.string.art_scroll_duration), 12));

                displaySongTimes.setChecked(sharedPreferences.getBoolean(getString(R.string.display_song_times), false));
                songTimeLeft.setChecked(sharedPreferences.getBoolean(getString(R.string.song_time_left), false));
                songTimeLeft.setEnabled(sharedPreferences.getBoolean(getString(R.string.display_song_times), false));
                fullThemeDefault.setChecked(sharedPreferences.getBoolean(getString(R.string.full_theme_framework), false));
                fullThemePalette.setChecked(sharedPreferences.getBoolean(getString(R.string.full_theme_palette), false));
                metadataThemeCustom.setValue(sharedPreferences.getInt(getString(R.string.metadata_color_custom), Color.WHITE));
                metadataThemeDefault.setChecked(sharedPreferences.getBoolean(getString(R.string.metadata_color_framework), true));
                metadataThemeDefault.setChecked(sharedPreferences.getBoolean(getString(R.string.metadata_color_palette), false));
                clockThemeCustom.setValue(sharedPreferences.getInt(getString(R.string.clock_color_custom), Color.WHITE));
                clockThemeDefault.setChecked(sharedPreferences.getBoolean(getString(R.string.clock_color_framework), true));
                clockThemePalette.setChecked(sharedPreferences.getBoolean(getString(R.string.clock_color_palette), false));
                controlThemeCustom.setValue(sharedPreferences.getInt(getString(R.string.control_color_custom), Color.WHITE));
                controlThemeDefault.setChecked(sharedPreferences.getBoolean(getString(R.string.control_color_framework), true));
                controlThemePalette.setChecked(sharedPreferences.getBoolean(getString(R.string.control_color_palette), false));
                progressBarThemeCustom.setValue(sharedPreferences.getInt(getString(R.string.progress_color_custom), Color.WHITE));
                progressBarThemeDefault.setChecked(sharedPreferences.getBoolean(getString(R.string.progress_color_framework), false));
                progressBarThemePalette.setChecked(sharedPreferences.getBoolean(getString(R.string.progress_color_palette), true));
                batteryThemeCustom.setValue(sharedPreferences.getInt(getString(R.string.battery_color_custom), Color.WHITE));
                batteryThemeDefault.setChecked(sharedPreferences.getBoolean(getString(R.string.battery_color_framework), true));
                batteryThemePalette.setChecked(sharedPreferences.getBoolean(getString(R.string.battery_color_palette), false));
                backgroundColorBlack.setChecked(sharedPreferences.getBoolean(getString(R.string.background_black), false));
                backgroundColorPalette.setChecked(sharedPreferences.getBoolean(getString(R.string.background_palette), true));

                if (sharedPreferences.getInt(getString(R.string.metadata_color), Color.WHITE) == sharedPreferences.getInt(getString(R.string.clock_color), Color.WHITE)
                        && sharedPreferences.getInt(getString(R.string.metadata_color), Color.WHITE) == sharedPreferences.getInt(getString(R.string.control_color), Color.WHITE)
                        && sharedPreferences.getInt(getString(R.string.metadata_color), Color.WHITE) == sharedPreferences.getInt(getString(R.string.progress_color), Color.WHITE)
                        && sharedPreferences.getInt(getString(R.string.metadata_color), Color.WHITE) == sharedPreferences.getInt(getString(R.string.battery_color), Color.WHITE)) {
                    fullThemeCustom.setDefaultValue(sharedPreferences.getInt(getString(R.string.metadata_color), Color.WHITE));
                }

                notificationIconSize.setValue(sharedPreferences.getInt(getString(R.string.notification_icon_size), 0));
                clockTextSize.setValue(sharedPreferences.getInt(getString(R.string.clock_text_size), 0));
                artistTextSize.setValue(sharedPreferences.getInt(getString(R.string.artist_name_text_size), 0));
                trackTextSize.setValue(sharedPreferences.getInt(getString(R.string.track_title_text_size), 0));
                albumTextSize.setValue(sharedPreferences.getInt(getString(R.string.album_name_text_size), 0));

                screenTimeout.setValue(sharedPreferences.getString(getString(R.string.screen_timeout_pause), "Never"));

                sortOrder.setValue(sharedPreferences.getString(getString(R.string.sort_order), "Alphabetical"));
                sansSerifFont.setValue(sharedPreferences.getString(getString(R.string.sans_serif), null));
                serifFont.setValue(sharedPreferences.getString(getString(R.string.serif), null));
                displayFont.setValue(sharedPreferences.getString(getString(R.string.font_display), null));
                handwritingFont.setValue(sharedPreferences.getString(getString(R.string.handwriting), null));
                monospaceFont.setValue(sharedPreferences.getString(getString(R.string.monospace), null));

                defaultFont.setChecked(sharedPreferences.getBoolean(getString(R.string.framework_font), true));
                defaultFont.setEnabled(!sharedPreferences.getBoolean(getString(R.string.framework_font), true));

                doubleTapAction.setValue(sharedPreferences.getString(getString(R.string.double_tap_options), null));
                doubleTapAction.setSummary("Set to: " + sharedPreferences.getString(getString(R.string.double_tap_options), null));

                maintainPosition.setChecked(sharedPreferences.getBoolean(getString(R.string.maintain_position), true));
                fullProgressBar.setChecked(sharedPreferences.getBoolean(getString(R.string.full_progress_bar), false));
                hideStartDreaming.setChecked(sharedPreferences.getBoolean(getString(R.string.hide_start_dreaming), false));
//                folderSelection.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//                    @Override
//                    public boolean onPreferenceClick(Preference preference) {
//                        Intent intent = new Intent();
//                        intent.setType("image/*");
//                        intent.setAction(Intent.ACTION_GET_CONTENT);
//                        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
//                        startActivityForResult(Intent.createChooser(intent,
//                                "Select Picture"), SELECT_PICTURE);
//                        return false;
//                    }
//                });
            }
        });
    }

    //UPDATED
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                if (data.hasExtra(Intent.EXTRA_STREAM)) {
                    Log.i(TAG, "YESSSSSSSSSS");
                    // retrieve a collection of selected images
                    ArrayList<Parcelable> list = data.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                    // iterate over these images
                    for (Parcelable parcel : list) {
                        Uri uri = (Uri) parcel;
                        // handle the images one by one here
                    }
                } else {
                    Uri selectedImageUri = data.getData();

                    //OI FILE Manager
                    filemanagerstring = selectedImageUri.getPath();

                    //MEDIA GALLERY
                    selectedImagePath = getPath(selectedImageUri);

                    //DEBUG PURPOSE - you can delete this if you want
                    if (selectedImagePath != null)
                        System.out.println(selectedImagePath);
                    else System.out.println("selectedImagePath is null");
                    if (filemanagerstring != null)
                        System.out.println(filemanagerstring);
                    else System.out.println("filemanagerstring is null");

                    //NOW WE HAVE OUR WANTED STRING
                    if (selectedImagePath != null)
                        System.out.println("selectedImagePath is the right one for you!");
                    else
                        System.out.println("filemanagerstring is the right one for you!");
                }
            }
        }
    }

    //UPDATED!
    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        CursorLoader cursorLoader = new CursorLoader(FirstTimeSetup.context, uri, projection, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();
        if (cursor != null) {
            //HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            //THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else return null;
    }

    public class GetBlacklistArray implements Runnable {
        public void run() {
            try {
                if (sharedPreferences.getStringSet(getString(R.string.notification_packages), new HashSet<String>()) != null) {
                    if (sharedPreferences.getStringSet(getString(R.string.notification_packages), new HashSet<String>()).size() > 0) {
                        String[] packageNameArray = sharedPreferences.getStringSet(getString(R.string.notification_packages), new HashSet<String>()).toArray(new String[1]);
                        String[] stringNameArray = new String[packageNameArray.length];

                        if (DaydreamSettings.context != null) {
                            for (int x = 0; x < packageNameArray.length; x++) {
                                try {
                                    String name = DaydreamSettings.context.getPackageManager().getApplicationLabel(DaydreamSettings.context.getPackageManager().getApplicationInfo(packageNameArray[x], 0)).toString();
                                    stringNameArray[x] = name;
//                                    Log.i(packageNameArray[x], stringNameArray[x]);
                                } catch (PackageManager.NameNotFoundException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        blackListPreference.setEntries(stringNameArray);
                        blackListPreference.setEntryValues(packageNameArray);
                    } else {
                        blackListPreference.setEnabled(false);
                        blackListPreference.setSummary("No applications have sent notifications yet");
                    }
                    if (sharedPreferences.getStringSet(getString(R.string.blacklist), new HashSet<String>()) != null &&
                            sharedPreferences.getStringSet(getString(R.string.blacklist), new HashSet<String>()).size() > 0) {
                        blackListPreference.setValues(sharedPreferences.getStringSet(getString(R.string.blacklist), new HashSet<String>()));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences inSharedPreferences, String key) {
        Log.i(TAG, key + " changed");
        if (key.equals(getString(R.string.hide_start_dreaming))) {
            PackageManager packageManager = context.getPackageManager();
            ComponentName componentName = new ComponentName(context.getPackageName(), "com.packruler.musicaldaydream.release.StartDreaming");
            if (sharedPreferences.getBoolean(key, false)) {
                packageManager.setComponentEnabledSetting(componentName,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            } else {
                packageManager.setComponentEnabledSetting(componentName,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            }
        } else if (key.equals(getString(R.string.fullscreen_art))) {
            fitArt.setEnabled(!sharedPreferences.getBoolean(key, true));
            if (sharedPreferences.getBoolean(key, true)) {
                fitArt.setChecked(false);
                editor.putBoolean(getString(R.string.fit_art), false);
                editor.apply();
            }
        } else if (key.equals(getString(R.string.fit_art))) {
            scrollArt.setEnabled(!sharedPreferences.getBoolean(key, false));
            if (sharedPreferences.getBoolean(key, false)) {
                scrollArt.setChecked(false);
                editor.putBoolean(getString(R.string.scroll_art), true);
                editor.apply();
            }
        } else if (key.equals(getString(R.string.immersive_mode))) {
            stableControls.setEnabled(sharedPreferences.getBoolean(getString(R.string.immersive_mode), true));
        } else if (key.equals(getString(R.string.full_theme_custom))) {
            metadataThemeCustom.setValue(fullThemeCustom.getValue());
            clockThemeCustom.setValue(fullThemeCustom.getValue());
            controlThemeCustom.setValue(fullThemeCustom.getValue());
            progressBarThemeCustom.setValue(fullThemeCustom.getValue());
            batteryThemeCustom.setValue(fullThemeCustom.getValue());
            fullThemeDefault.setChecked(false);
            fullThemePalette.setChecked(false);
        } else if (key.equals(getString(R.string.full_theme_framework))) {
            if (fullThemeDefault.isChecked()) {
                metadataThemeDefault.setChecked(fullThemeDefault.isChecked());
                clockThemeDefault.setChecked(fullThemeDefault.isChecked());
                controlThemeDefault.setChecked(fullThemeDefault.isChecked());
                progressBarThemeDefault.setChecked(fullThemeDefault.isChecked());
                batteryThemeDefault.setChecked(fullThemeDefault.isChecked());
                backgroundColorBlack.setChecked(fullThemeDefault.isChecked());
                fullThemeDefault.setEnabled(false);
                fullThemePalette.setChecked(false);
            } else {
                fullThemeDefault.setEnabled(true);
            }
        } else if (key.equals(getString(R.string.full_theme_palette))) {
            if (fullThemePalette.isChecked()) {
                metadataThemePalette.setChecked(fullThemePalette.isChecked());
                clockThemePalette.setChecked(fullThemePalette.isChecked());
                controlThemePalette.setChecked(fullThemePalette.isChecked());
                progressBarThemePalette.setChecked(fullThemePalette.isChecked());
                batteryThemePalette.setChecked(fullThemePalette.isChecked());
                backgroundColorPalette.setChecked(fullThemePalette.isChecked());
                fullThemeDefault.setChecked(false);
                fullThemePalette.setEnabled(false);
            } else {
                fullThemePalette.setEnabled(true);
            }
        } else if (key.equals(getString(R.string.metadata_color_custom))) {
            metadataThemeDefault.setChecked(false);
            metadataThemePalette.setChecked(false);
            fullThemeDefault.setChecked(false);
            fullThemePalette.setChecked(false);
        } else if (key.equals(getString(R.string.clock_color_custom))) {
            clockThemeDefault.setChecked(false);
            clockThemePalette.setChecked(false);
            fullThemeDefault.setChecked(false);
            fullThemePalette.setChecked(false);
        } else if (key.equals(getString(R.string.progress_color_custom))) {
            progressBarThemeDefault.setChecked(false);
            progressBarThemePalette.setChecked(false);
            fullThemeDefault.setChecked(false);
            fullThemePalette.setChecked(false);
        } else if (key.equals(getString(R.string.control_color_custom))) {
            controlThemeDefault.setChecked(false);
            controlThemePalette.setChecked(false);
            fullThemeDefault.setChecked(false);
            fullThemePalette.setChecked(false);
        } else if (key.equals(getString(R.string.battery_color_custom))) {
            batteryThemeDefault.setChecked(false);
            batteryThemePalette.setChecked(false);
            fullThemeDefault.setChecked(false);
            fullThemePalette.setChecked(false);
        } else if (key.equals(getString(R.string.background_custom))) {
            backgroundColorBlack.setChecked(false);
            backgroundColorPalette.setChecked(false);
            fullThemeDefault.setChecked(false);
            fullThemePalette.setChecked(false);
        } else if (key.equals(getString(R.string.metadata_color_framework))) {
            if (metadataThemeDefault.isChecked()) {
                metadataThemeDefault.setEnabled(false);
                metadataThemePalette.setChecked(false);
            } else {
                metadataThemeDefault.setEnabled(true);
//                fullThemeDefault.setChecked(false);
//                fullThemePalette.setChecked(false);
            }
        } else if (key.equals(getString(R.string.clock_color_framework))) {
            if (clockThemeDefault.isChecked()) {
                clockThemeDefault.setEnabled(false);
                clockThemePalette.setChecked(false);
            } else {
                clockThemeDefault.setEnabled(true);
//                fullThemeDefault.setChecked(false);
//                fullThemePalette.setChecked(false);
            }
        } else if (key.equals(getString(R.string.progress_color_framework))) {
            if (progressBarThemeDefault.isChecked()) {
                progressBarThemeDefault.setEnabled(false);
                progressBarThemePalette.setChecked(false);
            } else {
                progressBarThemeDefault.setEnabled(true);
//                fullThemeDefault.setChecked(false);
//                fullThemePalette.setChecked(false);
            }
        } else if (key.equals(getString(R.string.battery_color_framework))) {
            if (batteryThemeDefault.isChecked()) {
                batteryThemeDefault.setEnabled(false);
                batteryThemePalette.setChecked(false);
            } else {
                batteryThemeDefault.setEnabled(true);
//                fullThemeDefault.setChecked(false);
//                fullThemePalette.setChecked(false);
            }
        } else if (key.equals(getString(R.string.control_color_framework))) {
            if (controlThemeDefault.isChecked()) {
                controlThemeDefault.setEnabled(false);
                controlThemePalette.setChecked(false);
            } else {
                controlThemeDefault.setEnabled(true);
//                fullThemeDefault.setChecked(false);
//                fullThemePalette.setChecked(false);
            }
        } else if (key.equals(getString(R.string.background_black))) {
            if (backgroundColorBlack.isChecked()) {
                backgroundColorBlack.setEnabled(false);
                backgroundColorPalette.setChecked(false);
            } else {
                backgroundColorBlack.setEnabled(true);
//                fullThemeDefault.setChecked(false);
//                fullThemePalette.setChecked(false);
            }
        } else if (key.equals(getString(R.string.metadata_color_palette))) {
            if (metadataThemePalette.isChecked()) {
                metadataThemePalette.setEnabled(false);
                metadataThemeDefault.setChecked(false);
            } else {
                metadataThemePalette.setEnabled(true);
//                fullThemeDefault.setChecked(false);
//                fullThemePalette.setChecked(false);
            }
        } else if (key.equals(getString(R.string.clock_color_palette))) {
            if (clockThemePalette.isChecked()) {
                clockThemePalette.setEnabled(false);
                clockThemeDefault.setChecked(false);
            } else {
                clockThemePalette.setEnabled(true);
//                fullThemeDefault.setChecked(false);
//                fullThemePalette.setChecked(false);
            }
        } else if (key.equals(getString(R.string.progress_color_palette))) {
            if (progressBarThemePalette.isChecked()) {
                progressBarThemePalette.setEnabled(false);
                progressBarThemeDefault.setChecked(false);
            } else {
                progressBarThemePalette.setEnabled(true);
//                fullThemeDefault.setChecked(false);
//                fullThemePalette.setChecked(false);
            }
        } else if (key.equals(getString(R.string.battery_color_palette))) {
            if (batteryThemePalette.isChecked()) {
                batteryThemePalette.setEnabled(false);
                batteryThemeDefault.setChecked(false);
            } else {
                batteryThemePalette.setEnabled(true);
//                fullThemeDefault.setChecked(false);
//                fullThemePalette.setChecked(false);
            }
        } else if (key.equals(getString(R.string.control_color_palette))) {
            if (controlThemePalette.isChecked()) {
                controlThemePalette.setEnabled(false);
                controlThemeDefault.setChecked(false);
            } else {
                controlThemePalette.setEnabled(true);
//                fullThemeDefault.setChecked(false);
//                fullThemePalette.setChecked(false);
            }
        } else if (key.equals(getString(R.string.background_palette))) {
            if (backgroundColorPalette.isChecked()) {
                backgroundColorPalette.setEnabled(false);
                backgroundColorBlack.setChecked(false);
            } else {
                backgroundColorPalette.setEnabled(true);
//                fullThemeDefault.setChecked(false);
//                fullThemePalette.setChecked(false);
            }
        } else if (key.equals(getString(R.string.blacklist))) {
            Log.i(TAG, sharedPreferences.getStringSet(getString(R.string.blacklist), new HashSet<String>()).toString());
        } else if (key.equals(getString(R.string.display_song_times))) {
            songTimeLeft.setEnabled(sharedPreferences.getBoolean(getString(R.string.display_song_times), false));
        } else if (key.equals(getString(R.string.fade_all))) {
            fadeClock.setChecked(sharedPreferences.getBoolean(getString(R.string.fade_all), true));
            fadeMetadata.setChecked(sharedPreferences.getBoolean(getString(R.string.fade_all), true));
            fadeNotification.setChecked(sharedPreferences.getBoolean(getString(R.string.fade_all), true));
            fadeMusicControls.setChecked(sharedPreferences.getBoolean(getString(R.string.fade_all), true));
        } else if (key.equals(getString(R.string.all_alpha_playing))) {
            clockAlphaPlaying.setValue(sharedPreferences.getInt(getString(R.string.all_alpha_playing), 100));
            metadataAlphaPlaying.setValue(sharedPreferences.getInt(getString(R.string.all_alpha_playing), 100));
            notificationIconAlphaPlaying.setValue(sharedPreferences.getInt(getString(R.string.all_alpha_playing), 100));
            controlAlphaPlaying.setValue(sharedPreferences.getInt(getString(R.string.all_alpha_playing), 100));
        } else if (key.equals(getString(R.string.all_alpha_paused))) {
            clockAlphaPaused.setValue(sharedPreferences.getInt(getString(R.string.all_alpha_paused), 100));
            metadataAlphaPaused.setValue(sharedPreferences.getInt(getString(R.string.all_alpha_paused), 100));
            notificationIconAlphaPaused.setValue(sharedPreferences.getInt(getString(R.string.all_alpha_paused), 100));
            controlAlphaPaused.setValue(sharedPreferences.getInt(getString(R.string.all_alpha_paused), 100));
        } else if (key.equals(getString(R.string.display_battery))) {
            hideBatteryFull.setEnabled(sharedPreferences.getBoolean(getString(R.string.display_battery), false));
        } else if (key.equals(getString(R.string.sort_order))) {
            sansSerifFont.setEnabled(false);
            serifFont.setEnabled(false);
            displayFont.setEnabled(false);
            handwritingFont.setEnabled(false);
            monospaceFont.setEnabled(false);

            updateFonts();
        } else if (key.equals(getString(R.string.sans_serif)) && sharedPreferences.getString(key, null) != null) {
            serifFont.setValue(null);
            displayFont.setValue(null);
            handwritingFont.setValue(null);
            monospaceFont.setValue(null);
            defaultFont.setChecked(false);
            editor.putBoolean(getString(R.string.sans_serif_set), true);
            editor.putBoolean(getString(R.string.serif_set), false);
            editor.putBoolean(getString(R.string.font_display_set), false);
            editor.putBoolean(getString(R.string.handwriting_set), false);
            editor.putBoolean(getString(R.string.monospace_set), false);
            editor.apply();
            defaultFont.setEnabled(true);

            new DownloadFont(sharedPreferences.getString(key, null)).start();
        } else if (key.equals(getString(R.string.serif)) && sharedPreferences.getString(key, null) != null) {
            sansSerifFont.setValue(null);
            displayFont.setValue(null);
            handwritingFont.setValue(null);
            monospaceFont.setValue(null);
            defaultFont.setChecked(false);
            editor.putBoolean(getString(R.string.sans_serif_set), false);
            editor.putBoolean(getString(R.string.serif_set), true);
            editor.putBoolean(getString(R.string.font_display_set), false);
            editor.putBoolean(getString(R.string.handwriting_set), false);
            editor.putBoolean(getString(R.string.monospace_set), false);
            editor.apply();
            defaultFont.setEnabled(true);

            new DownloadFont(sharedPreferences.getString(key, null)).start();
        } else if (key.equals(getString(R.string.font_display)) && sharedPreferences.getString(key, null) != null) {
            sansSerifFont.setValue(null);
            serifFont.setValue(null);
            handwritingFont.setValue(null);
            monospaceFont.setValue(null);
            defaultFont.setChecked(false);
            editor.putBoolean(getString(R.string.sans_serif_set), false);
            editor.putBoolean(getString(R.string.serif_set), false);
            editor.putBoolean(getString(R.string.font_display_set), true);
            editor.putBoolean(getString(R.string.handwriting_set), false);
            editor.putBoolean(getString(R.string.monospace_set), false);
            editor.apply();
            defaultFont.setEnabled(true);

            new DownloadFont(sharedPreferences.getString(key, null)).start();
        } else if (key.equals(getString(R.string.handwriting)) && sharedPreferences.getString(key, null) != null) {
            sansSerifFont.setValue(null);
            serifFont.setValue(null);
            displayFont.setValue(null);
            monospaceFont.setValue(null);
            defaultFont.setChecked(false);
            editor.putBoolean(getString(R.string.sans_serif_set), false);
            editor.putBoolean(getString(R.string.serif_set), false);
            editor.putBoolean(getString(R.string.font_display_set), false);
            editor.putBoolean(getString(R.string.handwriting_set), true);
            editor.putBoolean(getString(R.string.monospace_set), false);
            defaultFont.setEnabled(true);

            new DownloadFont(sharedPreferences.getString(key, null)).start();
        } else if (key.equals(getString(R.string.monospace)) && sharedPreferences.getString(key, null) != null) {
            sansSerifFont.setValue(null);
            serifFont.setValue(null);
            displayFont.setValue(null);
            handwritingFont.setValue(null);
            defaultFont.setChecked(false);
            editor.putBoolean(getString(R.string.sans_serif_set), false);
            editor.putBoolean(getString(R.string.serif_set), false);
            editor.putBoolean(getString(R.string.font_display_set), false);
            editor.putBoolean(getString(R.string.handwriting_set), false);
            editor.putBoolean(getString(R.string.monospace_set), true);
            editor.apply();
            defaultFont.setEnabled(true);

            new DownloadFont(sharedPreferences.getString(key, null)).start();
        } else if (key.equals(getString(R.string.framework_font)) && sharedPreferences.getBoolean(key, true)) {
            sansSerifFont.setValue(null);
            serifFont.setValue(null);
            displayFont.setValue(null);
            handwritingFont.setValue(null);
            monospaceFont.setValue(null);
            editor.putBoolean(getString(R.string.sans_serif_set), false);
            editor.putBoolean(getString(R.string.serif_set), false);
            editor.putBoolean(getString(R.string.font_display_set), false);
            editor.putBoolean(getString(R.string.handwriting_set), false);
            editor.putBoolean(getString(R.string.monospace_set), false);
            editor.apply();
            defaultFont.setEnabled(false);


        } else if (key.equals(getString(R.string.delete_fonts)) && sharedPreferences.getStringSet(key, null) != null) {
            new DeleteFonts(sharedPreferences.getStringSet(key, null)).start();
        } else if (key.equals(getString(R.string.delete_all))) {
            if (sharedPreferences.getString(key, null).equals("Yes")) {
                new DeleteFonts().start();
            }
            deleteAllFonts.setValue("No");
            defaultFont.setEnabled(false);
            defaultFont.setChecked(true);
        } else if (key.equals(getString(R.string.download_sans_serif))) {
            if (sharedPreferences.getString(key, null).equals("Yes")) {
                downloadAllSansSerif = new DownloadAllSansSerif();
                downloadAllSansSerif.start();
            }
            downloadAllSansSerifPref.setValue("No");
        } else if (key.equals(getString(R.string.download_serif))) {
            if (sharedPreferences.getString(key, null).equals("Yes")) {
                downloadAllSerif = new DownloadAllSerif();
                downloadAllSerif.start();
            }
            downloadAllSerifPref.setValue("No");
        } else if (key.equals(getString(R.string.download_display))) {
            if (sharedPreferences.getString(key, null).equals("Yes")) {
                downloadAllDisplay = new DownloadAllDisplay();
                downloadAllDisplay.start();
            }
            downloadAllDisplayPref.setValue("No");
        } else if (key.equals(getString(R.string.download_handwriting))) {
            if (sharedPreferences.getString(key, null).equals("Yes")) {
                downloadAllHandwriting = new DownloadAllHandwriting();
                downloadAllHandwriting.start();
            }
            downloadAllHandwritingPref.setValue("No");
        } else if (key.equals(getString(R.string.download_monospace))) {
            if (sharedPreferences.getString(key, null).equals("Yes")) {
                downloadAllMonospace = new DownloadAllMonospace();
                downloadAllMonospace.start();
            }
            downloadAllMonospacePref.setValue("No");
        } else if (key.equals(getString(R.string.download_all))) {
            if (sharedPreferences.getString(key, null).equals("Yes")) {
                downloadAllFonts = new DownloadAllFonts();
                downloadAllFonts.start();
            }
            downloadAllPref.setValue("No");
        } else if (key.equals(getString(R.string.double_tap_options))) {
            doubleTapAction.setSummary("Set to: " + sharedPreferences.getString(key, null));
        } else if (key.equals(getString(R.string.maintain_position))) {
            fullProgressBar.setEnabled(!sharedPreferences.getBoolean(key, true));
        }

        sendToast(key + " Set");

        BackupManager.dataChanged(DaydreamSettings.context.getPackageName());
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        super.onPreferenceTreeClick(preferenceScreen, preference);

        // If the user has clicked on a preference screen, set up the action bar
        if (preference instanceof PreferenceScreen) {
            initializeActionBar((PreferenceScreen) preference, getResources().getColor(R.color.material_green_700), getResources().getColor(R.color.material_grey_50));
        }

        return false;
    }

    /**
     * Sets up the action bar for an {@link PreferenceScreen}
     */
    public static void initializeActionBar(PreferenceScreen preferenceScreen, int statusBarColor, int titleColor) {
        final Dialog dialog = preferenceScreen.getDialog();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = dialog.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            Log.i("InitializeActionBar", "Set bar");
        }
        dialog.setContentView(R.layout.toolbar_preference_screen);
        preferenceScreen.bind((android.widget.ListView) dialog.findViewById(android.R.id.list));
        if (dialog != null) {
            Toolbar toolbar = (Toolbar) dialog.getWindow().findViewById(R.id.dialog_toolbar);
            if (toolbar != null) {
                // Inialize the action bar
                toolbar.setTitle(preferenceScreen.getTitle());
                toolbar.inflateMenu(R.menu.action_bar_menu);
//                toolbar.setTitleTextColor(titleColor);
                toolbar.setNavigationIcon(R.drawable.toolbar_back);
                toolbar.setNavigationContentDescription("Back");
                toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                toolbar.setOnMenuItemClickListener(onMenuItemClickListener);
            }
            if (preferenceScreen.getTitle().equals(DaydreamSettings.context.getString(R.string.font_selection))) {
                Log.i("Find", "SUCCESS");
                Intent i = new Intent("com.packruler.MusicalDaydream.SETTINGS_RECEIVER");
                i.putExtra("command", "updateFonts");
                DaydreamSettings.context.sendBroadcast(i);

                preferenceScreen.getDialog().setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        Intent i = new Intent("com.packruler.MusicalDaydream.SETTINGS_RECEIVER");
                        i.putExtra("command", "stopUpdateFonts");
                        DaydreamSettings.context.sendBroadcast(i);
                    }
                });
            }
        }
    }

    private class InitializeVariables implements Runnable {
        public void run() {
            scrollArt = (CheckBoxPreference) findPreference(getString(R.string.scroll_art));
            fitArt = (CheckBoxPreference) findPreference(getString(R.string.fit_art));
            swipeInversion = (CheckBoxPreference) findPreference(getString(R.string.swipe_inversion));
            dimScreen = (CheckBoxPreference) findPreference(getString(R.string.dim_screen));
            albumTitle = (CheckBoxPreference) findPreference(getString(R.string.display_album_title));
            immersiveMode = (CheckBoxPreference) findPreference(getString(R.string.immersive_mode));
            fullscreenArt = (CheckBoxPreference) findPreference(getString(R.string.fullscreen_art));
            stableControls = (CheckBoxPreference) findPreference(getString(R.string.stable_bottom));
            displaySongTimes = (CheckBoxPreference) findPreference(getString(R.string.display_song_times));
            songTimeLeft = (CheckBoxPreference) findPreference(getString(R.string.song_time_left));
            limitMarquee = (CheckBoxPreference) findPreference(getString(R.string.limit_marquee));
            displayBattery = (CheckBoxPreference) findPreference(getString(R.string.display_battery));
            hideBatteryFull = (CheckBoxPreference) findPreference(getString(R.string.hide_battery_full));

            scrollDuration = (NumberPickerDialogPreference) findPreference(getString(R.string.art_scroll_duration));

            fullThemeCustom = (ColorPickerPreference) findPreference(getString(R.string.full_theme_custom));
            metadataThemeCustom = (ColorPickerPreference) findPreference(getString(R.string.metadata_color_custom));
            batteryThemeCustom = (ColorPickerPreference) findPreference(getString(R.string.battery_color_custom));
            clockThemeCustom = (ColorPickerPreference) findPreference(getString(R.string.clock_color_custom));
            controlThemeCustom = (ColorPickerPreference) findPreference(getString(R.string.control_color_custom));
            progressBarThemeCustom = (ColorPickerPreference) findPreference(getString(R.string.progress_color_custom));

            fullThemeDefault = (CheckBoxPreference) findPreference(getString(R.string.full_theme_framework));
            metadataThemeDefault = (CheckBoxPreference) findPreference(getString(R.string.metadata_color_framework));
            batteryThemeDefault = (CheckBoxPreference) findPreference(getString(R.string.battery_color_framework));
            clockThemeDefault = (CheckBoxPreference) findPreference(getString(R.string.clock_color_framework));
            controlThemeDefault = (CheckBoxPreference) findPreference(getString(R.string.control_color_framework));
            progressBarThemeDefault = (CheckBoxPreference) findPreference(getString(R.string.progress_color_framework));
            backgroundColorBlack = (CheckBoxPreference) findPreference(getString(R.string.background_black));

            fullThemePalette = (CheckBoxPreference) findPreference(getString(R.string.full_theme_palette));
            metadataThemePalette = (CheckBoxPreference) findPreference(getString(R.string.metadata_color_palette));
            batteryThemePalette = (CheckBoxPreference) findPreference(getString(R.string.battery_color_palette));
            clockThemePalette = (CheckBoxPreference) findPreference(getString(R.string.clock_color_palette));
            controlThemePalette = (CheckBoxPreference) findPreference(getString(R.string.control_color_palette));
            progressBarThemePalette = (CheckBoxPreference) findPreference(getString(R.string.progress_color_palette));
            backgroundColorPalette = (CheckBoxPreference) findPreference(getString(R.string.background_palette));


            notificationIconSize = (NumberPickerDialogPreference) findPreference(getString(R.string.notification_icon_size));
            clockTextSize = (NumberPickerDialogPreference) findPreference(getString(R.string.clock_text_size));
            artistTextSize = (NumberPickerDialogPreference) findPreference(getString(R.string.artist_name_text_size));
            trackTextSize = (NumberPickerDialogPreference) findPreference(getString(R.string.track_title_text_size));
            albumTextSize = (NumberPickerDialogPreference) findPreference(getString(R.string.album_name_text_size));

            screenTimeout = (ListPreference) findPreference(getString(R.string.screen_timeout_pause));

            sortOrder = (ListPreference) findPreference(getString(R.string.sort_order));
            sansSerifFont = (FontPreference) findPreference(getString(R.string.sans_serif));
            serifFont = (FontPreference) findPreference(getString(R.string.serif));
            displayFont = (FontPreference) findPreference(getString(R.string.font_display));
            handwritingFont = (FontPreference) findPreference(getString(R.string.handwriting));
            monospaceFont = (FontPreference) findPreference(getString(R.string.monospace));
            defaultFont = (CheckBoxPreference) findPreference(getString(R.string.framework_font));

            deleteMultFonts = (MultiSelectListPreference) findPreference(getString(R.string.delete_fonts));
            deleteMultFonts.setEnabled(false);
            deleteAllFonts = (ListPreference) findPreference(getString(R.string.delete_all));

            downloadAllSansSerifPref = (ListPreference) findPreference(getString(R.string.download_sans_serif));
            downloadAllSerifPref = (ListPreference) findPreference(getString(R.string.download_serif));
            downloadAllDisplayPref = (ListPreference) findPreference(getString(R.string.download_display));
            downloadAllHandwritingPref = (ListPreference) findPreference(getString(R.string.download_handwriting));
            downloadAllMonospacePref = (ListPreference) findPreference(getString(R.string.download_monospace));
            downloadAllPref = (ListPreference) findPreference(getString(R.string.download_all));

            fontPrefScreen = (PreferenceScreen) findPreference(getString(R.string.font_selection));
            doubleTapAction = (ListPreference) findPreference(getString(R.string.double_tap_options));

            displayNotifications = (CheckBoxPreference) findPreference(getString(R.string.display_notifications));
            maintainPosition = (CheckBoxPreference) findPreference(getString(R.string.maintain_position));
            fullProgressBar = (CheckBoxPreference) findPreference(getString(R.string.full_progress_bar));
            hideStartDreaming = (CheckBoxPreference) findPreference(getString(R.string.hide_start_dreaming));

            updateToggles();

            notificationIconSize.setMinValue(0);
            notificationIconSize.setMaxValue(100);

            clockTextSize.setMinValue(0);
            clockTextSize.setMaxValue(100);

            artistTextSize.setMinValue(0);
            artistTextSize.setMaxValue(100);

            trackTextSize.setMinValue(0);
            trackTextSize.setMaxValue(100);

            albumTextSize.setMinValue(0);
            albumTextSize.setMaxValue(100);

            getDownloadedFontList = new GetDownloadedFontList();
            getDownloadedFontList.start();

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                findPreference(getString(R.string.progress_color)).setEnabled(false);
                findPreference(getString(R.string.progress_color)).setSummary("Requires Lollipop (Android 5.0+)");
            }
        }
    }

    private class SetAlphaPreferences implements Runnable {
        @Override
        public void run() {
            fadeAll = (CheckBoxPreference) findPreference(getString(R.string.fade_all));
            fadeClock = (CheckBoxPreference) findPreference(getString(R.string.fade_clock));
            fadeMetadata = (CheckBoxPreference) findPreference(getString(R.string.fade_metadata));
            fadeNotification = (CheckBoxPreference) findPreference(getString(R.string.fade_notification_icons));
            fadeMusicControls = (CheckBoxPreference) findPreference(getString(R.string.fade_music_controls));

            allAlphaPlaying = (NumberPickerDialogPreference) findPreference(getString(R.string.all_alpha_playing));
            allAlphaPaused = (NumberPickerDialogPreference) findPreference(getString(R.string.all_alpha_paused));
            clockAlphaPlaying = (NumberPickerDialogPreference) findPreference(getString(R.string.clock_alpha_playing));
            clockAlphaPaused = (NumberPickerDialogPreference) findPreference(getString(R.string.clock_alpha_paused));
            metadataAlphaPlaying = (NumberPickerDialogPreference) findPreference(getString(R.string.metadata_alpha_playing));
            metadataAlphaPaused = (NumberPickerDialogPreference) findPreference(getString(R.string.metadata_alpha_paused));
            notificationIconAlphaPlaying = (NumberPickerDialogPreference) findPreference(getString(R.string.notification_alpha_playing));
            notificationIconAlphaPaused = (NumberPickerDialogPreference) findPreference(getString(R.string.notification_alpha_paused));
            controlAlphaPlaying = (NumberPickerDialogPreference) findPreference(getString(R.string.control_alpha_playing));
            controlAlphaPaused = (NumberPickerDialogPreference) findPreference(getString(R.string.control_alpha_paused));

            fadeClock.setChecked(sharedPreferences.getBoolean(getString(R.string.fade_clock), true));
            fadeMetadata.setChecked(sharedPreferences.getBoolean(getString(R.string.fade_metadata), true));
            fadeNotification.setChecked(sharedPreferences.getBoolean(getString(R.string.fade_notification_icons), true));
            fadeMusicControls.setChecked(sharedPreferences.getBoolean(getString(R.string.fade_music_controls), true));

            allAlphaPlaying.setMaxValue(100);
            allAlphaPlaying.setMinValue(0);
            allAlphaPlaying.setValue(sharedPreferences.getInt(getString(R.string.all_alpha_playing), 100));
            allAlphaPaused.setMaxValue(100);
            allAlphaPaused.setMinValue(0);
            allAlphaPaused.setValue(sharedPreferences.getInt(getString(R.string.all_alpha_paused), 50));

            clockAlphaPlaying.setMaxValue(100);
            clockAlphaPlaying.setMinValue(0);
            clockAlphaPlaying.setValue(sharedPreferences.getInt(getString(R.string.clock_alpha_playing), 100));
            clockAlphaPaused.setMaxValue(100);
            clockAlphaPaused.setMinValue(0);
            clockAlphaPaused.setValue(sharedPreferences.getInt(getString(R.string.clock_alpha_paused), 50));

            metadataAlphaPlaying.setMaxValue(100);
            metadataAlphaPlaying.setMinValue(0);
            metadataAlphaPlaying.setValue(sharedPreferences.getInt(getString(R.string.metadata_alpha_playing), 100));
            metadataAlphaPaused.setMaxValue(100);
            metadataAlphaPaused.setMinValue(0);
            metadataAlphaPaused.setValue(sharedPreferences.getInt(getString(R.string.metadata_alpha_paused), 50));

            notificationIconAlphaPlaying.setMaxValue(100);
            notificationIconAlphaPlaying.setMinValue(0);
            notificationIconAlphaPlaying.setValue(sharedPreferences.getInt(getString(R.string.notification_alpha_playing), 100));
            notificationIconAlphaPaused.setMaxValue(100);
            notificationIconAlphaPaused.setMinValue(0);
            notificationIconAlphaPaused.setValue(sharedPreferences.getInt(getString(R.string.notification_alpha_paused), 50));

            controlAlphaPlaying.setMaxValue(100);
            controlAlphaPlaying.setMinValue(0);
            controlAlphaPlaying.setValue(sharedPreferences.getInt(getString(R.string.control_alpha_playing), 100));
            controlAlphaPaused.setMaxValue(100);
            controlAlphaPaused.setMinValue(0);
            controlAlphaPaused.setValue(sharedPreferences.getInt(getString(R.string.control_alpha_paused), 50));

            if (sharedPreferences.getBoolean(getString(R.string.fade_clock), true) &&
                    sharedPreferences.getBoolean(getString(R.string.fade_metadata), true) &&
                    sharedPreferences.getBoolean(getString(R.string.fade_notification_icons), true) &&
                    sharedPreferences.getBoolean(getString(R.string.fade_music_controls), true)) {
                fadeAll.setChecked(true);
            }

            if (sharedPreferences.getInt(getString(R.string.clock_alpha_playing), 100) == sharedPreferences.getInt(getString(R.string.metadata_alpha_playing), 100) &&
                    sharedPreferences.getInt(getString(R.string.clock_alpha_playing), 100) == sharedPreferences.getInt(getString(R.string.notification_alpha_playing), 100) &&
                    sharedPreferences.getInt(getString(R.string.clock_alpha_playing), 100) == sharedPreferences.getInt(getString(R.string.control_alpha_playing), 100)) {
                allAlphaPlaying.setValue(sharedPreferences.getInt(getString(R.string.clock_alpha_playing), 100));
            }

            if (sharedPreferences.getInt(getString(R.string.clock_alpha_paused), 50) == sharedPreferences.getInt(getString(R.string.metadata_alpha_paused), 50) &&
                    sharedPreferences.getInt(getString(R.string.clock_alpha_paused), 50) == sharedPreferences.getInt(getString(R.string.notification_alpha_paused), 50) &&
                    sharedPreferences.getInt(getString(R.string.clock_alpha_paused), 50) == sharedPreferences.getInt(getString(R.string.control_alpha_paused), 50)) {
                allAlphaPaused.setValue(sharedPreferences.getInt(getString(R.string.clock_alpha_paused), 50));
            }

        }
    }

    private class GetFonts extends Thread {

        @Override
        public void run() {
            netHttpTransport = new NetHttpTransport();
            jsonFactory = new AndroidJsonFactory();
            requestInitializer = new WebfontsRequestInitializer("AIzaSyBo0n27MsFhM-DoXcYcPHnVyW-qZgfWKFk");
            builder = new Webfonts.Builder(netHttpTransport, jsonFactory, null);
            builder.setApplicationName(DaydreamSettings.context.getPackageName());
            builder.setWebfontsRequestInitializer(requestInitializer);
            webfonts = builder.build();
            operations = webfonts.webfonts();
            boolean run = false;
            try {
                if (sharedPreferences.getString(getString(R.string.sort_order), "Alphabetical").equalsIgnoreCase("Alphabetical") && !lastSort.equals("alpha")) {
                    lastSort = "alpha";
                    run = true;
                } else if (sharedPreferences.getString(getString(R.string.sort_order), "Alphabetical").equalsIgnoreCase("Most Recent") && !lastSort.equals("date")) {
                    lastSort = "date";
                    run = true;
                } else if (sharedPreferences.getString(getString(R.string.sort_order), "Alphabetical").equalsIgnoreCase("Popularity") && !lastSort.equals("popularity")) {
                    lastSort = "popularity";
                    run = true;
                } else if (sharedPreferences.getString(getString(R.string.sort_order), "Alphabetical").equalsIgnoreCase("Trending") && !lastSort.equals("trending")) {
                    lastSort = "trending";
                    run = true;
                }
//                Log.i(TAG, "run: " + run + "fontList " + (fontList == null));
                if (run || fontList == null) {
                    sendToast("Updating Available Fonts");
                    list = operations.list();
                    list.setSort(lastSort);
                    fontList = list.execute();
                    webfontList = fontList.getItems();
                }
//                Log.i(TAG, fontList.getItems().toString());
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (run) {
                sansSerif.clear();
                serif.clear();
                display.clear();
                handwriting.clear();
                monospace.clear();

                for (Webfont current : webfontList) {
                    if (current.getCategory().equalsIgnoreCase("sans-serif") && current.getVariants().contains("regular") && !sansSerif.contains(current)) {
                        sansSerif.add(current);
                    } else if (current.getCategory().equalsIgnoreCase("serif") && current.getVariants().contains("regular") && !serif.contains(current)) {
                        serif.add(current);
                    } else if (current.getCategory().equalsIgnoreCase("display") && current.getVariants().contains("regular") && !display.contains(current)) {
                        display.add(current);
                    } else if (current.getCategory().equalsIgnoreCase("handwriting") && current.getVariants().contains("regular") && !handwriting.contains(current)) {
                        handwriting.add(current);
                    } else if (current.getCategory().equalsIgnoreCase("monospace") && current.getVariants().contains("regular") && !monospace.contains(current)) {
                        monospace.add(current);
                    }
                }
                Log.i(TAG, "Sans-Serif: " + sansSerif.size());
                Log.i(TAG, "Serif: " + serif.size());
                Log.i(TAG, "Display: " + display.size());
                Log.i(TAG, "Handwriting: " + handwriting.size());
                Log.i(TAG, "Monospace: " + monospace.size());

                if (setSansSerifArray != null) {
                    if (setSansSerifArray.isAlive()) {
                        setSansSerifArray.interrupt();
                    }
                    setSansSerifArray = new SetSansSerifArray();
                    setSansSerifArray.start();
                } else {
                    setSansSerifArray = new SetSansSerifArray();
                    setSansSerifArray.start();
                }
                if (setSerifArray != null) {
                    if (setSerifArray.isAlive()) {
                        setSerifArray.interrupt();
                    }
                    setSerifArray = new SetSerifArray();
                    setSerifArray.start();
                } else {
                    setSerifArray = new SetSerifArray();
                    setSerifArray.start();
                }
                if (setDisplayArray != null) {
                    if (setDisplayArray.isAlive()) {
                        setDisplayArray.interrupt();
                    }
                    setDisplayArray = new SetDisplayArray();
                    setDisplayArray.start();
                } else {
                    setDisplayArray = new SetDisplayArray();
                    setDisplayArray.start();
                }
                if (setHandwritingArray != null) {
                    if (setHandwritingArray.isAlive()) {
                        setHandwritingArray.interrupt();
                    }
                    setHandwritingArray = new SetHandwritingArray();
                    setHandwritingArray.start();
                } else {
                    setHandwritingArray = new SetHandwritingArray();
                    setHandwritingArray.start();
                }
                if (setDisplayArray != null) {
                    if (setDisplayArray.isAlive()) {
                        setDisplayArray.interrupt();
                    }
                    setDisplayArray = new SetDisplayArray();
                    setDisplayArray.start();
                } else {
                    setDisplayArray = new SetDisplayArray();
                    setDisplayArray.start();
                }
                if (setMonospaceArray != null) {
                    if (setMonospaceArray.isAlive()) {
                        setMonospaceArray.interrupt();
                    }
                    setMonospaceArray = new SetMonospaceArray();
                    setMonospaceArray.start();
                } else {
                    setMonospaceArray = new SetMonospaceArray();
                    setMonospaceArray.start();
                }
            }
        }
    }

    private class SetSansSerifArray extends Thread {
        private CharSequence[] entryValues;
        private CharSequence[] entries;

        public SetSansSerifArray() {
            entryValues = new CharSequence[sansSerif.size()];
            entries = new CharSequence[sansSerif.size()];
        }

        @Override
        public void run() {
            for (int x = 0; x < sansSerif.size(); x++) {
                entryValues[x] = sansSerif.get(x).getFamily();
                entries[x] = sansSerif.get(x).getFamily();
            }
            sansSerifFont.setEntries(entries);
            sansSerifFont.setEntryValues(entryValues);
            handlerUI.post(new Runnable() {
                @Override
                public void run() {
                    sansSerifFont.setEnabled(true);
                }
            });
        }
    }

    private class SetSerifArray extends Thread {
        private CharSequence[] entryValues;
        private CharSequence[] entries;

        public SetSerifArray() {
            entryValues = new CharSequence[serif.size()];
            entries = new CharSequence[serif.size()];
        }

        @Override
        public void run() {
            for (int x = 0; x < serif.size(); x++) {
                entryValues[x] = serif.get(x).getFamily();
                entries[x] = serif.get(x).getFamily();
            }
            serifFont.setEntries(entries);
            serifFont.setEntryValues(entryValues);
            handlerUI.post(new Runnable() {
                @Override
                public void run() {
                    serifFont.setEnabled(true);
                }
            });
        }
    }

    private class SetDisplayArray extends Thread {
        private CharSequence[] entryValues;
        private CharSequence[] entries;

        public SetDisplayArray() {
            entryValues = new CharSequence[display.size()];
            entries = new CharSequence[display.size()];
        }

        @Override
        public void run() {
            for (int x = 0; x < display.size(); x++) {
                entryValues[x] = display.get(x).getFamily();
                entries[x] = display.get(x).getFamily();
            }
            displayFont.setEntries(entries);
            displayFont.setEntryValues(entryValues);
            handlerUI.post(new Runnable() {
                @Override
                public void run() {
                    displayFont.setEnabled(true);
                }
            });
        }
    }

    private class SetHandwritingArray extends Thread {
        private CharSequence[] entryValues;
        private CharSequence[] entries;

        public SetHandwritingArray() {
            entryValues = new CharSequence[handwriting.size()];
            entries = new CharSequence[handwriting.size()];
        }

        @Override
        public void run() {
            for (int x = 0; x < handwriting.size(); x++) {
                entryValues[x] = handwriting.get(x).getFamily();
                entries[x] = handwriting.get(x).getFamily();
            }
            handwritingFont.setEntries(entries);
            handwritingFont.setEntryValues(entryValues);
            handlerUI.post(new Runnable() {
                @Override
                public void run() {
                    handwritingFont.setEnabled(true);
                }
            });
        }
    }

    private class SetMonospaceArray extends Thread {
        private CharSequence[] entryValues;
        private CharSequence[] entries;

        public SetMonospaceArray() {
            entryValues = new CharSequence[monospace.size()];
            entries = new CharSequence[monospace.size()];
        }

        @Override
        public void run() {
            for (int x = 0; x < monospace.size(); x++) {
                entryValues[x] = monospace.get(x).getFamily();
                entries[x] = monospace.get(x).getFamily();
            }
            monospaceFont.setEntries(entries);
            monospaceFont.setEntryValues(entryValues);
            handlerUI.post(new Runnable() {
                @Override
                public void run() {
                    monospaceFont.setEnabled(true);
                }
            });
        }
    }

    private class DownloadFont extends Thread {
        private String fontName;

        public DownloadFont(String name) {
            fontName = name;
            Log.i(TAG, "Set fontName: " + fontName);
        }

        @Override
        public void run() {
            if (isFontDownloaded(fontName)) {
                Log.i(TAG, "Font already downloaded.");
            } else {
                Looper.prepare();
                Log.i(TAG, "Download needed");
                sendToast("Downloading " + fontName);

                handlerUI.post(new Runnable() {
                    @Override
                    public void run() {
                        sansSerifFont.setEnabled(false);
                        serifFont.setEnabled(false);
                        displayFont.setEnabled(false);
                        handwritingFont.setEnabled(false);
                        monospaceFont.setEnabled(false);
                    }
                });

                String url;
                Webfont font = null;
                for (int x = 0; x < webfontList.size(); x++) {
                    if (webfontList.get(x).getFamily().equals(fontName)) {
                        font = webfontList.get(x);
                        Log.i(TAG, "Found Font");
                        break;
                    }
                }
                if (font != null) {
                    Map<String, String> fontFiles = font.getFiles();
                    url = fontFiles.get("regular");
                    Log.i(TAG, "URL: " + url);
                    try {
                        URL u = new URL(url);
                        InputStream is = u.openStream();

                        DataInputStream dis = new DataInputStream(is);

                        byte[] buffer = new byte[4096];
                        int length;

                        FileOutputStream fos = DaydreamSettings.context.openFileOutput(fontName + ".ttf", Context.MODE_PRIVATE);
                        while ((length = dis.read(buffer)) > 0) {
                            fos.write(buffer, 0, length);
                        }
                        fos.close();
                        Log.i(TAG, "DONE");
                    } catch (MalformedURLException mue) {
                        Log.e("SYNC getUpdate", "malformed url error", mue);
                        sendToast("Malformed URL Error");
                    } catch (IOException ioe) {
                        Log.e("SYNC getUpdate", "io error", ioe);
                        sendToast("IO Error");
                    } catch (SecurityException se) {
                        Log.e("SYNC getUpdate", "security error", se);
                        sendToast("Security error");
                    }
                }
                handlerUI.post(new Runnable() {
                    @Override
                    public void run() {
                        sansSerifFont.setEnabled(true);
                        serifFont.setEnabled(true);
                        displayFont.setEnabled(true);
                        handwritingFont.setEnabled(true);
                        monospaceFont.setEnabled(true);
                    }
                });
                sendToast("Done Downloading Font");
            }

            new GetDownloadedFontList().start();
        }
    }

    private boolean isFontDownloaded(String name) {
        fileList = fontDir.listFiles();
        if (fileList.length <= 0) {
            Log.i(TAG, "No Files Here");
            return false;
        }
        for (int x = 0; x < fileList.length; x++) {
            if (fileList[x].getName().equals(name + ".ttf")) {
                return true;
            }
        }
        return false;
    }

    private class GetDownloadedFontList extends Thread {
        String[] downloadedList;

        @Override
        public void run() {
            fileList = fontDir.listFiles();
            downloadedList = new String[fileList.length];
            if (downloadedList.length > 0) {
                for (int x = 0; x < fileList.length; x++) {
                    downloadedList[x] = fileList[x].getName();
                }
                deleteMultFonts.setEntryValues(downloadedList);
                deleteMultFonts.setEntries(downloadedList);
                handlerUI.post(new Runnable() {
                    @Override
                    public void run() {
                        deleteAllFonts.setEnabled(true);
                        deleteMultFonts.setEnabled(true);
                    }
                });
            }
        }
    }

    private void setGetDownloadedFontList() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (getDownloadedFontList != null) {
                    if (getDownloadedFontList.isAlive()) {
                        getDownloadedFontList.interrupt();
                    }
                }
                try {
                    while (downloadAllMonospace.isAlive() ||
                            downloadAllSerif.isAlive() ||
                            downloadAllHandwriting.isAlive() ||
                            downloadAllSansSerif.isAlive() ||
                            downloadAllDisplay.isAlive()) {
                        sendToast("Downloading...");
                        ((Object) this).wait(1000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                getDownloadedFontList = new GetDownloadedFontList();
                getDownloadedFontList.start();
            }
        }).start();
    }

    private class DeleteFonts extends Thread {
        private boolean all = false;
        private Set<String> delete;

        public DeleteFonts(Set<String> in) {
            delete = in;
            all = false;
        }

        public DeleteFonts() {
            all = true;
        }

        @Override
        public void run() {
            Looper.prepare();
            if (all) {
                handlerUI.post(new Runnable() {
                    @Override
                    public void run() {
                        deleteAllFonts.setEnabled(false);
                        deleteMultFonts.setEnabled(false);
                        defaultFont.setChecked(true);
                    }
                });
            }

            fileList = fontDir.listFiles();
            if (all) {
                for (int x = 0; x < fileList.length; x++) {
                    Log.i(TAG, fileList[x].getName() + " " + fileList[x].delete());
                }
            } else {
                DaydreamSettings.handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (delete.contains(sansSerifFont.getValue() + ".ttf")) {
                            sansSerifFont.setValue(null);
                            defaultFont.setChecked(true);
                        }
                        if (delete.contains(serifFont.getValue() + ".ttf")) {
                            serifFont.setValue(null);
                            defaultFont.setChecked(true);
                        }
                        if (delete.contains(displayFont.getValue() + ".ttf")) {
                            displayFont.setValue(null);
                            defaultFont.setChecked(true);
                        }
                        if (delete.contains(handwritingFont.getValue() + ".ttf")) {
                            handwritingFont.setValue(null);
                            defaultFont.setChecked(true);
                        }
                        if (delete.contains(monospaceFont.getValue() + ".ttf")) {
                            monospaceFont.setValue(null);
                            defaultFont.setChecked(true);
                        }
                    }
                });
                for (int x = 0; x < fileList.length; x++) {
                    if (delete.contains(fileList[x].getName())) {
                        Log.i(TAG, fileList[x].getName() + " " + fileList[x].delete());
                    }
                }
            }
            sendToast("Done Deleting Fonts");

            deleteMultFonts.setEntries(null);
            if (delete != null) {
                delete.removeAll(delete);
                deleteMultFonts.setValues(delete);
            }
            new GetDownloadedFontList().start();
        }
    }

    private File getFontFile(String name) {
        name += ".ttf";
        File[] files = fontDir.listFiles();
        for (int x = 0; x < files.length; x++) {
            if (files[x].getName().equals(name)) {
                return files[x];
            }
        }
        return null;
    }

    protected void updateFonts() {
        if (getFonts != null) {
            if (getFonts.isAlive()) {
                getFonts.interrupt();
            }
        }
        getFonts = new GetFonts();
        getFonts.start();
    }

    private class DownloadAllSansSerif extends Thread {

        @Override
        public void run() {
            DaydreamSettings.handler.post(new Runnable() {
                @Override
                public void run() {
                    downloadAllSansSerifPref.setEnabled(false);
                }
            });
            sendToast("Downloading All Sans Serif Fonts");
            for (int x = 0; x < sansSerif.size(); x++) {
                Log.i(TAG, "Searching for " + sansSerif.get(x).getFamily());
                boolean fontFound = false;
                for (int i = 0; i < fileList.length; i++) {
                    if (fileList[i].getName().equals(sansSerif.get(x).getFamily() + ".ttf")) {
                        fontFound = true;
                        Log.i(TAG, sansSerif.get(x).getFamily() + "Already Downloaded");
                        break;
                    }
                }
                if (!fontFound) {
                    String url;
                    Webfont font = null;
                    for (int i = 0; i < webfontList.size(); i++) {
                        if (webfontList.get(i).getFamily().equals(sansSerif.get(x).getFamily())) {
                            font = webfontList.get(i);
                            break;
                        }
                    }
                    if (font != null) {
                        Map<String, String> fontFiles = font.getFiles();
                        url = fontFiles.get("regular");
                        Log.i(TAG, "URL: " + url);
                        try {
                            URL u = new URL(url);
                            InputStream is = u.openStream();

                            DataInputStream dis = new DataInputStream(is);

                            byte[] buffer = new byte[4096];
                            int length;

                            FileOutputStream fos = DaydreamSettings.context.openFileOutput(sansSerif.get(x).getFamily() + ".ttf", Context.MODE_PRIVATE);
                            while ((length = dis.read(buffer)) > 0) {
                                fos.write(buffer, 0, length);
                            }
                            fos.close();
                            Log.i(TAG, "DONE");
                        } catch (MalformedURLException mue) {
                            Log.e("SYNC getUpdate", "malformed url error", mue);
                            sendToast("Malformed URL Error");
                        } catch (IOException ioe) {
                            Log.e("SYNC getUpdate", "io error", ioe);
                            sendToast("IO Error");
                        } catch (SecurityException se) {
                            Log.e("SYNC getUpdate", "security error", se);
                            sendToast("Security error");
                        }
                    }
                    Log.i(TAG, "Done downloading " + font.getFamily());
                }
            }
            setGetDownloadedFontList();
            Log.i(TAG, "Done Downloading All");
            sendToast("All Sans Serif Fonts Downloaded");
            DaydreamSettings.handler.post(new Runnable() {
                @Override
                public void run() {
                    downloadAllSansSerifPref.setEnabled(true);
                }
            });
        }
    }

    private class DownloadAllSerif extends Thread {

        @Override
        public void run() {
            DaydreamSettings.handler.post(new Runnable() {
                @Override
                public void run() {
                    downloadAllSerifPref.setEnabled(false);
                }
            });
            sendToast("Downloading All Serif Fonts");
            for (int x = 0; x < serif.size(); x++) {
                Log.i(TAG, "Searching for " + serif.get(x).getFamily());
                boolean fontFound = false;
                for (int i = 0; i < fileList.length; i++) {
                    if (fileList[i].getName().equals(serif.get(x).getFamily() + ".ttf")) {
                        fontFound = true;
                        Log.i(TAG, sansSerif.get(x).getFamily() + "Already Downloaded");
                        break;
                    }
                }
                if (!fontFound) {
                    String url;
                    Webfont font = null;
                    for (int i = 0; i < webfontList.size(); i++) {
                        if (webfontList.get(i).getFamily().equals(serif.get(x).getFamily())) {
                            font = webfontList.get(i);
                            break;
                        }
                    }
                    if (font != null) {
                        Map<String, String> fontFiles = font.getFiles();
                        url = fontFiles.get("regular");
                        Log.i(TAG, "URL: " + url);
                        try {
                            URL u = new URL(url);
                            InputStream is = u.openStream();

                            DataInputStream dis = new DataInputStream(is);

                            byte[] buffer = new byte[4096];
                            int length;

                            FileOutputStream fos = DaydreamSettings.context.openFileOutput(serif.get(x).getFamily() + ".ttf", Context.MODE_PRIVATE);
                            while ((length = dis.read(buffer)) > 0) {
                                fos.write(buffer, 0, length);
                            }
                            fos.close();
                            Log.i(TAG, "DONE");
                        } catch (MalformedURLException mue) {
                            Log.e("SYNC getUpdate", "malformed url error", mue);
                            sendToast("Malformed URL Error");
                        } catch (IOException ioe) {
                            Log.e("SYNC getUpdate", "io error", ioe);
                            sendToast("IO Error");
                        } catch (SecurityException se) {
                            Log.e("SYNC getUpdate", "security error", se);
                            sendToast("Security error");
                        }
                    }
                    Log.i(TAG, "Done downloading " + font.getFamily());
                }
            }
            setGetDownloadedFontList();
            Log.i(TAG, "Done Downloading All");
            sendToast("All Serif Fonts Downloaded");
            DaydreamSettings.handler.post(new Runnable() {
                @Override
                public void run() {
                    downloadAllSerifPref.setEnabled(true);
                }
            });
        }
    }

    private class DownloadAllDisplay extends Thread {

        @Override
        public void run() {
            DaydreamSettings.handler.post(new Runnable() {
                @Override
                public void run() {
                    downloadAllDisplayPref.setEnabled(false);
                }
            });
            sendToast("Downloading All Display Fonts");
            for (int x = 0; x < display.size(); x++) {
                Log.i(TAG, "Searching for " + display.get(x).getFamily());
                boolean fontFound = false;
                for (int i = 0; i < fileList.length; i++) {
                    if (fileList[i].getName().equals(display.get(x).getFamily() + ".ttf")) {
                        fontFound = true;
                        Log.i(TAG, display.get(x).getFamily() + "Already Downloaded");
                        break;
                    }
                }
                if (!fontFound) {
                    String url;
                    Webfont font = null;
                    for (int i = 0; i < webfontList.size(); i++) {
                        if (webfontList.get(i).getFamily().equals(display.get(x).getFamily())) {
                            font = webfontList.get(i);
                            break;
                        }
                    }
                    if (font != null) {
                        Map<String, String> fontFiles = font.getFiles();
                        url = fontFiles.get("regular");
                        Log.i(TAG, "URL: " + url);
                        try {
                            URL u = new URL(url);
                            InputStream is = u.openStream();

                            DataInputStream dis = new DataInputStream(is);

                            byte[] buffer = new byte[4096];
                            int length;

                            FileOutputStream fos = DaydreamSettings.context.openFileOutput(display.get(x).getFamily() + ".ttf", Context.MODE_PRIVATE);
                            while ((length = dis.read(buffer)) > 0) {
                                fos.write(buffer, 0, length);
                            }
                            fos.close();
                            Log.i(TAG, "DONE");
                        } catch (MalformedURLException mue) {
                            Log.e("SYNC getUpdate", "malformed url error", mue);
                            sendToast("Malformed URL Error");
                        } catch (IOException ioe) {
                            Log.e("SYNC getUpdate", "io error", ioe);
                            sendToast("IO Error");
                        } catch (SecurityException se) {
                            Log.e("SYNC getUpdate", "security error", se);
                            sendToast("Security error");
                        }
                    }
                    Log.i(TAG, "Done downloading " + font.getFamily());
                }
            }
            setGetDownloadedFontList();
            Log.i(TAG, "Done Downloading All");
            sendToast("All Display Fonts Downloaded");
            DaydreamSettings.handler.post(new Runnable() {
                @Override
                public void run() {
                    downloadAllDisplayPref.setEnabled(true);
                }
            });
        }
    }

    private class DownloadAllHandwriting extends Thread {
        @Override
        public void run() {
            DaydreamSettings.handler.post(new Runnable() {
                @Override
                public void run() {
                    downloadAllHandwritingPref.setEnabled(false);
                }
            });
            sendToast("Downloading All Handwriting Fonts");
            for (int x = 0; x < handwriting.size(); x++) {
                Log.i(TAG, "Searching for " + handwriting.get(x).getFamily());
                boolean fontFound = false;
                for (int i = 0; i < fileList.length; i++) {
                    if (fileList[i].getName().equals(handwriting.get(x).getFamily() + ".ttf")) {
                        fontFound = true;
                        Log.i(TAG, handwriting.get(x).getFamily() + "Already Downloaded");
                        break;
                    }
                }
                if (!fontFound) {
                    String url;
                    Webfont font = null;
                    for (int i = 0; i < webfontList.size(); i++) {
                        if (webfontList.get(i).getFamily().equals(handwriting.get(x).getFamily())) {
                            font = webfontList.get(i);
                            break;
                        }
                    }
                    if (font != null) {
                        Map<String, String> fontFiles = font.getFiles();
                        url = fontFiles.get("regular");
                        Log.i(TAG, "URL: " + url);
                        try {
                            URL u = new URL(url);
                            InputStream is = u.openStream();

                            DataInputStream dis = new DataInputStream(is);

                            byte[] buffer = new byte[4096];
                            int length;

                            FileOutputStream fos = DaydreamSettings.context.openFileOutput(handwriting.get(x).getFamily() + ".ttf", Context.MODE_PRIVATE);
                            while ((length = dis.read(buffer)) > 0) {
                                fos.write(buffer, 0, length);
                            }
                            fos.close();
                            Log.i(TAG, "DONE");
                        } catch (MalformedURLException mue) {
                            Log.e("SYNC getUpdate", "malformed url error", mue);
                            sendToast("Malformed URL Error");
                        } catch (IOException ioe) {
                            Log.e("SYNC getUpdate", "io error", ioe);
                            sendToast("IO Error");
                        } catch (SecurityException se) {
                            Log.e("SYNC getUpdate", "security error", se);
                            sendToast("Security error");
                        }
                    }
                    Log.i(TAG, "Done downloading " + font.getFamily());
                }
            }
            setGetDownloadedFontList();
            Log.i(TAG, "Done Downloading All");
            sendToast("All Handwriting Fonts Downloaded");
            DaydreamSettings.handler.post(new Runnable() {
                @Override
                public void run() {
                    downloadAllHandwritingPref.setEnabled(true);
                }
            });
        }
    }

    private class DownloadAllMonospace extends Thread {
        @Override
        public void run() {
            DaydreamSettings.handler.post(new Runnable() {
                @Override
                public void run() {
                    downloadAllMonospacePref.setEnabled(false);
                }
            });
            sendToast("Downloading All Monospace Fonts");
            for (int x = 0; x < monospace.size(); x++) {
                Log.i(TAG, "Searching for " + monospace.get(x).getFamily());
                boolean fontFound = false;
                for (int i = 0; i < fileList.length; i++) {
                    if (fileList[i].getName().equals(monospace.get(x).getFamily() + ".ttf")) {
                        fontFound = true;
                        Log.i(TAG, monospace.get(x).getFamily() + "Already Downloaded");
                        break;
                    }
                }
                if (!fontFound) {
                    String url;
                    Webfont font = null;
                    for (int i = 0; i < webfontList.size(); i++) {
                        if (webfontList.get(i).getFamily().equals(monospace.get(x).getFamily())) {
                            font = webfontList.get(i);
                            break;
                        }
                    }
                    if (font != null) {
                        Map<String, String> fontFiles = font.getFiles();
                        url = fontFiles.get("regular");
                        Log.i(TAG, "URL: " + url);
                        try {
                            URL u = new URL(url);
                            InputStream is = u.openStream();

                            DataInputStream dis = new DataInputStream(is);

                            byte[] buffer = new byte[4096];
                            int length;

                            FileOutputStream fos = DaydreamSettings.context.openFileOutput(monospace.get(x).getFamily() + ".ttf", Context.MODE_PRIVATE);
                            while ((length = dis.read(buffer)) > 0) {
                                fos.write(buffer, 0, length);
                            }
                            fos.close();
                            Log.i(TAG, "DONE");
                        } catch (MalformedURLException mue) {
                            Log.e("SYNC getUpdate", "malformed url error", mue);
                            sendToast("Malformed URL Error");
                        } catch (IOException ioe) {
                            Log.e("SYNC getUpdate", "io error", ioe);
                            sendToast("IO Error");
                        } catch (SecurityException se) {
                            Log.e("SYNC getUpdate", "security error", se);
                            sendToast("Security error");
                        }
                    }
                    Log.i(TAG, "Done downloading " + font.getFamily());
                }
            }
            setGetDownloadedFontList();
            Log.i(TAG, "Done Downloading All");
            sendToast("All Monospace Fonts Downloaded");
            DaydreamSettings.handler.post(new Runnable() {
                @Override
                public void run() {
                    downloadAllMonospacePref.setEnabled(true);
                }
            });
        }
    }

    private class DownloadAllFonts extends Thread {

        @Override
        public void run() {
            if (downloadAllSansSerif != null) {
                if (!downloadAllSansSerif.isAlive()) {
                    downloadAllSansSerif = new DownloadAllSansSerif();
                    downloadAllSansSerif.start();
                }
            } else {
                downloadAllSansSerif = new DownloadAllSansSerif();
                downloadAllSansSerif.start();
            }
            if (downloadAllSerif != null) {
                if (!downloadAllSerif.isAlive()) {
                    downloadAllSerif = new DownloadAllSerif();
                    downloadAllSerif.start();
                }
            } else {
                downloadAllSerif = new DownloadAllSerif();
                downloadAllSerif.start();
            }
            if (downloadAllDisplay != null) {
                if (!downloadAllDisplay.isAlive()) {
                    downloadAllDisplay = new DownloadAllDisplay();
                    downloadAllDisplay.start();
                }
            } else {
                downloadAllDisplay = new DownloadAllDisplay();
                downloadAllDisplay.start();
            }
            if (downloadAllHandwriting != null) {
                if (!downloadAllHandwriting.isAlive()) {
                    downloadAllHandwriting = new DownloadAllHandwriting();
                    downloadAllHandwriting.start();
                }
            } else {
                downloadAllHandwriting = new DownloadAllHandwriting();
                downloadAllHandwriting.start();
            }
            if (downloadAllMonospace != null) {
                if (!downloadAllMonospace.isAlive()) {
                    downloadAllMonospace = new DownloadAllMonospace();
                    downloadAllMonospace.start();
                }
            } else {
                downloadAllMonospace = new DownloadAllMonospace();
                downloadAllMonospace.start();
            }
            sendToast("Downloading All Fonts");
            setGetDownloadedFontList();
            Log.i(TAG, "Done Downloading All");
        }

    }

    private void sendToast(String message) {
        final String msg = message;
        DaydreamSettings.handler.post(new Runnable() {
            @Override
            public void run() {
                if (toast != null) {
                    toast.cancel();
                }
                toast = Toast.makeText(DaydreamSettings.context, msg, Toast.LENGTH_LONG);
                toast.show();
            }
        });
    }

    protected void stopUpdateFonts() {
        Log.i("Stop", "Stop Updating Fonts");
        if (setSansSerifArray != null) {
            if (setSansSerifArray.isAlive()) {
                setSansSerifArray.interrupt();
            }
        }
        if (setSerifArray != null) {
            if (setSerifArray.isAlive()) {
                setSerifArray.interrupt();
            }
        }
        if (setDisplayArray != null) {
            if (setDisplayArray.isAlive()) {
                setDisplayArray.interrupt();
            }
        }
        if (setHandwritingArray != null) {
            if (setHandwritingArray.isAlive()) {
                setHandwritingArray.interrupt();
            }
        }
        if (setMonospaceArray != null) {
            if (setMonospaceArray.isAlive()) {
                setMonospaceArray.interrupt();
            }
        }
        if (getFonts != null) {
            if (getFonts.isAlive()) {
                getFonts.interrupt();
            }
        }
    }

    private static android.support.v7.widget.Toolbar.OnMenuItemClickListener onMenuItemClickListener;

    protected void setOnMenuListener(android.support.v7.widget.Toolbar.OnMenuItemClickListener listener) {
        onMenuItemClickListener = listener;
    }

    private Context context;

    protected void setContext(Context context) {
        this.context = context;
    }
}