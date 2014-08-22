package org.droidplanner.core.helpers.units;

import java.util.Locale;

public class Speed {
	private double speedInMetersPerSecond;

	public Speed(double speedInMetersPerSecond) {
		set(speedInMetersPerSecond);
	}

	public double valueInMetersPerSecond() {
		return speedInMetersPerSecond;
	}

	public void set(double speedInMetersPerSecond) {
		this.speedInMetersPerSecond = speedInMetersPerSecond;
	}

	public String toStringInMetersPerSecond() {
		return String.format(Locale.US, "%2.1f m/s", speedInMetersPerSecond);
	}

	@Override
	public String toString() {
		if (speedInMetersPerSecond >= 1000) {
			return String.format(Locale.US, "%2.1f km/s", speedInMetersPerSecond / 1000);
		} else if (speedInMetersPerSecond >= 1) {
			return toStringInMetersPerSecond();
		} else {
			return speedInMetersPerSecond + " m/s";
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Speed) {
			return speedInMetersPerSecond == ((Speed) o).speedInMetersPerSecond;
		}
		return false;
	}

}
