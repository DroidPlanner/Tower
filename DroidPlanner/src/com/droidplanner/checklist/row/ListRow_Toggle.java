package com.droidplanner.checklist.row;

import com.droidplanner.R;
import com.droidplanner.checklist.CheckListItem;
import com.droidplanner.drone.Drone;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class ListRow_Toggle extends ListRow implements OnCheckedChangeListener {

	public ListRow_Toggle(Drone drone, LayoutInflater inflater,
			CheckListItem checkListItem) {
		super(drone, inflater, checkListItem);
	}

	public View getView(View convertView) {
		View view;
		if (convertView == null) {
			ViewGroup viewGroup = (ViewGroup) inflater.inflate(
					R.layout.list_toggle_item, null);
			holder = new ViewHolder(viewGroup, checkListItem);

			viewGroup.setTag(holder);
			view = viewGroup;
		} else {
			view = convertView;
			holder = (ViewHolder) convertView.getTag();
		}

		updateDisplay(view, (ViewHolder) holder, checkListItem);
		return view;
	}

	private void updateDisplay(View view, ViewHolder holder,
			CheckListItem mListItem) {
		boolean failMandatory = false;

		getDroneVariable(this.drone, mListItem);
		failMandatory = !checkListItem.isSys_activated();

		holder.toggleButton.setOnCheckedChangeListener(this);
		holder.toggleButton.setChecked(checkListItem.isSys_activated());
		holder.toggleButton.setClickable(checkListItem.isEditable());

		updateCheckBox(checkListItem.isMandatory() && !failMandatory);
	}

	public int getViewType() {
		return ListRow_Type.TOGGLE_ROW.ordinal();
	}

	private static class ViewHolder extends BaseViewHolder {
		private ToggleButton toggleButton;

		private ViewHolder(ViewGroup viewGroup, CheckListItem checkListItem) {
			super(viewGroup, checkListItem);
		}

		@Override
		protected void setupViewItems(ViewGroup viewGroup,
				CheckListItem checkListItem) {
			this.toggleButton = (ToggleButton) viewGroup
					.findViewById(R.id.lst_toggle);
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		this.checkListItem.setSys_activated(isChecked);
		updateRowChanged((View) (buttonView), this.checkListItem);
	}
}
