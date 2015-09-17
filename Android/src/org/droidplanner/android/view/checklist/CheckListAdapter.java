package org.droidplanner.android.view.checklist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.droidplanner.android.R;
import org.droidplanner.android.view.checklist.listadapter.ListXmlAdapter;
import org.droidplanner.android.view.checklist.row.ListRow;
import org.droidplanner.android.view.checklist.row.ListRow_CheckBox;
import org.droidplanner.android.view.checklist.row.ListRow_Interface;
import org.droidplanner.android.view.checklist.row.ListRow_Interface.OnRowItemChangeListener;
import org.droidplanner.android.view.checklist.row.ListRow_Level;
import org.droidplanner.android.view.checklist.row.ListRow_Note;
import org.droidplanner.android.view.checklist.row.ListRow_Radio;
import org.droidplanner.android.view.checklist.row.ListRow_Select;
import org.droidplanner.android.view.checklist.row.ListRow_Switch;
import org.droidplanner.android.view.checklist.row.ListRow_Toggle;
import org.droidplanner.android.view.checklist.row.ListRow_Type;
import org.droidplanner.android.view.checklist.row.ListRow_Value;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.widget.TextView;

public class CheckListAdapter extends ListXmlAdapter implements OnRowItemChangeListener {

	public interface OnCheckListItemUpdateListener {
		void onRowItemChanged(CheckListItem checkListItem);

		void onRowItemGetData(CheckListItem checkListItem, String mSysTag);
	}

	private OnCheckListItemUpdateListener listener;

	public CheckListAdapter(LayoutInflater inflater, List<String> listHeader,
			HashMap<String, List<CheckListItem>> listDataChild) {
		super(inflater, listHeader);

		setHeaderLayout(R.layout.list_group_header);

		for (String dataHeader : listHeader) {
			List<ListRow_Interface> xmlRows = new ArrayList<ListRow_Interface>();
			for (CheckListItem listItem : listDataChild.get(dataHeader)) {
				if (listItem.getTagName().equalsIgnoreCase("check_item")) {
					ListRow_CheckBox row = new ListRow_CheckBox(this.inflater, listItem);
					row.setOnRowItemChangeListener(this);
					xmlRows.add(row);

				} else if (listItem.getTagName().equalsIgnoreCase("value_item")) {
					ListRow_Value row = new ListRow_Value(this.inflater, listItem);
					row.setOnRowItemChangeListener(this);
					xmlRows.add(row);

				} else if (listItem.getTagName().equalsIgnoreCase("radio_item")) {
					ListRow_Radio row = new ListRow_Radio(this.inflater, listItem);
					row.setOnRowItemChangeListener(this);
					xmlRows.add(row);

				} else if (listItem.getTagName().equalsIgnoreCase("select_item")) {
					ListRow_Select row = new ListRow_Select(this.inflater, listItem);
					row.setOnRowItemChangeListener(this);
					xmlRows.add(row);

				} else if (listItem.getTagName().equalsIgnoreCase("toggle_item")) {
					ListRow_Toggle row = new ListRow_Toggle(this.inflater, listItem);
					row.setOnRowItemChangeListener(this);
					xmlRows.add(row);

				} else if (listItem.getTagName().equalsIgnoreCase("switch_item")) {
					ListRow_Switch row = new ListRow_Switch(this.inflater, listItem);
					row.setOnRowItemChangeListener(this);
					xmlRows.add(row);

				} else if (listItem.getTagName().equalsIgnoreCase("level_item")) {
					ListRow_Level row = new ListRow_Level(this.inflater, listItem);
					row.setOnRowItemChangeListener(this);
					xmlRows.add(row);
				} else if (listItem.getTagName().equalsIgnoreCase("note_item")) {
					ListRow_Note row = new ListRow_Note(this.inflater, listItem);
					xmlRows.add(row);

				}
			}
			listItems.put(dataHeader, xmlRows);
		}
	}

	public void setOnCheckListItemUpdateListener(OnCheckListItemUpdateListener listener) {
		this.listener = listener;
	}

	public boolean areAllVerified(int groupPosition) {
		return getChildrenVerified(groupPosition) >= getChildrenMandatory(groupPosition);
	}

	@Override
	public void updateRatioValue(TextView lblChkRatio, int groupPosition) {
		int childCount = getChildrenCount(groupPosition);
		int childVerified = getChildrenVerified(groupPosition);
		int childMandatory = getChildrenMandatory(groupPosition);

		if (childVerified < childMandatory)
			lblChkRatio.setTextColor(0xfff9093d);
		else
			lblChkRatio.setTextColor(0xff09f93d);

		lblChkRatio.setTypeface(null, Typeface.BOLD);
		lblChkRatio.setText(String.format("%d/%d [%d]", childVerified, childCount, childMandatory));
	}

	private int getChildrenVerified(int groupPosition) {
		int verified = 0;
		for (int c = 0; c < getChildrenCount(groupPosition); c++) {
			ListRow row = (ListRow) getChild(groupPosition, c);
			CheckListItem listItem = row.getCheckListItem();
			if (listItem.isVerified())
				verified++;
		}
		return verified;
	}

	private int getChildrenMandatory(int groupPosition) {
		int count = 0;
		for (int c = 0; c < getChildrenCount(groupPosition); c++) {
			ListRow row = (ListRow) getChild(groupPosition, c);
			CheckListItem listItem = row.getCheckListItem();
			if (listItem.isMandatory())
				count++;
		}
		return count;
	}

	@Override
	public int getChildTypeCount() {
		return ListRow_Type.values().length;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	@Override
	public void onRowItemChanged(CheckListItem listItem) {
		if (this.listener == null)
			return;
		this.listener.onRowItemChanged(listItem);
	}

	@Override
	public void onRowItemGetData(CheckListItem listItem, String sysTag) {
		if (this.listener == null)
			return;
		this.listener.onRowItemGetData(listItem, sysTag);

	}
}