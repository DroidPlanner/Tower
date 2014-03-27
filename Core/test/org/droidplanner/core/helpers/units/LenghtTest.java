package org.droidplanner.core.helpers.units;

import junit.framework.TestCase;

import org.droidplanner.core.helpers.units.Length;

public class LenghtTest extends TestCase {
	private static final double TOLERANCE = 1e-8;

	private Length length;

	protected void setUp() throws Exception {
		super.setUp();
		length = new Length(1.0);
	}

	public void testValueInMeters() {
		assertEquals(1, length.valueInMeters(), TOLERANCE);
	}

	public void testSet() {
		length.set(2);
		assertEquals(2, length.valueInMeters(), TOLERANCE);
	}

	public void testToString() {
		length.set(1.0);
		assertEquals("1.0 m", length.toString());
		length.set(1.234);
		assertEquals("1.2 m", length.toString());
		length.set(10);
		assertEquals("10.0 m", length.toString());

		length.set(1000);
		assertEquals("1.0 km", length.toString());
		length.set(1234);
		assertEquals("1.2 km", length.toString());

		length.set(0.001);
		assertEquals("1.0 mm", length.toString());
		length.set(0.01234);
		assertEquals("12.3 mm", length.toString());

		length.set(1e-6);
		assertEquals("1.0E-6 m", length.toString());
	}

	public void testEquals() {
		assertTrue(length.equals(new Length(1.0)));
		assertFalse(length.equals(new Length(2.0)));
		assertFalse(length.equals(new Object()));
	}

}
