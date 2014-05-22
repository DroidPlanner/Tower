package org.droidplanner.core.helpers.units;

import java.util.Locale;

public class Length {
	private double lengthInMeters;

	public Length(double lengthInMeters) {
		set(lengthInMeters);
	}

	public double valueInMeters() {
		return lengthInMeters;
	}

	public void set(double lengthInMeters) {
		this.lengthInMeters = lengthInMeters;
	}

	@Override
	public String toString() {
		if (lengthInMeters >= 1000) {
			return String.format(Locale.US, "%2.0f km", lengthInMeters / 1000);
		} else if (lengthInMeters >= 1 || lengthInMeters == 0) {
			return String.format(Locale.US, "%2.0f m", lengthInMeters);
		} else  {
			return String.format(Locale.US, "%2.1f mm", lengthInMeters * 1000);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Length) {
			return lengthInMeters == ((Length) o).lengthInMeters;
		}
		return false;
	}

}
