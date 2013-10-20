package com.droidplanner.checklist.row;

import com.droidplanner.R;
import com.droidplanner.checklist.CheckListItem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class ListRow_Toggle implements ListRow_Interface, OnCheckedChangeListener {
    public interface OnToggleChangeListener {
    	public void onToggleChanged(CheckListItem checkListItem, boolean isToggled);
    }

    private OnToggleChangeListener listener;
    private final CheckListItem checkListItem;
    private final LayoutInflater inflater;

    public ListRow_Toggle(LayoutInflater inflater, CheckListItem checkListItem) {
        this.checkListItem = checkListItem;
        this.inflater = inflater;
    }

	public View getView(View convertView) {
		ViewHolder holder;
		View view;
		if (convertView == null) {
			ViewGroup viewGroup = (ViewGroup) inflater.inflate(
					R.layout.list_toggle_item, null);
			holder = new ViewHolder(viewGroup);

			viewGroup.setTag(holder);
			view = viewGroup;
		} else {
			view = convertView;
			holder = (ViewHolder) convertView.getTag();
		}

		// TODO - Add spinner items
		holder.toggleButton.setOnCheckedChangeListener(this);
		holder.checkBox.setText(checkListItem.getTitle());
		return view;
	}

	public int getViewType() {
		return ListRow_Type.TOGGLE_ROW.ordinal();
	}
	
	public void setOnToggleChangeListener(OnToggleChangeListener listener) {
		this.listener = listener;
	}


	private static class ViewHolder {
		final LinearLayout layoutView;
		final ToggleButton toggleButton;
		final CheckBox checkBox;

		private ViewHolder(ViewGroup viewGroup) {
			this.layoutView = (LinearLayout) viewGroup.findViewById(R.id.lst_layout);
			this.toggleButton = (ToggleButton) viewGroup.findViewById(R.id.lst_toggle);
			this.checkBox = (CheckBox)viewGroup.findViewById(R.id.lst_check);
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if(this.listener!=null)
			this.listener.onToggleChanged(this.checkListItem, isChecked);
	}

}
