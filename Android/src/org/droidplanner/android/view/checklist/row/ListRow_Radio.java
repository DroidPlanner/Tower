package org.droidplanner.android.view.checklist.row;

import java.util.List;

import org.droidplanner.android.R;
import org.droidplanner.android.view.checklist.CheckListItem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class ListRow_Radio extends ListRow implements OnCheckedChangeListener {

	public ListRow_Radio(LayoutInflater inflater, CheckListItem checkListItem) {
		super(inflater, checkListItem);
	}

	@Override
	public View getView(View convertView) {
		View view;
		if (convertView == null) {
			ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.list_radio_item, null);
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

		holder.radioGroupView.setOnCheckedChangeListener(this);

		getData();
		updateCheckBox(checkListItem.isVerified());

	}

	@Override
	public int getViewType() {
		return ListRow_Type.RADIO_ROW.ordinal();
	}

	private static class ViewHolder extends BaseViewHolder {
		private RadioGroup radioGroupView;

		public ViewHolder(ViewGroup viewGroup, CheckListItem checkListItem) {
			super(viewGroup, checkListItem);
		}

		@Override
		protected void setupViewItems(ViewGroup viewGroup, CheckListItem checkListItem) {
			this.radioGroupView = (RadioGroup) viewGroup.findViewById(R.id.lst_radioGroup);

			this.radioGroupView.removeAllViews();

			List<String> optionLists = checkListItem.getOptionLists();
			for (String optionlist : optionLists) {
				RadioButton rButton = new RadioButton(viewGroup.getContext());
				rButton.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
						LayoutParams.WRAP_CONTENT));
				rButton.setText(optionlist);
				rButton.setId(optionLists.indexOf(optionlist));
				this.radioGroupView.addView(rButton);
			}
			this.radioGroupView.check(checkListItem.getSelectedIndex());

		}
	}

	@Override
	public void onCheckedChanged(RadioGroup arg0, int arg1) {
		checkListItem.setSelectedIndex(arg1);
		updateRowChanged();

	}
}
