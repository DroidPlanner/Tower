package com.droidplanner.widgets.ChecklistAdapter;

import com.droidplanner.R;
import com.droidplanner.preflightcheck.CheckListItem;
import com.droidplanner.widgets.ChecklistAdapter.Radio_XmlRow.OnRadioGroupCheckedChangeListener;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

public class CheckBox_XmlRow implements XmlRow, OnClickListener {
	public interface OnCheckBoxChangeListener {
		public void onCheckBoxChanged(CheckListItem checkListItem, boolean isChecked);
	}

	private OnCheckBoxChangeListener listener;
	private final CheckListItem checkListItem;
	private final LayoutInflater inflater;

	public CheckBox_XmlRow(LayoutInflater inflater, CheckListItem checkListItem) {
		this.checkListItem = checkListItem;
		this.inflater = inflater;
	}

	public View getView(View convertView) {
		ViewHolder holder;
		View view;
		if (convertView == null) {
			ViewGroup viewGroup = (ViewGroup) inflater.inflate(
					R.layout.preflight_check_item, null);
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
		return XmlRowType.CHECKBOX_ROW.ordinal();
	}
	public void setOnCheckBoxChangeListener(OnCheckBoxChangeListener listener){
		this.listener = listener;
	}
	
	private static class ViewHolder {
		final LinearLayout layoutView;
		final TextView checkBox;

		private ViewHolder(ViewGroup viewGroup) {
			this.layoutView = (LinearLayout) viewGroup
					.findViewById(R.id.layout_check);
			this.checkBox = (CheckBox) viewGroup.findViewById(R.id.chk_check);
		}
	}

	@Override
	public void onClick(View v) {
		if(this.listener!=null){
			this.listener.onCheckBoxChanged(this.checkListItem,((CheckBox)v).isChecked());
		}
	}
}
