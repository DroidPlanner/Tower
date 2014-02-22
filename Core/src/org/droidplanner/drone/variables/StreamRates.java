package org.droidplanner.drone.variables;

import org.droidplanner.drone.Drone;
import org.droidplanner.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.drone.DroneVariable;
import org.droidplanner.drone.variables.Preferences.Rates;

public class StreamRates extends DroneVariable implements OnDroneListener {

	public StreamRates(Drone myDrone) {
		super(myDrone);
		myDrone.events.addDroneListener(this);
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
		case HEARTBEAT_FIRST:
		case HEARTBEAT_RESTORED:
			setupStreamRatesFromPref();
			break;
		default:
			break;
		}
	}

	public void setupStreamRatesFromPref() {
		Rates rates = myDrone.preferences.getRates();

		MavLinkStreamRates.setupStreamRates(myDrone.MavClient, rates.extendedStatus,
				rates.extra1, rates.extra2, rates.extra3, rates.position, rates.rcChannels, rates.rawSensors,
				rates.rawController);
	}
	
	

}
