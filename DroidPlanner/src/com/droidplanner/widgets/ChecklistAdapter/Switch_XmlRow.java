package com.droidplanner.widgets.ChecklistAdapter;

import com.droidplanner.R;
import com.droidplanner.preflightcheck.CheckListItem;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

public class Switch_XmlRow implements XmlRow, OnCheckedChangeListener {
    public interface OnSwitchChangeListener {
    	public void onSwitchChanged(CheckListItem checkListItem, boolean isSwitched);
    }
    
    private OnSwitchChangeListener listener;
	private final CheckListItem checkListItem;
    private final LayoutInflater inflater;

    public Switch_XmlRow(LayoutInflater inflater, CheckListItem checkListItem) {
        this.checkListItem = checkListItem;
        this.inflater = inflater;
    }

	public View getView(View convertView) {
		ViewHolder holder;
		View view;
		if (convertView == null) {
			ViewGroup viewGroup = (ViewGroup) inflater.inflate(
					R.layout.preflight_switch_item, null);
			holder = new ViewHolder(viewGroup);

			viewGroup.setTag(holder);
			view = viewGroup;
		} else {
			view = convertView;
			holder = (ViewHolder) convertView.getTag();
		}

		// TODO - Add spinner items
		holder.switchView.setOnCheckedChangeListener(this);
		holder.textView.setText(checkListItem.getTitle());
		return view;
	}

	public int getViewType() {
		return XmlRowType.SWITCH_ROW.ordinal();
	}

	public void setOnSwitchChangeListener(OnSwitchChangeListener listener) {
		this.listener = listener;
	}

	private static class ViewHolder {
		final LinearLayout layoutView;
		final Switch switchView;
		final TextView textView;

		private ViewHolder(ViewGroup viewGroup) {
			this.layoutView = (LinearLayout) viewGroup.findViewById(R.id.layout_toggle);
			this.switchView = (Switch) viewGroup.findViewById(R.id.chk_switch);
			this.textView = (TextView) viewGroup.findViewById(R.id.chk_label);
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
		if(this.listener!=null)
			this.listener.onSwitchChanged(this.checkListItem, arg1);
	}


}
