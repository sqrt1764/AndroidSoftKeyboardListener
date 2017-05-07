package com.example.jaanis.keyboard.sample.keyboardsample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private SoftKeyboardListener mKeyboardListener
            = new SoftKeyboardListener(
            new SoftKeyboardListener.Callback() {
                @Override
                public void onShowingRefreshed(boolean isShowing) {
                    tv0.setText("keyboard showing: " + isShowing);
                }
            });

    private TextView tv0;
    private TrackedEditText et0;
    private TrackedEditText et1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setup the default-focus-view
        View defaultFocusView = findViewById(R.id.container0);
        defaultFocusView.setFocusableInTouchMode(true);
        if (savedInstanceState == null) {
            //focus on the default-focus-view on first-create
            defaultFocusView.requestFocus();
        }
        //init the keyboard-listener
        mKeyboardListener.onCreate(this, defaultFocusView);

        tv0 = (TextView) findViewById(R.id.textView0);
        et0 = (TrackedEditText) findViewById(R.id.et0);
        et1 = (TrackedEditText) findViewById(R.id.et1);

        mKeyboardListener.addTracking(et0);
        mKeyboardListener.addTracking(et1);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //restore-state keyboard-listener
        mKeyboardListener.onResume();
    }
}
