package org.droidplanner.services.android.impl.core.drone.variables;

import org.droidplanner.services.android.impl.core.MAVLink.MavLinkStreamRates;
import org.droidplanner.services.android.impl.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.services.android.impl.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.services.android.impl.core.drone.DroneVariable;
import org.droidplanner.services.android.impl.core.drone.autopilot.MavLinkDrone;

public class StreamRates extends DroneVariable<MavLinkDrone> implements OnDroneListener<MavLinkDrone> {

    private Rates rates;

	public StreamRates(MavLinkDrone myDrone) {
		super(myDrone);
		myDrone.addDroneListener(this);
	}

    public void setRates(Rates rates) {
		if(this.rates == null || !this.rates.equals(rates)) {
			this.rates = rates;
			if (myDrone.isConnected() && myDrone.isConnectionAlive()) {
				setupStreamRatesFromPref();
			}
		}
    }

    @Override
	public void onDroneEvent(DroneEventsType event, MavLinkDrone drone) {
		switch (event) {
        case CONNECTED:
		case HEARTBEAT_FIRST:
		case HEARTBEAT_RESTORED:
			setupStreamRatesFromPref();
			break;
		default:
			break;
		}
	}

	private void setupStreamRatesFromPref() {
        if(rates == null)
            return;

		MavLinkStreamRates.setupStreamRates(myDrone.getMavClient(), myDrone.getSysid(),
				myDrone.getCompid(), rates.extendedStatus, rates.extra1, rates.extra2,
				rates.extra3, rates.position, rates.rcChannels, rates.rawSensors,
				rates.rawController);
	}

    public static class Rates {
        public int extendedStatus;
        public int extra1;
        public int extra2;
        public int extra3;
        public int position;
        public int rcChannels;
        public int rawSensors;
        public int rawController;
		
		public Rates(int rate){
			this.extendedStatus = rate;
			this.extra1 = rate;
			this.extra2 = rate;
			this.extra3 = rate;
			this.position = rate;
			this.rcChannels = rate;
			this.rawSensors = rate;
			this.rawController = rate;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof Rates)) {
				return false;
			}

			Rates rates = (Rates) o;

			if (extendedStatus != rates.extendedStatus) {
				return false;
			}
			if (extra1 != rates.extra1) {
				return false;
			}
			if (extra2 != rates.extra2) {
				return false;
			}
			if (extra3 != rates.extra3) {
				return false;
			}
			if (position != rates.position) {
				return false;
			}
			if (rcChannels != rates.rcChannels) {
				return false;
			}
			if (rawSensors != rates.rawSensors) {
				return false;
			}
			return rawController == rates.rawController;

		}

		@Override
		public int hashCode() {
			int result = extendedStatus;
			result = 31 * result + extra1;
			result = 31 * result + extra2;
			result = 31 * result + extra3;
			result = 31 * result + position;
			result = 31 * result + rcChannels;
			result = 31 * result + rawSensors;
			result = 31 * result + rawController;
			return result;
		}
	}

}
