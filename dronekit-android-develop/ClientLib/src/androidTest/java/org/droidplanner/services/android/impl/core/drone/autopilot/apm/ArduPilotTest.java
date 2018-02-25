package org.droidplanner.services.android.impl.core.drone.autopilot.apm;

import com.github.zafarkhaja.semver.Version;

import junit.framework.TestCase;

/**
 * Unit tests for ArduPilot.
 */
public class ArduPilotTest extends TestCase {
    public void testExtractVersionNumber() throws Exception {
        Version version = ArduPilot.extractVersionNumber("APM:Copter V3.3");
        assertEquals(3, version.getMajorVersion());
        assertEquals(3, version.getMinorVersion());
        assertEquals(0, version.getPatchVersion());

        version = ArduPilot.extractVersionNumber("APM:Plane V3.3");
        assertEquals(3, version.getMajorVersion());
        assertEquals(3, version.getMinorVersion());
        assertEquals(0, version.getPatchVersion());

        version = ArduPilot.extractVersionNumber("APM:Rover V3.3");
        assertEquals(3, version.getMajorVersion());
        assertEquals(3, version.getMinorVersion());
        assertEquals(0, version.getPatchVersion());

        version = ArduPilot.extractVersionNumber("ArduCopter 3.3.0");
        assertEquals(3, version.getMajorVersion());
        assertEquals(3, version.getMinorVersion());
        assertEquals(0, version.getPatchVersion());

        version = ArduPilot.extractVersionNumber("ArduPlane 3.3.0");
        assertEquals(3, version.getMajorVersion());
        assertEquals(3, version.getMinorVersion());
        assertEquals(0, version.getPatchVersion());

        version = ArduPilot.extractVersionNumber("ArduRover 3.3.0");
        assertEquals(3, version.getMajorVersion());
        assertEquals(3, version.getMinorVersion());
        assertEquals(0, version.getPatchVersion());

        version = ArduPilot.extractVersionNumber("Invalid Version");
        assertEquals(0, version.getMajorVersion());
        assertEquals(0, version.getMinorVersion());
        assertEquals(0, version.getPatchVersion());
    }
}