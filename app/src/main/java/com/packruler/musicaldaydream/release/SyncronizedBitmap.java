package com.packruler.musicaldaydream.release;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;

/**
 * Created by packr_000 on 12/17/2014.
 */
public class SyncronizedBitmap {
    private final String TAG = this.getClass().getSimpleName();

    private Intent updatedImage = new Intent(ListenerService.RECEIVER_STRING).putExtra(ListenerService.COMMAND, ListenerService.UPDATED_IMAGE);
    Bitmap image;
    Context context;

    public SyncronizedBitmap(Context in) {
        image = null;
        context = in;
    }

    public synchronized void setImage(Bitmap in) {
        Log.i(TAG, "setImage");
        image = in;
        context.sendBroadcast(updatedImage);
    }

    public synchronized Bitmap getImage() {
        Log.i(TAG, "getImage");
        return image;
    }

    public synchronized boolean isNull() {
        return image == null;
    }
}
