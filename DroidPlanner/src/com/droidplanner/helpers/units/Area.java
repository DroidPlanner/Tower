package com.droidplanner.helpers.units;

import java.util.Locale;

public class Area {
	private final String SQUARE = "\u00B2";
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
		if (areaInSqMeters > 100000) {
			return String.format(Locale.US,"%2.1f km"+SQUARE,areaInSqMeters/1000000);
		}else if (areaInSqMeters>1) {
			return String.format(Locale.US,"%2.1f m"+SQUARE,areaInSqMeters);
		}else if (areaInSqMeters>0.00001) {
			return String.format(Locale.US,"%2.2f cm"+SQUARE,areaInSqMeters*10000);
		}else{
			return areaInSqMeters + " m"+SQUARE;
		}
		
	}	
	
}
