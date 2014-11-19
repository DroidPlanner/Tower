package org.droidplanner.android.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.SoundPool;
import android.support.v4.content.LocalBroadcastManager;

import com.o3dr.android.client.Drone;

import org.droidplanner.android.R;

public class EmergencyBeepNotificationProvider implements NotificationHandler.NotificationProvider {

	private static final IntentFilter eventFilter = new IntentFilter(
			Drone.ACTION_GROUND_COLLISION_IMMINENT);

	private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (Drone.ACTION_GROUND_COLLISION_IMMINENT.equals(action)) {
				if (intent.getBooleanExtra(Drone.EXTRA_IS_GROUND_COLLISION_IMMINENT, false)) {
					mPool.play(beepBeep, 1f, 1f, 1, 1, 1f);
				} else {
					mPool.stop(beepBeep);
				}
			}
		}
	};

	private SoundPool mPool;
	private int beepBeep;

	private final Context context;

	public EmergencyBeepNotificationProvider(Context context) {
		this.context = context;
		mPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
		beepBeep = mPool.load(context, R.raw.beep_beep, 1);

		LocalBroadcastManager.getInstance(context).registerReceiver(eventReceiver, eventFilter);
	}

	@Override
	public void onTerminate() {
		mPool.release();
		LocalBroadcastManager.getInstance(context).unregisterReceiver(eventReceiver);
	}

}
