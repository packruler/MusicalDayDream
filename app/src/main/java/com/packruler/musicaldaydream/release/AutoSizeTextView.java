package com.packruler.musicaldaydream.release;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by Packruler on 12/29/2014.
 */
public class AutoSizeTextView extends TextView {
    private final String TAG = this.getClass().getSimpleName();

    private float fontSize = 1000;
    private int resizeHeight = Integer.MAX_VALUE;

    public AutoSizeTextView(final Context context) {
        this(context, null, 0);
    }

    public AutoSizeTextView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoSizeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        fontSize = getTextSize();
    }

    @Override
    public void setTypeface(Typeface tf) {
        super.setTypeface(tf);
        resizeTextSize();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getHeight() > resizeHeight) {
            resizeTextSize();
        }
    }

    @Override
    public void setTextSize(float size) {
        Log.i(TAG, "Set text size: " + size);
        if (size > 0) {
            super.setTextSize(size);
            fontSize = size;
        }
    }

    public void setResizeHeight(int maxHeight) {
        resizeHeight = maxHeight;
    }

    private void resizeTextSize() {
        Log.i(TAG, "Current Height: " + getHeight() + " resizeHeight: " + resizeHeight + " TextSize: " + getTextSize());
        if (getHeight() > (resizeHeight + 5)) {
            setTextSize(fontSize - 1);
        }else if (getHeight() < (resizeHeight - 5) && resizeHeight != Integer.MAX_VALUE){
            setTextSize(fontSize + 1);
        }
    }
}
