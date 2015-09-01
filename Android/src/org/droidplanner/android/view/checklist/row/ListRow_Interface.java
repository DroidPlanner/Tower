package org.droidplanner.android.view.checklist.row;

import org.droidplanner.android.view.checklist.CheckListItem;

import android.view.View;

public interface ListRow_Interface {
	public interface OnRowItemChangeListener {
		public void onRowItemChanged(CheckListItem listItem);

		public void onRowItemGetData(CheckListItem listItem, String sysTag);
	}

	public View getView(View convertView);

	public int getViewType();
}
