package ellipsoidFit;

/**
 * A representation of a three space point with double precision.
 * 
 * Apache License Version 2.0, January 2004.
 * 
 * @author Kaleb
 * @version 1.0
 * 
 */
public class ThreeSpacePoint
{
	public double x;
	public double y;
	public double z;

	/**
	 * Instantiate a new object.
	 * @param x the point on the x-axis
	 * @param y the point on the y-axis
	 * @param z the point on the z-axis
	 */
	public ThreeSpacePoint(double x, double y, double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
}
