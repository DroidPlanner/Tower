package com.droidplanner.checklist.row;

import com.droidplanner.R;
import com.droidplanner.checklist.CheckListItem;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

public class ListRow_Value extends ListRow implements OnFocusChangeListener,
		OnClickListener {
	private final CheckListItem checkListItem;
	private final LayoutInflater inflater;
	@SuppressWarnings("unused")
	private EditText editText;
	private boolean lastFocusState;
	private float lastValue;

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
			if (holder.editTextView.getText().toString() == null)
				holder.editTextView.setText("0.0");

			lastValue = checkListItem.getFloatValue();

		} else {
			view = convertView;
			holder = (ViewHolder) convertView.getTag();

		}

		updateDisplay(view, holder, checkListItem);
		return view;
	}

	@SuppressWarnings("unused")
	private void updateDisplay(View view, ViewHolder holder,
			CheckListItem mListItem) {
		double minVal = mListItem.getMin_val();
		double nomVal = mListItem.getNom_val();
		double sysValue = mListItem.getSys_value();
		String unit = mListItem.getUnit();
		boolean failMandatory = sysValue <= minVal;

		editText = holder.editTextView;
		holder.editTextView.setOnFocusChangeListener(this);
		holder.editTextView.setText(String.valueOf(checkListItem
				.getFloatValue()));
		
		// Common display update
		holder.checkBox.setOnClickListener(this);
		holder.checkBox.setClickable(checkListItem.isEditable());
		holder.checkBox.setText(checkListItem.getTitle());
		holder.checkBox.setChecked(checkListItem.isMandatory()&&!failMandatory);

		checkListItem.setVerified(holder.checkBox.isChecked());
	}

	public int getViewType() {
		return ListRow_Type.VALUE_ROW.ordinal();
	}

	private static class ViewHolder {
		@SuppressWarnings("unused")
		final LinearLayout layoutView;
		final EditText editTextView;
		final CheckBox checkBox;

		private ViewHolder(ViewGroup viewGroup) {
			this.layoutView = (LinearLayout) viewGroup
					.findViewById(R.id.lst_layout);
			this.editTextView = (EditText) viewGroup
					.findViewById(R.id.lst_editText);
			this.editTextView.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL
					| InputType.TYPE_NUMBER_FLAG_SIGNED
					| InputType.TYPE_CLASS_NUMBER);
			// this.editTextView.setInputType(InputType.TYPE_CLASS_PHONE);

			this.checkBox = (CheckBox) viewGroup.findViewById(R.id.lst_check);
		}
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (lastFocusState != hasFocus) {
			lastFocusState = hasFocus;
			float a = (float) 0.0;

			try {
				a = Float.parseFloat(((EditText) v).getText().toString());
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (a != lastValue) {
				lastValue = a;
				this.checkListItem
						.setValue(((EditText) v).getText().toString());
			}

			if (listener != null)
				listener.onRowItemChanged(v, this.checkListItem,
						this.checkListItem.isVerified());

		}
	}

	@Override
	public void onClick(View v) {
		this.checkListItem.setVerified(((CheckBox) v).isChecked());
	}
}
