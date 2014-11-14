package org.droidplanner.core.helpers.math;

import junit.framework.TestCase;

public class MathUtilTest extends TestCase {

	public void testNormalize() {
		double max = 10;
		double min = 0;
		assertEquals(0.0, MathUtil.Normalize(0, min, max));
		assertEquals(0.5, MathUtil.Normalize(5, min, max));
		assertEquals(1.0, MathUtil.Normalize(10, min, max));

		assertEquals(0.0, MathUtil.Normalize(-1, min, max));
		assertEquals(1.0, MathUtil.Normalize(100, min, max));
	}

	public void testIfCanCreateObject() {
		assertNotNull(new MathUtil());
	}
	
	public void testDCMmatrix(){
		double [][] dcm = MathUtil.dcmFromEuler(0,0,0);
		double [][] expected = new double[][] {{1,0,0},{0,1,0},{0,0,1}};
		
		for (int i = 0; i < dcm.length; i++) {
			for (int j = 0; j < dcm.length; j++) {
				assertEquals(expected[i][j], dcm[i][j],1e-10);
			}
		}
	}

}
