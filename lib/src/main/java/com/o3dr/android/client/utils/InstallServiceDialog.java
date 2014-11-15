package com.o3dr.android.client.utils;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;

import com.o3dr.android.client.R;

/**
 * Created by fhuya on 11/14/14.
 */
public class InstallServiceDialog extends FragmentActivity {

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_install_service_dialog);

        final Button cancelButton = (Button) findViewById(R.id.dialog_cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        final Button installButton = (Button) findViewById(R.id.dialog_install_button);
        installButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=org.droidplanner.services.android")));
                finish();
            }
        });
    }
}
