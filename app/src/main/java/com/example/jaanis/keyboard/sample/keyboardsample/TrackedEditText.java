package com.example.jaanis.keyboard.sample.keyboardsample;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.view.KeyEvent;

public class TrackedEditText extends AppCompatEditText implements ITracker.TrackableView {
    public TrackedEditText(Context context) {
        super(context);
    }

    public TrackedEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TrackedEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private ITracker mTracker = null;

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (mTracker != null) {
            if (mTracker.onKeyPreIme(event)) {
                return true;
            }
        }

        return super.onKeyPreIme(keyCode, event);
    }


    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);

        if (mTracker != null) {
            mTracker.onFocusChange(this, focused);
        }
    }

    @Override
    public void setTracker(ITracker tracker) {
        mTracker = tracker;
    }
}
