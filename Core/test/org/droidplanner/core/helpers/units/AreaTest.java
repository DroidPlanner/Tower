package org.droidplanner.core.helpers.units;

import junit.framework.TestCase;

public class AreaTest extends TestCase {

	private Area oneSqMeter;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		oneSqMeter = new Area(1.0);
	}

	public void testValueInSqMeters() {
		assertEquals(1.0, oneSqMeter.valueInSqMeters());
	}

	public void testSet() {
		oneSqMeter.set(2.3);
		assertEquals(2.3, oneSqMeter.valueInSqMeters());
	}

	public void testToString() {
		assertEquals("1.0 m²", oneSqMeter.toString());

		oneSqMeter.set(1e6);
		assertEquals("1.0 km²", oneSqMeter.toString());

		oneSqMeter.set(1e-4);
		assertEquals("1.00 cm²", oneSqMeter.toString());

		oneSqMeter.set(1e-10);
		assertEquals("1.0E-10 m²", oneSqMeter.toString());
	}

}
