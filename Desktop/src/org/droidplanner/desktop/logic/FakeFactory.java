package org.droidplanner.desktop.logic;

import java.util.Stack;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.droidplanner.core.drone.DroneInterfaces.Clock;
import org.droidplanner.core.drone.DroneInterfaces.Handler;
import org.droidplanner.core.drone.Preferences;
import org.droidplanner.core.drone.profiles.VehicleProfile;
import org.droidplanner.core.firmware.FirmwareType;

public class FakeFactory {

	static Preferences fakePreferences() {
		return new Preferences() {

			@Override
			public VehicleProfile loadVehicleProfile(FirmwareType firmwareType) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public FirmwareType getVehicleType() {
				// TODO Auto-generated method stub
				return FirmwareType.ARDU_COPTER;
			}

			@Override
			public Rates getRates() {
				return new Rates();
			}
		};
	}

	static Handler fakeHandler() {
		return new Handler() {
			
			private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

			@Override
			public void removeCallbacks(Runnable thread) {
				// TODO Auto-generated method stub

			}
			
			@Override
			public void post(Runnable thread){
				scheduler.execute(thread);
			}

			@Override
			public void postDelayed(Runnable thread, long timeout) {
				//scheduler.schedule(thread, timeout, TimeUnit.MILLISECONDS);
			}
		};
	}

	static Clock fakeClock() {
		return new Clock() {
			@Override
			public long elapsedRealtime() {
				return System.currentTimeMillis();
			}
		};
	}

}
