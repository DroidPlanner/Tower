package com.droidplanner.widgets.ChecklistAdapter;

import java.util.List;

import com.droidplanner.R;
import com.droidplanner.preflightcheck.CheckListItem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class Radio_XmlRow implements XmlRow {
	private final CheckListItem checkListItem;
	private final LayoutInflater inflater;

	public Radio_XmlRow(LayoutInflater inflater, CheckListItem checkListItem) {
		this.checkListItem = checkListItem;
		this.inflater = inflater;
	}

	public View getView(View convertView) {
		ViewHolder holder;
		View view;
		if (convertView == null) {
			ViewGroup viewGroup = (ViewGroup) inflater.inflate(
					R.layout.preflight_radio_item, null);
			holder = new ViewHolder(viewGroup, checkListItem);
			viewGroup.setTag(holder);
			view = viewGroup;
		} else {
			view = convertView;
			holder = (ViewHolder) convertView.getTag();
		}
		// TODO - Add information (radio items here)
		holder.textView.setText(checkListItem.getTitle());
		return view;
	}

	public int getViewType() {
		return XmlRowType.RADIO_ROW.ordinal();
	}

	private static class ViewHolder {
		final LinearLayout layoutView;
		final TextView textView;
		final RadioGroup radioGroupView;

		private ViewHolder(ViewGroup viewGroup, CheckListItem checkListItem) {
			this.layoutView = (LinearLayout) viewGroup
					.findViewById(R.id.layout_radio);
			this.textView = (TextView) viewGroup
					.findViewById(R.id.chk_label);
			this.radioGroupView = (RadioGroup) viewGroup
					.findViewById(R.id.chk_radioGroup);
			
			setupRadioButtons(viewGroup, checkListItem);
		}

		private void setupRadioButtons(ViewGroup viewGroup,
				CheckListItem checkListItem) {
			
			this.radioGroupView.removeAllViews();

			List<String> optionLists = checkListItem.getOptionLists();
			for (String optionlist : optionLists) {
				RadioButton rButton = new RadioButton(viewGroup.getContext());
				rButton.setLayoutParams(new LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
				rButton.setText(optionlist);
				rButton.setId(optionLists.indexOf(optionlist));
				this.radioGroupView.addView(rButton);
			}
			this.radioGroupView.check(checkListItem.getSelectedIndex());
			
		}
	}
}
