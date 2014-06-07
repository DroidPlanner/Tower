package org.droidplanner.android.communication.service;

import java.io.File;

import org.droidplanner.android.communication.connection.MAVLinkConnection;
import org.droidplanner.android.utils.DroidplannerPrefs;
import org.droidplanner.android.utils.file.DirectoryPath;

import com.geeksville.apiproxy.DirectoryUploader;
import com.geeksville.apiproxy.IUploadListener;

import org.droidplanner.R;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * Provides delayed uploads to the DroneShare service.
 * 
 * If you send any intent to this service it will scan the tlog directory and
 * upload any complete tlogs it finds.
 * 
 * @author kevinh
 */
public class UploaderService extends IntentService {

	private static final String TAG = UploaderService.class.getSimpleName();
	static final String apiKey = "2d38fb2e.72afe7b3761d5ee6346c178fdd6b680f";

	private DroidplannerPrefs prefs;
	private int numUploaded = 0;

	private IUploadListener callback = new IUploadListener() {
		public void onUploadStart(File f) {
			Log.i(TAG, "Upload start: " + f);
			// Generate initial notification
			updateNotification(true);
		}

		public void onUploadSuccess(File f, String viewURL) {
			if (viewURL == null) {
				Log.i(TAG, "Server thought flight was boring");
				notifyManager.cancel(notifyId);
			} else {
				Log.i(TAG, "Upload success: " + f + " url=" + viewURL);

				numUploaded += 1;
				nBuilder.setContentText("Select to view..."); // FIXME localize

				// Attach the view URL
				PendingIntent pintent = PendingIntent.getActivity(
						UploaderService.this, 0, new Intent(Intent.ACTION_VIEW,
								Uri.parse(viewURL)), 0);
				nBuilder.setContentIntent(pintent);

				// Attach the google earth link
				// val geintent = PendingIntent.getActivity(acontext, 0, new
				// Intent(Intent.ACTION_VIEW, Uri.parse(kmzURL)), 0)
				// nBuilder.addAction(android.R.drawable.ic_menu_mapmode,
				// S(R.string.google_earth), geintent)

				// Attach a web link
				nBuilder.addAction(android.R.drawable.ic_menu_set_as, "Web",
						pintent);

				// Add a share link
				Intent sendIntent = new Intent(Intent.ACTION_SEND);
				sendIntent.putExtra(Intent.EXTRA_TEXT, viewURL);
				sendIntent.setType("text/plain");
				// val chooser = Intent.createChooser(sendIntent,
				// "Share log to...")
				nBuilder.addAction(android.R.drawable.ic_menu_share, "Share",
						PendingIntent.getActivity(UploaderService.this, 0,
								sendIntent, 0));
				if (numUploaded > 1)
					nBuilder.setNumber(numUploaded);
				nBuilder.setPriority(NotificationCompat.PRIORITY_HIGH); // The
																		// user
																		// probably
																		// wants
																		// to
																		// choose
																		// us
																		// now

				// FIXME, include action buttons for sharing

				updateNotification(false);
			}
		}

		public void onUploadFailure(File f, Exception ex) {
			Log.i(TAG, "Upload fail: " + f + " " + ex);
			nBuilder.setContentText("Upload failed: " + ex.getMessage());
			nBuilder.setSubText("Will try again later"); // FIXME - localize
			updateNotification(false);
		}
	};

	private int notifyId = 2;

	private NotificationManager notifyManager;
	private NotificationCompat.Builder nBuilder;

	public UploaderService() {
		super("Uploader");
	}

	@Override
	public void onCreate() {
		super.onCreate();

		prefs = new DroidplannerPrefs(this);
		notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		nBuilder = new NotificationCompat.Builder(this);
		nBuilder.setContentTitle("Droneshare upload")
				// FIXME - extract for localization
				.setContentText("Uploading log file")
				.setSmallIcon(R.drawable.ic_launcher).setAutoCancel(true)
				// .setProgress(fileSize, 0, false)
				.setPriority(NotificationCompat.PRIORITY_HIGH);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// Any time we receive an intent - rescan the directory
		if (NetworkStateReceiver.isNetworkAvailable(this)) {

			Log.i(TAG, "Scanning for new uploads");
			doUploads();
		} else {
			Log.e(TAG, "Not scanning - network offline");
		}
	}

	private void doUploads() {
		File srcDir = DirectoryPath.getTLogPath();
		File destDir = DirectoryPath.getSentPath();

		String login = prefs.getDroneshareLogin();
		String password = prefs.getDronesharePassword();

		if (!login.isEmpty() && !password.isEmpty()) {
			DirectoryUploader up = new DirectoryUploader(srcDir, destDir,
					callback, login, password, prefs.getVehicleId(), apiKey);
			up.run();
		}
	}

	private void updateNotification(boolean isForeground) {
		Notification n = nBuilder.build();

		Log.d(TAG, "Updating notification " + isForeground);
		notifyManager.cancel(notifyId);
		notifyId += 1; // Generate a new notification for each status change
		notifyManager.notify(notifyId, n);
		if (isForeground)
			startForeground(notifyId, n);
		else {
			stopForeground(false);
		}
	}

	// private void removeProgress() { nBuilder.setProgress(0, 0, false); }

	// / Create an Intent that will start this service
	static public Intent createIntent(Context context) {
		return new Intent(context, UploaderService.class);
	}
}
