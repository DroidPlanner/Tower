package org.droidplanner.drone.variables;

import org.droidplanner.file.IO.VehicleProfile;

public interface Preferences {

	public abstract String getVehicleType();

	public abstract VehicleProfile loadVehicleProfile(String vehicle);

	public abstract Rates getRates();

	public class Rates {
		public int extendedStatus;
		public int extra1;
		public int extra2;
		public int extra3;
		public int position;
		public int rcChannels;
		public int rawSensors;
		public int rawController;
	}
}
