package org.droidplanner.android.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.droidplanner.core.MAVLink.MavLinkRC;
import org.droidplanner.core.model.Drone;

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
		parrentContext = context;
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
		MavLinkRC.sendRcOverrideMsg(drone, rcOutputs); // Just to be sure send 3
														// disable
		MavLinkRC.sendRcOverrideMsg(drone, rcOutputs);
		MavLinkRC.sendRcOverrideMsg(drone, rcOutputs);
	}

	public void enableRcOverride() {
		if (!isRcOverrided()) {
			Arrays.fill(rcOutputs, DISABLE_OVERRIDE);
			MavLinkRC.sendRcOverrideMsg(drone, rcOutputs); // Just to be sure
															// send 3
			MavLinkRC.sendRcOverrideMsg(drone, rcOutputs);
			MavLinkRC.sendRcOverrideMsg(drone, rcOutputs);
			Arrays.fill(rcOutputs, DISABLE_OVERRIDE);
			scheduleTaskExecutor = Executors.newScheduledThreadPool(5);
			scheduleTaskExecutor.scheduleWithFixedDelay(new Runnable() {
				@Override
				public void run() {
					MavLinkRC.sendRcOverrideMsg(drone, rcOutputs);
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

}
