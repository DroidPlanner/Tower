package com.droidplanner.checklist.row;

import com.droidplanner.R;
import com.droidplanner.checklist.CheckListItem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ListRow_Level implements ListRow_Interface {
    private final CheckListItem checkListItem;
    private final LayoutInflater inflater;

    public ListRow_Level(LayoutInflater inflater, CheckListItem checkListItem) {
        this.checkListItem = checkListItem;
        this.inflater = inflater;
    }

	public View getView(View convertView) {
		ViewHolder holder;
		View view;
		if (convertView == null) {
			ViewGroup viewGroup = (ViewGroup) inflater.inflate(
					R.layout.list_level_item, null);
			holder = new ViewHolder(viewGroup);

			viewGroup.setTag(holder);
			view = viewGroup;
		} else {
			view = convertView;
			holder = (ViewHolder) convertView.getTag();
		}

		// TODO - Add spinner items
		holder.checkBox.setText(checkListItem.getTitle());
		updateProgress(view,holder.progressBar,checkListItem);
		return view;
	}

	private void updateProgress(View view, ProgressBar progressBar, CheckListItem checkListItem2) {
		progressBar.setIndeterminateDrawable(view.getResources().getDrawable(R.drawable.pstate_1));
		progressBar.setProgress(75);
	}

	public int getViewType() {
		return ListRow_Type.LEVEL_ROW.ordinal();
	}

	private static class ViewHolder {
		final LinearLayout layoutView;
		final ProgressBar progressBar;
		final TextView textView;
		final CheckBox checkBox;
		
		private ViewHolder(ViewGroup viewGroup) {
			this.layoutView = (LinearLayout) viewGroup.findViewById(R.id.lst_layout);
			this.checkBox = (CheckBox) viewGroup.findViewById(R.id.lst_check);
			this.progressBar = (ProgressBar) viewGroup.findViewById(R.id.lst_level);
			this.textView = (TextView) viewGroup.findViewById(R.id.lst_unit);
		}
	}

}
