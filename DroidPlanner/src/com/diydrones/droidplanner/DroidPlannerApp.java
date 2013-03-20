package com.diydrones.droidplanner;

import android.app.Application;
import android.util.Log;
import com.MAVLink.Drone;

public class DroidPlannerApp extends Application {
	Drone drone;
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d("APP", "Created");
		drone = new Drone();		
	}

}
