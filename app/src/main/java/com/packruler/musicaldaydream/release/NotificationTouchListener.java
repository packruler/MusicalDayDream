package com.packruler.musicaldaydream.release;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

public class NotificationTouchListener implements OnTouchListener {

    private final GestureDetector gestureDetector;
    private StatusBarNotification statusBarNotification;
    private Notification notification;
    private String packageName;
    private View remoteView;
    private CharSequence title;
    private CharSequence summary;
    private CharSequence sub;
    private CharSequence text;
    private CharSequence info;
    private CharSequence[] textLines;
    private CharSequence titleBig;
    private Bitmap pictureBitmap;
    private RelativeLayout.LayoutParams noMargin = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    private ViewGroup.LayoutParams yesMargin = MusicalDaydreamService.largeNotificationDisplay.getLayoutParams();

    public NotificationTouchListener(Context ctx, StatusBarNotification inStatusBarNotification) {
        gestureDetector = new GestureDetector(ctx, new GestureListener());
        gestureDetector.setIsLongpressEnabled(false);
        statusBarNotification = inStatusBarNotification;
        notification = inStatusBarNotification.getNotification();
        packageName = statusBarNotification.getPackageName();
        title = notification.extras.getCharSequence(Notification.EXTRA_TITLE);

        MusicalDaydreamService.uiHandler.postAtFrontOfQueue(new Runnable() {
            @Override
            public void run() {
                if (statusBarNotification.getNotification().bigContentView != null) {
                    remoteView = statusBarNotification.getNotification().bigContentView.apply(MusicalDaydreamService.context, MusicalDaydreamService.largeNotificationDisplay);
                    remoteView.setAlpha((float) .8);
                } else {
                    remoteView = statusBarNotification.getNotification().contentView.apply(MusicalDaydreamService.context, MusicalDaydreamService.largeNotificationDisplay);
                    remoteView.setAlpha((float) .8);
                }
            }
        });

        if (notification.extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT) != null) {
            summary = notification.extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT);
        }
        if (notification.extras.getCharSequence(Notification.EXTRA_TEXT) != null) {
            summary = notification.extras.getCharSequence(Notification.EXTRA_TEXT);
        }
        if (notification.extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES) != null) {
            String summaryString = "";
            CharSequence[] array = notification.extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES);
            for (int x = 0; x < array.length; x++) {
                if (x != (array.length - 1)) {
                    summaryString += array[x] + "\n";
                } else {
                    summaryString += array[x];
                }
            }
            summary = summaryString;
        }
        noMargin.setMargins(0, 0, 0, 0);
//        noMargin.addRule(RelativeLayout.ABOVE, R.id.notification_panel);
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
//            Log.i("Down", "" + NotificationDisplayUpdateThread.keepNotification);
            if (!NotificationDisplayUpdateThread.keepNotification) {
                MusicalDaydreamService.uiHandler.postAtFrontOfQueue(new Runnable() {
                    @Override
                    public void run() {
                        MusicalDaydreamService.largeNotificationDisplay.setLayoutParams(yesMargin);
                        MusicalDaydreamService.largeNotificationDisplay.setBackgroundColor(MusicalDaydreamService.context.getResources().getColor(R.color.notification_display_background));
                        MusicalDaydreamService.notificationTitle.setVisibility(View.VISIBLE);
                        MusicalDaydreamService.notificationSummary.setVisibility(View.VISIBLE);

                        MusicalDaydreamService.notificationTitle.setText(title);

                        if (summary != null) {
                            MusicalDaydreamService.notificationSummary.setText(summary);
                        } else {
                            MusicalDaydreamService.notificationSummary.setText("");
                        }
                    }
                });
            }
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            MusicalDaydreamService.uiHandler.postAtFrontOfQueue(new Runnable() {
                @Override
                public void run() {
                    MusicalDaydreamService.notificationTitle.setText("");
                    MusicalDaydreamService.notificationSummary.setText("");
                    if (!NotificationDisplayUpdateThread.keepNotification) {
                        MusicalDaydreamService.largeNotificationDisplay.setBackgroundColor(View.INVISIBLE);
                    }
                }
            });

            MusicalDaydreamService.context.sendBroadcast(MusicalDaydreamService.startTimeout);
        }

        return gestureDetector.onTouchEvent(event);
    }


    private final class GestureListener extends SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 50;

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

    }

    public void onSwipeTop() {
        if (!NotificationDisplayUpdateThread.keepNotification) {
            NotificationDisplayUpdateThread.keepNotification = true;
//            Log.i("Swipe Up", "Keep notification " + NotificationDisplayUpdateThread.keepNotification);

//            MusicalDaydreamService.remoteView = remoteView;
            MusicalDaydreamService.uiHandler.postAtFrontOfQueue(new Runnable() {
                @Override
                public void run() {
                    MusicalDaydreamService.largeNotificationDisplay.addView(remoteView);
                    MusicalDaydreamService.largeNotificationDisplay.setPadding(0, 0, 0, 0);
                    MusicalDaydreamService.largeNotificationDisplay.setLayoutParams(noMargin);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        MusicalDaydreamService.largeNotificationDisplay.setBackgroundColor(MusicalDaydreamService.context.getResources().getColor(android.R.color.background_light));
                    }
                    MusicalDaydreamService.notificationTitle.setText("");
                    MusicalDaydreamService.notificationSummary.setText("");
                    MusicalDaydreamService.notificationSummary.setVisibility(View.GONE);
                    MusicalDaydreamService.notificationTitle.setVisibility(View.GONE);

                    MusicalDaydreamService.largeNotificationDisplay.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                notification.contentIntent.send();
                                Intent i = new Intent("com.packruler.MusicalDaydream.MAIN_RECEIVER");
                                i.putExtra("command", "shutdown");
                                MusicalDaydreamService.context.sendBroadcast(i);
                            } catch (PendingIntent.CanceledException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });
        }
    }

    public void onSwipeRight() {

//        Log.i("Swipe right", "YES");
    }

    public void onSwipeLeft() {

//        Log.i("Swipe left", "YES");
    }

    public void onSwipeBottom() {
//        Log.i("Swipe Down", "Keep notification " + NotificationDisplayUpdateThread.keepNotification);
        MusicalDaydreamService.uiHandler.postAtFrontOfQueue(new Runnable() {
            @Override
            public void run() {
                if (NotificationDisplayUpdateThread.keepNotification) {
                    NotificationDisplayUpdateThread.keepNotification = false;
                    MusicalDaydreamService.largeNotificationDisplay.removeAllViews();
                    MusicalDaydreamService.largeNotificationDisplay.setBackgroundColor(View.INVISIBLE);
                    MusicalDaydreamService.largeNotificationDisplay.addView(MusicalDaydreamService.notificationTitle);
                    MusicalDaydreamService.largeNotificationDisplay.addView(MusicalDaydreamService.notificationSummary);
                    MusicalDaydreamService.largeNotificationDisplay.setLayoutParams(yesMargin);
                } else {
//            Log.i("Sending Broadcast", "Package: " + statusBarNotification.getPackageName() +
//                    " Tag: " + statusBarNotification.getTag() +
//                    " ID: " + statusBarNotification.getId());
                    Intent i = new Intent(ListenerService.RECEIVER_STRING);
                    i.putExtra(ListenerService.COMMAND, ListenerService.CLEAR_NOTIFICATION);
                    i.putExtra(ListenerService.NOTIFICATION_PACKAGE, statusBarNotification.getPackageName());
                    i.putExtra(ListenerService.NOTIFICATION_TAG, statusBarNotification.getTag());
                    i.putExtra(ListenerService.NOTIFICATION_ID, statusBarNotification.getId());
                    MusicalDaydreamService.context.sendBroadcast(i);
                }
            }
        });
    }
}