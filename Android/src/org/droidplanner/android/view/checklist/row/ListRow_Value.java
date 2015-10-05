package org.droidplanner.android.view.checklist.row;

import org.droidplanner.android.R;
import org.droidplanner.android.view.checklist.CheckListItem;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.EditText;

public class ListRow_Value extends ListRow implements OnFocusChangeListener {
	private boolean lastFocusState;
	private float lastValue;

	public ListRow_Value(LayoutInflater inflater, final CheckListItem checkListItem) {
		super(inflater, checkListItem);
	}

	@Override
	public View getView(View convertView) {
		View view;

		if (convertView == null) {
			ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.list_value_item, null);
			holder = new ViewHolder(viewGroup, checkListItem);
			viewGroup.setTag(holder);
			view = viewGroup;

			lastValue = checkListItem.getFloatValue();

		} else {
			view = convertView;
			holder = (ViewHolder) convertView.getTag();

		}

		updateDisplay(view, (ViewHolder) holder, checkListItem);
		return view;
	}

	@SuppressWarnings("unused")
	private void updateDisplay(View view, ViewHolder holder, CheckListItem mListItem) {
		double minVal = mListItem.getMin_val();
		double nomVal = mListItem.getNom_val();
		double sysValue = mListItem.getSys_value();
		String unit = mListItem.getUnit();
		boolean failMandatory = sysValue <= minVal;

		getData();

		EditText editText = holder.editTextView;
		if (holder.editTextView.getText().toString() == null)
			holder.editTextView.setText("0.0");
		holder.editTextView.setOnFocusChangeListener(this);
		holder.editTextView.setText(String.valueOf(checkListItem.getFloatValue()));

		updateCheckBox(checkListItem.isMandatory() && !failMandatory);
	}

	@Override
	public int getViewType() {
		return ListRow_Type.VALUE_ROW.ordinal();
	}

	private static class ViewHolder extends BaseViewHolder {
		private EditText editTextView;

		private ViewHolder(ViewGroup viewGroup, CheckListItem checkListItem) {
			super(viewGroup, checkListItem);
		}

		@Override
		protected void setupViewItems(ViewGroup viewGroup, CheckListItem checkListItem) {
			this.editTextView = (EditText) viewGroup.findViewById(R.id.lst_editText);
			this.editTextView.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL
					| InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_CLASS_NUMBER);
		}
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (!hasFocus) {

			if (!lastFocusState)
				return;

			lastFocusState = false;

			float a = (float) 0.0;

			try {
				a = Float.parseFloat(((EditText) v).getText().toString());
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (a != lastValue) {
				lastValue = a;
				this.checkListItem.setValue(((EditText) v).getText().toString());
			}

			if (listener != null)
				listener.onRowItemChanged(this.checkListItem);
		} else if (hasFocus) {
			lastFocusState = !lastFocusState;
		}
	}
}
