package org.droidplanner.android.fragments.geotag;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
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
import org.droidplanner.android.maps.providers.google_map.GoogleMapPrefFragment;
import org.droidplanner.android.utils.NetworkUtils;
import org.droidplanner.android.utils.connection.DroneKitCloudClient;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.swagger.client.ApiException;
import io.swagger.client.model.CreateMission;
import io.swagger.client.model.CreateUser;
import io.swagger.client.model.CreateVehicle;
import io.swagger.client.model.ItemId;
import io.swagger.client.model.LoginPassword;
import io.swagger.client.model.Media;
import io.swagger.client.model.Mission;
import io.swagger.client.model.Params;
import io.swagger.client.model.Recap;
import io.swagger.client.model.RecapAuth;
import io.swagger.client.model.RecapResult;
import io.swagger.client.model.Token;
import io.swagger.client.model.User;
import io.swagger.client.model.Vehicle;
import timber.log.Timber;

/**
 * Created by chavi on 10/20/15.
 */
public class UploadImagesFragment extends Fragment {
    private DroneKitCloudClient defaultApi = new DroneKitCloudClient();
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Handler handler = new Handler();

    private static final String API_KEY = "key";
    private static final String testUserName = "IG Test";
    private static final String testEmail = "Ig3DRTest2@gmail.com";
    private static final String testPassword = "IgTestIgTest";
    private static final String testCopter = "testCopter";

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

    private Collection<File> geotaggedFiles;

    private Token token;
    private Mission mission;

    private AlertDialog alertDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_upload_images, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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
                        updateState(STATE_INIT);
                        break;
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (currState == STATE_INIT || currState == STATE_NO_NETWORK) {
            if (!NetworkUtils.isNetworkAvailable(getContext())) {
                updateState(STATE_NO_NETWORK);
            } else {
                updateState(STATE_INIT);
            }
        }
    }

    public void setFileList(Collection<File> geotaggedFiles) {
        this.geotaggedFiles = geotaggedFiles;
        if (progressBar != null) {
            progressBar.setMax(geotaggedFiles.size());
        }
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
                break;
            case STATE_STARTED:
                startButton.setActivated(true);
                startButton.setText(R.string.button_setup_cancel);
                instructionText.setText(R.string.uploading_images);
                break;
        }
    }

    private void startUpload() {
        executor.execute(new Runnable() {
            @Override
            public void run() {

                if (!createUser()) {
                    return;
                }

                token = login();
                if (token == null) {
                    return;
                }

//                logoutRecap();

                Vehicle vehicle = getVehicle();
                if (vehicle == null) {
                    vehicle = createVehicle();
                }

                if (vehicle == null) {
                    return;
                }

                String missionName = "IgMission-" + new Date().toString();
                mission = createMission(missionName, vehicle.getId());

                if (mission == null) {
                    return;
                }

                updateText("Mission created with id: " + mission.getId());

                final int numPhotos = uploadImages(mission.getId(), token.getToken());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        doneLoading(numPhotos);
                    }
                });

                RecapAuth recapAuth = getRecapUrl();

                if (recapAuth == null) {
                    return;
                }

                if (recapAuth.getNeedToAuth()) {
                    startRecapWebview(handler, recapAuth.getUrl());
                } else {
                    createRecapJob();
                }
            }

        });
    }

    private boolean createUser() {
        CreateUser createUser = new CreateUser();
        createUser.setEmail(testEmail);
        createUser.setPassword(testPassword);
        createUser.setUsername(testUserName);

        try {
            defaultApi.createUser(API_KEY, createUser);
            return true;
        } catch (ApiException e) {
            Timber.e("Failed to create user " + e + " code: " + e.getCode());
            if (e.getCode() == 400) {
                return true;
            } else {
                return false;
            }
        }
    }

    private Token login() {
        updateText("Logging in");
        LoginPassword loginPassword = new LoginPassword();
        loginPassword.setEmail(testEmail);
        loginPassword.setPassword(testPassword);

        Token token = null;
        try {
            token = defaultApi.login(API_KEY, loginPassword);
        } catch (ApiException e) {
            Timber.e("Failed to login " + e + " code: " + e.getCode());
        }

        return token;
    }

    private Mission createMission(String missionName, String vehicleId) {
        updateText("Creating new mission: " + missionName);

        Mission mission = null;
        try {
            CreateMission createMission = new CreateMission();
            createMission.setName(missionName);
            createMission.setVehicleId(vehicleId);

            mission = defaultApi.missionsPost(API_KEY, token.getToken(), createMission);
            Timber.d("mission info: name %s id %s vehicle %s", mission.getName(), mission.getId(), mission.getVehicleId());
        } catch (ApiException e) {
            Timber.e("Failed to create mission " + e + " code: " + e.getCode());
        }
        return mission;
    }

    private Vehicle getVehicle() {
        updateText("Getting vehicle");

        try {
            List<Vehicle> vehicle = defaultApi.usersUserIdVehiclesGet(token.getUserId(), API_KEY, token.getToken());
            if (vehicle.size() > 0) {
                return vehicle.get(0);
            }
        } catch (ApiException e) {
            Timber.e("Failed to get vehicle " + e + " code: " + e.getCode());
        }
        return null;
    }

    private Vehicle createVehicle() {
        updateText("Creating vehicle");

        CreateVehicle createVehicle = new CreateVehicle();
        createVehicle.setName(testCopter);

        try {
            return defaultApi.vehiclesPost(API_KEY, token.getToken(), createVehicle);
        } catch (ApiException e) {
            Timber.e("Failed to create vehicle " + e + " code: " + e.getCode());
        }
        return null;
    }

    private int uploadImages(String missionId, String token) {
        if (geotaggedFiles == null) {
            return 0;
        }

        int numPhotos = 0;
        for (File image : geotaggedFiles) {
            if (uploadImage(missionId, token, image)) {
                numPhotos++;
            }
            uploadProgress(numPhotos);
        }
        return numPhotos;
    }

    private boolean uploadImage(String missionId, String token, File image) {
        Timber.d("file: " + image);
        try {
            Media media = defaultApi.missionsMissionIdMediaPost(missionId, API_KEY, token, image, "image/*");
            if (media != null) {
                Timber.d("media: id %s mission %s s3 %s ", media.getId(), media.getMissionId(), media.getS3url());
                return true;
            }
        } catch (ApiException e) {
            Timber.e("Failed to add media " + e + " code: " + e.getCode());
        }
        return false;
    }

    private void doneLoading(int numPhotos) {
        if (numPhotos > 0) {
            instructionText.setText(String.format("Successfully uploaded %s photos", numPhotos));
        } else {
            instructionText.setText("Failed to upload photos");
        }
    }

    private RecapAuth getRecapUrl() {
        try {
            return defaultApi.actionsRecapAuthPost(API_KEY, token.getToken(), null);
        } catch (ApiException e) {
            Timber.e("Failed to login to recap " + e + " code: " + e.getCode());
        }
        return null;
    }

    private void startRecapWebview(Handler handler, final String url) {
        handler.post(new Runnable() {
            @Override
            public void run() {
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
        });
    }

    private void finishedRecapLogin(boolean success) {
        if (success) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    createRecapJob();
                }
            });
        }
    }

    private void createRecapJob() {
        RecapResult recapResult = startRecapJob();
        if (recapResult == null) {
            return;
        }

        updateText("Recap job created: " + recapResult.getId());
        GoogleMapPrefFragment.PrefManager.setLastMission(getContext(), recapResult.getId());
        Timber.d("Recap result: " + recapResult.getId());
    }

    private RecapResult startRecapJob() {
        updateText("Creating recap job");

        Params params = new Params();
        params.setFormat("obj");
        params.setQuality("9");
        params.setService("recap");

        ArrayList<ItemId> missionList = new ArrayList<>(1);
        ItemId itemId = new ItemId();
        itemId.setId(mission.getId());
        itemId.setKind("mission");
        missionList.add(itemId);

        Recap recap = new Recap();
        recap.setParams(params);
        recap.setItemIds(missionList);

        RecapResult recapResult = null;
        try {
            recapResult = defaultApi.actionsRecapPost(API_KEY, token.getToken(), recap);
        } catch (ApiException e) {
            Timber.e("Failed to start recap job " + e  + " code: " + e.getCode());
            updateText("Failed to start recap job " + e  + " code: " + e.getCode());
        }

        return recapResult;
    }

    private void updateText(final String text) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                descriptionText.setText(text);
            }
        });
    }

    private void uploadProgress(final int progress) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setProgress(progress);
            }
        });
    }

    private void logoutRecap() {
        try {
            User user = defaultApi.actionsRecapAuthLogoutPost(API_KEY, token.getToken());
            Timber.d("logged out of recap " + user.getEmail());
        } catch (ApiException e) {
            Timber.e("Failed to logout " + e + " code: " + e.getCode());
        }
    }
}
