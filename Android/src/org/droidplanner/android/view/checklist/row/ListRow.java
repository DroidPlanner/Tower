package org.droidplanner.android.view.checklist.row;

import org.droidplanner.android.view.checklist.CheckListItem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.CheckBox;
import android.widget.Toast;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.Battery;
import com.o3dr.services.android.lib.drone.property.Gps;
import com.o3dr.services.android.lib.drone.property.State;

public class ListRow implements ListRow_Interface, OnClickListener, OnLongClickListener {
	protected final CheckListItem checkListItem;
	protected final LayoutInflater inflater;

	public OnRowItemChangeListener listener;
	public BaseViewHolder holder;

	public ListRow(LayoutInflater mInflater, CheckListItem mCheckListItem) {
		this.checkListItem = mCheckListItem;
		this.inflater = mInflater;
	}

	protected void updateCheckBox(boolean mState) {

		// Common display update
		holder.layoutView.setOnLongClickListener(this);
		holder.checkBox.setOnClickListener(this);
		holder.checkBox.setText(checkListItem.getTitle());
		holder.checkBox.setChecked(mState);
		holder.checkBox.setClickable(checkListItem.getSys_tag() == null ? checkListItem
				.isEditable() : !checkListItem.getSys_tag().contains("SYS"));

		checkListItem.setVerified(mState);
	}

	public void updateRowChanged() {
		if (listener != null)
			listener.onRowItemChanged(this.checkListItem);
	}

	public CheckListItem getCheckListItem() {
		return checkListItem;
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

	public void setOnRowItemChangeListener(OnRowItemChangeListener mListener) {
		listener = mListener;
	}

	protected void getDroneVariable(Drone mDrone, CheckListItem mListItem) {
		String sys_tag = mListItem.getSys_tag();

        final Battery droneBattery = mDrone.getAttribute(AttributeType.BATTERY);
		if (sys_tag.equalsIgnoreCase("SYS_BATTREM_LVL")) {
			mListItem.setSys_value(droneBattery.getBatteryRemain());
		} else if (sys_tag.equalsIgnoreCase("SYS_BATTVOL_LVL")) {
			mListItem.setSys_value(droneBattery.getBatteryVoltage());
		} else if (sys_tag.equalsIgnoreCase("SYS_BATTCUR_LVL")) {
			mListItem.setSys_value(droneBattery.getBatteryCurrent());
		}

        final Gps droneGps = mDrone.getAttribute(AttributeType.GPS);
        if (sys_tag.equalsIgnoreCase("SYS_GPS3D_LVL")) {
			mListItem.setSys_value(droneGps.getSatellitesCount());
		}

        final State droneState = mDrone.getAttribute(AttributeType.STATE);
        if (sys_tag.equalsIgnoreCase("SYS_ARM_STATE")) {
			mListItem.setSys_activated(droneState.isArmed());
		} else if (sys_tag.equalsIgnoreCase("SYS_FAILSAFE_STATE")) {
			mListItem.setSys_activated(droneState.isWarning());
		} else if (sys_tag.equalsIgnoreCase("SYS_CONNECTION_STATE")) {
			mListItem.setSys_activated(droneState.isConnected());
		}
	}

	protected void getData() {
		if (this.listener != null)
			this.listener.onRowItemGetData(checkListItem, checkListItem.getSys_tag());
	}

	@Override
	public void onClick(View v) {
		this.checkListItem.setVerified(((CheckBox) v).isChecked());
		updateRowChanged();
	}

	@Override
	public boolean onLongClick(View arg0) {
		if (arg0.equals(this.holder.layoutView)) {
			Toast.makeText(arg0.getContext(), checkListItem.getDesc(), Toast.LENGTH_SHORT).show();

		}
		return false;
	}

}
