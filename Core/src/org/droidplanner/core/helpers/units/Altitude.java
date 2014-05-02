package org.droidplanner.core.helpers.units;

public class Altitude extends Length {

	public Altitude(double heightInMeters) {
		super(heightInMeters);
	}

	public Length subtract(Altitude toSubtract) {
		return new Length(this.valueInMeters() - toSubtract.valueInMeters());
	}
}
