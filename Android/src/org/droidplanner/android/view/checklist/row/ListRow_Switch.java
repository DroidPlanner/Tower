package org.droidplanner.android.view.checklist.row;

import org.droidplanner.android.R;
import org.droidplanner.android.view.checklist.CheckListItem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

public class ListRow_Switch extends ListRow implements OnCheckedChangeListener {

	public ListRow_Switch(LayoutInflater inflater, CheckListItem checkListItem) {
		super(inflater, checkListItem);
	}

	@Override
	public View getView(View convertView) {
		View view;
		if (convertView == null) {
			ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.list_switch_item, null);
			holder = new ViewHolder(viewGroup, checkListItem);

			viewGroup.setTag(holder);
			view = viewGroup;
		} else {
			view = convertView;
			holder = (ViewHolder) convertView.getTag();
		}

		updateDisplay((ViewHolder) holder, checkListItem);
		return view;
	}

	private void updateDisplay(ViewHolder holder, CheckListItem mListItem) {
		boolean failMandatory = false;

		getData();

		failMandatory = !checkListItem.isSys_activated();

		holder.switchView.setOnCheckedChangeListener(this);
		holder.switchView.setClickable(checkListItem.isEditable());
		holder.switchView.setChecked(mListItem.isSys_activated());

		updateCheckBox(checkListItem.isMandatory() && !failMandatory);
	}

	@Override
	public int getViewType() {
		return ListRow_Type.SWITCH_ROW.ordinal();
	}

	private static class ViewHolder extends BaseViewHolder {
		private Switch switchView;

		private ViewHolder(ViewGroup viewGroup, CheckListItem checkListItem) {
			super(viewGroup, checkListItem);
		}

		@Override
		protected void setupViewItems(ViewGroup viewGroup, CheckListItem checkListItem) {
			this.switchView = (Switch) viewGroup.findViewById(R.id.lst_switch);
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
		this.checkListItem.setSys_activated(arg1);
		updateRowChanged();
	}
}
