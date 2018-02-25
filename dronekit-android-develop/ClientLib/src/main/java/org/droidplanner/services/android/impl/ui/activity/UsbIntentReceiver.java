package org.droidplanner.services.android.impl.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * Created by fhuya on 11/12/14.
 */
public class UsbIntentReceiver extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        TextView contentView = new TextView(getApplicationContext());
        contentView.setText("Accessing USB Device...");
        contentView.setGravity(Gravity.CENTER);
        setContentView(contentView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT));
        handleIntent(getIntent());
    }

    @Override
    public void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        finish();
    }
}
