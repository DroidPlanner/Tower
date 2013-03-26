package com.droidplanner.helpers;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


import com.MAVLink.Messages.ardupilotmega.msg_rc_channels_override;
import com.droidplanner.service.MAVLinkClient;

public class RcOutput {
	private static final int DISABLE_OVERRIDE = 0;
	private static final int RC_TRIM = 1500;
	private ScheduledExecutorService scheduleTaskExecutor;
	private MAVLinkClient MAV;
	public int[] rcOutputs = new int[8];

	public RcOutput(MAVLinkClient MAV) {
		this.MAV = MAV;
	}

	public void disableRcOverride() {
		if (isRcOverrided()) {
			scheduleTaskExecutor.shutdownNow();
			scheduleTaskExecutor = null;
		}
		Arrays.fill(rcOutputs, DISABLE_OVERRIDE);
		sendRcOverrideMsg(); // Just to be sure send 3 disable
		sendRcOverrideMsg();
		sendRcOverrideMsg();
	}

	public void enableRcOverride() {
		if (!isRcOverrided()) {
			Arrays.fill(rcOutputs, RC_TRIM);
			scheduleTaskExecutor = Executors.newScheduledThreadPool(5);
			scheduleTaskExecutor.scheduleWithFixedDelay(new Runnable() {
				@Override
				public void run() {
					sendRcOverrideMsg();
				}
			}, 0, 25, TimeUnit.MILLISECONDS);
		}
	}

	public void sendRcOverrideMsg() {
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
		msg.target_component = 1;
		MAV.sendMavPacket(msg.pack());
	}
	
	public boolean isRcOverrided(){
		return (scheduleTaskExecutor!=null);
	}

}
