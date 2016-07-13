package org.droidplanner.android.droneshare;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.geeksville.apiproxy.DirectoryUploader;
import com.geeksville.apiproxy.IUploadListener;

import org.apache.http.client.HttpResponseException;
import org.droidplanner.android.R;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;

import java.io.File;

/**
 * Provides delayed uploads to the DroneShare service.
 *
 * If you send any intent to this service it will scan the tlog directory and
 * upload any complete tlogs it finds.
 */
public class UploaderService extends IntentService {

	private static final String TAG = UploaderService.class.getSimpleName();
	static final String apiKey = "2d38fb2e.72afe7b3761d5ee6346c178fdd6b680f";

	private static final int ONGOING_UPLOAD_NOTIFICATION_ID = 123;
	private static final int UPLOAD_STATUS_NOTIFICATION_ID = 124;

    private DroidPlannerPrefs dpPrefs;

	private final IUploadListener callback = new IUploadListener() {

		private int numUploaded = 0;
		private Notification failedUploadNotification;

		@Override
		public void onUploadStart(File f) {
			Log.i(TAG, "Upload start: " + f);
		}

		@Override
		public void onUploadSuccess(File f, String viewURL) {
			if (viewURL == null) {
				Log.i(TAG, "Server thought flight was boring");
				notifyManager.cancel(ONGOING_UPLOAD_NOTIFICATION_ID);
			} else {
				Log.i(TAG, "Upload success: " + f + " url=" + viewURL);

				// Attach the view URL
				final PendingIntent pIntent = PendingIntent.getActivity(UploaderService.this, 0,
						new Intent(Intent.ACTION_VIEW, Uri.parse(viewURL)),
						PendingIntent.FLAG_UPDATE_CURRENT);

				final Intent sendIntent = new Intent(Intent.ACTION_SEND).putExtra(
						Intent.EXTRA_TEXT, viewURL).setType("text/plain");

				final PendingIntent sendPIntent = PendingIntent.getActivity(UploaderService.this,
						0, sendIntent, PendingIntent.FLAG_UPDATE_CURRENT);

				numUploaded++;

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

		@Override
		public void onUploadFailure(File f, Exception ex) {
			Log.i(TAG, "Upload fail: " + f + " " + ex);

			String msg = "Upload Failed";
			if (ex instanceof HttpResponseException)
				msg = ex.getMessage();

			if (failedUploadNotification == null) {
				failedUploadNotification = generateNotificationBuilder().setContentText(msg)
						.setSubText(getString(R.string.uploader_fail_retry_message)).build();
			}
			updateUploadStatusNotification(failedUploadNotification);

			if (!NetworkConnectivityReceiver.isNetworkAvailable(getApplicationContext())) {
				// Activating the network connectivity receiver so we can be
				// restarted when
				// connectivity is restored.
				Log.d(TAG, "Activating connectivity receiver");
				NetworkConnectivityReceiver.enableConnectivityReceiver(getApplicationContext(),
						true);
			}
		}
	};

	private NotificationManagerCompat notifyManager;

	public UploaderService() {
		super("Uploader");
	}

	@Override
	public void onCreate() {
		super.onCreate();

        final Context context = getApplicationContext();
		dpPrefs = DroidPlannerPrefs.getInstance(context);
		notifyManager = NotificationManagerCompat.from(context);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// Check if droneshare is enabled, and the login credentials set before trying to do anything.
		if (dpPrefs.isDroneshareEnabled()) {

            final Context context = getApplicationContext();
			// Any time we receive an intent - rescan the directory
			if (NetworkConnectivityReceiver.isNetworkAvailable(context)) {
				Log.i(TAG, "Scanning for new uploads");
				doUploads();
			} else {
				Log.v(TAG, "Not scanning - network offline");

				// Activating the network connectivity receiver so we can be restarted when connectivity is restored.
				Log.d(TAG, "Activating connectivity receiver");
				NetworkConnectivityReceiver.enableConnectivityReceiver(context,	true);
			}
		}
	}

	private NotificationCompat.Builder generateNotificationBuilder() {
		return new NotificationCompat.Builder(getApplicationContext())
				.setContentTitle(getString(R.string.uploader_notification_title))
				.setSmallIcon(R.drawable.ic_stat_notify)
				// .setProgress(fileSize, 0, false)
				.setAutoCancel(true).setPriority(NotificationCompat.PRIORITY_HIGH);
	}

	private void doUploads() {
        final Context context = getApplicationContext();
        File appSrcDir = DirectoryPath.getTLogPath(context, appId);

		String login = dpPrefs.getDroneshareLogin();
		String password = dpPrefs.getDronesharePassword();

		if (!login.isEmpty() && !password.isEmpty()) {
			DirectoryUploader up = new DirectoryUploader(appSrcDir, appDestDir, callback, login,
					password, dpPrefs.getVehicleId(), apiKey, "DEFAULT");

			final Notification notification = generateNotificationBuilder()
                    .setContentText("Uploading log file")
                    .build();
			startForeground(ONGOING_UPLOAD_NOTIFICATION_ID, notification);
			up.run();
			stopForeground(true);
		}
	}

	private void updateUploadStatusNotification(Notification notification) {
		notifyManager.notify(UPLOAD_STATUS_NOTIFICATION_ID, notification);
	}

	/**
	 * Create an Intent that will start this service
	 */
	static public Intent createIntent(Context context) {
		return new Intent(context, UploaderService.class);
	}

    public static void kickStart(Context context){
        if(DroidPlannerPrefs.getInstance(context).isDroneshareEnabled()) {
            context.startService(UploaderService.createIntent(context));
        }
    }
}
