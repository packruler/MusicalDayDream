package com.packruler.musicaldaydream.release;

import android.app.Notification;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

/**
 * Created by packr_000 on 4/24/2014.
 */
public class NotificationDisplayUpdateThread extends Thread {

    private final String TAG = this.getClass().getSimpleName();

    private Bitmap icon;
    private LinearLayout notificationIcons;
    private ImageButton newIcon;
    protected android.os.Handler handler;
    private View remoteView;
    private Context notificationContext;
    private Bundle extras;
    private CharSequence title;
    private CharSequence summary;
    private CharSequence sub;
    private CharSequence text;
    private CharSequence info;
    private CharSequence[] textLines;
    private CharSequence titleBig;
    private Bitmap pictureBitmap;
    protected static boolean keepNotification;
    private int notificationSize;
    private SharedPreferences sharedPreferences;

    /**
     * Add all current notifications in NotificationArrayList to notification display bar.
     * Also sets them as buttons
     */
    @Override
    public void run() {
        keepNotification = false;
        if (MusicalDaydreamService.isActive) {
            sharedPreferences = MusicalDaydreamService.context.getSharedPreferences(MusicalDaydreamService.context.getString(R.string.settings_string), 0);
            if (sharedPreferences.getBoolean(MusicalDaydreamService.context.getString(R.string.display_notifications), true)) {

//            Log.i("notificationUpdating", "Updating Notification Panel");
                int numIcons = 0;

                notificationIcons = new LinearLayout(MusicalDaydreamService.context);
                notificationIcons.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                notificationIcons.setPadding(0, 10, 0, 10);
                notificationIcons.setAlpha((float) 0.8);
                notificationIcons.setOrientation(LinearLayout.HORIZONTAL);
                notificationIcons.setGravity(Gravity.CENTER_HORIZONTAL);
                notificationIcons.setVerticalGravity(Gravity.CENTER_HORIZONTAL);
                MusicalDaydreamService.uiHandler.postAtFrontOfQueue(new Runnable() {
                    @Override
                    public void run() {
//                    MusicalDaydreamService.largeNotificationDisplay.removeAllViews();
                        MusicalDaydreamService.largeNotificationDisplay.removeAllViews();
                        MusicalDaydreamService.largeNotificationDisplay.addView(MusicalDaydreamService.notificationSummary);
                        MusicalDaydreamService.largeNotificationDisplay.addView(MusicalDaydreamService.notificationTitle);
                        MusicalDaydreamService.notificationPanel.removeAllViews();
                    }
                });


                for (int x = 0; x < ListenerService.statusBarNotificationArrayList.size(); x++) {
                    Bitmap icon;
//                Log.i("updateNotificationDisplay", "" + x + ListenerService.statusBarNotificationArrayList.get(x).getTag());

                    final StatusBarNotification statusBarNotification = ListenerService.statusBarNotificationArrayList.get(x);
                    final Notification currentNotification = ListenerService.statusBarNotificationArrayList.get(x).getNotification();
                    final String packageName = ListenerService.statusBarNotificationArrayList.get(x).getPackageName();

                    //Pull expanded notification display

                    if (sharedPreferences.getInt(MusicalDaydreamService.context.getString(R.string.notification_icon_size), 0) != 0) {
                        notificationSize = sharedPreferences.getInt(MusicalDaydreamService.context.getString(R.string.notification_icon_size), 0) * 2;
                    } else {
                        notificationSize = MusicalDaydreamService.context.getResources().getDimensionPixelSize(R.dimen.notification_icon_size);
                    }

                    try {
                        final Context notificationContext = MusicalDaydreamService.context.createPackageContext(packageName, 0);

                        if (notificationContext.getResources().getDrawable(currentNotification.icon) != null) {
                            //Setup notification buttons
                            MusicalDaydreamService.uiHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    newIcon = new ImageButton(MusicalDaydreamService.context);
                                    newIcon.setLayoutParams(new ViewGroup.LayoutParams(notificationSize, notificationSize));
                                    newIcon.setPadding(20, 0, 20, 0);
                                    newIcon.setBackground(notificationContext.getResources().getDrawable(currentNotification.icon));
                                    newIcon.setOnTouchListener(new NotificationTouchListener(MusicalDaydreamService.context, statusBarNotification));
                                    newIcon.setTag(statusBarNotification.getTag());
                                    notificationIcons.addView(newIcon);
                                }
                            });
                        } else {
                            MusicalDaydreamService.uiHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    newIcon = new ImageButton(MusicalDaydreamService.context);
                                    newIcon.setLayoutParams(new ViewGroup.LayoutParams(MusicalDaydreamService.context.getResources().getDimensionPixelSize(R.dimen.notification_icon_size),
                                            MusicalDaydreamService.context.getResources().getDimensionPixelSize(R.dimen.notification_icon_size)));
                                    newIcon.setPadding(20, 0, 20, 0);
                                    newIcon.setBackgroundColor(MusicalDaydreamService.context.getResources().getColor(R.color.black_overlay));
                                    newIcon.setOnTouchListener(new NotificationTouchListener(MusicalDaydreamService.context, statusBarNotification));
                                    newIcon.setTag(statusBarNotification.getTag());
                                    notificationIcons.addView(newIcon);
                                }
                            });
                        }

                    } catch (Exception e) {
                        if (e instanceof NullPointerException || e instanceof Resources.NotFoundException) {
                            Log.i(TAG, "Expected Exception Handled");
                        } else {
                            e.printStackTrace();
                        }
                    }
                }

                //Add button to notificationPanel
                MusicalDaydreamService.uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        MusicalDaydreamService.notificationPanel.addView(notificationIcons);
                    }

                });
            }
        }
    }
}