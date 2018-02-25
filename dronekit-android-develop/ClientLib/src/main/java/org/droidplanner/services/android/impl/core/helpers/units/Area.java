package org.droidplanner.services.android.impl.core.helpers.units;

import java.util.Locale;

public class Area {
	public final static String SQUARE_SYMBOL = "\u00B2";
	private double areaInSqMeters;

	public Area(double areaInSqMeters) {
		this.areaInSqMeters = areaInSqMeters;
	}

	public double valueInSqMeters() {
		return areaInSqMeters;
	}

	public void set(double areaInSqMeters) {
		this.areaInSqMeters = areaInSqMeters;
	}

	@Override
	public String toString() {
		if (areaInSqMeters >= 100000) {
			return String.format(Locale.US, "%2.1f km" + SQUARE_SYMBOL, areaInSqMeters / 1000000);
		} else if (areaInSqMeters >= 1) {
			return String.format(Locale.US, "%2.1f m" + SQUARE_SYMBOL, areaInSqMeters);
		} else if (areaInSqMeters >= 0.00001) {
			return String.format(Locale.US, "%2.2f cm" + SQUARE_SYMBOL, areaInSqMeters * 10000);
		} else {
			return areaInSqMeters + " m" + SQUARE_SYMBOL;
		}

	}

}
