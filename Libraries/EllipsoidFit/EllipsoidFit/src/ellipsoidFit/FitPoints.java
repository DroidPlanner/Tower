package ellipsoidFit;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;

/**
 * Determines the center, radii, eigenvalues and eigenvectors of the ellipse
 * using an algorithm from Yury Petrov's Ellipsoid Fit MATLAB script. The
 * algorithm fits points from an ellipsoid to the polynomial expression Ax^2 +
 * By^2 + Cz^2 + 2Dxy + 2Exz + 2Fyz + 2Gx + 2Hy + 2Iz = 1. The polynomial
 * expression is then solved and the center and radii of the ellipse are
 * determined.
 * 
 * Caveat Emptor: The polynomial expression not guaranteed to be one of an
 * ellipsoid. It could result in any quadric (hyperboloid, paraboloid, etc). If
 * the data is to sparse, the values of the eigenvectors could be reversed
 * resulting in a fit that is not an ellipsoid.
 * 
 * Apache License Version 2.0, January 2004.
 * 
 * @author Kaleb
 * @version 1.0
 * @see http://www.mathworks.com/matlabcentral/fileexchange/24693-ellipsoid-fit
 * 
 */
public class FitPoints
{
	public RealVector center;
	public RealVector radii;
	public RealVector evecs;
	public RealVector evecs1;
	public RealVector evecs2;

	public double[] evals;
	
	/**
	 * Fit points to the polynomial expression Ax^2 + By^2 + Cz^2 + 2Dxy + 2Exz
	 * + 2Fyz + 2Gx + 2Hy + 2Iz = 1 and determine the center and radii of the
	 * fit ellipsoid.
	 * 
	 * @param points
	 *            the points to be fit to the ellipsoid.
	 */
	public void fitEllipsoid(ArrayList<ThreeSpacePoint> points)
	{
		// Fit the points to Ax^2 + By^2 + Cz^2 + 2Dxy + 2Exz
		// + 2Fyz + 2Gx + 2Hy + 2Iz = 1 and solve the system.
		// v = (( d' * d )^-1) * ( d' * ones.mapAddToSelf(1));
		RealVector v = solveSystem(points);

		// Form the algebraic form of the ellipsoid.
		RealMatrix a = formAlgebraicMatrix(v);

		// Find the center of the ellipsoid.
		center = findCenter(a);

		// Translate the algebraic form of the ellipsoid to the center.
		RealMatrix r = translateToCenter(center, a);

		// Generate a submatrix of r.
		RealMatrix subr = r.getSubMatrix(0, 2, 0, 2);

		// subr[i][j] = subr[i][j] / -r[3][3]).
		double divr = -r.getEntry(3, 3);
		for (int i = 0; i < subr.getRowDimension(); i++)
		{
			for (int j = 0; j < subr.getRowDimension(); j++)
			{
				subr.setEntry(i, j, subr.getEntry(i, j) / divr);
			}
		}

		// Get the eigenvalues and eigenvectors.
		EigenDecomposition ed = new EigenDecomposition(subr, 0);
		evals = ed.getRealEigenvalues();
		evecs = ed.getEigenvector(0);
		evecs1 = ed.getEigenvector(1);
		evecs2 = ed.getEigenvector(2);

		// Find the radii of the ellipsoid.
		radii = findRadii(evals);
	}

	/**
	 * Solve the polynomial expression Ax^2 + By^2 + Cz^2 + 2Dxy + 2Exz + 2Fyz +
	 * 2Gx + 2Hy + 2Iz from the provided points.
	 * 
	 * @param points
	 *            the points that will be fit to the polynomial expression.
	 * @return the solution vector to the polynomial expression.
	 */
	private RealVector solveSystem(ArrayList<ThreeSpacePoint> points)
	{
		// determine the number of points
		int numPoints = points.size();

		// the design matrix
		// size: numPoints x 9
		RealMatrix d = new Array2DRowRealMatrix(numPoints, 9);

		// Fit the ellipsoid in the form of
		// Ax^2 + By^2 + Cz^2 + 2Dxy + 2Exz + 2Fyz + 2Gx + 2Hy + 2Iz
		for (int i = 0; i < d.getRowDimension(); i++)
		{
			double xx = Math.pow(points.get(i).x, 2);
			double yy = Math.pow(points.get(i).y, 2);
			double zz = Math.pow(points.get(i).z, 2);
			double xy = 2 * (points.get(i).x * points.get(i).y);
			double xz = 2 * (points.get(i).x * points.get(i).z);
			double yz = 2 * (points.get(i).y * points.get(i).z);
			double x = 2 * points.get(i).x;
			double y = 2 * points.get(i).y;
			double z = 2 * points.get(i).z;

			d.setEntry(i, 0, xx);
			d.setEntry(i, 1, yy);
			d.setEntry(i, 2, zz);
			d.setEntry(i, 3, xy);
			d.setEntry(i, 4, xz);
			d.setEntry(i, 5, yz);
			d.setEntry(i, 6, x);
			d.setEntry(i, 7, y);
			d.setEntry(i, 8, z);
		}

		// solve the normal system of equations
		// v = (( d' * d )^-1) * ( d' * ones.mapAddToSelf(1));

		// Multiply: d' * d
		RealMatrix dtd = d.transpose().multiply(d);

		// Create a vector of ones.
		RealVector ones = new ArrayRealVector(numPoints);
		ones.mapAddToSelf(1);

		// Multiply: d' * ones.mapAddToSelf(1)
		RealVector dtOnes = d.transpose().operate(ones);

		// Find ( d' * d )^-1
		DecompositionSolver solver = new SingularValueDecomposition(dtd)
				.getSolver();
		RealMatrix dtdi = solver.getInverse();

		// v = (( d' * d )^-1) * ( d' * ones.mapAddToSelf(1));
		RealVector v = dtdi.operate(dtOnes);

		return v;
	}

	/**
	 * Create a matrix in the algebraic form of the polynomial Ax^2 + By^2 +
	 * Cz^2 + 2Dxy + 2Exz + 2Fyz + 2Gx + 2Hy + 2Iz = 1.
	 * 
	 * @param v
	 *            the vector polynomial.
	 * @return the matrix of the algebraic form of the polynomial.
	 */
	private RealMatrix formAlgebraicMatrix(RealVector v)
	{
		// a =
		// [ Ax^2 2Dxy 2Exz 2Gx ]
		// [ 2Dxy By^2 2Fyz 2Hy ]
		// [ 2Exz 2Fyz Cz^2 2Iz ]
		// [ 2Gx 2Hy 2Iz -1 ] ]
		RealMatrix a = new Array2DRowRealMatrix(4, 4);

		a.setEntry(0, 0, v.getEntry(0));
		a.setEntry(0, 1, v.getEntry(3));
		a.setEntry(0, 2, v.getEntry(4));
		a.setEntry(0, 3, v.getEntry(6));
		a.setEntry(1, 0, v.getEntry(3));
		a.setEntry(1, 1, v.getEntry(1));
		a.setEntry(1, 2, v.getEntry(5));
		a.setEntry(1, 3, v.getEntry(7));
		a.setEntry(2, 0, v.getEntry(4));
		a.setEntry(2, 1, v.getEntry(5));
		a.setEntry(2, 2, v.getEntry(2));
		a.setEntry(2, 3, v.getEntry(8));
		a.setEntry(3, 0, v.getEntry(6));
		a.setEntry(3, 1, v.getEntry(7));
		a.setEntry(3, 2, v.getEntry(8));
		a.setEntry(3, 3, -1);

		return a;
	}

	/**
	 * Find the center of the ellipsoid.
	 * 
	 * @param a
	 *            the algebraic from of the polynomial.
	 * @return a vector containing the center of the ellipsoid.
	 */
	private RealVector findCenter(RealMatrix a)
	{
		RealMatrix subA = a.getSubMatrix(0, 2, 0, 2);

		for (int q = 0; q < subA.getRowDimension(); q++)
		{
			for (int s = 0; s < subA.getColumnDimension(); s++)
			{
				subA.multiplyEntry(q, s, -1.0);
			}
		}

		RealVector subV = a.getRowVector(3).getSubVector(0, 3);

		// inv (dtd)
		DecompositionSolver solver = new SingularValueDecomposition(subA)
				.getSolver();
		RealMatrix subAi = solver.getInverse();

		return subAi.operate(subV);
	}

	/**
	 * Translate the algebraic form of the ellipsoid to the center.
	 * 
	 * @param center
	 *            vector containing the center of the ellipsoid.
	 * @param a
	 *            the algebraic form of the polynomial.
	 * @return the center translated form of the algebraic ellipsoid.
	 */
	private RealMatrix translateToCenter(RealVector center, RealMatrix a)
	{
		// Form the corresponding translation matrix.
		RealMatrix t = MatrixUtils.createRealIdentityMatrix(4);

		RealMatrix centerMatrix = new Array2DRowRealMatrix(1, 3);

		centerMatrix.setRowVector(0, center);

		t.setSubMatrix(centerMatrix.getData(), 3, 0);

		// Translate to the center.
		RealMatrix r = t.multiply(a).multiply(t.transpose());

		return r;
	}


	
	/**
	 * Find the radii of the ellipsoid in ascending order.
	 * @param evals the eigenvalues of the ellipsoid.
	 * @return the radii of the ellipsoid.
	 */
	private RealVector findRadii(double[] evals)
	{
		RealVector radii = new ArrayRealVector(evals.length);

		// radii[i] = sqrt(1/eval[i]);
		for (int i = 0; i < evals.length; i++)
		{
			radii.setEntry(i, Math.sqrt(1 / evals[i]));
		}
		
		return radii;
	}

	private void printLog()
	{
		System.out.println("");
	
		for (int i = 0; i < evals.length; i++)
		{
			System.out.println(Arrays.toString(evals));
		}
		System.out.println(evecs.toString());
		System.out.println(evecs1.toString());
		System.out.println(evecs2.toString());

		System.out.print("Center: " + center.toString());
		System.out.print(" Radii: " + radii.toString());
	}

	public double getFitness() {
		double x = evecs.getEntry(2);
		double y = evecs1.getEntry(0);
		double z = evecs2.getEntry(1);
		return Math.sqrt(x*x+y*y+z*z)/Math.sqrt(3);
	}
}