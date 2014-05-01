package org.droidplanner.core.helpers.coordinates;

import org.droidplanner.core.helpers.units.Altitude;

public class Coord3D extends Coord2D {
	private Altitude alt;

	public Coord3D(double lat, double lon, Altitude alt) {
		super(lat, lon);
		this.alt = alt;
	}

	public Coord3D(Coord2D point, Altitude alt) {
		this(point.getLat(), point.getLng(), alt);
	}

	public Coord3D(int lat, int lon, int alt) {
		this(lat, lon, new Altitude(alt));
	}

	public void set(double lat, double lon, Altitude alt) {
		super.set(lat, lon);
		this.alt = alt;
	}

	public Altitude getAltitude() {
		return alt;
	}
}
