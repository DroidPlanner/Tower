package org.droidplanner.android.widgets.checklist;

import org.droidplanner.android.api.DroneApi;
import org.droidplanner.android.api.model.DPDrone;
import org.droidplanner.core.MAVLink.MavLinkArm;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.helpers.units.Altitude;
import org.droidplanner.core.model.Drone;

public class CheckListSysLink {
	private DroneApi droneApi;

	public CheckListSysLink(DroneApi droneApi) {
		this.droneApi = droneApi;
	}

	public void getSystemData(CheckListItem mListItem, String mSysTag) {
		if (mSysTag == null)
			return;

		if (mSysTag.equalsIgnoreCase("SYS_BATTREM_LVL")) {
			mListItem.setSys_value(droneApi.getBattery().getBatteryRemain());
		} else if (mSysTag.equalsIgnoreCase("SYS_BATTVOL_LVL")) {
			mListItem.setSys_value(droneApi.getBattery().getBatteryVoltage());
		} else if (mSysTag.equalsIgnoreCase("SYS_BATTCUR_LVL")) {
			mListItem.setSys_value(droneApi.getBattery().getBatteryCurrent());
		} else if (mSysTag.equalsIgnoreCase("SYS_GPS3D_LVL")) {
			mListItem.setSys_value(droneApi.getGps().getSatellitesCount());
		} else if (mSysTag.equalsIgnoreCase("SYS_ARM_STATE")) {
			mListItem.setSys_activated(droneApi.getState().isArmed());
		} else if (mSysTag.equalsIgnoreCase("SYS_FAILSAFE_STATE")) {
			mListItem.setSys_activated(droneApi.getState().isWarning());
		} else if (mSysTag.equalsIgnoreCase("SYS_CONNECTION_STATE")) {
			mListItem.setSys_activated(droneApi.isConnected());
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
		if (droneApi.isConnected()) {
			if (checkListItem.isSys_activated() && !droneApi.getState().isArmed()) {
				droneApi.arm(true);
			} else {
				droneApi.arm(false);
			}
		}
	}

	private void doSysConnect(CheckListItem checkListItem) {
		boolean activated = checkListItem.isSys_activated();
		boolean connected = droneApi.isConnected();
		if (activated != connected) {
			droneApi.toggleConnectionState();
		}
	}

}
