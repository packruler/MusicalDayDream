package com.packruler.musicaldaydream.release;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;


public class DaydreamSettings extends ActionBarActivity {
    private final String TAG = ((Object) this).getClass().getSimpleName();
    protected static Context context;
    private MainReceiver mainReceiver;
    private SettingsFragment preferenceFragment;
    protected static Handler handler = new Handler(Looper.getMainLooper());
    private IntentFilter filter = new IntentFilter();
    protected static Toolbar toolbar;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daydream_settings);
        context = getApplicationContext();

        mainReceiver = new MainReceiver();
        filter.addAction("com.packruler.MusicalDaydream.SETTINGS_RECEIVER");
        registerReceiver(mainReceiver, filter);

        preferenceFragment = (SettingsFragment) getFragmentManager().findFragmentById(R.id.fragment);
        preferenceFragment.setContext(this);
        Log.i(TAG, "Build " + Build.VERSION.SDK_INT);
        toolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            Log.i(TAG, "Set bar");
        }

        android.support.v7.widget.Toolbar.OnMenuItemClickListener onMenuItemClickListener = new android.support.v7.widget.Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.launch_security_settings:
                        launchSecuritySettings();
                        return true;
                    case R.id.launch_dream_settings:
                        launchDaydreamSettings();
                        return true;
                    case R.id.launch_dream:
                        launchDream();
                        return true;
                    default:
                        return false;
                }
            }
        };
        preferenceFragment.setOnMenuListener(onMenuItemClickListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mainReceiver, filter);
    }

    @Override
    protected void onStop() {
        try {
            unregisterReceiver(mainReceiver);
        }catch (Exception e){
            Log.i(TAG, e.toString());
        }
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the action_bar_menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.action_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.launch_security_settings:
                launchSecuritySettings();
                return true;
            case R.id.launch_dream_settings:
                launchDaydreamSettings();
                return true;
            case R.id.launch_dream:
                launchDream();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void launchDream() {
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName("com.android.systemui", "com.android.systemui.Somnambulator");
        startActivity(intent);
    }

    public void launchSecuritySettings() {
        try {
            startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
        } catch (Exception e){
            e.printStackTrace();
            startActivity(new Intent(Settings.ACTION_SECURITY_SETTINGS));
        }
    }

    public void launchDaydreamSettings() {
        startActivity(new Intent(android.provider.Settings.ACTION_DREAM_SETTINGS));
    }

    private class MainReceiver extends BroadcastReceiver {
        /**
         * Only command that can be received is "shutdown" to end daydream
         *
         * @param context
         * @param intent
         */
        @Override
        public void onReceive(final Context context, Intent intent) {
            if (intent.getStringExtra("command").equals("updateFonts")) {
                preferenceFragment.updateFonts();
            }else if (intent.getStringExtra("command").equals("stopUpdateFonts")) {
                preferenceFragment.stopUpdateFonts();
            }
        }
    }
}
