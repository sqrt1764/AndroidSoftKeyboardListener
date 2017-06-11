package com.example.jaanis.keyboard.sample.keyboardsample.helper;

import android.view.View;
import android.widget.EditText;

import com.example.jaanis.keyboard.sample.keyboardsample.ITracker;
import com.example.jaanis.keyboard.sample.keyboardsample.SoftKeyboardListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class KeyboardHookManager {

    private SoftKeyboardListener mSoftKeyboardListener = new SoftKeyboardListener();
    private Set<HookWrapper> mKeyboardHookList = new HashSet<>();

    private KeyboardHook.RequestCallback mHookCallback = new KeyboardHook.RequestCallback()  {
        @Override
        public void requestHide() {
            mSoftKeyboardListener.hideKeyboard();
        }

        @Override
        public void requestShow(ITracker.TrackableView view) {
            ((EditText)view).requestFocus();
        }

        @Override
        public void requestDisconnect(KeyboardHook hook) {
            removeHook(hook);
        }

        @Override
        public boolean isKeyboardShowing() {
            return mSoftKeyboardListener.isKeyboardShowing();
        }
    };

    public <Tracked extends EditText & ITracker.TrackableView> void addHook(KeyboardHook hook) {
        HookWrapper wrapper = new HookWrapper(hook);
        mKeyboardHookList.add(wrapper);

        hook.setImplementer(mHookCallback);

        //add tracking
        if (hook.getTrackedViews() == null) {
            return;
        }
        for (ITracker.TrackableView view : hook.getTrackedViews()) {
            wrapper.trackedList.add(view);

            Tracked tracked = (Tracked) view;
            mSoftKeyboardListener.addTracking(tracked);
        }
    }

    private <Tracked extends EditText & ITracker.TrackableView> void removeHook(KeyboardHook hook) {
        //find the matching wrapper
        HookWrapper wrapper = null;
        for (HookWrapper hookWrapper : mKeyboardHookList) {
            if (hookWrapper.hook == hook) {
                wrapper = hookWrapper;
                break;
            }
        }
        if (wrapper == null) {
            return;
        }

        mKeyboardHookList.remove(wrapper);

        hook.setImplementer(null);

        //remove tracking
        if (wrapper.trackedList.isEmpty()) {
            return;
        }
        for (ITracker.TrackableView view : wrapper.trackedList) {
            Tracked tracked = (Tracked) view;
            mSoftKeyboardListener.removeTracking(tracked);
        }
    }

    public void init(View defaultFocusView, boolean noSavedState) {

        //setup the default-focus-view
        defaultFocusView.setFocusableInTouchMode(true);
        if (noSavedState) {
            //focus on the default-focus-view on first-create
            defaultFocusView.requestFocus();
        }

        mSoftKeyboardListener.setCallback(new SoftKeyboardListener.Callback() {
            @Override
            public void onShowingRefreshed(boolean isShowing) {
                for (HookWrapper wrapper : mKeyboardHookList) {
                    wrapper.hook.onShowingChanged(isShowing);
                }
            }

            @Override
            public void onInputMethodManagedFailedToCooperate() {
                for (HookWrapper wrapper : mKeyboardHookList) {
                    wrapper.hook.onInputMethodManagedFailedToCooperate();
                }
            }
        });

        //init the keyboard-listener
        mSoftKeyboardListener.onCreate(defaultFocusView.getContext(), defaultFocusView);
    }

    public void resume() {
        // restore-state keyboard-listener
        mSoftKeyboardListener.onResume();
    }

    public void release() {
        List<HookWrapper> warppers = new ArrayList<>(mKeyboardHookList);
        for (HookWrapper warpper : warppers) {
            warpper.hook.disconnect();
        }
    }

    public boolean isKeyboardShowing() {
        return mHookCallback.isKeyboardShowing();
    }

    public void requestHide() {
        mHookCallback.requestHide();
    }

    public void requestShow(ITracker.TrackableView view) {
        mHookCallback.requestShow(view);
    }


    private static class HookWrapper {
        KeyboardHook hook;
        final Set<ITracker.TrackableView> trackedList = new HashSet<>();

        HookWrapper(KeyboardHook hook) {
            this.hook = hook;
        }
    }
}