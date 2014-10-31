package org.droidplanner.core.helpers.units;

import junit.framework.TestCase;

public class SpeedTest extends TestCase {
	private static final double TOLERANCE = 1e-8;

	private Speed speed;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		speed = new Speed(1.0);
	}

	public void testValueInMeters() {
		assertEquals(1, speed.valueInMetersPerSecond(), TOLERANCE);
	}

	public void testSet() {
		speed.set(2);
		assertEquals(2, speed.valueInMetersPerSecond(), TOLERANCE);
	}

	public void testToString() {
		speed.set(1.0);
		assertEquals("1.0 m/s", speed.toString());
		speed.set(1.234);
		assertEquals("1.2 m/s", speed.toString());
		speed.set(10);
		assertEquals("10.0 m/s", speed.toString());

		speed.set(1000);
		assertEquals("1.0 km/s", speed.toString());
		speed.set(1234);
		assertEquals("1.2 km/s", speed.toString());

		speed.set(1e-6);
		assertEquals("1.0E-6 m/s", speed.toString());
	}

	public void testEquals() {
		assertTrue(speed.equals(new Speed(1.0)));
		assertFalse(speed.equals(new Speed(2.0)));
		assertFalse(speed.equals(new Object()));
	}

}
