package org.droidplanner.core.helpers.coordinates;

import java.util.ArrayList;

import junit.framework.TestCase;

public class CoordBoundsTest extends TestCase {

	private Coord2D origin;
	private Coord2D point1;
	private Coord2D point2;
	private Coord2D point3;
	private Coord2D point4;
	private ArrayList<Coord2D> list;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		origin = new Coord2D(0, 0);
		point1 = new Coord2D(2.0, 2.0);
		point2 = new Coord2D(-1.0, 3.0);
		point3 = new Coord2D(0.0, -5.0);
		point4 = new Coord2D(-6.0, -3.0);

		list = new ArrayList<Coord2D>();
		list.add(origin);
		list.add(point1);
		list.add(point2);
		list.add(point3);
		list.add(point4);
	}

	public void testSinglePoint() {
		assertEquals(0.0, new CoordBounds(origin).getDiag());
		assertEquals(0.0, new CoordBounds(point1).getDiag());
	}

	public void testList() {
		CoordBounds bounds = new CoordBounds(list);

		assertEquals(2.0, bounds.ne_1quadrant.getX());
		assertEquals(3.0, bounds.ne_1quadrant.getY());
		assertEquals(-6.0, bounds.sw_3quadrant.getX());
		assertEquals(-5.0, bounds.sw_3quadrant.getY());
	}

}
