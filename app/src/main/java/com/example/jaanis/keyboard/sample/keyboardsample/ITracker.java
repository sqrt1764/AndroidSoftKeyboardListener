package com.example.jaanis.keyboard.sample.keyboardsample;

import android.view.KeyEvent;
import android.view.View;

public interface ITracker {
    interface TrackableView {
        void setTracker(ITracker listener);
    }

    /**
     * @param event key event
     * @return `true` if the event was handled & should not be propagated
     */
    boolean onKeyPreIme(KeyEvent event);

    /**
     * @param v        view this happened to
     * @param hasFocus new focus-state
     */
    void onFocusChange(View v, boolean hasFocus);

    /**
     * @param actionId id received in matching view's method
     */
    void onEditorAction(int actionId);
}