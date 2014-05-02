package org.droidplanner.android.widgets.checklist.row;

import org.droidplanner.R;
import org.droidplanner.android.widgets.checklist.CheckListItem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ListRow_CheckBox extends ListRow {

	public ListRow_CheckBox(LayoutInflater inflater, CheckListItem checkListItem) {
		super(inflater, checkListItem);
	}

	public View getView(View convertView) {
		View view;
		if (convertView == null) {
			ViewGroup viewGroup = (ViewGroup) inflater.inflate(
					R.layout.list_check_item, null);
			holder = new ViewHolder(viewGroup, checkListItem);
			viewGroup.setTag(holder);
			view = viewGroup;
		} else {
			holder = (ViewHolder) convertView.getTag();
			view = convertView;
		}

		updateDisplay(view, (ViewHolder) holder, checkListItem);
		return view;
	}

	private void updateDisplay(View view, ViewHolder holder,
			CheckListItem mListItem) {

		getData(mListItem);

		updateCheckBox(checkListItem.isVerified());
	}

	public int getViewType() {
		return ListRow_Type.CHECKBOX_ROW.ordinal();
	}

	private static class ViewHolder extends BaseViewHolder {

		private ViewHolder(ViewGroup viewGroup, CheckListItem checkListItem) {
			super(viewGroup, checkListItem);
		}
	}
}
