package com.droidplanner.checklist.row;

import com.droidplanner.R;
import com.droidplanner.checklist.CheckListItem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;

public class ListRow_Select extends ListRow implements OnItemSelectedListener, OnClickListener {
	private final CheckListItem checkListItem;
	private final LayoutInflater inflater;

	public ListRow_Select(LayoutInflater inflater, CheckListItem checkListItem) {
		this.checkListItem = checkListItem;
		this.inflater = inflater;
	}

	public View getView(View convertView) {
		final ViewHolder holder;
		View view;
		if (convertView == null) {
			ViewGroup viewGroup = (ViewGroup) inflater.inflate(
					R.layout.list_select_item, null);
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
		holder.selectView.setOnItemSelectedListener(this);

		// Common display update
		holder.checkBox.setText(checkListItem.getTitle());
		holder.checkBox
				.setClickable(checkListItem.getSys_tag().contains("SYS") == false);
		holder.checkBox.setChecked(checkListItem.isVerified());

		checkListItem.setVerified(holder.checkBox.isChecked());
	}

	public int getViewType() {
		return ListRow_Type.SELECT_ROW.ordinal();
	}

	private static class ViewHolder {
		@SuppressWarnings("unused")
		final LinearLayout layoutView;
		final Spinner selectView;
		final CheckBox checkBox;
		@SuppressWarnings("unused")
		private CheckListItem checkListItem;

		private ArrayAdapter<String> adapter;

		private ViewHolder(ViewGroup viewGroup, CheckListItem checkListItem) {
			this.layoutView = (LinearLayout) viewGroup
					.findViewById(R.id.lst_layout);
			this.selectView = (Spinner) viewGroup.findViewById(R.id.lst_select);
			this.checkBox = (CheckBox) viewGroup.findViewById(R.id.lst_check);
			this.checkListItem = checkListItem;

			setupSpinner(viewGroup, checkListItem);
		}

		private void setupSpinner(ViewGroup viewGroup,
				CheckListItem checkListItem) {
			adapter = new ArrayAdapter<String>(viewGroup.getContext(),
					android.R.layout.simple_spinner_item,
					checkListItem.getOptionLists());
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

			selectView.setAdapter(adapter);

		}
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		checkListItem.setSelectedIndex(arg2);

		if (listener != null) {
			listener.onRowItemChanged(arg1, checkListItem,
					checkListItem.isSys_activated());
		}

	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onClick(View v) {
		this.checkListItem.setVerified(((CheckBox) v).isChecked());
	}
	
}
