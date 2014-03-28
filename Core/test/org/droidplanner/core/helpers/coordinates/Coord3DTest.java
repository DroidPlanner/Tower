package org.droidplanner.core.helpers.coordinates;

import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.coordinates.Coord3D;
import org.droidplanner.core.helpers.units.Altitude;

import junit.framework.TestCase;

public class Coord3DTest extends TestCase {

	public void testConstructor() {
		Altitude alt = new Altitude(50.0);
		Coord3D point = new Coord3D(10, -5.6, alt);
		assertEquals(10.0, point.getX());
		assertEquals(-5.6, point.getY());
		assertEquals(alt, point.getAltitude());
	}

	public void testConstFrom2dCoord() {
		Altitude alt = new Altitude(5.0);
		Coord2D point2d = new Coord2D(1, -0.6);
		Coord3D point = new Coord3D(point2d, alt);

		assertEquals(1.0, point.getX());
		assertEquals(-0.6, point.getY());
		assertEquals(alt, point.getAltitude());
	}

	public void testSet() {
		Altitude alt = new Altitude(50.0);
		Coord3D point = new Coord3D(10, -5.6, alt);
		point.set(0, 0, alt);
		assertEquals(0.0, point.getX());
		assertEquals(0.0, point.getY());
		assertEquals(alt, point.getAltitude());
	}

}
