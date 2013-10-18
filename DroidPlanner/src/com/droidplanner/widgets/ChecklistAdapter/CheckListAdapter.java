package com.droidplanner.widgets.ChecklistAdapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.droidplanner.R;
import com.droidplanner.preflightcheck.CheckListItem;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.widget.TextView;

public class CheckListAdapter extends BaseExpandableListAdapter {
	final LayoutInflater inflater;
	final List<String> listCheckListHeader;
	final HashMap<String, List<XmlRow>> listCheckListChild;
	final View cvCheck,cvSelect,cvRadio,cvToggle,cvValue;
	
	public CheckListAdapter(LayoutInflater inflater,
			List<String> listDataHeader,
			HashMap<String, List<CheckListItem>> listDataChild) {

		this.inflater = inflater;
		this.listCheckListHeader = listDataHeader;
		this.listCheckListChild = new HashMap<String, List<XmlRow>>();
		this.cvCheck = null;
		this.cvRadio = null;
		this.cvSelect = null;
		this.cvToggle = null;
		this.cvValue = null;
//		this.convertViews = new View[XmlRowType.values().length];
		
		for (String dataHeader : listDataHeader) {
			List<XmlRow> xmlRows = new ArrayList<XmlRow>();
			for (CheckListItem listItem : listDataChild.get(dataHeader)) {
				if (listItem.getType().equalsIgnoreCase("check_item")) {
					xmlRows.add(new CheckBox_XmlRow(this.inflater, listItem));
				} else if (listItem.getType().equalsIgnoreCase("value_item")) {
					xmlRows.add(new Value_XmlRow(this.inflater, listItem));
				} else if (listItem.getType().equalsIgnoreCase("radio_item")) {
					xmlRows.add(new Radio_XmlRow(this.inflater, listItem));
				} else if (listItem.getType().equalsIgnoreCase("select_item")) {
					xmlRows.add(new Select_XmlRow(this.inflater, listItem));
				} else if (listItem.getType().equalsIgnoreCase("toggle_item")) {
					xmlRows.add(new Toggle_XmlRow(this.inflater, listItem));
				} else if (listItem.getType().equalsIgnoreCase("switch_item")) {
					xmlRows.add(new Switch_XmlRow(this.inflater, listItem));
				}
			}
			listCheckListChild.put(dataHeader, xmlRows);
		}
	}

	@Override
	public Object getChild(int groupPosition, int childPosititon) {
		return this.listCheckListChild.get(this.listCheckListHeader.get(groupPosition))
				.get(childPosititon);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, final int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		XmlRow row = (XmlRow)getChild(groupPosition, childPosition);
		return row.getView(convertView);
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return this.listCheckListChild.get(this.listCheckListHeader.get(groupPosition))
				.size();
	}

	@Override
	public int getChildTypeCount() {
		return XmlRowType.values().length;
	}

	@Override
	public int getChildType(int groupPosition, int childPosition) {
		return ((XmlRow) getChild(groupPosition,childPosition)).getViewType();
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
}