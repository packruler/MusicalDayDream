package com.packruler.musicaldaydream.release;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.vending.licensing.AESObfuscator;
import com.google.android.vending.licensing.LicenseChecker;
import com.google.android.vending.licensing.LicenseCheckerCallback;
import com.google.android.vending.licensing.Policy;
import com.google.android.vending.licensing.ServerManagedPolicy;

import java.util.Calendar;

public class FirstTimeSetup extends Activity {

    private final String TAG = this.getClass().getSimpleName();

    private final String ENCODED_KEY = "Y]]V]~UZVs\u007Fe|\u007F}S-c$VUEQRUU[WUE,UY]]VWs_WUEQU\u007F!gG&-#}qQZ$fu&$~&aCQNf&YL}\\_;`Qa!L;b y^e@|?m`nNyp^Mr&&rPGcy_Us}\"q]ffWf!\"ysCrf-C`;[PQ Y,gA\"!w@]zmBECuqzu$CSXG-sqwN@zFaLV|dYF&cW#?@\\p|]A'q&V?x_g#XVLl@L;]ggXD;WRG`URZB@;bqRDW?|[ 'Ym;#?e`CZaS]|Az'D&MzXY}P&zX{fL|D;xB@N`bs]\"[%wz^lDm~#,__!DVRDdcR~!q~s\u007F$ f#`G;#'cEW%qu]p[n\\e` gL@cgrX\\rXEqBLVp,lus-Wgr&eFw[ GV\u007Fmz}y@GA%LwRa %p;L;ll|{[DmN\"C^@uDbzpCqnPc]PUEUV";
    protected static boolean isAuthorized = false;
    private Toast authorizationToast;
    protected MyLicenseCheckerCallback mLicenseCheckerCallback;
    protected LicenseChecker mChecker;
    protected final byte[] SALT = new byte[]{
            16, 74, 71, -80, 32,
            101, -47, 72, 117, -14,
            0, -29, 70, 65, -12,
            74, 75, 04, 80, -22};
    private TelephonyManager telephonyManager;
    //private Handler handler;
    protected TextView authorizationStatus;
    protected ProgressBar authorizationBar;
    protected Button authorizationButton;
    protected RelativeLayout authorizationLayout;
    protected String deviceId;
    protected Button playStoreLink;
    protected static Context context;
    protected int result = 2;
    private RelativeLayout step1;
    private RelativeLayout step2;

    private static Intent storeLink = new Intent(Intent.ACTION_VIEW);

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;


    private ContentResolver contentResolver;
    private String enabledNotificationListeners;
    private String packageName;

    private boolean justSecurity = false;
    private boolean loadSecurity = false;

    private boolean launchDaydreamSettings = true;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DaydreamSettings.context = getApplicationContext();
        String DECODED_KEY = stringTransform(ENCODED_KEY, 20);

        sharedPreferences = getSharedPreferences(getString(R.string.settings_string), 0);
        editor = sharedPreferences.edit();

        context = getApplicationContext();
        contentResolver = getContentResolver();
        enabledNotificationListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners");
        packageName = getPackageName();

        setContentView(R.layout.authorization_activity);

        //Log.i("ID1", deviceId);

        authorizationLayout = (RelativeLayout) findViewById(R.id.authorization_progress_layout);
        authorizationStatus = (TextView) findViewById(R.id.authorization_status);
        authorizationBar = (ProgressBar) findViewById(R.id.authorization_bar);
        authorizationButton = (Button) findViewById(R.id.authorization_button);
        playStoreLink = (Button) findViewById(R.id.buy_app_button);
        playStoreLink.setVisibility(View.INVISIBLE);
        authorizationBar.setIndeterminate(true);

//        Log.i(TAG, enabledNotificationListeners);
//        Log.i(TAG, packageName);

        //Check for license status
        if (needLicenseCheck()) {
            deviceId = android.provider.Settings.Secure.getString(getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ANDROID_ID);

            // Construct the LicenseCheckerCallback. The library calls this when done.
            mLicenseCheckerCallback = new MyLicenseCheckerCallback();

            // Construct the LicenseChecker with a Policy.
            mChecker = new LicenseChecker(this, new ServerManagedPolicy(this,
                    new AESObfuscator(SALT, getPackageName(), deviceId))
                    , DECODED_KEY);

            updateStatus();
        } else if (sharedPreferences.getBoolean("first_run", true)) {
            setContentView(R.layout.initial_setup);
            initialSetup();
        } else if (enabledNotificationListeners == null || !enabledNotificationListeners.contains(packageName)) {
            // check to see if the enabledNotificationListeners String contains our package name
            Log.i(TAG, "Security time!");
            justSecurity = true;
            setContentView(R.layout.initial_setup);
            initialSetup();
        } else {
            openSettings();
        }
    }

    /**
     * Launch actual settings app when license and first setup complete
     */
    private void openSettings() {
        Intent i = new Intent(this, DaydreamSettings.class);
        startActivity(i);
        this.finish();
    }

    /**
     * Begin initial setup walk through
     */
    private void initialSetup() {
        step1 = (RelativeLayout) findViewById(R.id.step_one);
        step2 = (RelativeLayout) findViewById(R.id.step_two);
        step1.setVisibility(View.VISIBLE);
        step2.setVisibility(View.GONE);
        if (justSecurity) {
            ((TextView) findViewById(R.id.settings_setup_title)).setText("Security Settings Guide:");
        }
    }

    /**
     * Launch Android Security Settings to enable notification settings
     *
     * @param v
     */
    public void launchSecuritySettings(View v) {
        try {
            startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
        } catch (Exception e) {
            e.printStackTrace();
            startActivity(new Intent(Settings.ACTION_SECURITY_SETTINGS));
        }
        if (!justSecurity) {
            step1.setVisibility(View.GONE);
            step2.setVisibility(View.VISIBLE);
        } else if (!loadSecurity) {
            ((Button) findViewById(R.id.settingsButton)).setText("Click When Done");
            loadSecurity = true;
        } else {
            openSettings();
            this.finish();
        }
    }

    /**
     * Launch Android Daydream Settings
     */
    public void launchDaydreamSettings() {
        startActivity(new Intent(android.provider.Settings.ACTION_DREAM_SETTINGS));
    }

    /**
     * Launch Android Daydream Settings
     */
    public void launchDaydreamSettings(View v) {
        if (launchDaydreamSettings) {
            launchDaydreamSettings();
            editor.putBoolean("first_run", false);
            editor.commit();
            launchDaydreamSettings = false;
            ((Button) findViewById(R.id.DaydreamSettingsButton)).setText("Continue");
            ((TextView) findViewById(R.id.textView3)).setText("Continue to customize your daydream:");
            ((TextView) findViewById(R.id.textView3)).setGravity(Gravity.CENTER);
            ((TextView) findViewById(R.id.textView4)).setText("");
        } else {
            openSettings();
        }
    }

    /**
     * Check current license status to decide if license needs check again
     */
    public boolean needLicenseCheck() {
        Log.i(TAG, "Millis since first success: " + (Calendar.getInstance().getTimeInMillis() - sharedPreferences.getLong("first_success", 0)));
        if (sharedPreferences.getLong("first_success", 0) == 0) {
            return true;
        }
        long timeSinceFirstSuccess = Calendar.getInstance().getTimeInMillis() - sharedPreferences.getLong("first_success", 0);
        if (timeSinceFirstSuccess >= (1000 * 60 * 60)) {
            return false;
        }
        return true;
    }

    /**
     * Link to Play Store page for app
     *
     * @param v
     */
    public void linkToMarket(View v) {
        storeLink.setData(Uri.parse("market://details?id=com.packruler.musicaldaydream.release"));
        startActivity(storeLink);
    }

    /**
     * Run license check
     */
    public void updateStatus() {
        authorizationLayout.setVisibility(View.VISIBLE);
        authorizationStatus.setText("Authorization In Progress...");
        authorizationBar.setVisibility(View.VISIBLE);
        authorizationBar.setIndeterminate(true);

        mChecker.checkAccess(mLicenseCheckerCallback);
    }

    /**
     * Respond to license check status
     */
    private void updateLicenseStatus() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (result == 256) {
                    authorizationBar.setVisibility(View.GONE);
                    playStoreLink.setVisibility(View.GONE);
                    authorizationStatus.setText("Success!!!");

                    setContentView(R.layout.initial_setup);
                    step1 = (RelativeLayout) findViewById(R.id.step_one);
                    step2 = (RelativeLayout) findViewById(R.id.step_two);

                    if (sharedPreferences.getLong("first_success", 0) == 0) {
                        editor.putLong("first_success", Calendar.getInstance().getTimeInMillis());
                        editor.commit();
                    }

                    if (sharedPreferences.getBoolean("first_run", true)) {
                        initialSetup();
                    } else if (enabledNotificationListeners == null || !enabledNotificationListeners.contains(packageName)) {
                        // check to see if the enabledNotificationListeners String contains our package name
                        Log.i(TAG, "Security time!");
                        justSecurity = true;
                        initialSetup();
                    } else {
                        openSettings();
                    }
                } else if (result == 561) {
                    authorizationBar.setVisibility(View.GONE);
                    playStoreLink.setVisibility(View.VISIBLE);
                    authorizationStatus.setText("Not Licensed!!\nPlease Buy My App!!!");
                } else if (result == -1) {
                    authorizationBar.setVisibility(View.GONE);
                    playStoreLink.setVisibility(View.GONE);
                    authorizationStatus.setText("Connection Error\nPlease Retry");
                } else if (result == 291) {
                    authorizationBar.setVisibility(View.GONE);
                    playStoreLink.setVisibility(View.GONE);
                    authorizationStatus.setText("Error Contacting Licensing Server.\nPlease Retry");
                } else if (result == 501) {
                    authorizationBar.setVisibility(View.GONE);
                    playStoreLink.setVisibility(View.VISIBLE);
                    authorizationStatus.setText("Error In Package Name");
                } else if (result == 502) {
                    authorizationBar.setVisibility(View.GONE);
                    playStoreLink.setVisibility(View.VISIBLE);
                    authorizationStatus.setText("Non Matching UID");
                } else {
                    authorizationBar.setVisibility(View.GONE);
                    playStoreLink.setVisibility(View.VISIBLE);
                    authorizationStatus.setText("Unknown Error\nPlease Retry");
                }
            }
        });
    }

    private class MyLicenseCheckerCallback implements LicenseCheckerCallback {
        public void allow(int reason) {
            if (isFinishing()) {
                // Don't update UI if Activity is finishing.
                return;
            }

            editor.putBoolean("licensed", true);
            editor.commit();
            Log.i("First Run:", "" + sharedPreferences.getBoolean("first_run", true));
            result = reason;
            isAuthorized = true;
            Log.i(TAG, "Result: " + result);
            updateLicenseStatus();
            mChecker.onDestroy();
        }

        public void dontAllow(int reason) {
            if (isFinishing()) {
                // Don't update UI if Activity is finishing.
                return;
            }

            if (reason == Policy.RETRY) {
                // If the reason received from the policy is RETRY, it was probably
                // due to a loss of connection with the service, so we should give the
                // user a chance to retry. So show a dialog to retry.
            } else {
                // Otherwise, the user is not licensed to use this app.
                // Your response should always inform the user that the application
                // is not licensed, but your behavior at that point can vary. You might
                // provide the user a limited access version of your app or you can
                // take them to Google Play to purchase the app.
                //authorizationStatus.setText("FAILED!!");
                editor.putBoolean("licensed", false);
                editor.commit();
            }
            result = reason;
            isAuthorized = false;
            Log.i("Success", isAuthorized + " result: " + result);
            updateLicenseStatus();
            mChecker.onDestroy();
        }

        @Override
        public void applicationError(int errorCode) {
            Log.i("App ERROR", errorCode + "");
            result = 500 + errorCode;
            isAuthorized = false;
            updateLicenseStatus();
            mChecker.onDestroy();
        }
    }

    private static String removeSpaces(String s) {
        String returnString = "";
        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            returnString += chars[i];
            if (chars[i] == '/') {
                i++;
                Log.i("removeSpaces", "Found whitespace");
            }
        }
        return returnString;
    }

    private static String stringTransform(String s, int i) {
        char[] chars = s.toCharArray();
        for (int j = 0; j < chars.length; j++)
            chars[j] = (char) (chars[j] ^ i);
        return String.valueOf(chars);
    }
}