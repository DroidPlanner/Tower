package org.droidplanner.android.fragments.geotag;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.okhttp.Headers;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.droidplanner.android.R;
import org.droidplanner.android.activities.GeoTagActivity;
import org.droidplanner.android.maps.providers.google_map.GoogleMapPrefFragment;
import org.droidplanner.android.utils.NetworkUtils;
import org.droidplanner.android.utils.connection.DroneKitCloudClient;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import timber.log.Timber;

/**
 * Created by chavi on 10/20/15.
 */
public class UploadImagesFragment extends Fragment {
    private DroneKitCloudClient defaultApi = new DroneKitCloudClient();

    private static final String API_KEY = "key";

    private static final String recapLogin = "mckinnon@3dr.com";
    private static final String recapPass = "Wh1tetape";

    private static final int STATE_NO_NETWORK = -1;
    private static final int STATE_INIT = 0;
    private static final int STATE_STARTED = 1;

    private int currState = STATE_INIT;

    private Button startButton;
    private TextView instructionText;
    private TextView descriptionText;
    private ProgressBar progressBar;

    private ArrayList<File> geotaggedFiles;

    private AlertDialog alertDialog;
    private LocalBroadcastManager lbm;
    private GeoTagActivity activity;

    private static final IntentFilter filter = new IntentFilter();

    static {
        filter.addAction(GeoTagImagesService.STATE_FINISHED_CLOUD_UPLOADING);
        filter.addAction(GeoTagImagesService.STATE_DRONEKIT_LOGGED_IN);
        filter.addAction(GeoTagImagesService.STATE_VEHICLE_CHOSEN);
        filter.addAction(GeoTagImagesService.STATE_MISSION_CREATED);
        filter.addAction(GeoTagImagesService.STATE_IMAGES_UPLOADING);
        filter.addAction(GeoTagImagesService.STATE_IMAGES_UPLOADED);
        filter.addAction(GeoTagImagesService.STATE_RECAP_LOGIN);
        filter.addAction(GeoTagImagesService.STATE_RECAP_JOB_CREATED);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case GeoTagImagesService.STATE_FINISHED_CLOUD_UPLOADING:
                    finishedUploading(intent);
                    break;
                case GeoTagImagesService.STATE_DRONEKIT_LOGGED_IN:
                    String token = intent.getStringExtra(GeoTagImagesService.EXTRA_TOKEN);
                    GoogleMapPrefFragment.PrefManager.setDroneKitToken(getContext(), token);
                    updateText("User logged in");
                    break;
                case GeoTagImagesService.STATE_VEHICLE_CHOSEN:
                    updateText("Vehicle selected");
                    break;
                case GeoTagImagesService.STATE_MISSION_CREATED:
                    String missionName = intent.getStringExtra(GeoTagImagesService.EXTRA_MISSION_NAME);
                    updateText("Created mission: " + missionName);
                    break;
                case GeoTagImagesService.STATE_IMAGES_UPLOADING:
                    int numUploaded = intent.getIntExtra(GeoTagImagesService.EXTRA_PROGRESS, 0);
                    progressBar.setProgress(numUploaded);
                    break;
                case GeoTagImagesService.STATE_IMAGES_UPLOADED:
                    int numPhotos = intent.getIntExtra(GeoTagImagesService.EXTRA_TOTAL, 0);
                    if (numPhotos > 0) {
                        instructionText.setText(String.format("Successfully uploaded %s photos", numPhotos));
                    } else {
                        instructionText.setText("Failed to upload photos");
                    }
                    break;
                case GeoTagImagesService.STATE_RECAP_LOGIN:
                    String recapUrl = intent.getStringExtra(GeoTagImagesService.EXTRA_URL);
                    startRecapWebview(recapUrl);
                    break;
                case GeoTagImagesService.STATE_RECAP_JOB_CREATED:
                    String recapId = intent.getStringExtra(GeoTagImagesService.EXTRA_RECAP_ID);
                    updateText("Recap job created: " + recapId);
                    Timber.d("Recap result: " + recapId);
                    GoogleMapPrefFragment.PrefManager.setLastRecapId(getContext(), recapId);
                    break;
            }
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (!(context instanceof GeoTagActivity)) {
            throw new UnsupportedOperationException("Activity is not instance of GeoTagActivity");
        }

        activity = (GeoTagActivity) context;
        lbm = LocalBroadcastManager.getInstance(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_upload_images, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        geotaggedFiles = (ArrayList<File>) getArguments().getSerializable(GeoTagImagesService.EXTRA_GEOTAGGED_FILES);

        startButton = (Button) view.findViewById(R.id.upload_button);
        instructionText = (TextView) view.findViewById(R.id.instruction_text);
        descriptionText = (TextView) view.findViewById(R.id.description_text);
        progressBar = (ProgressBar) view.findViewById(R.id.upload_progress);
        if (geotaggedFiles != null) {
            progressBar.setMax(geotaggedFiles.size());
        }

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (currState) {
                    case STATE_NO_NETWORK:
                        startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
                        break;
                    case STATE_INIT:
                        updateState(STATE_STARTED);
                        startUpload();
                        break;
                    case STATE_STARTED:
                        cancelUpload();
                        updateState(STATE_INIT);
                        break;
                }
            }
        });

        if (activity != null) {
            activity.updateTitle(R.string.upload_images);
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        lbm.registerReceiver(receiver, filter);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (NetworkUtils.isNetworkAvailable(getContext()) && !NetworkUtils.isOnSoloNetwork(getContext())) {
            updateState(STATE_INIT);
        } else {
            updateState(STATE_NO_NETWORK);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        lbm.unregisterReceiver(receiver);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    private void updateState(int state) {
        currState = state;

        switch (currState) {
            case STATE_NO_NETWORK:
                startButton.setText(R.string.menu_connect);
                instructionText.setText("Connect to a Network");
                break;
            case STATE_INIT:
                startButton.setText(R.string.label_begin);
                instructionText.setText(R.string.upload_images);
                progressBar.setProgress(0);
                break;
            case STATE_STARTED:
                startButton.setActivated(true);
                startButton.setText(R.string.button_setup_cancel);
                instructionText.setText(R.string.uploading_images);
                break;
        }
    }

    private void startUpload() {
        Context context = getContext();
        Intent intent = new Intent(context, GeoTagImagesService.class);
        intent.setAction(GeoTagImagesService.ACTION_START_CLOUD_UPLOAD);
        intent.putExtra(GeoTagImagesService.EXTRA_GEOTAGGED_FILES, geotaggedFiles);
        getContext().startService(intent);
    }

    private void cancelUpload() {
        Context context = getContext();
        Intent intent = new Intent(context, GeoTagImagesService.class);
        intent.setAction(GeoTagImagesService.ACTION_CANCEL_CLOUD_UPLOAD);
        intent.putExtra(GeoTagImagesService.EXTRA_GEOTAGGED_FILES, geotaggedFiles);
        getContext().startService(intent);
    }


    private void startRecapWebview(final String url) {
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());

        WebView webview = new WebView(getActivity());
        webview.loadUrl(url);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                Timber.d("loadUrl: " + url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                view.loadUrl("javascript:(function(){document.getElementById('userName').value='" + recapLogin + "';})()");
                view.loadUrl("javascript:(function(){document.getElementById('password').value='" + recapPass + "';})()");
                view.loadUrl("javascript:(function(){document.getElementById('btnSubmit').click();})()");
            }

            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest webResourceRequest) {
                if (webResourceRequest.getUrl().toString().contains("dronekitcloud")) {

                    Headers.Builder headersBuilder = new Headers.Builder();
                    for (Map.Entry<String, String> header : webResourceRequest.getRequestHeaders().entrySet()) {
                        headersBuilder.add(header.getKey(), header.getValue());
                    }

                    Request okRequest = new Request.Builder()
                        .url(webResourceRequest.getUrl().toString())
                        .headers(headersBuilder.build())
                        .build();
                    OkHttpClient client = new OkHttpClient();
                    try {
                        Response okResponse = client.newCall(okRequest).execute();
                        int responseCode = okResponse.code();
                        boolean success = responseCode >= 200 && responseCode < 300;
                        finishedRecapLogin(success);

                        Timber.d("response: " + responseCode);
                    } catch (IOException e) {
                        Timber.e("Failed to ping server");
                        finishedRecapLogin(false);
                    }
                    alertDialog.cancel();
                }

                return super.shouldInterceptRequest(view, webResourceRequest);

            }
        });

        alert.setView(webview);
        alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {

            }
        });

        alertDialog = alert.create();

        alertDialog.show();

    }

    private void finishedRecapLogin(boolean success) {
        if (success) {
            Context context = getContext();
            Intent intent = new Intent(context, GeoTagImagesService.class);
            intent.setAction(GeoTagImagesService.ACTION_START_RECAP_JOB);
            getContext().startService(intent);
        } else {
            failedLoading("Failed to login to recap");
        }
    }

    private void updateText(final String text) {
        descriptionText.setText(text);
    }

//    private void logoutRecap() {
//        try {
//            User user = defaultApi.actionsRecapAuthLogoutPost(API_KEY, token.getToken());
//            Timber.d("logged out of recap " + user.getEmail());
//        } catch (ApiException e) {
//            Timber.e("Failed to logout " + e + " code: " + e.getCode());
//        }
//    }

    private void finishedUploading(Intent intent) {
        boolean success = intent.getBooleanExtra(GeoTagImagesService.EXTRA_SUCCESS, false);
        if (success) {

//            files = (ArrayList<File>) intent.getSerializableExtra(GeoTagImagesService.EXTRA_GEOTAGGED_FILES);
//            updateState(STATE_DONE_GEOTAGGING);
//            instructionText.setText(String.format(getString(R.string.photos_geotagged), files.size()));
        } else {
            String failure = intent.getStringExtra(GeoTagImagesService.EXTRA_FAILURE_MESSAGE);
            failedLoading(failure);
        }
    }

    private void failedLoading(String message) {
        updateState(STATE_INIT);
        descriptionText.setText(String.format(getString(R.string.failed_geotag), message));
    }
}
