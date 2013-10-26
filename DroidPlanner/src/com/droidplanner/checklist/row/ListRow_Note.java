package com.droidplanner.checklist.row;

import com.droidplanner.R;
import com.droidplanner.checklist.CheckListItem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

public class ListRow_Note extends ListRow implements OnFocusChangeListener,
		OnClickListener {
	private final CheckListItem checkListItem;
	private final LayoutInflater inflater;
	private EditText editText;
	private ViewHolder holder;

	public ListRow_Note(LayoutInflater inflater,
			final CheckListItem checkListItem) {
		this.checkListItem = checkListItem;
		this.inflater = inflater;
	}

	public View getView(View convertView) {
		View view;
		if (convertView == null) {
			ViewGroup viewGroup = (ViewGroup) inflater.inflate(
					R.layout.list_note_item, null);
			holder = new ViewHolder(viewGroup, checkListItem);
			viewGroup.setTag(holder);
			view = viewGroup;
		} else {
			view = convertView;
			holder = (ViewHolder) convertView.getTag();
		}
		editText = holder.editTextView;

		updateDisplay(view, holder, checkListItem);
		return view;
	}

	private void updateDisplay(View view, ViewHolder holder,
			CheckListItem mListItem) {
		holder.editTextView.setOnFocusChangeListener(this);
		holder.editTextView.setText(checkListItem.getValue());

		// Common display update
		holder.checkBox.setOnClickListener(this);
		holder.checkBox.setClickable(checkListItem.isEditable());
		holder.checkBox.setText(checkListItem.getTitle());
		holder.checkBox.setChecked(checkListItem.isVerified());
	}

	public int getViewType() {
		return ListRow_Type.NOTE_ROW.ordinal();
	}

	private static class ViewHolder extends BaseViewHolder{
		private EditText editTextView;

		private ViewHolder(ViewGroup viewGroup, CheckListItem checkListItem) {
			super(viewGroup, checkListItem);
		}

		@Override
		protected void setupViewItems(ViewGroup viewGroup,
				CheckListItem checkListItem) {
			this.layoutView = (LinearLayout) viewGroup
					.findViewById(R.id.lst_layout);
			this.editTextView = (EditText) viewGroup
					.findViewById(R.id.lst_note);
			this.checkBox = (CheckBox) viewGroup.findViewById(R.id.lst_check);
		}
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (!v.isFocused() && this.listener != null) {
			checkListItem.setValue(this.editText.getText().toString());
			this.listener.onRowItemChanged(v, checkListItem,
					checkListItem.isVerified());
		}

	}

	@Override
	public void onClick(View v) {
		this.checkListItem.setVerified(((CheckBox) v).isChecked());
	}
}
