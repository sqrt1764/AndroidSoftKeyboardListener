package com.example.jaanis.keyboard.sample.keyboardsample;

import android.view.KeyEvent;
import android.view.View;

public interface ITracker {
    interface TrackableView {
        void setTracker(ITracker listener);
    }

    /**
     * @param event keyevent
     * @return `true` if the event was handled & should not be propagated
     */
    boolean onKeyPreIme(KeyEvent event);

    //todo
    void onFocusChange(View v, boolean hasFocus);
}
