package com.droidplanner.widgets.ChecklistAdapter;

import com.droidplanner.R;
import com.droidplanner.preflightcheck.CheckListItem;
import com.droidplanner.widgets.ChecklistAdapter.Switch_XmlRow.OnSwitchChangeListener;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class Toggle_XmlRow implements XmlRow, OnCheckedChangeListener {
    public interface OnToggleChangeListener {
    	public void onToggleChanged(CheckListItem checkListItem, boolean isToggled);
    }

    private OnToggleChangeListener listener;
    private final CheckListItem checkListItem;
    private final LayoutInflater inflater;

    public Toggle_XmlRow(LayoutInflater inflater, CheckListItem checkListItem) {
        this.checkListItem = checkListItem;
        this.inflater = inflater;
    }

	public View getView(View convertView) {
		ViewHolder holder;
		View view;
		if (convertView == null) {
			ViewGroup viewGroup = (ViewGroup) inflater.inflate(
					R.layout.preflight_toggle_item, null);
			holder = new ViewHolder(viewGroup);

			viewGroup.setTag(holder);
			view = viewGroup;
		} else {
			view = convertView;
			holder = (ViewHolder) convertView.getTag();
		}

		// TODO - Add spinner items
		holder.toggleButton.setOnCheckedChangeListener(this);
		holder.textView.setText(checkListItem.getTitle());
		return view;
	}

	public int getViewType() {
		return XmlRowType.TOGGLE_ROW.ordinal();
	}
	
	public void setOnToggleChangeListener(OnToggleChangeListener listener) {
		this.listener = listener;
	}


	private static class ViewHolder {
		final LinearLayout layoutView;
		final ToggleButton toggleButton;
		final TextView textView;

		private ViewHolder(ViewGroup viewGroup) {
			this.layoutView = (LinearLayout) viewGroup.findViewById(R.id.layout_toggle);
			this.toggleButton = (ToggleButton) viewGroup.findViewById(R.id.chk_toggle);
			this.textView = (TextView) viewGroup.findViewById(R.id.chk_label);
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if(this.listener!=null)
			this.listener.onToggleChanged(this.checkListItem, isChecked);
	}

}
