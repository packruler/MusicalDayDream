package com.packruler.musicaldaydream.release;

import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;
import android.util.Log;

/**
 * Created by Packruler on 6/30/2014.
 */
public class BackupUtility extends BackupAgentHelper {
    static String PREFS_BACKUP_STRING;
    static final String MY_PREFS_BACKUP_KEY = "preferences_backup";

    public void onCreate(){
        Log.i("BACKUP AGENT", "onCreate()");
        PREFS_BACKUP_STRING = getString(R.string.settings_string);
        SharedPreferencesBackupHelper helper =
                new SharedPreferencesBackupHelper(this, PREFS_BACKUP_STRING);
        addHelper(MY_PREFS_BACKUP_KEY, helper);
    }
}
