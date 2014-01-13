package org.droidplanner.helpers.units;

import junit.framework.*;

public class LenghtTest extends TestCase {
	private static final double TOLERANCE = 1e-8;

	private Length lenght;

	protected void setUp() throws Exception {
		super.setUp();
		lenght = new Length(1.0);
	}

	public void testValueInMeters() {
		assertEquals(lenght.valueInMeters(), 1, TOLERANCE);
	}

	public void testSet() {
		lenght.set(2);
		assertEquals(lenght.valueInMeters(), 2, TOLERANCE);
	}

	public void testToString() {
		lenght.set(1.0);
		assertEquals(lenght.toString(), "1.0 m");
		lenght.set(1.234);
		assertEquals(lenght.toString(), "1.2 m");
		lenght.set(10);
		assertEquals(lenght.toString(), "10.0 m");
		
		lenght.set(1000);
		assertEquals(lenght.toString(), "1.0 km");
		lenght.set(1234);
		assertEquals(lenght.toString(), "1.2 km");
		
		lenght.set(0.001);
		assertEquals(lenght.toString(), "1.0 mm");
		lenght.set(0.01234);
		assertEquals(lenght.toString(), "12.3 mm");
		
		lenght.set(1e-6);
		assertEquals(lenght.toString(), "1.0E-6 m");
	}
	
	public void testEquals(){
		assertTrue(lenght.equals(new Length(1.0)));
		assertFalse(lenght.equals(new Length(2.0)));
		assertFalse(lenght.equals(new Object()));
	}

}
