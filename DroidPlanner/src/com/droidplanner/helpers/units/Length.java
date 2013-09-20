package com.droidplanner.helpers.units;

import java.util.Locale;

public class Length {
	private double lengthInMeters;
	
	public Length(double length) {
		this.lengthInMeters = length;
	}

	public double get() {
		return lengthInMeters;
	}

	public void set(double length) {
		this.lengthInMeters = length;
	}

	@Override
	public String toString() {
		if (lengthInMeters > 1000) {
			return String.format(Locale.US,"%2.1f km",lengthInMeters/1000);
		}else if (lengthInMeters>1) {
			return String.format(Locale.US,"%2.1f m",lengthInMeters);
		}else if (lengthInMeters>0.001) {
			return String.format(Locale.US,"%2.1f m",lengthInMeters*1000);
		}else{
			return lengthInMeters + " m";
		}
	}	
	
}
