package org.droidplanner.core.survey;

import java.util.List;

import junit.framework.TestCase;

import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.units.Altitude;

import com.MAVLink.Messages.ardupilotmega.msg_camera_feedback;

public class FootprintTest extends TestCase {

	private CameraInfo camera;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		camera = new CameraInfo();
		camera.focalLength = 5.0;
		camera.sensorWidth = 6.0;
		camera.sensorHeight = 4.0;
		camera.sensorResolution = 10.0;
	}

	public void testGenericFootprint() throws Exception {
		Footprint footprint = new Footprint(camera, new Altitude(100));
	
		assertEquals(120.0, footprint.getLateralSize().valueInMeters(),0.001);
		assertEquals(80.0, footprint.getLongitudinalSize().valueInMeters(),0.001);
	}
	
	public void testGSD() throws Exception {
		Footprint footprint = new Footprint(camera, new Altitude(100));
	
		assertEquals(0.023238, footprint.getGSD(),0.01);
	}

	public void testNadirFootprint() throws Exception {
		msg_camera_feedback msg = new msg_camera_feedback();
		msg.lng = 1;
		msg.lat = 1;
		msg.alt_rel = 100;
		msg.pitch = 0;
		msg.roll = 0;
		msg.yaw = 45;

		Footprint footprint = new Footprint(camera, msg);

		List<Coord2D> vertex = footprint.getVertexInGlobalFrame();
		assertEquals(new Coord2D(0, 0), vertex.get(0));
		assertEquals(new Coord2D(0, 0), vertex.get(1));
		assertEquals(new Coord2D(0, 0), vertex.get(2));
		assertEquals(new Coord2D(0, 0), vertex.get(3));
	}
	
	public void testSkewedFootprint() throws Exception {
		msg_camera_feedback msg = new msg_camera_feedback();
		msg.lng = 1;
		msg.lat = 1;
		msg.alt_rel = 100;
		msg.pitch = 10;
		msg.roll = 30;
		msg.yaw = 60;

		Footprint footprint = new Footprint(camera, msg);

		List<Coord2D> vertex = footprint.getVertexInGlobalFrame();
		assertEquals(vertex.get(0), new Coord2D(0, 0));
		assertEquals(vertex.get(1), new Coord2D(0, 0));
		assertEquals(vertex.get(2), new Coord2D(0, 0));
		assertEquals(vertex.get(3), new Coord2D(0, 0));
	}

}
