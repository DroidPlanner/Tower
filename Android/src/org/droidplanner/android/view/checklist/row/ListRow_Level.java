package org.droidplanner.android.view.checklist.row;

import org.droidplanner.android.R;
import org.droidplanner.android.view.checklist.CheckListItem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ListRow_Level extends ListRow {

	public ListRow_Level(LayoutInflater inflater, CheckListItem checkListItem) {
		super(inflater, checkListItem);
	}

	@Override
	public View getView(View convertView) {
		View view;
		if (convertView == null) {
			ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.list_level_item, null);
			holder = new ViewHolder(viewGroup, checkListItem);
			viewGroup.setTag(holder);
			view = viewGroup;
		} else {
			view = convertView;
			holder = (ViewHolder) convertView.getTag();
		}

		updateDisplay(view, (ViewHolder) holder, checkListItem);
		return view;
	}

	private void updateDisplay(View view, ViewHolder holder, CheckListItem mListItem) {
		int drawableId;
		double minVal = mListItem.getMin_val();
		double nomVal = mListItem.getNom_val();
		double sysValue = mListItem.getSys_value();
		String unit = mListItem.getUnit();
		boolean failMandatory = false;

		getData();

		failMandatory = sysValue <= minVal;

		if (sysValue <= minVal)
			drawableId = R.drawable.pstate_poor;
		else if (sysValue > minVal && sysValue <= nomVal)
			drawableId = R.drawable.pstate_warning;
		else
			drawableId = R.drawable.pstate_good;

		holder.progressBar.setMax((int) mListItem.getMax_val());
		holder.progressBar.setProgressDrawable(view.getResources().getDrawable(drawableId));
		holder.progressBar.setProgress((int) sysValue);

		try {
			holder.unitView.setText(String.format(unit, sysValue));
		} catch (Exception e) {
			holder.unitView.setText("Error");
			e.printStackTrace();
		}

		updateCheckBox(checkListItem.isMandatory() && !failMandatory);
		/*
		 * if(holder.checkBox.isChecked())
		 * holder.layoutView.setBackgroundColor(getViewType()); else
		 * holder.layoutView.setBackgroundColor(Color.parseColor("#4f0f00"));
		 */

	}

	@Override
	public int getViewType() {
		return ListRow_Type.LEVEL_ROW.ordinal();
	}

	private static class ViewHolder extends BaseViewHolder {
		private ProgressBar progressBar;
		private TextView unitView;

		private ViewHolder(ViewGroup viewGroup, CheckListItem checkListItem) {
			super(viewGroup, checkListItem);
		}

		@Override
		protected void setupViewItems(ViewGroup viewGroup, CheckListItem checkListItem) {
			this.progressBar = (ProgressBar) viewGroup.findViewById(R.id.lst_level);
			this.unitView = (TextView) viewGroup.findViewById(R.id.lst_unit);
			this.progressBar.setMax((int) checkListItem.getMax_val());

		}
	}
}
