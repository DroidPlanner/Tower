package com.droidplanner.checklist.row;

import com.droidplanner.R;
import com.droidplanner.checklist.CheckListItem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ListRow_CheckBox implements ListRow_Interface, OnClickListener {
	public interface OnCheckBoxChangeListener {
		public void onCheckBoxChanged(CheckListItem checkListItem, boolean isChecked);
	}

	private OnCheckBoxChangeListener listener;
	private final CheckListItem checkListItem;
	private final LayoutInflater inflater;

	public ListRow_CheckBox(LayoutInflater inflater, CheckListItem checkListItem) {
		this.checkListItem = checkListItem;
		this.inflater = inflater;
	}

	public View getView(View convertView) {
		ViewHolder holder;
		View view;
		if (convertView == null) {
			ViewGroup viewGroup = (ViewGroup) inflater.inflate(
					R.layout.list_check_item, null);
			holder = new ViewHolder(viewGroup);
			viewGroup.setTag(holder);
			view = viewGroup;
		} else {
			holder = (ViewHolder) convertView.getTag();
			view = convertView;
		}
		holder.checkBox.setOnClickListener(this);
		holder.checkBox.setText(checkListItem.getTitle());

		return view;
	}

	public int getViewType() {
		return ListRow_Type.CHECKBOX_ROW.ordinal();
	}
	
	public void setOnCheckBoxChangeListener(OnCheckBoxChangeListener listener){
		this.listener = listener;
	}
	
	private static class ViewHolder {
		final LinearLayout layoutView;
		final TextView checkBox;

		private ViewHolder(ViewGroup viewGroup) {
			this.layoutView = (LinearLayout) viewGroup
					.findViewById(R.id.lst_layout);
			this.checkBox = (CheckBox) viewGroup.findViewById(R.id.lst_check);
		}
	}

	@Override
	public void onClick(View v) {
		if(this.listener!=null){
			this.listener.onCheckBoxChanged(this.checkListItem,((CheckBox)v).isChecked());
		}
	}
}
