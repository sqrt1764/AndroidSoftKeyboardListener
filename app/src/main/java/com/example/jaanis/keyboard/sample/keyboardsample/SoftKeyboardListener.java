package com.example.jaanis.keyboard.sample.keyboardsample;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;

/**
 * a keyboard listener that functions relying on customized edit-texts & their focus-state.<br>
 * this seems to be working nicely with multi-windows.<br><br>
 * this thing has limitations of course - see usage.
 *
 * usage:
 * <ol>
 *     <li>construct</li>
 *     <li>Acitivity.onCreate(..) -> this.onCreate(..)</li>
 *     <li>Acitivity.onCreate(..) -> this.addTracking(..) for all EditTexts you want
 *     managed by this listener</li>
 *     <li>Acitivity.onResume(..) -> this.onResume() necessary for
 *     restoring on orientation changes</li>
 *     <li>Tracked.requestFocus() to open the soft-keyboard for a specific view</li>
 *     <li>this.hideKeyboard() to hide the soft-keyboard if currently
 *     inputting in a tracked view</li>
 *     <li>DO NOT USE SHOW&HIDE THAT CAN BE FOUND IN InputMethodManager MANUALLY</li>
 * </ol>
 * (should also work in fragments)<br><br>
 *
 * how it works.<br>
 * the listener detects that a tracked edit-text has gained focus & in response
 * attempts to display the soft-keyboard, triggering the <i>execution of the callback</i>. <br>
 * when user attempts to close the keyboard, by pressing `back`, that key-event
 * is consumed & manual closing done, which triggers the <i>execution of the callback</i>. <br>
 * focus of a tracked view is an indication to this listener that the keyboard should be displayed
 * upon restoring of the state. <br><br>
 *
 * <span style="color: red;">known flaws: if initial focus is required, post-delay calling
 *  of the EditText.requestFocus() method (InputMethodManager.showSoftInput(...) returns `false`
 *  if it is called without a delay)</span>
 */
public class SoftKeyboardListener {
    private Callback mInternalCallback = new Callback() {
        @Override
        public void onShowingRefreshed(boolean isShowing) {
            mIsShowing = isShowing;
            if (mCallback != null) {
                mCallback.onShowingRefreshed(mIsShowing);
            }
        }

        @Override
        public void onInputMethodManagedFailedToCooperate() {
            if (mCallback != null) {
                mCallback.onInputMethodManagedFailedToCooperate();
            }
        }
    };
    private Callback mCallback;

    private InputMethodManager mImm;
    private Handler mHandler;
    private Set<View> mTrackedViews = new HashSet<>();
    private View mDefaultFocusView = null;
    private ResultReceiver mShowHideIMEResultReceiver = null;
    private boolean mIsShowing = false;

    /**
     * this method initializes the keyboard listener.
     * @param context context
     * @param defaultFocusView a neutral view that is focusable-in-touch-mode that can be
     *                         used to take away the focus from the active edit-text
     */
    public void onCreate(Context context, View defaultFocusView) {
        mainThreadCheck();

        mHandler = new Handler();
        mImm = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        mDefaultFocusView = defaultFocusView;

        mShowHideIMEResultReceiver = new ResultReceiverImpl(mHandler, mInternalCallback);
    }

    /**
     * method restores the soft-keyboard visibility on config-change
     */
    public void onResume() {
        mainThreadCheck();

        //android is not ready to do keyboard showing/hiding this early - wtf?
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                forceKeyboardSync();
            }
        }, 50);
    }

// don't think this is necessary, don't see how this could leak.
// this class will only live as long as its owner
//    public void onDestroy() {
//        mImm = null;
//        mHandler.removeCallbacks(mShowImeRunnable);
//        mHandler = null;
//        mTrackedViews.clear();
//        mDefaultFocusView = null;
//        mShowHideIMEResultReceiver = null;
//    }

    /**
     * @param callback callback you want executed when the showing refreshes/changes
     */
    public void setCallback(Callback callback) {
        if (callback == null) {
            throw new IllegalArgumentException();
        }
        mCallback = callback;
    }

    /**
     * method adds the specified view to be tracked by this listener. can add multiple
     * @param view track this
     */
    public <Tracked extends EditText & ITracker.TrackableView> void addTracking(Tracked view) {
        view.setTracker(mITracker);
        mTrackedViews.add(view);
    }

    /**
     * method removes a view from being-tracked. generally unused.
     * @param view stop tracking this
     */
    public <Tracked extends EditText & ITracker.TrackableView> void removeTracking(Tracked view) {
        if (!mTrackedViews.contains(view)) {
            return;
        }

        mTrackedViews.remove(view);
        view.setTracker(null);
    }

    /**
     * resycn the soft-keyboard showing depending on the focus of the tracked views
     */
    public void forceKeyboardSync() {
        View focusedView = findFocusedView();
        setImeVisibility(focusedView != null);
    }

    /**
     * @return is soft-keyboard showing for a tracked view
     */
    public boolean isKeyboardShowing() {
        return mIsShowing;
    }

    /**
     * method will hide the soft-keyboard if it is currently working with a tracked view
     */
    public void hideKeyboard() {
        Log.d("SoftKeyboardListener", "hideKeyboard");
        onBackPressedOnKeyboard();
    }

    ////////////

    private void mainThreadCheck() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new IllegalStateException();
        }
    }

    private View findFocusedView() {
        for (View v : mTrackedViews) {
            if (v.isFocused()) {
                return v;
            }
        }
        return null;
    }

    private void onTrackedViewGainedFocus() {
        Log.d("SoftKeyboardListener", "onTrackedViewGainedFocus");
        setImeVisibility(true);
    }

    private void onBackPressedOnKeyboard() {
        Log.d("SoftKeyboardListener", "onBackPressedOnKeyboard");

        View focusedView = findFocusedView();
        if (focusedView == null) {
            Log.w("SoftKeyboardListener", "wut focusedView==null? how is this possible?");
            return;
        }

        setImeVisibility(false);
        mDefaultFocusView.requestFocus();
    }

    private ITracker mITracker = new ITracker() {
        @Override
        public boolean onKeyPreIme(KeyEvent event) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                onBackPressedOnKeyboard();
                return true;
            }
            return false;
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                onTrackedViewGainedFocus();
            } else {
                triggerLostFocusCheck();
            }
        }

        @Override
        public void onEditorAction(int actionId) {
            int result = actionId & EditorInfo.IME_MASK_ACTION;
            if (result == EditorInfo.IME_ACTION_DONE ||
                    result == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard();
            }
        }
    };

    private void triggerLostFocusCheck() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("SoftKeyboardListener", "triggerLostFocusCheck");
                View focusedView = findFocusedView();
                if (focusedView == null) {
                    //focus no longer is on one of the tracked views -> close the keyboard
                    setImeVisibility(false);
                }
            }
        }, 100);
    }

    ///////////

    private Runnable mShowImeRunnable = new Runnable() {
        public void run() {
            Log.d("SoftKeyboardListener", "mShowImeRunnable");
            View focusedView = findFocusedView();
            if (mImm != null) {
                if (focusedView != null) {
                    boolean show = mImm.showSoftInput(focusedView, 0, mShowHideIMEResultReceiver);
                    if (!show) {
                        Log.w("SoftKeyboardListener", "mShowImeRunnable RETURNED FALSE");
                        mInternalCallback.onInputMethodManagedFailedToCooperate();
                    }
                } else {
                    //nothing focused - hide soft-keyboard & exec callback
                    mInternalCallback.onShowingRefreshed(false);
                }
            } else {
                throw new IllegalStateException("onCreate(..) not called");
            }
        }
    };

    private void setImeVisibility(final boolean visible) {
        if (visible) {
            mHandler.post(mShowImeRunnable);
        } else {
            mHandler.removeCallbacks(mShowImeRunnable);

            if (mImm != null) {
                if (mDefaultFocusView != null) {
                    boolean hide = mImm.hideSoftInputFromWindow(
                            mDefaultFocusView.getWindowToken(), 0, mShowHideIMEResultReceiver);
                    if (!hide) {
                        Log.w("SoftKeyboardListener", "hideSoftInputFromWindow RETURNED FALSE");
                        mInternalCallback.onInputMethodManagedFailedToCooperate();
                    }
                } else {
                    //nothing focused - hide soft-keyboard & exec callback
                    mInternalCallback.onShowingRefreshed(false);
                }
            } else {
                throw new IllegalStateException("onCreate(..) not called");
            }
        }
    }

    ///////////

    /**
     * InputMethodManager.showSoftInput(..)
     * InputMethodManager.hideSoftInputFromWindow(..)
     * both methods warn about a possible leak of this obj
     */
    private static class ResultReceiverImpl extends ResultReceiver {

        private WeakReference<Callback> mCallback;

        ResultReceiverImpl(Handler handler, Callback listener) {
            super(handler);

            mCallback = new WeakReference<>(listener);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            Callback callback = mCallback.get();
            if (callback == null) {
                return;
            }

            switch (resultCode) {
                case InputMethodManager.RESULT_HIDDEN:
                case InputMethodManager.RESULT_UNCHANGED_HIDDEN:
                    callback.onShowingRefreshed(false);
                    break;
                case InputMethodManager.RESULT_SHOWN:
                case InputMethodManager.RESULT_UNCHANGED_SHOWN:
                    callback.onShowingRefreshed(true);
                    break;
                default:
                    throw new IllegalArgumentException();
            }
        }
    }

    public interface Callback {
        void onShowingRefreshed(boolean isShowing);
        void onInputMethodManagedFailedToCooperate();
    }
}
