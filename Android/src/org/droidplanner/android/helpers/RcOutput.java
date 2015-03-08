package org.droidplanner.android.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.MAVLink.common.msg_rc_channels_override;
import com.o3dr.android.client.Drone;
import com.o3dr.android.client.apis.drone.ExperimentalApi;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.Parameter;
import com.o3dr.services.android.lib.mavlink.MavlinkMessageWrapper;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RcOutput {
	private static final int DISABLE_OVERRIDE = 0;
	private Context parrentContext;
	private ScheduledExecutorService scheduleTaskExecutor;
	private Drone drone;
	public int[] rcOutputs = new int[8];

	public RcOutput(Drone drone, Context context) {
		this.drone = drone;
		parrentContext = context.getApplicationContext();
	}

	public void disableRcOverride() {
		if (isRcOverrided()) {
			scheduleTaskExecutor.shutdownNow();
			scheduleTaskExecutor = null;
		}
		Arrays.fill(rcOutputs, DISABLE_OVERRIDE); // Start with all channels
													// disabled, external
													// callers can enable them
													// as desired
		sendRcOverrideMsg(drone, rcOutputs); // Just to be sure send 3
														// disable
		sendRcOverrideMsg(drone, rcOutputs);
		sendRcOverrideMsg(drone, rcOutputs);
	}

	public void enableRcOverride() {
		if (!isRcOverrided()) {
			Arrays.fill(rcOutputs, DISABLE_OVERRIDE);
			sendRcOverrideMsg(drone, rcOutputs); // Just to be sure
															// send 3
			sendRcOverrideMsg(drone, rcOutputs);
			sendRcOverrideMsg(drone, rcOutputs);
			Arrays.fill(rcOutputs, DISABLE_OVERRIDE);
			scheduleTaskExecutor = Executors.newScheduledThreadPool(5);
			scheduleTaskExecutor.scheduleWithFixedDelay(new Runnable() {
				@Override
				public void run() {
					sendRcOverrideMsg(drone, rcOutputs);
				}
			}, 0, getRcOverrideDelayMs(), TimeUnit.MILLISECONDS);
		}
	}

	private int getRcOverrideDelayMs() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(parrentContext);
		int rate = Integer.parseInt(prefs.getString("pref_mavlink_stream_rate_RC_override", "0"));
		if ((rate > 1) & (rate < 500)) {
			return 1000 / rate;
		} else {
			return 20;
		}
	}

	public boolean isRcOverrided() {
		return (scheduleTaskExecutor != null);
	}

    public void setRcChannel(int ch, float value) {
        rcOutputs[ch] = (int) value;
    }

    public static void sendRcOverrideMsg(Drone drone, int[] rcOutputs) {
        msg_rc_channels_override msg = new msg_rc_channels_override();
        msg.chan1_raw = (short) rcOutputs[0];
        msg.chan2_raw = (short) rcOutputs[1];
        msg.chan3_raw = (short) rcOutputs[2];
        msg.chan4_raw = (short) rcOutputs[3];
        msg.chan5_raw = (short) rcOutputs[4];
        msg.chan6_raw = (short) rcOutputs[5];
        msg.chan7_raw = (short) rcOutputs[6];
        msg.chan8_raw = (short) rcOutputs[7];
        msg.target_system = 1;
        drone.sendMavlinkMessage(new MavlinkMessageWrapper(msg));
    }
}
