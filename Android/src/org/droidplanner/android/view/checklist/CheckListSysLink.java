package org.droidplanner.android.view.checklist;

import android.content.Context;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.Battery;
import com.o3dr.services.android.lib.drone.property.Gps;
import com.o3dr.services.android.lib.drone.property.State;

import org.droidplanner.android.DroidPlannerApp;

public class CheckListSysLink {
    private Context context;
	private Drone drone;

	public CheckListSysLink(Context context, Drone drone) {
        this.context = context;
		this.drone = drone;
	}

	public void getSystemData(CheckListItem mListItem, String mSysTag) {
		if (mSysTag == null)
			return;

		Battery batt = drone.getAttribute(AttributeType.BATTERY);
		if (batt != null) {
			if (mSysTag.equalsIgnoreCase("SYS_BATTREM_LVL")) {
				mListItem.setSys_value(batt.getBatteryRemain());
			} else if (mSysTag.equalsIgnoreCase("SYS_BATTVOL_LVL")) {
				mListItem.setSys_value(batt.getBatteryVoltage());
			} else if (mSysTag.equalsIgnoreCase("SYS_BATTCUR_LVL")) {
				mListItem.setSys_value(batt.getBatteryCurrent());
			}
		}

		Gps gps = drone.getAttribute(AttributeType.GPS);
		if (gps != null) {
			if (mSysTag.equalsIgnoreCase("SYS_GPS3D_LVL")) {
				mListItem.setSys_value(gps.getSatellitesCount());
			}
		}

		State state = drone.getAttribute(AttributeType.STATE);
		if (state != null) {
			if (mSysTag.equalsIgnoreCase("SYS_ARM_STATE")) {
				mListItem.setSys_activated(state.isArmed());
			} else if (mSysTag.equalsIgnoreCase("SYS_FAILSAFE_STATE")) {
				mListItem.setSys_activated(state.isWarning());
			}
		}

		if (mSysTag.equalsIgnoreCase("SYS_CONNECTION_STATE")) {
			mListItem.setSys_activated(drone.isConnected());
		}
	}

	public void setSystemData(CheckListItem checkListItem) {

		if (checkListItem.getSys_tag() == null)
			return;

		if (checkListItem.getSys_tag().equalsIgnoreCase("SYS_CONNECTION_STATE")) {
			doSysConnect(checkListItem);

		} else if (checkListItem.getSys_tag().equalsIgnoreCase("SYS_ARM_STATE")) {
			doSysArm(checkListItem);

		}
	}

	private void doSysArm(CheckListItem checkListItem) {
        final State droneState = drone.getAttribute(AttributeType.STATE);
		if (droneState.isConnected()) {
			if (checkListItem.isSys_activated() && !droneState.isArmed()) {
				drone.arm(true);
			} else {
				drone.arm(false);
			}
		}
	}

	private void doSysConnect(CheckListItem checkListItem) {
		boolean activated = checkListItem.isSys_activated();
		boolean connected = drone.isConnected();
		if (activated != connected) {
			if (connected)
				DroidPlannerApp.disconnectFromDrone(context);
			else
				DroidPlannerApp.connectToDrone(context);
		}
	}

}
