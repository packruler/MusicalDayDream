package com.packruler.musicaldaydream.release;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;


public class StartDreaming extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_start_dreaming);

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName("com.android.systemui", "com.android.systemui.Somnambulator");
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast toast = Toast.makeText(getApplicationContext(), "Sorry this device is not capable of launching a DayDream from the launcher\nThis shortcut will now be removed", Toast.LENGTH_LONG);
            toast.show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    getPackageManager().setComponentEnabledSetting(getComponentName(), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
                }
            }, 4000);
        }
        this.finish();
    }

}
