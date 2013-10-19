package com.droidplanner.widgets.ChecklistAdapter;

import com.droidplanner.R;
import com.droidplanner.preflightcheck.CheckListItem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Value_XmlRow implements XmlRow {
    private final CheckListItem checkListItem;
    private final LayoutInflater inflater;

    public Value_XmlRow(LayoutInflater inflater, CheckListItem checkListItem) {
        this.checkListItem = checkListItem;
        this.inflater = inflater;
    }

	public View getView(View convertView) {
		ViewHolder holder;
		View view;
		if (convertView == null) {
			ViewGroup viewGroup = (ViewGroup) inflater.inflate(
					R.layout.preflight_value_item, null);
			holder = new ViewHolder(viewGroup);
			viewGroup.setTag(holder);
			view = viewGroup;
		} else {
			view = convertView;
			holder = (ViewHolder) convertView.getTag();
		}

		// TODO - Add spinner items
		holder.editTextView.setText(String.valueOf(checkListItem.getNom_val()));
		holder.textView.setText(checkListItem.getTitle());
		
		return view;
	}

	public int getViewType() {
		return XmlRowType.VALUE_ROW.ordinal();
	}

	private static class ViewHolder {
		final LinearLayout layoutView;
		final EditText editTextView;
		final TextView textView;

		private ViewHolder(ViewGroup viewGroup) {
			this.layoutView = (LinearLayout) viewGroup.findViewById(R.id.layout_value);
			this.editTextView = (EditText) viewGroup.findViewById(R.id.chk_editText);
			this.textView = (TextView) viewGroup.findViewById(R.id.chk_label);
		}
	}

}
