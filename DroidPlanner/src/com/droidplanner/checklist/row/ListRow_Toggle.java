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
import android.widget.LinearLayout;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class ListRow_Toggle extends ListRow implements OnCheckedChangeListener,
		OnClickListener {
	private final CheckListItem checkListItem;
	private final LayoutInflater inflater;
	private ViewHolder holder;
	
	public ListRow_Toggle(Drone drone, LayoutInflater inflater, CheckListItem checkListItem) {
		this.checkListItem = checkListItem;
		this.inflater = inflater;
		getDroneVariable(drone, checkListItem);
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

		updateDisplay(view, holder, checkListItem);
		return view;
	}

	private void updateDisplay(View view, ViewHolder holder,
			CheckListItem mListItem) {
		boolean failMandatory = !checkListItem.isSys_activated();

		holder.toggleButton.setOnCheckedChangeListener(this);
		holder.toggleButton.setChecked(checkListItem.isSys_activated());
		holder.toggleButton.setClickable(checkListItem.isEditable());
		
		// Common display update
		holder.checkBox.setOnClickListener(this);
		holder.checkBox.setText(checkListItem.getTitle());
		holder.checkBox
				.setClickable(checkListItem.getSys_tag().contains("SYS") == false);
		holder.checkBox.setChecked(checkListItem.isMandatory()
				&& !failMandatory);

		checkListItem.setVerified(holder.checkBox.isChecked());
	}

	public int getViewType() {
		return ListRow_Type.TOGGLE_ROW.ordinal();
	}

	private static class ViewHolder extends BaseViewHolder{
		private ToggleButton toggleButton;

		private ViewHolder(ViewGroup viewGroup, CheckListItem checkListItem) {
				super(viewGroup, checkListItem);
		}
		
		@Override
		protected void setupViewItems(ViewGroup viewGroup, CheckListItem checkListItem)
		{
			this.layoutView = (LinearLayout) viewGroup
					.findViewById(R.id.lst_layout);
			this.toggleButton = (ToggleButton) viewGroup
					.findViewById(R.id.lst_toggle);
			this.checkBox = (CheckBox) viewGroup.findViewById(R.id.lst_check);
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		this.checkListItem.setSys_activated(isChecked);

		if (listener != null)
			listener.onRowItemChanged(buttonView, this.checkListItem, this.checkListItem.isVerified());
	}

	@Override
	public void onClick(View v) {
		this.checkListItem.setVerified(((CheckBox) v).isChecked());
	}
}
