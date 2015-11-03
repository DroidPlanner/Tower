package org.droidplanner.android.fragments.geotag;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.o3dr.android.client.utils.data.tlog.TLogParser;
import com.o3dr.android.client.utils.data.tlog.TLogParserCallback;
import com.o3dr.android.client.utils.data.tlog.TLogParserFilter;
import com.o3dr.android.client.utils.geotag.GeoTagAsyncTask;

import org.droidplanner.android.BuildConfig;
import org.droidplanner.android.utils.Utils;
import org.droidplanner.android.utils.connection.DroneKitCloudClient;
import org.droidplanner.android.utils.connection.SshConnection;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
 * Created by chavi on 10/19/15.
 */
public class GeoTagImagesService extends Service {
    private DroneKitCloudClient defaultApi = new DroneKitCloudClient();
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private static final String API_KEY = "key";
    private static final String testUserName = "IG Test";
    private static final String testEmail = "Ig3DRTest2@gmail.com";
    private static final String testPassword = "IgTestIgTest";
    private static final String testCopter = "testCopter";

    private static final String SSH_USERNAME = "root";
    private static final String SSH_PASSWORD = "TjSDBkAu";
    private static final SshConnection soloSshLink = new SshConnection(BuildConfig.SOLO_LINK_IP, SSH_USERNAME, SSH_PASSWORD);
    private static final String CAMERA_TLOG_FILE = "/log/camera_msgs.tlog";
    private static final int NUMBER_OF_RETRIES = 3;
    private static final int UPLOAD_POOL_SIZE = 10;

    private static final String PACKAGE_NAME = Utils.PACKAGE_NAME;

    public static final String ACTION_START_LOADING_LOGS = PACKAGE_NAME + ".action.START_LOADING_LOGS";
    public static final String ACTION_CANCEL_LOADING_LOGS = PACKAGE_NAME + ".action.CANCEL_LOADING_LOGS";
    public static final String ACTION_START_GEOTAGGING = PACKAGE_NAME + ".action.START_GEOTAGGING";
    public static final String ACTION_CANCEL_GEOTAGGING = PACKAGE_NAME + ".action.CANCEL_GEOTAGGING";
    public static final String ACTION_START_CLOUD_UPLOAD = PACKAGE_NAME + ".action.START_CLOUD_UPLOAD";
    public static final String ACTION_CANCEL_CLOUD_UPLOAD = PACKAGE_NAME + ".action.CANCEL_CLOUD_UPLOAD";
    public static final String ACTION_START_RECAP_JOB = PACKAGE_NAME + ".action.START_RECAP_JOB";

    public static final String STATE_FINISHED_LOADING_LOGS = PACKAGE_NAME + ".FINISHED_LOADING_LOGS";

    public static final String STATE_FINISHED_GEOTAGGING = PACKAGE_NAME + ".FINISHED_GEOTAGGING";
    public static final String STATE_PROGRESS_UPDATE_GEOTAGGING = PACKAGE_NAME + ".PROGRESS_UPDATE_GEOTAGGING";

    public static final String STATE_FINISHED_CLOUD_UPLOADING = PACKAGE_NAME + ".FINISHED_CLOUD_UPLOADING";
    public static final String STATE_DRONEKIT_LOGGED_IN = PACKAGE_NAME + ".DRONEKIT_LOGGED_IN";
    public static final String STATE_VEHICLE_CHOSEN = PACKAGE_NAME + ".VEHICLE_CHOSEN";
    public static final String STATE_MISSION_CREATED = PACKAGE_NAME + ".STATE_MISSION_CREATED";
    public static final String STATE_IMAGES_UPLOADING = PACKAGE_NAME + ".STATE_IMAGES_UPLOADING";
    public static final String STATE_IMAGES_UPLOADED = PACKAGE_NAME + ".STATE_IMAGES_UPLOADED";
    public static final String STATE_RECAP_LOGIN = PACKAGE_NAME + ".STATE_RECAP_LOGIN";
    public static final String STATE_RECAP_JOB_CREATED = PACKAGE_NAME + ".STATE_RECAP_JOB_CREATED";


    public static final String EXTRA_SUCCESS = PACKAGE_NAME + ".EXTRA_SUCCESS";
    public static final String EXTRA_FAILURE_MESSAGE = PACKAGE_NAME + ".EXTRA_FAILURE_MESSAGE";
    public static final String EXTRA_PROGRESS = PACKAGE_NAME + ".EXTRA_PROGRESS";
    public static final String EXTRA_TOTAL = PACKAGE_NAME + ".EXTRA_TOTAL";
    public static final String EXTRA_GEOTAGGED_FILES = PACKAGE_NAME + ".EXTRA_GEOTAGGED_FILES";
    public static final String EXTRA_MISSION_NAME = PACKAGE_NAME + ".EXTRA_MISSION_NAME";
    public static final String EXTRA_URL = PACKAGE_NAME + ".EXTRA_URL";
    public static final String EXTRA_RECAP_ID = PACKAGE_NAME + ".EXTRA_RECAP_ID";
    public static final String EXTRA_TOKEN = PACKAGE_NAME + ".EXTRA_TOKEN";

    private AsyncTask asyncTask;
    private GeoTagTask geoTagTask;
    private LocalBroadcastManager lbm;
    private Token token;
    private Mission mission;

    Integer numPhotos = 0;

    @Override
    public void onCreate() {
        super.onCreate();

        lbm = LocalBroadcastManager.getInstance(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return super.onStartCommand(intent, flags, startId);
        }

        switch (intent.getAction()) {
            case ACTION_START_LOADING_LOGS:
                startLoadingLogs();
                break;
            case ACTION_CANCEL_LOADING_LOGS:
                cancelLoadingLogs();
                break;
            case ACTION_START_GEOTAGGING:
                startGeoTagging();
                break;
            case ACTION_CANCEL_GEOTAGGING:
                cancelGeoTagging();
                break;
            case ACTION_START_CLOUD_UPLOAD:
                ArrayList<File> files = (ArrayList<File>) intent.getSerializableExtra(EXTRA_GEOTAGGED_FILES);
                startCloudUpload(files);
                break;
            case ACTION_START_RECAP_JOB:
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        createRecapJobWithRetries();
                    }
                });
                break;
            case ACTION_CANCEL_CLOUD_UPLOAD:
                if (executor != null) {
                    executor.shutdownNow();
                }
                break;

        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startLoadingLogs() {
        if (asyncTask != null) {
            asyncTask.cancel(true);
        }
        asyncTask = new DownloadCameraTlogs(getApplicationContext());
        asyncTask.execute();
    }

    private void cancelLoadingLogs() {

    }

    private void startGeoTagging() {
        File folder = getApplicationContext().getExternalFilesDir(null);
        File tlogFile = new File(folder.getPath() + "/camera_msgs.tlog");
        Timber.d("path: " + tlogFile.getPath());

        geoTagImages(getApplicationContext(), tlogFile);
    }

    private void cancelGeoTagging() {
        if (geoTagTask != null) {
            geoTagTask.cancel(true);
            geoTagTask = null;
        }
    }

    private void geoTagImages(final Context context, File tlogFile) {
        final String extMount = getExternalStorage(context);
        if (extMount == null) {
            return;
        }

        final ArrayList<File> photoFiles = new ArrayList<>();

        List<File> photos = searchDir(extMount);
        if (photos != null) {
            photoFiles.addAll(photos);
        }

        if (photoFiles.size() == 0) {
            sendFailedGeotaggingIntent("No photos on SD card for GoPro device.");
            return;
        }

        Uri uri = Uri.fromFile(tlogFile);
        Handler handler = new Handler();
        TLogParser.getAllEventsAsync(handler, uri, new TLogParserFilter() {

            @Override
            public boolean includeEvent(TLogParser.Event event) {
                return 180 == event.getMavLinkMessage().msgid;
            }

            @Override
            public boolean shouldIterate() {
                return true;
            }


        }, new TLogParserCallback() {
            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void onResult(List<TLogParser.Event> eventList) {
                if (eventList.size() < 0) {
                    sendFailedGeotaggingIntent("No camera message events found");
                    return;
                }

                if (geoTagTask != null) {
                    geoTagTask.cancel(true);
                }
                geoTagTask = new GeoTagTask(context, eventList, photoFiles);
                geoTagTask.execute();
            }

            @Override
            public void onFailed(Exception e) {
                if (e instanceof NoSuchElementException) {
                    sendFailedGeotaggingIntent("No camera message events found");
                } else {
                    sendFailedGeotaggingIntent(e.getMessage());
                }
            }
        });

    }

    private String getExternalStorage(Context context) {
        boolean hasNullFile = false;

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            File[] files = context.getExternalFilesDirs(null);
            for (File extFile : files) {
                if (extFile == null) {
                    hasNullFile = true;
                } else if (Environment.isExternalStorageRemovable(extFile)) {
                    return findRootPath(extFile);
                }
            }
        }
        if (hasNullFile) {
            sendFailedGeotaggingIntent("No external storage device found.");
        } else {
            sendFailedGeotaggingIntent("Incompatible device. No external SD card reader found.");
        }
        return null;
    }

    private static String findRootPath(File extFile) {
        File currPath = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            currPath = extFile;
            try {
                while (Environment.isExternalStorageRemovable(currPath.getParentFile())) {
                    currPath = currPath.getParentFile();
                }
            } catch (IllegalArgumentException e) {
                //swallow
                return currPath.getAbsolutePath();
            }
        }
        return currPath.getAbsolutePath();
    }

    private static List<File> searchDir(String mount) {
        File photoDir = new File(mount + "/DCIM");
        File[] goProDirs = photoDir.listFiles();
        if (goProDirs == null || goProDirs.length == 0) {
            return null;
        }

        List<File> photoFiles = new ArrayList<>();

        for (File picDir : goProDirs) {
            if (picDir.getName().toLowerCase().contains("gopro")) {
                photoFiles.addAll(Arrays.asList(picDir.listFiles()));
            }
        }

        return photoFiles;
    }

    private void sendFailedGeotaggingIntent(String failure) {
        Intent intent = new Intent(STATE_FINISHED_GEOTAGGING);
        intent.putExtra(EXTRA_SUCCESS, false);
        intent.putExtra(EXTRA_FAILURE_MESSAGE, failure);
        lbm.sendBroadcast(intent);
    }

    private void sendFailedCloudUploadIntent(String failure) {
        Intent intent = new Intent(STATE_FINISHED_CLOUD_UPLOADING);
        intent.putExtra(EXTRA_SUCCESS, false);
        intent.putExtra(EXTRA_FAILURE_MESSAGE, failure);
        lbm.sendBroadcast(intent);
    }

    private void sendUpdates(String action, Bundle extras) {
        Intent intent = new Intent(action);
        if (extras != null) {
            intent.putExtras(extras);
        }
        lbm.sendBroadcast(intent);
    }

    private void startCloudUpload(final ArrayList<File> files) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (executor.isShutdown()) {
                    return;
                }

                if (!createUserWithRetries()) {
                    return;
                }

                if (executor.isShutdown()) {
                    return;
                }

                token = loginWithRetries();
                if (token == null) {
                    return;
                }

                logoutRecap(token.getToken());

                Bundle bundle = new Bundle();
                bundle.putString(EXTRA_TOKEN, token.getToken());
                sendUpdates(STATE_DRONEKIT_LOGGED_IN, bundle);

                if (executor.isShutdown()) {
                    return;
                }

                Vehicle vehicle = getVehicle();

                if (executor.isShutdown()) {
                    return;
                }

                if (vehicle == null) {
                    vehicle = createVehicleWithRetries();
                }

                if (executor.isShutdown()) {
                    return;
                }
                if (vehicle == null) {
                    sendFailedCloudUploadIntent("Failed to get or create vehicle");
                    return;
                }

                sendUpdates(STATE_VEHICLE_CHOSEN, null);

                String missionName = "IgMission-" + new Date().toString();

                if (executor.isShutdown()) {
                    return;
                }

                mission = createMissionWithRetries(missionName, vehicle.getId());

                if (mission == null) {
                    return;
                }

                bundle = new Bundle();
                bundle.putString(EXTRA_MISSION_NAME, missionName);
                sendUpdates(STATE_MISSION_CREATED, bundle);

                numPhotos = 0;
                uploadImages(mission.getId(), files);

                if (executor.isShutdown()) {
                    return;
                }

                bundle = new Bundle();
                bundle.putInt(EXTRA_TOTAL, numPhotos);
                sendUpdates(STATE_IMAGES_UPLOADED, bundle);

                RecapAuth recapAuth = getRecapUrlWithRetries();

                if (executor.isShutdown()) {
                    return;
                }

                if (recapAuth == null) {
                    return;
                }

                if (executor.isShutdown()) {
                    return;
                }

                if (recapAuth.getNeedToAuth()) {
                    bundle = new Bundle();
                    bundle.putString(EXTRA_URL, recapAuth.getUrl());
                    sendUpdates(STATE_RECAP_LOGIN, bundle);
                } else {
                    createRecapJobWithRetries();
                }

            }
        });
    }

    private boolean createUserWithRetries() {
        StringBuilder builder = new StringBuilder();
        for (int numTries = 0; numTries < NUMBER_OF_RETRIES; numTries++) {
            builder = new StringBuilder();
            if (createUser(builder)) {
                return true;
            }
        }
        sendFailedCloudUploadIntent(builder.toString());
        return false;
    }

    private boolean createUser(StringBuilder message) {
        CreateUser createUser = new CreateUser();
        createUser.setEmail(testEmail);
        createUser.setPassword(testPassword);
        createUser.setUsername(testUserName);

        try {
            defaultApi.createUser(API_KEY, createUser);
            return true;
        } catch (ApiException e) {
            if (e.getCode() == 409) {
                return true;
            } else {
                message.append("Failed to create user " + e + " code: " + e.getCode());
                return false;
            }
        }
    }

    private Token loginWithRetries() {
        StringBuilder message = new StringBuilder();
        for (int numTries = 0; numTries < NUMBER_OF_RETRIES; numTries++) {
            Token token = login(message);
            if (token != null) {
                return token;
            }
        }
        sendFailedCloudUploadIntent(message.toString());
        return null;
    }

    private Token login(StringBuilder message) {
        LoginPassword loginPassword = new LoginPassword();
        loginPassword.setEmail(testEmail);
        loginPassword.setPassword(testPassword);

        Token token = null;
        try {
            token = defaultApi.login(API_KEY, loginPassword);
        } catch (ApiException e) {
            message.append("Failed to login " + e + " code: " + e.getCode());
            Timber.e("Failed to login " + e + " code: " + e.getCode());
        }

        return token;
    }

    private Mission createMissionWithRetries(String missionName, String vehicleId) {
        StringBuilder message = new StringBuilder();
        for (int numTries = 0; numTries < NUMBER_OF_RETRIES; numTries++) {
            message = new StringBuilder();
            Mission mission = createMission(message, missionName, vehicleId);
            if (mission != null) {
                return mission;
            }
        }

        sendFailedCloudUploadIntent(message.toString());
        return null;
    }

    private Mission createMission(StringBuilder message, String missionName, String vehicleId) {
        Mission mission = null;
        try {
            CreateMission createMission = new CreateMission();
            createMission.setName(missionName);
            createMission.setVehicleId(vehicleId);

            mission = defaultApi.missionsPost(API_KEY, token.getToken(), createMission);
            Timber.d("mission info: name %s id %s vehicle %s", mission.getName(), mission.getId(), mission.getVehicleId());
        } catch (ApiException e) {
            message.append("Failed to create mission " + e + " code: " + e.getCode());
            Timber.e("Failed to create mission " + e + " code: " + e.getCode());
        }
        return mission;
    }

    private Vehicle getVehicle() {
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

    private Vehicle createVehicleWithRetries() {
        for (int numTries = 0; numTries < NUMBER_OF_RETRIES; numTries++) {
            Vehicle vehicle = createVehicle();
            if (vehicle != null) {
                return vehicle;
            }
        }

        return null;
    }

    private Vehicle createVehicle() {
        CreateVehicle createVehicle = new CreateVehicle();
        createVehicle.setName(testCopter);

        try {
            return defaultApi.vehiclesPost(API_KEY, token.getToken(), createVehicle);
        } catch (ApiException e) {
            Timber.e("Failed to create vehicle " + e + " code: " + e.getCode());
        }
        return null;
    }

    private void uploadImages(String missionId, ArrayList<File> geotaggedFiles) {
        if (geotaggedFiles == null) {
            return;
        }

        if (executor.isShutdown()) {
            return;
        }

        ExecutorService service = Executors.newFixedThreadPool(UPLOAD_POOL_SIZE);
        List<Future<Runnable>> futures = new ArrayList<>();

        for (File image : geotaggedFiles) {
            if (executor.isShutdown()) {
                service.shutdownNow();
                return;
            }

            if (service.isShutdown()) {
                return;
            }

            UploadImages uploadImages = new UploadImages(image, missionId);
            Future f = service.submit(uploadImages);
            futures.add(f);
        }

        for (Future<Runnable> f : futures) {
            try {
                f.get();
            } catch (Exception e) {
                Timber.e(e, "Failed to get runnable data");
            }
        }
        service.shutdownNow();
    }

    private boolean uploadImageWithRetries(String missionId, String token, File image) {
        for (int numTries = 0; numTries < NUMBER_OF_RETRIES; numTries++) {
            boolean success = uploadImage(missionId, token, image);
            if (success) {
                return true;
            }
        }

        return false;
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

    private RecapAuth getRecapUrlWithRetries() {
        StringBuilder message = new StringBuilder();
        for (int numTries = 0; numTries < NUMBER_OF_RETRIES; numTries++) {
            message = new StringBuilder();
            RecapAuth recapAuth = getRecapUrl(message);
            if (recapAuth != null) {
                return recapAuth;
            }
        }

        sendFailedCloudUploadIntent(message.toString());
        return null;
    }

    private RecapAuth getRecapUrl(StringBuilder message) {
        try {
            return defaultApi.actionsRecapAuthPost(API_KEY, token.getToken());
        } catch (ApiException e) {
            message.append("Failed to login to recap " + e + " code: " + e.getCode());
            Timber.e("Failed to login to recap " + e + " code: " + e.getCode());
        }
        return null;
    }

    private void createRecapJobWithRetries() {
        StringBuilder message = new StringBuilder();
        for (int numTries = 0; numTries < NUMBER_OF_RETRIES; numTries++) {
            message = new StringBuilder();
            RecapResult recapResult = startRecapJob(message);
            if (recapResult != null) {
                Bundle bundle = new Bundle();
                bundle.putString(EXTRA_RECAP_ID, recapResult.getId());
                sendUpdates(STATE_RECAP_JOB_CREATED, bundle);
                return;
            }
        }

        sendFailedCloudUploadIntent(message.toString());
    }

    private RecapResult startRecapJob(StringBuilder message) {

        Params params = new Params();
        params.setFormat("ortho");
        params.setQuality("10");
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
            Timber.e("Failed to start recap job " + e + " code: " + e.getCode());
            message.append("Failed to start recap job " + e + " code: " + e.getCode());
        }

        return recapResult;
    }

    private synchronized void uploadProgress(final int progress) {
        Intent intent = new Intent(STATE_IMAGES_UPLOADING);
        intent.putExtra(EXTRA_PROGRESS, progress);
        lbm.sendBroadcast(intent);
    }

    private void logoutRecap(String token) {
        try {
            User user = defaultApi.actionsRecapAuthLogoutPost(API_KEY, token);
            Timber.d("logged out of recap " + user.getEmail());
        } catch (ApiException e) {
            Timber.e("Failed to logout " + e + " code: " + e.getCode());
        }
    }

    private class DownloadCameraTlogs extends AsyncTask<Object, Integer, Boolean> {

        private WeakReference<Context> weakContext;

        private DownloadCameraTlogs(Context context) {
            this.weakContext = new WeakReference<>(context);
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
            Intent intent = new Intent(STATE_FINISHED_LOADING_LOGS);
            intent.putExtra(EXTRA_SUCCESS, result);
            lbm.sendBroadcast(intent);
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

    private class GeoTagTask extends GeoTagAsyncTask {

        public GeoTagTask(Context context, List<TLogParser.Event> events, ArrayList<File> photos) {
            super(context, events, photos);
        }

        @Override
        public void onResult(HashMap<File, File> geotaggedFiles, HashMap<File, Exception> failedFiles) {
            ArrayList<File> geotaggedList = new ArrayList<>();
            for (File file : geotaggedFiles.values()) {
                geotaggedList.add(file);
            }

            Intent intent = new Intent(STATE_FINISHED_GEOTAGGING);
            intent.putExtra(EXTRA_SUCCESS, true);
            intent.putExtra(EXTRA_GEOTAGGED_FILES, geotaggedList);
            lbm.sendBroadcast(intent);

            geoTagTask = null;
        }

        @Override
        public void onProgress(int numProcessed, int numTotal) {
            Intent intent = new Intent(STATE_PROGRESS_UPDATE_GEOTAGGING);
            intent.putExtra(EXTRA_TOTAL, numTotal);
            intent.putExtra(EXTRA_PROGRESS, numProcessed);
            lbm.sendBroadcast(intent);
        }

        @Override
        public void onFailed(Exception e) {
            sendFailedGeotaggingIntent(e.getMessage());
            geoTagTask = null;
        }
    }

    private class UploadImages implements Runnable {
        private File image;
        private String missionId;

        UploadImages(File image, String missionId) {
            this.image = image;
            this.missionId = missionId;
        }

        @Override
        public void run() {
            for (int numTries = 0; numTries < NUMBER_OF_RETRIES; numTries++) {
                boolean success = uploadImage(missionId, token.getToken(), image);
                if (success) {
                    synchronized (numPhotos) {
                        numPhotos++;
                    }
                    uploadProgress(numPhotos);
                    return;
                }
            }
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
    }
}
