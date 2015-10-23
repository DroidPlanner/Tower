package org.droidplanner.android.fragments.geotag;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;

import org.droidplanner.android.BuildConfig;
import org.droidplanner.android.R;
import org.droidplanner.android.activities.GeoTagActivity;
import org.droidplanner.android.fragments.helpers.ApiListenerFragment;
import org.droidplanner.android.utils.connection.SshConnection;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

import timber.log.Timber;

/**
 * Created by chavi on 10/15/15.
 */
public class GetCameraLogsFragment extends ApiListenerFragment {
    private static final String SSH_USERNAME = "root";
    private static final String SSH_PASSWORD = "TjSDBkAu";
    private static final SshConnection soloSshLink = new SshConnection(BuildConfig.SOLO_LINK_IP, SSH_USERNAME, SSH_PASSWORD);
    private static final String CAMERA_TLOG_FILE = "/usr/bin/camera_msgs.tlog";//"/log/camera_msgs.tlog";

    private static final int NUMBER_OF_RETRIES = 3;

    private static final int STATE_INIT = -1;
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTED_NOT_STARTED = 1;
    private static final int STATE_LOADING_LOGS = 2;
    private static final int STATE_DONE_LOGS = 3;

    private final static IntentFilter filter = new IntentFilter();

    static {
        filter.addAction(AttributeEvent.STATE_CONNECTED);
        filter.addAction(AttributeEvent.STATE_DISCONNECTED);
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
            }
        }
    };

    private AsyncTask asyncTask;
    private GeoTagActivity activity;

    private TextView instructionText;
    private TextView secondaryInstruction;
    private Button geotagButton;

    private ImageView phoneControlDots;
    private ImageView controlCopterDots;

    private int currState = STATE_INIT;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_get_camera_logs, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        instructionText = (TextView) view.findViewById(R.id.instruction_text);
        secondaryInstruction = (TextView) view.findViewById(R.id.secondary_instruction_text);
        geotagButton = (Button) view.findViewById(R.id.geotag_button);

        phoneControlDots = (ImageView) view.findViewById(R.id.phone_control_dots);
        controlCopterDots = (ImageView) view.findViewById(R.id.control_copter_dots);

        updateState(STATE_DISCONNECTED);
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
        if (currState == state) {
            return;
        }

        currState = state;
        switch (currState) {
            case STATE_DISCONNECTED:
                instructionText.setText(R.string.device_powered_on_message);
                geotagButton.setText(R.string.menu_connect);
                secondaryInstruction.setVisibility(View.INVISIBLE);
                geotagButton.setActivated(false);
                disconnectedUI();
                //stopAnimation();
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
                activity.finishedLoadingLogs();
                break;
        }
    }

    private void startLoadingLogs() {
        if (asyncTask != null) {
            asyncTask.cancel(true);
        }
        asyncTask = new DownloadCameraTlogs(getContext());
        asyncTask.execute();
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

    private class DownloadCameraTlogs extends AsyncTask<Object, Integer, Boolean> {

        private WeakReference<Context> weakContext;

        private DownloadCameraTlogs(Context context) {
            this.weakContext = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Object... params) {
            Timber.d("Downloading the logs from Artoo");

            try {

                Context context = weakContext.get();
                if (context != null) {
                    File folder = context.getExternalFilesDir(null);
                    if (folder == null) {
                        return false;
                    }

                    final boolean isSoloDownloadSuccessful = downloadFileFromDevice
                        (CAMERA_TLOG_FILE, folder, soloSshLink, NUMBER_OF_RETRIES);

                    return isSoloDownloadSuccessful;

                }
            } catch (IOException e) {
                Timber.e("Unable to download the file to the phone.", e);
            } catch (NullPointerException e) {
                Timber.e("Unable to create file path. ", e);
            }
            return false;
        }

        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
        }

        /**
         * After completing background task Dismiss the progress dialog
         */
        protected void onPostExecute(Boolean result) {
            if (result) {
                updateState(STATE_DONE_LOGS);
            }
        }

        /**
         * Download a file from from the given source, to the specified destination.
         *
         * @param file        File to download.
         * @param destination Folder destination.
         * @return true if successful.
         * @throws IOException
         */
        private boolean downloadFileFromDevice(String file, File destination, SshConnection connection, int numberOfRetries) throws IOException {
            boolean gotOne = false;
            try {
                //Try to download the file
                for (int retries = 0; retries < numberOfRetries; retries++) {
                    final boolean downloadResult = connection.downloadFile(destination.getAbsolutePath(), file);
                    gotOne = gotOne || downloadResult;
                    if (!downloadResult) {
                        Timber.w("Unable to download file " + file + " to the phone. Trying again");
                        //Delete corrupted file
                        final File corruptedFile = new File(destination, file);
                        corruptedFile.delete();
                    } else {
                        //File downloaded
                        break;
                    }
                }
            } catch (IOException e) {
                Timber.w("Unable to download file", e);
            }

            Timber.i("Logs were downloaded to the device");
            return gotOne;

        }
    }
}