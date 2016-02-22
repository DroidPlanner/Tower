package org.droidplanner.android.view.checklist.row;

import org.droidplanner.android.R;
import org.droidplanner.android.view.checklist.CheckListItem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.EditText;

public class ListRow_Note extends ListRow implements OnFocusChangeListener {

	public ListRow_Note(LayoutInflater inflater, final CheckListItem checkListItem) {
		super(inflater, checkListItem);
	}

	@Override
	public View getView(View convertView) {
		View view;
		if (convertView == null) {
			ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.list_note_item, null);
			holder = new ViewHolder(viewGroup, checkListItem);
			viewGroup.setTag(holder);
			view = viewGroup;
		} else {
			view = convertView;
			holder = (ViewHolder) convertView.getTag();
		}

		updateDisplay((ViewHolder) holder);
		return view;
	}

	private void updateDisplay(ViewHolder holder) {
		holder.editTextView.setOnFocusChangeListener(this);
		holder.editTextView.setText(checkListItem.getValue());

		updateCheckBox(checkListItem.isVerified());
	}

	@Override
	public int getViewType() {
		return ListRow_Type.NOTE_ROW.ordinal();
	}

	private static class ViewHolder extends BaseViewHolder {
		private EditText editTextView;

		private ViewHolder(ViewGroup viewGroup, CheckListItem checkListItem) {
			super(viewGroup, checkListItem);
		}

		@Override
		protected void setupViewItems(ViewGroup viewGroup, CheckListItem checkListItem) {
			this.editTextView = (EditText) viewGroup.findViewById(R.id.lst_note);
		}
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (!v.isFocused() && this.listener != null) {
			checkListItem.setValue(((ViewHolder) this.holder).editTextView.getText().toString());
			updateRowChanged();
		}

	}
}
