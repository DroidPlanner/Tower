package com.droidplanner.checklist.row;

import java.util.List;

import com.droidplanner.R;
import com.droidplanner.checklist.CheckListItem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class ListRow_Radio extends ListRow implements OnCheckedChangeListener , OnClickListener{
	private final CheckListItem checkListItem;
	private final LayoutInflater inflater;

	public ListRow_Radio(LayoutInflater inflater, CheckListItem checkListItem) {
		this.checkListItem = checkListItem;
		this.inflater = inflater;
	}

	public View getView(View convertView) {
		final ViewHolder holder;
		View view;
		if (convertView == null) {
			ViewGroup viewGroup = (ViewGroup) inflater.inflate(
					R.layout.list_radio_item, null);
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
		holder.radioGroupView.setOnCheckedChangeListener(this);
		// Common display update
		holder.checkBox.setOnClickListener(this);
		holder.checkBox.setClickable(checkListItem.isEditable());
		holder.checkBox.setText(checkListItem.getTitle());
		holder.checkBox.setChecked(checkListItem.isVerified());

	}

	public int getViewType() {
		return ListRow_Type.RADIO_ROW.ordinal();
	}

	private static class ViewHolder {
		@SuppressWarnings("unused")
		final LinearLayout layoutView;
		final CheckBox checkBox;
		final RadioGroup radioGroupView;

		private ViewHolder(ViewGroup viewGroup, CheckListItem checkListItem) {

			this.layoutView = (LinearLayout) viewGroup
					.findViewById(R.id.lst_layout);
			this.checkBox = (CheckBox) viewGroup.findViewById(R.id.lst_check);
			this.radioGroupView = (RadioGroup) viewGroup
					.findViewById(R.id.lst_radioGroup);

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

	@Override
	public void onCheckedChanged(RadioGroup arg0, int arg1) {
		checkListItem.setSelectedIndex(arg1);

		if (listener != null)
			listener.onRowItemChanged(arg0, checkListItem, checkListItem.isVerified());
	}

	@Override
	public void onClick(View v) {
		this.checkListItem.setVerified(((CheckBox) v).isChecked());
	}
}
