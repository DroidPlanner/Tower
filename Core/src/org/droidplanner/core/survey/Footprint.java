package org.droidplanner.core.survey;

import java.util.ArrayList;
import java.util.List;

import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.geoTools.GeoTools;
import org.droidplanner.core.helpers.math.MathUtil;
import org.droidplanner.core.helpers.units.Altitude;
import org.droidplanner.core.helpers.units.Length;
import org.droidplanner.core.mission.survey.CameraInfo;

import com.MAVLink.Messages.ardupilotmega.msg_camera_feedback;

public class Footprint {
	/**
	 * Vertex of the footprint in local frame index 0 is top right, where top is
	 * direction of longitudinal travel. Index increases CCW
	 */
	private List<Coord2D> vertex = new ArrayList<Coord2D>();
	private double meanGSD;

	public Footprint(CameraInfo camera, Altitude altitude) {
		this(camera, getFakeCameraMessage(altitude));
	}

	public Footprint(CameraInfo camera, msg_camera_feedback msg) {
		Coord2D center = new Coord2D(msg.lat / 1E7, msg.lng / 1E7);
		double[][] dcm = MathUtil.dcmFromEuler(Math.toRadians(msg.roll), Math.toRadians(msg.pitch), Math.toRadians(msg.yaw));
		double sx = camera.getSensorLateralSize() / 2;
		double sy = camera.getSensorLongitudinalSize() / 2;
		double f = camera.focalLength;
		float alt = msg.alt_rel;

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

	private static msg_camera_feedback getFakeCameraMessage(Altitude altitude) {
		msg_camera_feedback msg = new msg_camera_feedback();
		msg.alt_rel = (float) altitude.valueInMeters();
		msg.pitch = 0;
		msg.roll = 0;
		msg.yaw = 0;
		return msg;
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
