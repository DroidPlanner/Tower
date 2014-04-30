package org.droidplanner.android.widgets.checklist.row;

import org.droidplanner.android.widgets.checklist.CheckListItem;
import org.droidplanner.core.drone.Drone;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.CheckBox;
import android.widget.Toast;

public class ListRow implements ListRow_Interface, OnClickListener,
		OnLongClickListener {
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
		holder.checkBox
				.setClickable(checkListItem.getSys_tag() == null ? checkListItem
						.isEditable() : !checkListItem.getSys_tag().contains(
						"SYS"));

		checkListItem.setVerified(mState);
	}

	public void updateRowChanged(View mView, CheckListItem mListItem) {
		if (listener != null)
			listener.onRowItemChanged(mView, this.checkListItem,
					this.checkListItem.isVerified());
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

		if (sys_tag.equalsIgnoreCase("SYS_BATTREM_LVL")) {
			mListItem.setSys_value(mDrone.battery.getBattRemain());
		} else if (sys_tag.equalsIgnoreCase("SYS_BATTVOL_LVL")) {
			mListItem.setSys_value(mDrone.battery.getBattVolt());
		} else if (sys_tag.equalsIgnoreCase("SYS_BATTCUR_LVL")) {
			mListItem.setSys_value(mDrone.battery.getBattCurrent());
		} else if (sys_tag.equalsIgnoreCase("SYS_GPS3D_LVL")) {
			mListItem.setSys_value(mDrone.GPS.getSatCount());
		} else if (sys_tag.equalsIgnoreCase("SYS_DEF_ALT")) {
			mListItem.setSys_value(mDrone.mission.getDefaultAlt()
					.valueInMeters());
		} else if (sys_tag.equalsIgnoreCase("SYS_ARM_STATE")) {
			mListItem.setSys_activated(mDrone.state.isArmed());
		} else if (sys_tag.equalsIgnoreCase("SYS_FAILSAFE_STATE")) {
			mListItem.setSys_activated(mDrone.state.isFailsafe());
		} else if (sys_tag.equalsIgnoreCase("SYS_CONNECTION_STATE")) {
			mListItem.setSys_activated(mDrone.MavClient.isConnected());
		}
	}

	protected void getData(CheckListItem mListItem) {
		if (this.listener != null)
			this.listener.onRowItemGetData(checkListItem,
					checkListItem.getSys_tag());
	}

	@Override
	public void onClick(View v) {
		this.checkListItem.setVerified(((CheckBox) v).isChecked());
		updateRowChanged(v, this.checkListItem);
	}

	@Override
	public boolean onLongClick(View arg0) {
		if (arg0.equals(this.holder.layoutView)) {
			Toast.makeText(arg0.getContext(), checkListItem.getDesc(),
					Toast.LENGTH_SHORT).show();

		}
		return false;
	}

}
