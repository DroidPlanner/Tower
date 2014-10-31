package org.droidplanner.core.helpers.coordinates;

import junit.framework.TestCase;

public class Coord2DTest extends TestCase {

	public void testConstructor() {
		Coord2D point = new Coord2D(10, -5.6);
		assertEquals(10.0, point.getX());
		assertEquals(-5.6, point.getY());
	}

}
