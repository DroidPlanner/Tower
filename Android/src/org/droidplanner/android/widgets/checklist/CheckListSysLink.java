package org.droidplanner.android.widgets.checklist;

import org.droidplanner.android.api.model.DPDrone;
import org.droidplanner.core.MAVLink.MavLinkArm;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.helpers.units.Altitude;
import org.droidplanner.core.model.Drone;

public class CheckListSysLink {
	private DPDrone drone;

	public CheckListSysLink(DPDrone mDrone) {
		this.drone = mDrone;
	}

	public void getSystemData(CheckListItem mListItem, String mSysTag) {
		if (mSysTag == null)
			return;

		if (mSysTag.equalsIgnoreCase("SYS_BATTREM_LVL")) {
			mListItem.setSys_value(drone.getBattery().getBatteryRemain());
		} else if (mSysTag.equalsIgnoreCase("SYS_BATTVOL_LVL")) {
			mListItem.setSys_value(drone.getBattery().getBatteryVoltage());
		} else if (mSysTag.equalsIgnoreCase("SYS_BATTCUR_LVL")) {
			mListItem.setSys_value(drone.getBattery().getBatteryCurrent());
		} else if (mSysTag.equalsIgnoreCase("SYS_GPS3D_LVL")) {
			mListItem.setSys_value(drone.getGps().getSatellitesCount());
		} else if (mSysTag.equalsIgnoreCase("SYS_DEF_ALT")) {
			mListItem.setSys_value(drone.getAltitude().getAltitude());
		} else if (mSysTag.equalsIgnoreCase("SYS_ARM_STATE")) {
			mListItem.setSys_activated(drone.getState().isArmed());
		} else if (mSysTag.equalsIgnoreCase("SYS_FAILSAFE_STATE")) {
			mListItem.setSys_activated(drone.getState().isWarning());
		} else if (mSysTag.equalsIgnoreCase("SYS_CONNECTION_STATE")) {
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

		} else if (checkListItem.getSys_tag().equalsIgnoreCase("SYS_DEF_ALT")) {
			doDefAlt(checkListItem);

		}
	}

	private void doDefAlt(CheckListItem checkListItem) {
		drone.getMission().setDefaultAlt(new Altitude(checkListItem.getFloatValue()));
	}

	private void doSysArm(CheckListItem checkListItem) {
		if (drone.getMavClient().isConnected()) {
			if (checkListItem.isSys_activated() && !drone.getState().isArmed()) {
				drone.notifyDroneEvent(DroneEventsType.ARMING_STARTED);
				MavLinkArm.sendArmMessage(drone, true);
			} else {
				MavLinkArm.sendArmMessage(drone, false);
			}
		}
	}

	private void doSysConnect(CheckListItem checkListItem) {
		boolean activated = checkListItem.isSys_activated();
		boolean connected = drone.isConnected();
		if (activated != connected) {
			drone.toggleConnectionState();
		}
	}

}
