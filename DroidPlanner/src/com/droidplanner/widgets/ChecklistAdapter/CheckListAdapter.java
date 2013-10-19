package com.droidplanner.widgets.ChecklistAdapter;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.RadioGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.droidplanner.R;
import com.droidplanner.preflightcheck.CheckListItem;
import com.droidplanner.widgets.ChecklistAdapter.CheckBox_XmlRow.OnCheckBoxChangeListener;
import com.droidplanner.widgets.ChecklistAdapter.Radio_XmlRow.OnRadioGroupCheckedChangeListener;
import com.droidplanner.widgets.ChecklistAdapter.Select_XmlRow.OnSelectChangeListener;
import com.droidplanner.widgets.ChecklistAdapter.Switch_XmlRow.OnSwitchChangeListener;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.widget.TextView;

public class CheckListAdapter extends BaseExpandableListAdapter implements
		OnRadioGroupCheckedChangeListener, OnSelectChangeListener,
		OnCheckBoxChangeListener, OnSwitchChangeListener {

	public interface OnCheckListItemUpdateListener {
		public void onRadioGroupUpdate(CheckListItem checkListItem,
				RadioGroup group, int checkId);

		public void onSelectUpdate(CheckListItem checkListItem, int selectId);

		public void onCheckBoxUpdate(CheckListItem checkListItem,
				boolean isChecked);

		public void onSwitchUpdate(CheckListItem checkListItem,
				boolean isSwitched);

	}

	private OnCheckListItemUpdateListener listener;

	final LayoutInflater inflater;
	final List<String> listCheckListHeader;
	final HashMap<String, List<XmlRow>> listCheckListChild;

	public CheckListAdapter(LayoutInflater inflater,
			List<String> listDataHeader,
			HashMap<String, List<CheckListItem>> listDataChild) {

		this.inflater = inflater;
		this.listCheckListHeader = listDataHeader;
		this.listCheckListChild = new HashMap<String, List<XmlRow>>();

		for (String dataHeader : listDataHeader) {
			List<XmlRow> xmlRows = new ArrayList<XmlRow>();
			for (CheckListItem listItem : listDataChild.get(dataHeader)) {
				if (listItem.getType().equalsIgnoreCase("check_item")) {
					CheckBox_XmlRow row = new CheckBox_XmlRow(this.inflater,
							listItem);
					row.setOnCheckBoxChangeListener(this);
					xmlRows.add(row);
				} else if (listItem.getType().equalsIgnoreCase("value_item")) {
					xmlRows.add(new Value_XmlRow(this.inflater, listItem));
				} else if (listItem.getType().equalsIgnoreCase("radio_item")) {
					Radio_XmlRow row = new Radio_XmlRow(this.inflater, listItem);
					row.setOnRadioGroupChackedChangeListener(this);
					xmlRows.add(row);
				} else if (listItem.getType().equalsIgnoreCase("select_item")) {
					Select_XmlRow row = new Select_XmlRow(this.inflater,
							listItem);
					row.setOnSelectChangeListener(this);
					xmlRows.add(row);
				} else if (listItem.getType().equalsIgnoreCase("toggle_item")) {
					xmlRows.add(new Toggle_XmlRow(this.inflater, listItem));
				} else if (listItem.getType().equalsIgnoreCase("switch_item")) {
					Switch_XmlRow row = new Switch_XmlRow(this.inflater,
							listItem);
					row.setOnSwitchChangeListener(this);
					xmlRows.add(row);
				} else if (listItem.getType().equalsIgnoreCase("level_item")) {
					xmlRows.add(new Level_XmlRow(this.inflater, listItem));
				}
			}
			listCheckListChild.put(dataHeader, xmlRows);
		}
	}

	public void setOnCheckListItemUpdateListener(
			OnCheckListItemUpdateListener listener) {
		this.listener = listener;
	}

	@Override
	public Object getChild(int groupPosition, int childPosititon) {
		return this.listCheckListChild.get(
				this.listCheckListHeader.get(groupPosition))
				.get(childPosititon);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, final int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		XmlRow row = (XmlRow) getChild(groupPosition, childPosition);
		return row.getView(convertView);
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return this.listCheckListChild.get(
				this.listCheckListHeader.get(groupPosition)).size();
	}

	@Override
	public int getChildTypeCount() {
		return XmlRowType.values().length;
	}

	@Override
	public int getChildType(int groupPosition, int childPosition) {
		return ((XmlRow) getChild(groupPosition, childPosition)).getViewType();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return this.listCheckListHeader.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return this.listCheckListHeader.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		String headerTitle = (String) getGroup(groupPosition);
		if (convertView == null) {
			LayoutInflater infalInflater = this.inflater;
			convertView = infalInflater.inflate(R.layout.preflight_list_group,
					null);
		}

		TextView lblListHeader = (TextView) convertView
				.findViewById(R.id.lblListHeader);
		lblListHeader.setTypeface(null, Typeface.BOLD);
		lblListHeader.setText(headerTitle);

		return convertView;
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
	public void onRadioGroupCheckedChanged(CheckListItem checkListItem,
			RadioGroup group, int checkId) {
		if (this.listener != null)
			this.listener.onRadioGroupUpdate(checkListItem, group, checkId);
	}

	@Override
	public void onSelectChanged(CheckListItem checkListItem, int selectId) {
		if (this.listener != null)
			this.listener.onSelectUpdate(checkListItem, selectId);
	}

	@Override
	public void onCheckBoxChanged(CheckListItem checkListItem, boolean isChecked) {
		if (this.listener != null)
			this.listener.onCheckBoxUpdate(checkListItem, isChecked);
	}

	@Override
	public void onSwitchChanged(CheckListItem checkListItem, boolean isSwitched) {
		if (this.listener != null)
			this.listener.onSwitchUpdate(checkListItem, isSwitched);
	}

}