package org.droidplanner.android.fragments.geotag;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
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
import org.droidplanner.android.utils.connection.SshConnection;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

import timber.log.Timber;

/**
 * Created by chavi on 10/19/15.
 */
public class GeoTagImagesService extends Service {
    private static final String SSH_USERNAME = "root";
    private static final String SSH_PASSWORD = "TjSDBkAu";
    private static final SshConnection soloSshLink = new SshConnection(BuildConfig.SOLO_LINK_IP, SSH_USERNAME, SSH_PASSWORD);
    private static final String CAMERA_TLOG_FILE = "/log/camera_msgs.tlog";
    private static final int NUMBER_OF_RETRIES = 3;
    private static final String GEO_TAG_ROOT_NAME = "GeoTag";

    private static final String PACKAGE_NAME = Utils.PACKAGE_NAME;

    public static final String ACTION_START_LOADING_LOGS = PACKAGE_NAME + ".action.START_LOADING_LOGS";
    public static final String ACTION_CANCEL_LOADING_LOGS = PACKAGE_NAME + ".action.CANCEL_LOADING_LOGS";
    public static final String ACTION_START_GEOTAGGING = PACKAGE_NAME + ".action.START_GEOTAGGING";
    public static final String ACTION_CANCEL_GEOTAGGING = PACKAGE_NAME + ".action.CANCEL_GEOTAGGING";

    public static final String STATE_FINISHED_LOADING_LOGS = PACKAGE_NAME + ".FINISHED_LOADING_LOGS";
    public static final String STATE_FINISHED_GEOTAGGING = PACKAGE_NAME + ".FINISHED_GEOTAGGING";
    public static final String STATE_PROGRESS_UPDATE_GEOTAGGING = PACKAGE_NAME + ".PROGRESS_UPDATE_GEOTAGGING";

    public static final String EXTRA_SUCCESS = PACKAGE_NAME + ".EXTRA_SUCCESS";
    public static final String EXTRA_FAILURE_MESSAGE = PACKAGE_NAME + ".EXTRA_FAILURE_MESSAGE";
    public static final String EXTRA_PROGRESS = PACKAGE_NAME + ".EXTRA_PROGRESS";
    public static final String EXTRA_TOTAL = PACKAGE_NAME + ".EXTRA_TOTAL";
    public static final String EXTRA_GEOTAGGED_FILES = PACKAGE_NAME + ".EXTRA_GEOTAGGED_FILES";

    private static final int CAMERA_TRIGGER_MSG_ID = 180;
    private static final String IMAGE_PATH = "/DCIM";

    private AsyncTask getTlogsTask;
    private GeoTagTask geoTagTask;
    private LocalBroadcastManager lbm;

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
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startLoadingLogs() {
        if (getTlogsTask != null) {
            getTlogsTask.cancel(true);
        }
        getTlogsTask = new DownloadCameraTlogs(getApplicationContext());
        getTlogsTask.execute();
    }

    private void cancelLoadingLogs() {
        if (getTlogsTask != null) {
            getTlogsTask.cancel(true);
            getTlogsTask = null;
        }
    }

    private void startGeoTagging() {
        File folder = getExternalFilesDir(null);
        File tlogFile = new File(folder.getPath() + "/camera_msgs.tlog");
        Timber.d("path: " + tlogFile.getPath());

        geoTagImages(tlogFile);
    }

    private void cancelGeoTagging() {
        if (geoTagTask != null) {
            geoTagTask.cancel(true);
            geoTagTask = null;
        }
    }

    private void geoTagImages(File tlogFile) {
        final String extMount = getExternalStorage();
        if (extMount == null) {
            return;
        }

        final ArrayList<File> photos = searchDir(extMount);

        if (photos.isEmpty()) {
            sendFailedGeotaggingIntent("No photos on SD card for GoPro device.");
            return;
        }

        Uri uri = Uri.fromFile(tlogFile);
        Handler handler = new Handler();
        TLogParser.getAllEventsAsync(handler, uri, new TLogParserFilter() {

            @Override
            public boolean includeEvent(TLogParser.Event event) {
                return CAMERA_TRIGGER_MSG_ID == event.getMavLinkMessage().msgid;
            }

            @Override
            public boolean shouldIterate() {
                return true;
            }


        }, new TLogParserCallback() {
            @Override
            public void onResult(List<TLogParser.Event> eventList) {
                if (eventList.isEmpty()) {
                    sendFailedGeotaggingIntent("No camera message events found");
                    return;
                }

                if (geoTagTask != null) {
                    geoTagTask.cancel(true);
                }

                File file = new File(getSaveRootDir(), GEO_TAG_ROOT_NAME);

                geoTagTask = new GeoTagTask(file, eventList, photos);
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

    private String getExternalStorage() {
        boolean hasNullFile = false;

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            File[] files = getExternalFilesDirs(null);
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
        if (extFile == null) {
            return null;
        }

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

    private static ArrayList<File> searchDir(String mount) {
        File photoDir = new File(mount + IMAGE_PATH);
        File[] goProDirs = photoDir.listFiles();
        if (goProDirs == null || goProDirs.length == 0) {
            return null;
        }

        ArrayList<File> photoFiles = new ArrayList<>();

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
                    File folder = getExternalFilesDir(null);
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

    private File getSaveRootDir() {
        File saveDir = getExternalFilesDir(null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            File dirs[] = getExternalFilesDirs(null);
            for (File dir : dirs) {
                // dir can be null if the device contains an external SD card slot but no SD card is present.
                if (dir != null && Environment.isExternalStorageRemovable(dir)) {
                    saveDir = dir;
                    break;
                }
            }
        }
        return saveDir;
    }

    private class GeoTagTask extends GeoTagAsyncTask {

        public GeoTagTask(File rootDir, List<TLogParser.Event> events, ArrayList<File> photos) {
            super(rootDir, events, photos);
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
}
