package com.droidplanner.checklist.row;

import com.droidplanner.R;
import com.droidplanner.checklist.CheckListItem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;

public class ListRow_CheckBox extends ListRow implements OnClickListener {
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

		updateDisplay(view,holder,checkListItem);
		return view;
	}
	
	private void updateDisplay(View view, ViewHolder holder,
			CheckListItem mListItem) {
		
		//Common display update
		holder.checkBox.setOnClickListener(this);
		holder.checkBox.setText(checkListItem.getTitle());
		holder.checkBox.setClickable(checkListItem.isEditable());
		holder.checkBox.setChecked(checkListItem.isSys_activated());

		checkListItem.setVerified(holder.checkBox.isChecked());
	}

	public int getViewType() {
		return ListRow_Type.CHECKBOX_ROW.ordinal();
	}
	
	private static class ViewHolder {
		@SuppressWarnings("unused")
		final LinearLayout layoutView;
		final CheckBox checkBox;

		private ViewHolder(ViewGroup viewGroup) {
			this.layoutView = (LinearLayout) viewGroup
					.findViewById(R.id.lst_layout);
			this.checkBox = (CheckBox) viewGroup.findViewById(R.id.lst_check);
		}
	}

	@Override
	public void onClick(View v) {
		this.checkListItem.setSys_activated(((CheckBox)v).isChecked());
		
		if(this.listener!=null){
			this.listener.onRowItemChanged(v, this.checkListItem,((CheckBox)v).isChecked());
		}
	}
}
