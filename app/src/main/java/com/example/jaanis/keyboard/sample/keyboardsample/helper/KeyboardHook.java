package com.example.jaanis.keyboard.sample.keyboardsample.helper;

import android.util.Log;

import com.example.jaanis.keyboard.sample.keyboardsample.ITracker;

import java.util.List;

public abstract class KeyboardHook {
    private RequestCallback mCallback;

    public void requestHide() {
        if (mCallback == null) {
            Log.w("KeyboardHook", "callback is not set" + new IllegalArgumentException());
            return;
        }
        mCallback.requestHide();

    }

    public void requestShow(ITracker.TrackableView view) {
        if (mCallback == null) {
            Log.w("KeyboardHook", "callback is not set" + new IllegalArgumentException());
            return;
        }

        mCallback.requestShow(view);
    }

    public void disconnect() {
        if (mCallback == null) {
            Log.w("KeyboardHook", "callback is not set" + new IllegalArgumentException());
            return;
        }

        mCallback.requestDisconnect(this);
    }

    public boolean isKeyboardShowing() {
        if (mCallback == null) {
            Log.w("KeyboardHook", "callback is not set" + new IllegalArgumentException());
            return false;
        }

        return mCallback.isKeyboardShowing();
    }

    public void setImplementer(RequestCallback cb) {
        mCallback = cb;
    }

    public abstract void onShowingChanged(boolean isShowing);

    public abstract void onInputMethodManagedFailedToCooperate();

    public abstract List<ITracker.TrackableView> getTrackedViews();

    interface RequestCallback {
        void requestHide();
        void requestShow(ITracker.TrackableView view);
        void requestDisconnect(KeyboardHook hook);
        boolean isKeyboardShowing();
    }
}