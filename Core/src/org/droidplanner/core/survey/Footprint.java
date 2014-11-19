package org.droidplanner.core.survey;

import java.util.ArrayList;
import java.util.List;

import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.geoTools.GeoTools;
import org.droidplanner.core.helpers.math.MathUtil;
import org.droidplanner.core.helpers.units.Altitude;
import org.droidplanner.core.helpers.units.Length;

import com.MAVLink.ardupilotmega.msg_camera_feedback;

public class Footprint {
	/**
	 * Vertex of the footprint in local frame index 0 is top right, where top is
	 * direction of longitudinal travel. Index increases CCW
	 */
	private List<Coord2D> vertex = new ArrayList<Coord2D>();
	private double meanGSD;

	public Footprint(CameraInfo camera, Altitude altitude) {
		this(camera,new Coord2D(0,0),(float) altitude.valueInMeters(),0,0,0);
	}

	public Footprint(CameraInfo camera, msg_camera_feedback msg) {
		this(camera, new Coord2D(msg.lat / 1E7, msg.lng / 1E7), msg.alt_rel, msg.pitch, msg.roll, msg.yaw);
	}

	public Footprint(CameraInfo camera, Coord2D center, double alt, double pitch, double roll, double yaw) {
		double sx = camera.getSensorLateralSize() / 2;
		double sy = camera.getSensorLongitudinalSize() / 2;
		double f = camera.focalLength;
		double[][] dcm = MathUtil.dcmFromEuler(Math.toRadians(pitch), Math.toRadians(-roll+180), Math.toRadians(-yaw));
		vertex.add(cameraFrameToLocalFrame(new Coord2D(-sx, -sy), dcm, alt, f, center));
		vertex.add(cameraFrameToLocalFrame(new Coord2D(+sx, -sy), dcm, alt, f, center));
		vertex.add(cameraFrameToLocalFrame(new Coord2D(+sx, +sy), dcm, alt, f, center));
		vertex.add(cameraFrameToLocalFrame(new Coord2D(-sx, +sy), dcm, alt, f, center));

		meanGSD = 0.001 * getLateralSize().valueInMeters() * (sy / sx)
				/ Math.sqrt(camera.sensorResolution);
	}

	/**
	 * based on http://www.asprs.org/a/publications/pers/2005journal/july/2005_july_863-871.pdf 
	 */
	static private Coord2D cameraFrameToLocalFrame(Coord2D img, double[][] dcm, double alt,
			double focalLength, Coord2D center) {
		double x = alt
				* (dcm[0][0] * img.getX() + dcm[1][0] * img.getY() + dcm[2][0] * (-focalLength))
				/ (dcm[0][2] * img.getX() + dcm[1][2] * img.getY() + dcm[2][2] * (-focalLength));
		double y = alt
				* (dcm[0][1] * img.getX() + dcm[1][1] * img.getY() + dcm[2][1] * (-focalLength))
				/ (dcm[0][2] * img.getX() + dcm[1][2] * img.getY() + dcm[2][2] * (-focalLength));

		return GeoTools.moveCoordinate(center, x, y);
	}
	
	public Length getLateralSize() {
		return new Length(
				(GeoTools.getDistance(vertex.get(0), vertex.get(1)).valueInMeters() + GeoTools
						.getDistance(vertex.get(2), vertex.get(3)).valueInMeters()) / 2);
	}

	public Length getLongitudinalSize() {
		return new Length(
				(GeoTools.getDistance(vertex.get(0), vertex.get(3)).valueInMeters() + GeoTools
						.getDistance(vertex.get(1), vertex.get(2)).valueInMeters()) / 2);
	}

	public List<Coord2D> getVertexInGlobalFrame() {
		return vertex;
	}

	public double getGSD() {
		return meanGSD;
	}
}
