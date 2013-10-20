package com.droidplanner.checklist.row;

import com.droidplanner.R;
import com.droidplanner.checklist.CheckListItem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.Switch;

public class ListRow_Switch implements ListRow_Interface, OnCheckedChangeListener {
    public interface OnSwitchChangeListener {
    	public void onSwitchChanged(CheckListItem checkListItem, boolean isSwitched);
    }
    
    private OnSwitchChangeListener listener;
	private final CheckListItem checkListItem;
    private final LayoutInflater inflater;

    public ListRow_Switch(LayoutInflater inflater, CheckListItem checkListItem) {
        this.checkListItem = checkListItem;
        this.inflater = inflater;
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
		holder.switchView.setOnCheckedChangeListener(this);
		holder.checkBox.setText(checkListItem.getTitle());
		return view;
	}

	public int getViewType() {
		return ListRow_Type.SWITCH_ROW.ordinal();
	}

	public void setOnSwitchChangeListener(OnSwitchChangeListener listener) {
		this.listener = listener;
	}

	private static class ViewHolder {
		final LinearLayout layoutView;
		final Switch switchView;
		final CheckBox checkBox;

		private ViewHolder(ViewGroup viewGroup) {
			this.layoutView = (LinearLayout) viewGroup.findViewById(R.id.lst_layout);
			this.switchView = (Switch) viewGroup.findViewById(R.id.lst_switch);
			this.checkBox = (CheckBox) viewGroup.findViewById(R.id.lst_check);
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
		if(this.listener!=null)
			this.listener.onSwitchChanged(this.checkListItem, arg1);
	}


}
