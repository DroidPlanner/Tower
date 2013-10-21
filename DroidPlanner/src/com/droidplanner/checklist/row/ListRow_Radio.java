package com.droidplanner.checklist.row;

import java.util.List;

import com.droidplanner.R;
import com.droidplanner.checklist.CheckListItem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

public class ListRow_Radio implements ListRow_Interface, OnCheckedChangeListener {
	public interface OnRadioGroupCheckedChangeListener {
		public void onRadioGroupCheckedChanged(CheckListItem checkListItem,
				RadioGroup group, int checkId);
	}

	private OnRadioGroupCheckedChangeListener listener;
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
		// TODO - Add information (radio items here)
		holder.radioGroupView.setOnCheckedChangeListener(this);
		holder.checkBox.setText(checkListItem.getTitle());

		return view;
	}

	public int getViewType() {
		return ListRow_Type.RADIO_ROW.ordinal();
	}

	public void setOnRadioGroupChackedChangeListener(
			OnRadioGroupCheckedChangeListener listener) {
		this.listener = listener;
	}

	private static class ViewHolder {
		final LinearLayout layoutView;
		final CheckBox checkBox;
		final RadioGroup radioGroupView;
		final CheckListItem checkListItem;

		private ViewHolder(ViewGroup viewGroup, CheckListItem checkListItem) {
			this.checkListItem = checkListItem;

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
		if (listener != null)
			listener.onRadioGroupCheckedChanged(checkListItem, arg0, arg1);
	}
}
