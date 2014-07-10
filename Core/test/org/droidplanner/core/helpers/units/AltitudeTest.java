package org.droidplanner.core.helpers.units;

import junit.framework.TestCase;

public class AltitudeTest extends TestCase {

	private Altitude altitude;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		altitude = new Altitude(1.0);
	}

	public void testSuperMethods() {
		assertEquals(1.0, altitude.valueInMeters());
		assertEquals("1.0 m", altitude.toString());
	}

	public void testSubtraction() {
		Length difference = altitude.subtract(new Altitude(0.2));
		assertEquals(1.0, altitude.valueInMeters());
		assertEquals(new Length(1.0 - 0.2), difference);
	}

}
