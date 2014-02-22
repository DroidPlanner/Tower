package org.droidplanner.helpers.coordinates;

import org.droidplanner.helpers.units.Altitude;

import junit.framework.TestCase;

public class Coord3DTest extends TestCase {

	public void testSetXY() {
		Altitude alt = new Altitude(50.0);
		Coord3D point = new Coord3D(10, -5.6, alt);
		assertEquals(10.0, point.getX());
		assertEquals(-5.6, point.getY());
		assertEquals(alt, point.getAltitude());
	}

}
