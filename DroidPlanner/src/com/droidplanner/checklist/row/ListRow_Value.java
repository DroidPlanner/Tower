package com.droidplanner.checklist.row;

import com.droidplanner.R;
import com.droidplanner.checklist.CheckListItem;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ListRow_Value implements ListRow_Interface, OnFocusChangeListener {
	public interface OnValueChangeListener {
		public void onValueChanged(CheckListItem checkListItem, String newValue);
	}

	private OnValueChangeListener listener;
	private final CheckListItem checkListItem;
	private final LayoutInflater inflater;
	private EditText editText;
	private boolean lastFocusState;
	private String lastValue;

	public ListRow_Value(LayoutInflater inflater,
			final CheckListItem checkListItem) {
		this.checkListItem = checkListItem;
		this.inflater = inflater;
	}

	public View getView(View convertView) {
		ViewHolder holder;
		View view;

		if (convertView == null) {
			ViewGroup viewGroup = (ViewGroup) inflater.inflate(
					R.layout.list_value_item, null);
			holder = new ViewHolder(viewGroup);
			viewGroup.setTag(holder);
			view = viewGroup;
			lastValue = holder.editTextView.getText().toString();

		} else {
			view = convertView;
			holder = (ViewHolder) convertView.getTag();

		}

		// TODO - Add spinner items
		editText = holder.editTextView;
		holder.editTextView.setOnFocusChangeListener(this);
		holder.editTextView.setText(checkListItem.getValue());
		holder.checkBox.setText(checkListItem.getTitle());

		return view;
	}

	public int getViewType() {
		return ListRow_Type.VALUE_ROW.ordinal();
	}

	public void setOnValueChangeListener(OnValueChangeListener listener) {
		this.listener = listener;
	}

	private static class ViewHolder {
		final LinearLayout layoutView;
		final EditText editTextView;
		final CheckBox checkBox;

		private ViewHolder(ViewGroup viewGroup) {
			this.layoutView = (LinearLayout) viewGroup
					.findViewById(R.id.lst_layout);
			this.editTextView = (EditText) viewGroup
					.findViewById(R.id.lst_editText);
			this.checkBox = (CheckBox) viewGroup.findViewById(R.id.lst_check);
		}
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (lastFocusState != hasFocus) {
			lastFocusState = hasFocus;
			String a = ((EditText) v).getText().toString();

			if (!a.equals(lastValue)) {
				lastValue = a;
				this.checkListItem
						.setValue(((EditText) v).getText().toString());
			}

			if (!hasFocus && this.listener != null) {
				this.listener.onValueChanged(checkListItem, this.editText
						.getText().toString());

			}
		}
	}
}
