package org.droidplanner.android.fragments.geotag;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;

import org.droidplanner.android.R;
import org.droidplanner.android.activities.GeoTagActivity;
import org.droidplanner.android.fragments.helpers.ApiListenerFragment;

/**
 * Created by chavi on 10/15/15.
 */
public class GetCameraLogsFragment extends ApiListenerFragment {
    private static final String ARG_STATE = "state";

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTED_NOT_STARTED = 1;
    private static final int STATE_LOADING_LOGS = 2;
    private static final int STATE_DONE_LOGS = 3;

    private final static IntentFilter filter = new IntentFilter();

    static {
        filter.addAction(AttributeEvent.STATE_CONNECTED);
        filter.addAction(AttributeEvent.STATE_DISCONNECTED);
        filter.addAction(GeoTagImagesService.STATE_FINISHED_LOADING_LOGS);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case AttributeEvent.STATE_CONNECTED:
                    updateState(STATE_CONNECTED_NOT_STARTED);
                    break;
                case AttributeEvent.STATE_DISCONNECTED:
                    updateState(STATE_DISCONNECTED);
                    break;
                case GeoTagImagesService.STATE_FINISHED_LOADING_LOGS:
                    boolean success = intent.getBooleanExtra(GeoTagImagesService.EXTRA_SUCCESS, false);
                    if (success) {
                        updateState(STATE_DONE_LOGS);
                    } else {
                        updateState(STATE_CONNECTED_NOT_STARTED);
                    }
                    break;
            }
        }
    };

    private GeoTagActivity activity;

    private TextView instructionText;
    private TextView secondaryInstruction;
    private Button geotagButton;

    private ImageView phoneControlDots;
    private ImageView controlCopterDots;

    private int currState = STATE_DISCONNECTED;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_get_camera_logs, container, false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ARG_STATE, currState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState != null) {
            currState = savedInstanceState.getInt(ARG_STATE, STATE_DISCONNECTED);
        }

        instructionText = (TextView) view.findViewById(R.id.instruction_text);
        secondaryInstruction = (TextView) view.findViewById(R.id.secondary_instruction_text);
        geotagButton = (Button) view.findViewById(R.id.geotag_button);

        phoneControlDots = (ImageView) view.findViewById(R.id.phone_control_dots);
        controlCopterDots = (ImageView) view.findViewById(R.id.control_copter_dots);

        updateState(currState);
        geotagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (currState) {
                    case STATE_DISCONNECTED:
                        Drone drone = getDrone();
                        if (drone != null) {
                            connectToDrone();
                        }
                        break;
                    case STATE_CONNECTED_NOT_STARTED:
                        updateState(STATE_LOADING_LOGS);
                        startLoadingLogs();
                        break;

                }
            }
        });

        if (getDrone().isConnected()) {
            updateState(STATE_CONNECTED_NOT_STARTED);
        }

        if (activity != null) {
            activity.updateTitle(R.string.transfer_photo_label);
        }
    }

    @Override
    public void onApiConnected() {
        getBroadcastManager().registerReceiver(receiver, filter);
    }

    @Override
    public void onApiDisconnected() {
        getBroadcastManager().unregisterReceiver(receiver);
    }

    private void updateState(int state) {
        currState = state;
        switch (currState) {
            case STATE_DISCONNECTED:
                instructionText.setText(R.string.device_powered_on_message);
                geotagButton.setText(R.string.menu_connect);
                secondaryInstruction.setVisibility(View.INVISIBLE);
                geotagButton.setActivated(false);
                disconnectedUI();
                break;
            case STATE_CONNECTED_NOT_STARTED:
                instructionText.setText(R.string.ready_to_transfer_message);
                geotagButton.setText(R.string.label_begin);
                secondaryInstruction.setVisibility(View.VISIBLE);
                geotagButton.setActivated(false);
                connectedNotStartedUI();
                break;
            case STATE_LOADING_LOGS:
                startAnimation();
                instructionText.setText(R.string.transferring_data);
                geotagButton.setText(R.string.button_setup_cancel);
                secondaryInstruction.setVisibility(View.VISIBLE);
                geotagButton.setActivated(true);
                break;
            case STATE_DONE_LOGS:
                if (activity != null) {
                    activity.finishedLoadingLogs();
                }
                break;
        }
    }

    private void startLoadingLogs() {
        Context context = getContext();
        Intent intent = new Intent(context, GeoTagImagesService.class);
        intent.setAction(GeoTagImagesService.ACTION_START_LOADING_LOGS);
        getContext().startService(intent);
    }

    private void disconnectedUI() {
        phoneControlDots.setImageResource(R.drawable.red_loading_dots);
        controlCopterDots.setImageResource(R.drawable.red_loading_dots);
    }

    private void connectedNotStartedUI() {
        phoneControlDots.setImageResource(R.drawable.blue_loading_dots1);
        controlCopterDots.setImageResource(R.drawable.blue_loading_dots1);
    }

    private void stopAnimation() {
        AnimationDrawable phoneControlDotsAnimation = (AnimationDrawable) phoneControlDots.getDrawable();
        AnimationDrawable controlCopterDotsAnimation = (AnimationDrawable) controlCopterDots.getDrawable();

        if (phoneControlDotsAnimation.isRunning()) {
            phoneControlDotsAnimation.stop();
        }

        if (controlCopterDotsAnimation.isRunning()) {
            controlCopterDotsAnimation.stop();
        }
    }

    private void startAnimation() {
        phoneControlDots.setImageResource(R.drawable.blue_loading_dots);
        controlCopterDots.setImageResource(R.drawable.blue_loading_dots);

        AnimationDrawable phoneControlDotsAnimation = (AnimationDrawable) phoneControlDots.getDrawable();
        AnimationDrawable controlCopterDotsAnimation = (AnimationDrawable) controlCopterDots.getDrawable();

        phoneControlDotsAnimation.start();
        controlCopterDotsAnimation.start();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (!(activity instanceof GeoTagActivity)) {
            throw new UnsupportedOperationException("Activity is not instance of GeoTagActivity");
        }

        this.activity = (GeoTagActivity) activity;
    }

    @Override
    public void onStart() {
        super.onStart();
        getBroadcastManager().registerReceiver(receiver, filter);
    }

    @Override
    public void onStop() {
        super.onStop();
        getBroadcastManager().unregisterReceiver(receiver);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }
}