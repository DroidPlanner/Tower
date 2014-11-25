package org.droidplanner.core.helpers.geoTools;

import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.polygon.Polygon;

import junit.framework.TestCase;

public class PolygonToolsTest extends TestCase {

	public void testIsSimplePolygon() {
		Polygon polygon = new Polygon();

		polygon.addPoint(new Coord2D(0, 0));
		polygon.addPoint(new Coord2D(0, 1));
		polygon.addPoint(new Coord2D(1, 1));
		polygon.addPoint(new Coord2D(1, 0));

		assertTrue(PolygonTools.isSimplePolygon(polygon));
	}
	
	public void testIsComplexPolygon() {
		Polygon polygon = new Polygon();

		polygon.addPoint(new Coord2D(0, 0));
		polygon.addPoint(new Coord2D(1, 1));
		polygon.addPoint(new Coord2D(0, 1));
		polygon.addPoint(new Coord2D(1, 0));

		assertFalse(PolygonTools.isSimplePolygon(polygon));
	}
	
	public void testIsCWPolygon() {
		Polygon polygonCCW = new Polygon();
		polygonCCW.addPoint(new Coord2D(0, 0));
		polygonCCW.addPoint(new Coord2D(0, 1));
		polygonCCW.addPoint(new Coord2D(1, 1));
		polygonCCW.addPoint(new Coord2D(1, 0));

		assertFalse(PolygonTools.isClockWisePolygon(polygonCCW));
		
		Polygon polygonCW = new Polygon();
		polygonCW.addPoint(new Coord2D(1, 0));
		polygonCW.addPoint(new Coord2D(1, 1));
		polygonCW.addPoint(new Coord2D(0, 1));
		polygonCW.addPoint(new Coord2D(0, 0));
		
		assertTrue(PolygonTools.isClockWisePolygon(polygonCW));
	}
}