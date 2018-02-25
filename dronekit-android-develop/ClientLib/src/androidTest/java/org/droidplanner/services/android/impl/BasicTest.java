package org.droidplanner.services.android.impl;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.common.msg_command_long;
import com.MAVLink.enums.MAV_CMD;
import com.o3dr.android.client.BuildConfig;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.gcs.link.LinkConnectionStatus;

import org.junit.Assert;
import org.junit.Before;

import org.droidplanner.services.android.impl.communication.model.DataLink;
import org.droidplanner.services.android.impl.core.MAVLink.MavLinkCommands;
import org.droidplanner.services.android.impl.core.drone.LogMessageListener;
import org.droidplanner.services.android.impl.core.drone.autopilot.MavLinkDrone;
import org.droidplanner.services.android.impl.core.drone.autopilot.apm.ArduCopter;
import org.droidplanner.services.android.impl.core.firmware.FirmwareType;
import org.droidplanner.services.android.impl.mock.MockMAVLinkClient;
import org.droidplanner.services.android.impl.utils.AndroidApWarningParser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;


/**
 * Created by djmedina on 3/5/15.
 * This is a simple test case.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 18)
public class BasicTest {

    private MavLinkDrone drone;
    private MockMAVLinkClient mavClient;

    private final Handler dpHandler = new Handler();

    private final DataLink.DataLinkListener inputStreamListener = new DataLink.DataLinkListener() {
        @Override
        public void notifyReceivedData(Object packet) {
        }

        @Override
        public void onConnectionStatus(LinkConnectionStatus connectionStatus) {
        }
    };

    @Before
    public void setUp() throws Exception {
        final Context context = RuntimeEnvironment.application.getApplicationContext();

        ConnectionParameter connParams = new ConnectionParameter(0, new Bundle());
        mavClient = new MockMAVLinkClient(context, inputStreamListener, connParams);

        drone = new ArduCopter("test:" + FirmwareType.ARDU_COPTER.getType(), context, mavClient, dpHandler, new AndroidApWarningParser(), new LogMessageListener() {
            @Override
            public void onMessageLogged(int logLevel, String message) {

            }
        });
    }

    /**
     * The name 'test preconditions' is a convention to signal that if this
     * test doesn't pass, the test case was not set up properly and it might
     * explain any and all failures in other tests.  This is not guaranteed
     * to run before other tests, as junit uses reflection to find the tests.
     */
    @Test
    public void testPreconditions() {
    }

    /**
     * Basic MAVLink message test
     */
    @Test
    public void testArm() {
        MavLinkCommands.sendArmMessage(drone, true, false, null);
        MAVLinkPacket data = mavClient.getData();
        Assert.assertTrue(data != null);

        //Unpack the message into the right MAVLink message type
        MAVLinkMessage msg = data.unpack();
        msg_command_long longMsg = (msg_command_long) msg;

        //Validate the message
        Assert.assertEquals(MAV_CMD.MAV_CMD_COMPONENT_ARM_DISARM, longMsg.command);
        Assert.assertEquals(1f, longMsg.param1, 0.001);
    }
}
