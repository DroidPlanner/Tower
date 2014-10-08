package ellipsoidFit;

import java.util.ArrayList;
import java.util.Random;

/**
 * Generate the points of an ellipse with the specified parameters.
 * 
 * Apache License Version 2.0, January 2004.
 * 
 * @author Kaleb
 * @version 1.0
 * 
 */
public class GeneratePoints
{

	/**
	 * Generate the points of an ellipse with the specified parameters.
	 * 
	 * @param a
	 *            the radii of the x-axis.
	 * @param b
	 *            the radii of the y-axis.
	 * @param c
	 *            the radii of the z-axis.
	 * @param shiftx
	 *            the shift from center on the x-axis.
	 * @param shifty
	 *            the shift from center on the y-axis.
	 * @param shiftz
	 *            the shift from center on the z-axis.
	 * @param noiseIntensity
	 *            a base value for the intensity of the noise, 0 = no noise.
	 */
	public ArrayList<ThreeSpacePoint> generatePoints(double a, double b,
			double c, double shiftx, double shifty, double shiftz,
			double noiseIntensity)
	{
		ArrayList<ThreeSpacePoint> points = new ArrayList<ThreeSpacePoint>();
		double[] x;
		double[] y;
		double[] z;

		int numPoints = 1000;

		x = new double[numPoints];
		y = new double[numPoints];
		z = new double[numPoints];
		Random r = new Random();

		for (int i = 0; i < numPoints; i++)
		{
			double s = Math.toRadians(r.nextInt(360));
			double t = Math.toRadians(r.nextInt(360));

			x[i] = a * Math.cos(s) * Math.cos(t);
			y[i] = b * Math.cos(s) * Math.sin(t);
			z[i] = c * Math.sin(s);
		}

		double angle = Math.toRadians((Math.PI / 6));

		double[] xt = new double[numPoints];
		double[] yt = new double[numPoints];

		for (int i = 0; i < numPoints; i++)
		{
			xt[i] = x[i] * Math.cos(angle) - y[i] * Math.sin(angle);
			yt[i] = x[i] * Math.sin(angle) + y[i] * Math.cos(angle);
		}

		for (int i = 0; i < numPoints; i++)
		{
			x[i] = xt[i] + shiftx;
			y[i] = yt[i] + shifty;
			z[i] = z[i] + shiftz;
		}

		for (int i = 0; i < numPoints; i++)
		{
			x[i] = x[i] + r.nextDouble() * noiseIntensity;
			y[i] = y[i] + r.nextDouble() * noiseIntensity;
			z[i] = z[i] + r.nextDouble() * noiseIntensity;
		}

		ThreeSpacePoint tsp;

		for (int i = 0; i < numPoints; i++)
		{
			tsp = new ThreeSpacePoint(x[i], y[i], z[i]);
			points.add(tsp);
		}

		return points;
	}
}