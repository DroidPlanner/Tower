package ellipsoidFit;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Creates an example of fitting a sphere with center = [0,0,0] and radii =
 * [1,1,1] and an ellipsoid with with center = [2,2,2] and radii = [1.4,1.3,1.2]
 * 
 * Apache License Version 2.0, January 2004.
 * 
 * @author Kaleb
 * @version 1.0
 * 
 */
public class EllipsoidFitSample
{
	static ArrayList<ThreeSpacePoint> CONTROL_SPHERE_POINTS;
	static ArrayList<ThreeSpacePoint> CONTROL_ELLIPSOID_POINTS;

	// Draw a control sphere with center = [0,0,0] and radii = [1,1,1]
	static double A_CONTROL_SPHERE = 1;
	static double B_CONTROL_SPHERE = 1;
	static double C_CONTROL_SPHERE = 1;
	static double SHIFT_X_CONTROL_SPHERE = 0;
	static double SHIFT_Y_CONTROL_SPHERE = 0;
	static double SHIFT_Z_CONTROL_SPHERE = 0;

	// Draw a control ellipsoid != control sphere.
	// This ellipsoid will be scaled back to the sphere
	static double A_CONTROL_ELLIPSE = 1.4;
	static double B_CONTROL_ELLIPSE = 1.3;
	static double C_CONTROL_ELLIPSE = 1.2;
	static double SHIFT_X_CONTROL_ELLIPSE = 2;
	static double SHIFT_Y_CONTROL_ELLIPSE = 2;
	static double SHIFT_Z_CONTROL_ELLIPSE = 2;

	// The jzy3D plotter isn't good about creating square
	// charts and you can't set the bounds manually, so
	// the dirty workaround is to just create two dummy
	// points at the max and min bounds and plot them.
	static double BOUNDS_MAX = 4;
	static double BOUNDS_MIN = -4;

	static double NOISE_INTENSITY = 0.01;

	// Create a chart and add scatter.

	// Generates points for plots.
	GeneratePoints pointGenerator = new GeneratePoints();

	// Scale the ellipsoid into a sphere.

	public EllipsoidFitSample()
	{

		// Generate the random points for the control ellipsoid.
		CONTROL_ELLIPSOID_POINTS = pointGenerator.generatePoints(
				A_CONTROL_ELLIPSE, B_CONTROL_ELLIPSE, C_CONTROL_ELLIPSE,
				SHIFT_X_CONTROL_ELLIPSE, SHIFT_Y_CONTROL_ELLIPSE,
				SHIFT_Z_CONTROL_ELLIPSE, NOISE_INTENSITY);

		// Generate the random points for the control sphere.
		CONTROL_SPHERE_POINTS = pointGenerator
				.generatePoints(A_CONTROL_SPHERE, B_CONTROL_SPHERE,
						C_CONTROL_SPHERE, SHIFT_X_CONTROL_SPHERE,
						SHIFT_Y_CONTROL_SPHERE, SHIFT_Z_CONTROL_SPHERE,
						NOISE_INTENSITY);

		// Fit the ellipsoid points to a polynomial
		FitPoints ellipsoidFit = new FitPoints();
		ellipsoidFit.fitEllipsoid(CONTROL_ELLIPSOID_POINTS);

		// Fit the ellipsoid points to a polynomial
		FitPoints sphereFit = new FitPoints();
		sphereFit.fitEllipsoid(CONTROL_SPHERE_POINTS);

		log(ellipsoidFit, "Ellipsoid");
		log(sphereFit, "Sphere");
	}

	private void log(FitPoints points, String label)
	{
		System.out.println(label);
		System.out.println("Center: " + points.center.toString());
		System.out.println("Radii: " + points.radii.toString());
		System.out.println("Eigenvalues: " + Arrays.toString(points.evals));
		System.out.println("Eigenvector 0: " + points.evecs.toString());
		System.out.println("Eigenvector 1: " + points.evecs1.toString());
		System.out.println("Eigenvector 2: " + points.evecs2.toString());
		System.out.println();
	}
}
