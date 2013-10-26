package com.droidplanner.checklist.row;

import com.droidplanner.R;
import com.droidplanner.checklist.CheckListItem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;

public class ListRow_CheckBox extends ListRow implements OnClickListener {
	private final CheckListItem checkListItem;
	private final LayoutInflater inflater;
	private ViewHolder holder;
	
	public ListRow_CheckBox(LayoutInflater inflater, CheckListItem checkListItem) {
		this.checkListItem = checkListItem;
		this.inflater = inflater;
	}

	public View getView(View convertView) {
		View view;
		if (convertView == null) {
			ViewGroup viewGroup = (ViewGroup) inflater.inflate(
					R.layout.list_check_item, null);
			holder = new ViewHolder(viewGroup, checkListItem);
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
	
	private static class ViewHolder extends BaseViewHolder {

		private ViewHolder(ViewGroup viewGroup, CheckListItem checkListItem) {
			super(viewGroup, checkListItem);
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
