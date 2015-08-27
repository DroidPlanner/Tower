package org.droidplanner.android.view.checklist.row;

import org.droidplanner.android.R;
import org.droidplanner.android.view.checklist.CheckListItem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ListRow_CheckBox extends ListRow {

	public ListRow_CheckBox(LayoutInflater inflater, CheckListItem checkListItem) {
		super(inflater, checkListItem);
	}

	@Override
	public View getView(View convertView) {
		View view;
		if (convertView == null) {
			ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.list_check_item, null);
			holder = new ViewHolder(viewGroup, checkListItem);
			viewGroup.setTag(holder);
			view = viewGroup;
		} else {
			holder = (ViewHolder) convertView.getTag();
			view = convertView;
		}

		updateDisplay();
		return view;
	}

	private void updateDisplay() {

		getData();

		updateCheckBox(checkListItem.isVerified());
	}

	@Override
	public int getViewType() {
		return ListRow_Type.CHECKBOX_ROW.ordinal();
	}

	private static class ViewHolder extends BaseViewHolder {

		private ViewHolder(ViewGroup viewGroup, CheckListItem checkListItem) {
			super(viewGroup, checkListItem);
		}
	}
}
