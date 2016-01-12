package com.packruler.musicaldaydream.release;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.util.Log;

/**
 * Created by packr_000 on 8/27/2014.
 */
public class BatteryStatusReceiver extends BroadcastReceiver {
    private final String TAG = this.getClass().getSimpleName();

    public int status;
    public boolean isCharging;
    public int chargePlug;
    public boolean usbCharge;
    public boolean acCharge;
    public boolean wirelessCharging;
    public int batteryPct;
    private boolean updateBattery = true;

    private Intent i = new Intent("com.packruler.MusicalDaydream.MAIN_RECEIVER");

    @Override
    public void onReceive(Context context, Intent intent) {
        chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        if (chargePlug == 0 &&
                (isCharging || usbCharge || acCharge || wirelessCharging)) {
            isCharging = false;
            usbCharge = false;
            acCharge = false;
            wirelessCharging = false;
            updateBattery = true;
        } else if (chargePlug == BatteryManager.BATTERY_PLUGGED_USB &&
                (!isCharging || !usbCharge || acCharge || wirelessCharging)) {
            isCharging = true;
            usbCharge = true;
            acCharge = false;
            wirelessCharging = false;
            updateBattery = true;
        } else if (chargePlug == BatteryManager.BATTERY_PLUGGED_AC &&
                (!isCharging || usbCharge || !acCharge || wirelessCharging)) {
            isCharging = true;
            usbCharge = false;
            acCharge = true;
            wirelessCharging = false;
            updateBattery = true;
        } else if (chargePlug == BatteryManager.BATTERY_PLUGGED_WIRELESS &&
                (!isCharging || usbCharge || acCharge || !wirelessCharging)) {
            isCharging = true;
            usbCharge = false;
            acCharge = false;
            wirelessCharging = true;
            updateBattery = true;
        } else {
            Log.i(TAG, "Battery Plug Error");
        }

//        Log.i(TAG, "Charging: " + isCharging + " USB: " + usbCharge + " AC: " + acCharge + " Wireless: " + wirelessCharging);

        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        if (batteryPct != ((level / (float) scale) * 100)) {
            batteryPct = (int) ((level / (float) scale) * 100);
            updateBattery = true;
        }
//        Log.i(TAG, "Battery Level: " + batteryPct + " Update: " + updateBattery);
        try {
            if (updateBattery) {
                Log.i(TAG, "Charge state change");
                i.putExtra("command", "battery");
                MusicalDaydreamService.context.sendBroadcast(i);
                updateBattery = false;
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
