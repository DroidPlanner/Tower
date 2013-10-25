package com.droidplanner.checklist.row;

import com.droidplanner.checklist.CheckListItem;
import com.droidplanner.drone.Drone;

import android.view.View;

public class ListRow implements ListRow_Interface {

	public OnRowItemChangeListener listener;
	
	public ListRow() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public View getView(View convertView) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getViewType() {
		// TODO Auto-generated method stub
		return 0;
	}
	public void setOnRowItemChangeListener(OnRowItemChangeListener mListener){
		listener = mListener;
	}
	
	protected void getDroneVariable(Drone mDrone, CheckListItem mListItem) {
		String sys_tag = mListItem.getSys_tag();

		if (sys_tag.equalsIgnoreCase("SYS_BATTREM_LVL")) {
			mListItem.setSys_value(mDrone.battery.getBattRemain());
		} else if (sys_tag.equalsIgnoreCase("SYS_BATTVOL_LVL")) {
			mListItem.setSys_value(mDrone.battery.getBattVolt());
		} else if (sys_tag.equalsIgnoreCase("SYS_BATTCUR_LVL")) {
			mListItem.setSys_value(mDrone.battery.getBattCurrent());
		} else if (sys_tag.equalsIgnoreCase("SYS_GPS3D_LVL")) {
			mListItem.setSys_value(mDrone.GPS.getSatCount());
		} else if (sys_tag.equalsIgnoreCase("SYS_DEF_ALT")) {
			mListItem.setSys_value(mDrone.mission.getDefaultAlt());
		} else if (sys_tag.equalsIgnoreCase("SYS_ARM_STATE")) {
			mListItem.setSys_activated(mDrone.state.isArmed());
		} else if (sys_tag.equalsIgnoreCase("SYS_FAILSAFE_STATE")) {
			mListItem.setSys_activated(mDrone.state.isFailsafe());
		} else if (sys_tag.equalsIgnoreCase("SYS_CONNECTION_STATE")) {
			mListItem.setSys_activated(mDrone.MavClient.isConnected());
		}
	}

}
