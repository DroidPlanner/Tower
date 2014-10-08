package org.droidplanner.desktop.logic;

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

			@Override
			public void removeCallbacks(Runnable thread) {
				// TODO Auto-generated method stub

			}
			
			@Override
			public void post(Runnable thread){
				new Thread(thread).start();
			}

			@Override
			public void postDelayed(Runnable thread, long timeout) {
				// TODO Auto-generated method stub

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
