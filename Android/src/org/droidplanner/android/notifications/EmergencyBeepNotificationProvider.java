package org.droidplanner.android.notifications;

import org.droidplanner.R;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.model.Drone;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

public class EmergencyBeepNotificationProvider implements NotificationHandler.NotificationProvider {

	private SoundPool mPool;
	private int beepBeep;

	public EmergencyBeepNotificationProvider(Context context){
		mPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
		beepBeep = mPool.load(context, R.raw.beep_beep, 1);
	}

	@Override
	public void quickNotify(String feedback) {

	}

    @Override
    public void onTerminate() {
        mPool.release();
    }

	@Override
	public void onDroneEvent(DroneInterfaces.DroneEventsType event, Drone drone) {
		if (event == DroneInterfaces.DroneEventsType.STATE) {
			if (drone.getAltitude().isCollisionImminent()) {
				mPool.play(beepBeep, 1f, 1f, 1, 1, 1f);
			} else {
				mPool.stop(beepBeep);
			}
		}
	}
}
