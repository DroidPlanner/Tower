package com.droidplanner.widgets.ChecklistAdapter;

import com.droidplanner.R;
import com.droidplanner.preflightcheck.CheckListItem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

public class Select_XmlRow implements XmlRow {
	public interface OnSelectChangeListener{
		public void onSelectChanged(CheckListItem checkListItem, int selectId);
	}
	
	private OnSelectChangeListener listener;
	private final CheckListItem checkListItem;
	private final LayoutInflater inflater;

	public Select_XmlRow(LayoutInflater inflater, CheckListItem checkListItem) {
		this.checkListItem = checkListItem;
		this.inflater = inflater;
	}

	public View getView(View convertView) {
		final ViewHolder holder;
		View view;
		if (convertView == null) {
			ViewGroup viewGroup = (ViewGroup) inflater.inflate(
					R.layout.preflight_select_item, null);
			holder = new ViewHolder(viewGroup, checkListItem);
			viewGroup.setTag(holder);
			view = viewGroup;
		} else {
			view = convertView;
			holder = (ViewHolder) convertView.getTag();
		}

		// TODO - Add spinner items
		holder.textView.setText(checkListItem.getTitle());
		holder.selectView.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				if(listener!=null){
					listener.onSelectChanged(holder.checkListItem, arg2);
				}
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		});

		return view;
	}

	public int getViewType() {
		return XmlRowType.SELECT_ROW.ordinal();
	}

	public void setOnSelectChangeListener(OnSelectChangeListener listener) {
		this.listener = listener;
	}

	private static class ViewHolder {
		final LinearLayout layoutView;
		final Spinner selectView;
		final TextView textView;
		final CheckListItem checkListItem;

		private ArrayAdapter<String> adapter;

		private ViewHolder(ViewGroup viewGroup, CheckListItem checkListItem) {
			this.layoutView = (LinearLayout) viewGroup
					.findViewById(R.id.layout_select);
			this.selectView = (Spinner) viewGroup.findViewById(R.id.chk_select);
			this.textView = (TextView) viewGroup.findViewById(R.id.chk_label);
			this.checkListItem = checkListItem;
			
			setupSpinner(viewGroup, checkListItem);
		}

		private void setupSpinner(ViewGroup viewGroup,
				CheckListItem checkListItem) {
			adapter = new ArrayAdapter<String>(viewGroup.getContext(),
					android.R.layout.simple_spinner_item, checkListItem.getOptionLists());
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			
			selectView.setAdapter(adapter);
			
		}
	}
}
