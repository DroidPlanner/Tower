package com.o3dr.android.client.apis;

import android.content.Context;
import android.os.Bundle;
import android.util.SparseArray;

import com.o3dr.services.android.lib.model.AbstractCommandListener;
import com.o3dr.services.android.lib.model.action.Action;

import junit.framework.Assert;

import org.droidplanner.services.android.impl.mock.MockDrone;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;

import static com.o3dr.services.android.lib.drone.action.ControlActions.ACTION_SET_VELOCITY;
import static com.o3dr.services.android.lib.drone.action.ControlActions.EXTRA_VELOCITY_X;
import static com.o3dr.services.android.lib.drone.action.ControlActions.EXTRA_VELOCITY_Y;
import static com.o3dr.services.android.lib.drone.action.ControlActions.EXTRA_VELOCITY_Z;

/**
 * Created by Fredia Huya-Kouadio on 10/23/15.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = com.o3dr.android.client.BuildConfig.class, sdk = 18)
public class ControlApiTest {

    private static final SparseArray<float[][]> expectedVelocitiesPerAttitude = new SparseArray<>();

    static {
        expectedVelocitiesPerAttitude.append(0, new float[][]{{1f, 0f, 1f}, {1f, 0f, 1f}});
        expectedVelocitiesPerAttitude.append(45, new float[][]{{1f, 1f, 1f}, {0f, (float) Math.sqrt(2), 1f}});
        expectedVelocitiesPerAttitude.append(90, new float[][]{{1f, 1f, 1f}, {-1f, 1f, 1f}});
        expectedVelocitiesPerAttitude.append(135, new float[][]{{1f, 1f, 1f}, {-(float) Math.sqrt(2), 0, 1f}});
        expectedVelocitiesPerAttitude.append(180, new float[][]{{1f, 1f, 1f}, {-1f, -1f, 1f}});
        expectedVelocitiesPerAttitude.append(225, new float[][]{{1f, 1f, 1f}, {0f, -(float) Math.sqrt(2), 1f}});
        expectedVelocitiesPerAttitude.append(270, new float[][]{{1f, 1f, 1f}, {1f, -1f, 1f}});
        expectedVelocitiesPerAttitude.append(315, new float[][]{{1f, 1f, 1f}, {(float) Math.sqrt(2), 0, 1f}});
        expectedVelocitiesPerAttitude.append(360, new float[][]{{1f, 1f, 1f}, {1f, 1f, 1f}});
    }

    /**
     * Tests the ControlApi#manualControl(...) method.
     * Ensures the method correctly interpret its given parameters.
     *
     * @throws Exception
     */
    @Test
    public void testMoveAtVelocity() throws Exception {
        final Context context = ShadowApplication.getInstance().getApplicationContext();
        final MockDrone mockDrone = new MockDrone(context) {
            @Override
            public boolean performAsyncActionOnDroneThread(Action action, AbstractCommandListener listener) {
                this.asyncAction = action;
                return true;
            }

        };

        final ControlApi controlApi = ControlApi.getApi(mockDrone);

        //Test with the EARTH NED coordinate frame. What goes in should be what comes out.
        final int testCount = 100;
        for (int i = 0; i < testCount; i++) {
            final float randomX = (float) ((Math.random() * 2) - 1f);
            final float randomY = (float) ((Math.random() * 2) - 1f);
            final float randomZ = (float) ((Math.random() * 2) - 1f);

            controlApi.manualControl(randomX, randomY, randomZ, null);

            Assert.assertTrue(mockDrone.getAsyncAction().getType().equals(ACTION_SET_VELOCITY));

            Bundle params = mockDrone.getAsyncAction().getData();
            Assert.assertEquals(params.getFloat(EXTRA_VELOCITY_X), randomX, 0.001);
            Assert.assertEquals(params.getFloat(EXTRA_VELOCITY_Y), randomY, 0.001);
            Assert.assertEquals(params.getFloat(EXTRA_VELOCITY_Z), randomZ, 0.001);
        }

//        //Test with the VEHICLE coordinate frame. The output is dependent on the vehicle attitude data.
//        final Attitude attitude = new Attitude();
//        final int expectedValuesCount = expectedVelocitiesPerAttitude.size();
//        for(int i = 0; i < expectedValuesCount; i++) {
//            final int yaw = expectedVelocitiesPerAttitude.keyAt(i);
//            final float[][] paramsAndResults = expectedVelocitiesPerAttitude.valueAt(i);
//            final float[] params = paramsAndResults[0];
//
//            attitude.setYaw(yaw);
//            mockDrone.setAttribute(AttributeType.ATTITUDE, attitude);
//
//            controlApi.manualControl(ControlApi.VEHICLE_COORDINATE_FRAME, params[0], params[1], params[2], null);
//
//            Assert.assertTrue(mockDrone.getAsyncAction().getType().equals(ACTION_SET_VELOCITY));
//
//            final float[] expectedValues = paramsAndResults[1];
//            Bundle data = mockDrone.getAsyncAction().getData();
//            Assert.assertEquals("Invalid x velocity for attitude = " + yaw, data.getFloat(EXTRA_VELOCITY_X), expectedValues[0], 0.001);
//            Assert.assertEquals("Invalid y velocity for attitude = " + yaw, data.getFloat(EXTRA_VELOCITY_Y), expectedValues[1], 0.001);
//            Assert.assertEquals("Invalid z velocity for attitude = " + yaw, data.getFloat(EXTRA_VELOCITY_Z), expectedValues[2], 0.001);
//        }
    }
}