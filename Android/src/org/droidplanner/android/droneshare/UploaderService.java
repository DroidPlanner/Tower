package org.droidplanner.android.droneshare;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.util.Pair;
import android.text.TextUtils;

import com.geeksville.apiproxy.rest.RESTClient;

import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.R;
import org.droidplanner.android.droneshare.data.DroneShareDB;
import org.droidplanner.android.utils.Utils;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;

import java.io.File;
import java.io.IOException;
import java.util.List;

import timber.log.Timber;

/**
 * Provides delayed uploads to the DroneShare service.
 *
 * If you send any intent to this service it will scan the tlog directory and
 * upload any complete tlogs it finds.
 */
public class UploaderService extends IntentService {

    private static final String DRONESHARE_PRIVACY = "DEFAULT";
	static final String apiKey = "2d38fb2e.72afe7b3761d5ee6346c178fdd6b680f";

	private static final int ONGOING_UPLOAD_NOTIFICATION_ID = 123;
	private static final int UPLOAD_STATUS_NOTIFICATION_ID = 124;
	public static final String ACTION_CHECK_FOR_DRONESHARE_UPLOADS = Utils.PACKAGE_NAME +
		".ACTION_CHECK_FOR_DRONESHARE_UPLOADS";

	private DroidPlannerPrefs dpPrefs;
    private DroneShareDB droneShareDb;

	private NotificationManagerCompat notifyManager;
    private Notification failedUploadNotification;

	public UploaderService() {
		super("DroneShare Uploader");
	}

	@Override
	public void onCreate() {
		super.onCreate();

        final Context context = getApplicationContext();
		dpPrefs = DroidPlannerPrefs.getInstance(context);
		notifyManager = NotificationManagerCompat.from(context);

        droneShareDb = ((DroidPlannerApp) getApplication()).getDroneShareDatabase();
	}

	@Override
    protected void onHandleIntent(Intent intent) {
        final String action = intent.getAction();
        if (action == null) {
            return;
        }

        // Check if droneshare is enabled, and the login credentials set before trying to do anything.
        if (dpPrefs.isDroneshareEnabled()) {

            switch (action) {
                case ACTION_CHECK_FOR_DRONESHARE_UPLOADS:
                    List<Pair<Long, Uri>> dataToUpload = droneShareDb.getDataToUpload(dpPrefs.getDroneshareLogin());
                    if(!dataToUpload.isEmpty()){
                        if (NetworkConnectivityReceiver.isNetworkAvailable(getApplicationContext())) {
                            Timber.i("Preparing droneshare data for upload");
                            doUploads(dataToUpload);
                        } else {
                            Timber.w("Network offline.. Rescheduling droneshare data upload");

                            // Activating the network connectivity receiver so we can be restarted when connectivity is restored.
                            Timber.d("Activating connectivity receiver");
                            NetworkConnectivityReceiver.enableConnectivityReceiver(getApplicationContext(), true);
                        }
                    }
                    break;
            }
        }
    }

	private NotificationCompat.Builder generateNotificationBuilder() {
		return new NotificationCompat.Builder(getApplicationContext())
				.setContentTitle(getString(R.string.uploader_notification_title))
				.setSmallIcon(R.drawable.ic_stat_notify)
				.setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH);
	}

	private void doUploads(List<Pair<Long, Uri>> dataToUpload) {
		String login = dpPrefs.getDroneshareLogin();
		String password = dpPrefs.getDronesharePassword();

		if (!login.isEmpty() && !password.isEmpty()) {
            final Notification notification = generateNotificationBuilder()
                .setContentText("Uploading tlog data")
                .build();
            startForeground(ONGOING_UPLOAD_NOTIFICATION_ID, notification);

            try {
                int numUploaded = 0;
                for (Pair<Long, Uri> datumInfo : dataToUpload) {
                    long uploadId = datumInfo.first;

                    Uri dataUri = datumInfo.second;
                    File uploadFile = new File(dataUri.getPath());
                    if (uploadFile.isFile()) {
                        Timber.i("Starting upload for " + uploadFile);

                        String url = RESTClient.doUpload(uploadFile, login, password, null, apiKey, DRONESHARE_PRIVACY);
                        if (url != null) {
                            numUploaded++;
                        }

                        onUploadSuccess(uploadFile, url, numUploaded);
                    }
                    else{
                        Timber.w("TLog data file is not available.");
                    }

                    droneShareDb.commitUploadedData(uploadId, System.currentTimeMillis());
                }
            } catch (IOException e) {
                Timber.e(e, "Unable to complete tlog data upload");
                onUploadFailure(e);
            }
            stopForeground(true);
		}
	}

    private void onUploadSuccess(File f, String viewURL, int numUploaded) {
        if (viewURL == null) {
            Timber.i("Server thought flight was boring");
            notifyManager.cancel(ONGOING_UPLOAD_NOTIFICATION_ID);
        } else {
            Timber.i("Upload success: " + f + " url=" + viewURL);

            // Attach the view URL
            final PendingIntent pIntent = PendingIntent.getActivity(UploaderService.this, 0,
                new Intent(Intent.ACTION_VIEW, Uri.parse(viewURL)),
                PendingIntent.FLAG_UPDATE_CURRENT);

            final Intent sendIntent = new Intent(Intent.ACTION_SEND).putExtra(
                Intent.EXTRA_TEXT, viewURL).setType("text/plain");

            final PendingIntent sendPIntent = PendingIntent.getActivity(UploaderService.this,
                0, sendIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            final NotificationCompat.Builder notifBuilder = generateNotificationBuilder()
                .setContentText(getString(R.string.uploader_success_message))
                .setContentIntent(pIntent)
                // Attach a web link
                .addAction(android.R.drawable.ic_menu_set_as, "Web", pIntent)
                // Add a share link
                .addAction(android.R.drawable.ic_menu_share, "Share", sendPIntent);

            if (numUploaded > 1)
                notifBuilder.setNumber(numUploaded);

            updateUploadStatusNotification(notifBuilder.build());
        }
    }

    private void onUploadFailure(Exception ex) {
        String msg = ex.getMessage();
        if(TextUtils.isEmpty(msg)) {
            msg = "Upload Failed";
        }

        if (failedUploadNotification == null) {
            failedUploadNotification = generateNotificationBuilder().setContentText(msg)
                .setSubText(getString(R.string.uploader_fail_retry_message)).build();
        }
        updateUploadStatusNotification(failedUploadNotification);

        if (!NetworkConnectivityReceiver.isNetworkAvailable(getApplicationContext())) {
            // Activating the network connectivity receiver so we can be
            // restarted when
            // connectivity is restored.
            Timber.d("Activating connectivity receiver");
            NetworkConnectivityReceiver.enableConnectivityReceiver(getApplicationContext(),
                true);
        }
    }

	private void updateUploadStatusNotification(Notification notification) {
		notifyManager.notify(UPLOAD_STATUS_NOTIFICATION_ID, notification);
	}

    public static void kickStart(Context context){
        if(DroidPlannerPrefs.getInstance(context).isDroneshareEnabled()) {
            context.startService(
                new Intent(context, UploaderService.class)
                    .setAction(ACTION_CHECK_FOR_DRONESHARE_UPLOADS));
        }
    }
}
