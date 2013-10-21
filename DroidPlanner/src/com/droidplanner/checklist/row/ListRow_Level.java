package com.droidplanner.checklist.row;

import com.droidplanner.R;
import com.droidplanner.checklist.CheckListItem;
import com.droidplanner.drone.Drone;

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

    public ListRow_Level(Drone drone, LayoutInflater inflater, CheckListItem checkListItem) {
        this.checkListItem = checkListItem;
        this.inflater = inflater;
        getDroneVariable(drone,checkListItem);
    }

	private void getDroneVariable(Drone mDrone, CheckListItem mListItem) {
		String sys_tag = mListItem.getSys_tag();
		
		if(sys_tag.equalsIgnoreCase("SYS_BATTERY_LVL")){
			mListItem.setSys_value(mDrone.battery.getBattRemain());
		}
		
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
		updateProgress(view,holder,checkListItem);
		return view;
	}

	private void updateProgress(View view, ViewHolder holder, CheckListItem mListItem) {
		int drawableId;
		double minVal = mListItem.getMin_val();
		double nomVal = mListItem.getNom_val();
		double maxVal = mListItem.getMax_val();
		String unit = mListItem.getUnit();
		double sysValue = mListItem.getSys_value();
		
		if(sysValue<minVal)
			drawableId = R.drawable.pstate_poor;
		else if(sysValue>minVal && sysValue<nomVal)
			drawableId = R.drawable.pstate_warning;
		else
			drawableId = R.drawable.pstate_good;
		holder.progressBar.setIndeterminateDrawable(view.getResources().getDrawable(drawableId));
		holder.progressBar.setProgress((int) sysValue);
		holder.unitView.setText(String.format("%.0f", sysValue)+" "+mListItem.getUnit());
	}

	public int getViewType() {
		return ListRow_Type.LEVEL_ROW.ordinal();
	}

	private static class ViewHolder {
		final LinearLayout layoutView;
		final ProgressBar progressBar;
		final TextView unitView;
		final CheckBox checkBox;
		
		private ViewHolder(ViewGroup viewGroup) {
			this.layoutView = (LinearLayout) viewGroup.findViewById(R.id.lst_layout);
			this.checkBox = (CheckBox) viewGroup.findViewById(R.id.lst_check);
			this.progressBar = (ProgressBar) viewGroup.findViewById(R.id.lst_level);
			this.unitView = (TextView) viewGroup.findViewById(R.id.lst_unit);
			
		}
	}

}
