package org.droidplanner.services.android.impl.core.survey;

import com.MAVLink.ardupilotmega.msg_camera_feedback;
import com.o3dr.services.android.lib.coordinate.LatLong;

import junit.framework.TestCase;

import java.util.List;

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
        Footprint footprint = new Footprint(camera, (100));

        assertEquals(120.0, footprint.getLateralSize(), 0.001);
        assertEquals(80.0, footprint.getLongitudinalSize(), 0.001);
    }

    public void testGSD() throws Exception {
        Footprint footprint = new Footprint(camera, (100));

        assertEquals(0.023238, footprint.getGSD(), 0.01);
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

        List<LatLong> vertex = footprint.getVertexInGlobalFrame();
        assertEquals(new LatLong(-6.351048290444335E-4, 1.271409658088867E-4), vertex.get(0));
        assertEquals(new LatLong(1.271409658088868E-4, -6.35104829044434E-4), vertex.get(1));
        assertEquals(new LatLong(6.353048290444339E-4, -1.2694096580888691E-4), vertex.get(2));
        assertEquals(new LatLong(-1.269409658088866E-4, 6.353048290444336E-4), vertex.get(3));
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

        List<LatLong> vertex = footprint.getVertexInGlobalFrame();
        assertEquals(new LatLong(-7.664157697114501E-5, -1.4395576628291166E-4), vertex.get(0));
        assertEquals(new LatLong(0.001167096458779719, -0.0010463895304861626), vertex.get(1));
        assertEquals(new LatLong(0.0020605706434187595, 5.362476139283517E-6), vertex.get(2));
        assertEquals(new LatLong(1.8929947996117644E-4, 4.296458546032376E-4), vertex.get(3));
    }

}
