package org.droidplanner.android.notifications;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.widget.Toast;

import org.droidplanner.R;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.model.Drone;


public class EmergencyBeepNotificationProvider implements NotificationHandler.NotificationProvider {
	private MediaPlayer beeper;
	private Context mContext;
	private SoundPool mPool;
	private int beepBeep;
	public EmergencyBeepNotificationProvider(Context context){
		mContext = context;
		beeper = MediaPlayer.create(context, R.raw.beep_beep);
		mPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
		beepBeep = mPool.load(context, R.raw.beep_beep, 1);
	}
	@Override
	public void quickNotify(String feedback) {

	}

	@Override
	public void onDroneEvent(DroneInterfaces.DroneEventsType event, Drone drone) {
		if(event == DroneInterfaces.DroneEventsType.STATE){
			if(drone.getAltitude().isCollisionImminent()){
				mPool.play(beepBeep,1f,1f,1,1,1f);
			}else{
				mPool.stop(beepBeep);
			}
		}
	}
}
