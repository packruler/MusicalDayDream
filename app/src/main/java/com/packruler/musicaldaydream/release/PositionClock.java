package com.packruler.musicaldaydream.release;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

/**
 * Created by Packruler on 12/27/2014.
 */
public class PositionClock extends TextView {
    private HandlerThread backgroundThread;
    private Handler backgroundHandler;
    private Handler uiHandler = new Handler(Looper.getMainLooper());
    private String timeString = "";
    private long tempValue;
    private long hours;
    private long minutes;
    private long seconds;

    public PositionClock(Context context, AttributeSet attrs) {
        super(context, attrs);
        backgroundThread = new HandlerThread("PositionClock" + getId());
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    public PositionClock(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        backgroundThread = new HandlerThread("PositionClock" + getId());
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    public long currentValue;

    public void setValue(long value) {
        if (currentValue != value) {
            currentValue = value;
            timeString = "";
            backgroundHandler.post(new CalculateTime());
        }
    }

    private class CalculateTime implements Runnable {
        @Override
        public void run() {
            tempValue = currentValue;

            if (currentValue < 0) {
                timeString += "-";
                tempValue = (-currentValue);
            }

            hours = TimeUnit.MILLISECONDS.toHours(tempValue);
            tempValue -= TimeUnit.MINUTES.toMillis(hours);
            minutes = TimeUnit.MILLISECONDS.toMinutes(tempValue);
            tempValue -= TimeUnit.MINUTES.toMillis(minutes);
            seconds = TimeUnit.MILLISECONDS.toSeconds(tempValue);

            if (hours > 0) {
                timeString += hours + ":";
                if (minutes < 10) {
                    timeString += "0" + minutes + ":";
                } else {
                    timeString += minutes + ":";
                }
            } else {
                timeString += minutes + ":";
            }

            if (seconds < 10) {
                timeString += "0" + seconds;
            } else {
                timeString += seconds;
            }

            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    setText(timeString);
                }
            });
        }
    }
}
