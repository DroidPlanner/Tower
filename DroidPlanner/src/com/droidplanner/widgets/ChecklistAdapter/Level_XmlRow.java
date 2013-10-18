package com.droidplanner.widgets.ChecklistAdapter;

import com.droidplanner.R;
import com.droidplanner.preflightcheck.CheckListItem;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

public class Level_XmlRow implements XmlRow {
    private final CheckListItem checkListItem;
    private final LayoutInflater inflater;

    public Level_XmlRow(LayoutInflater inflater, CheckListItem checkListItem) {
        this.checkListItem = checkListItem;
        this.inflater = inflater;
    }

	public View getView(View convertView) {
		ViewHolder holder;
		View view;
		if (convertView == null) {
			ViewGroup viewGroup = (ViewGroup) inflater.inflate(
					R.layout.preflight_level_item, null);
			holder = new ViewHolder(viewGroup);

			viewGroup.setTag(holder);
			view = viewGroup;
		} else {
			view = convertView;
			holder = (ViewHolder) convertView.getTag();
		}

		// TODO - Add spinner items
		holder.textView.setText(checkListItem.getTitle());
		updateProgress(view,holder.progressBar,checkListItem);
		return view;
	}

	private void updateProgress(View view, ProgressBar progressBar, CheckListItem checkListItem2) {
		progressBar.setIndeterminateDrawable(view.getResources().getDrawable(R.drawable.pstate_1));
		progressBar.setProgress(75);
	}

	public int getViewType() {
		return XmlRowType.LEVEL_ROW.ordinal();
	}

	private static class ViewHolder {
		final LinearLayout layoutView;
		final ProgressBar progressBar;
		final TextView textView;

		private ViewHolder(ViewGroup viewGroup) {
			this.layoutView = (LinearLayout) viewGroup.findViewById(R.id.layout_toggle);
			this.progressBar = (ProgressBar) viewGroup.findViewById(R.id.chk_level);
			this.textView = (TextView) viewGroup.findViewById(R.id.chk_label);
		}
	}

}
