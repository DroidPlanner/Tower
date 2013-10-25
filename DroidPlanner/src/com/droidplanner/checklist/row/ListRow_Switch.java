package com.droidplanner.checklist.row;

import com.droidplanner.R;
import com.droidplanner.checklist.CheckListItem;
import com.droidplanner.drone.Drone;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.Switch;

public class ListRow_Switch extends ListRow implements OnCheckedChangeListener,
		OnClickListener {

	private final CheckListItem checkListItem;
	private final LayoutInflater inflater;

	public ListRow_Switch(Drone drone, LayoutInflater inflater,
			CheckListItem checkListItem) {
		this.checkListItem = checkListItem;
		this.inflater = inflater;
		getDroneVariable(drone, checkListItem);
	}

	public View getView(View convertView) {
		ViewHolder holder;
		View view;
		if (convertView == null) {
			ViewGroup viewGroup = (ViewGroup) inflater.inflate(
					R.layout.list_switch_item, null);
			holder = new ViewHolder(viewGroup);

			viewGroup.setTag(holder);
			view = viewGroup;
		} else {
			view = convertView;
			holder = (ViewHolder) convertView.getTag();
		}

		// TODO - Add spinner items
		updateDisplay(view, holder, checkListItem);
		return view;
	}

	private void updateDisplay(View view, ViewHolder holder,
			CheckListItem mListItem) {
		boolean failMandatory = !checkListItem.isSys_activated();

		holder.switchView.setOnCheckedChangeListener(this);
		holder.switchView.setClickable(checkListItem.isEditable());

		// Common display update
		holder.checkBox.setText(checkListItem.getTitle());
		holder.checkBox
				.setClickable(checkListItem.getSys_tag().contains("SYS") == false);
		holder.checkBox.setChecked(checkListItem.isMandatory()
				&& !failMandatory);

		checkListItem.setVerified(holder.checkBox.isChecked());
	}

	public int getViewType() {
		return ListRow_Type.SWITCH_ROW.ordinal();
	}

	private static class ViewHolder {
		@SuppressWarnings("unused")
		final LinearLayout layoutView;
		final Switch switchView;
		final CheckBox checkBox;

		private ViewHolder(ViewGroup viewGroup) {
			this.layoutView = (LinearLayout) viewGroup
					.findViewById(R.id.lst_layout);
			this.switchView = (Switch) viewGroup.findViewById(R.id.lst_switch);
			this.checkBox = (CheckBox) viewGroup.findViewById(R.id.lst_check);
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
		this.checkListItem.setSys_activated(arg1);

		if (listener != null)
			listener.onRowItemChanged(arg0, this.checkListItem,
					this.checkListItem.isVerified());
	}

	@Override
	public void onClick(View v) {
		this.checkListItem.setVerified(((CheckBox) v).isChecked());

	}

}
