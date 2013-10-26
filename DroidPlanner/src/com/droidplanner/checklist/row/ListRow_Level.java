package com.droidplanner.checklist.row;

import com.droidplanner.R;
import com.droidplanner.checklist.CheckListItem;
import com.droidplanner.drone.Drone;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ListRow_Level extends ListRow  implements OnClickListener{
	private final CheckListItem checkListItem;
	private final LayoutInflater inflater;
	private ViewHolder holder;
	
	public ListRow_Level(Drone drone, LayoutInflater inflater,
			CheckListItem checkListItem) {
		this.checkListItem = checkListItem;
		this.inflater = inflater;
		getDroneVariable(drone, checkListItem);
	}

	public View getView(View convertView) {
		View view;
		if (convertView == null) {
			ViewGroup viewGroup = (ViewGroup) inflater.inflate(
					R.layout.list_level_item, null);
			holder = new ViewHolder(viewGroup, checkListItem);
			viewGroup.setTag(holder);
			view = viewGroup;
		} else {
			view = convertView;
			holder = (ViewHolder) convertView.getTag();
		}
		
		updateDisplay(view, holder, checkListItem);
		return view;
	}

	private void updateDisplay(View view, ViewHolder holder,
			CheckListItem mListItem) {
		int drawableId;
		double minVal = mListItem.getMin_val();
		double nomVal = mListItem.getNom_val();
		double sysValue = mListItem.getSys_value();
		String unit = mListItem.getUnit();
		boolean failMandatory = sysValue <= minVal;

		
		if (sysValue < minVal)
			drawableId = R.drawable.pstate_poor;
		else if (sysValue > minVal && sysValue <= nomVal)
			drawableId = R.drawable.pstate_warning;
		else
			drawableId = R.drawable.pstate_good;
		holder.progressBar.setProgressDrawable(view.getResources().getDrawable(
				drawableId));
		holder.progressBar.setProgress((int) sysValue);

		try {
			holder.unitView.setText(String.format(unit, sysValue));
		} catch (Exception e) {
			holder.unitView.setText("Error");
			e.printStackTrace();
		}

		
		//Common display update
		holder.checkBox.setOnClickListener(this);
		holder.checkBox.setText(checkListItem.getTitle());
		holder.checkBox.setClickable(checkListItem.isEditable());			
		holder.checkBox.setChecked(checkListItem.isMandatory()&&!failMandatory);

		checkListItem.setVerified(holder.checkBox.isChecked());
/*	
		if(holder.checkBox.isChecked())
			holder.layoutView.setBackgroundColor(getViewType());
		else
			holder.layoutView.setBackgroundColor(Color.parseColor("#4f0f00"));
*/

	}

	public int getViewType() {
		return ListRow_Type.LEVEL_ROW.ordinal();
	}

	private static class ViewHolder extends BaseViewHolder {
		private ProgressBar progressBar;
		private TextView unitView;

		private ViewHolder(ViewGroup viewGroup, CheckListItem checkListItem) {
				super(viewGroup,checkListItem);
		}
		
		@Override
		protected void setupViewItems(ViewGroup viewGroup, CheckListItem checkListItem){
			this.layoutView = (LinearLayout) viewGroup
					.findViewById(R.id.lst_layout);
			this.checkBox = (CheckBox) viewGroup.findViewById(R.id.lst_check);
			this.progressBar = (ProgressBar) viewGroup
					.findViewById(R.id.lst_level);
			this.unitView = (TextView) viewGroup.findViewById(R.id.lst_unit);
			this.progressBar.setMax((int) checkListItem.getMax_val());

		}
	}

	@Override
	public void onClick(View v) {
		this.checkListItem.setVerified(((CheckBox) v).isChecked());
		
		if(this.listener!=null){
			this.listener.onRowItemChanged(v, checkListItem, checkListItem.isVerified());
		}
	}

}
