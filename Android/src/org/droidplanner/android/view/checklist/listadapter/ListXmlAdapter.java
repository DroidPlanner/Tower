package org.droidplanner.android.view.checklist.listadapter;

import java.util.HashMap;
import java.util.List;

import org.droidplanner.android.R;
import org.droidplanner.android.view.checklist.row.ListRow_Interface;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public abstract class ListXmlAdapter extends BaseExpandableListAdapter {

	protected LayoutInflater inflater;
	protected List<String> listHeader;
	protected ListRow_Interface rowHeader;
	protected HashMap<String, List<ListRow_Interface>> listItems;
	protected int layoutId;

	public ListXmlAdapter(LayoutInflater inflater, List<String> listHeader) {
		this.inflater = inflater;
		this.listHeader = listHeader;
		this.listItems = new HashMap<String, List<ListRow_Interface>>();
	}

	public void addRowItem() {

	}

	public void setHeaderLayout(int mLayoutId) {
		this.layoutId = mLayoutId;
	}

	public void setRowHeader(ListRow_Interface mRowHeader) {
		this.rowHeader = mRowHeader;
	}

	@Override
	public Object getChild(int groupPosition, int childPosititon) {
		return this.listItems.get(this.listHeader.get(groupPosition)).get(childPosititon);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, final int childPosition, boolean isLastChild,
			View convertView, ViewGroup parent) {
		ListRow_Interface row = (ListRow_Interface) getChild(groupPosition, childPosition);
		return row.getView(convertView);
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return this.listItems.get(this.listHeader.get(groupPosition)).size();
	}

	@Override
	public int getChildTypeCount() {
		return 0;
	}

	@Override
	public int getChildType(int groupPosition, int childPosition) {
		return ((ListRow_Interface) getChild(groupPosition, childPosition)).getViewType();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return this.listHeader.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return this.listHeader.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
			ViewGroup parent) {
		String headerTitle = (String) getGroup(groupPosition);
		if (convertView == null) {
			convertView = this.inflater.inflate(layoutId, parent, false);
		}

		TextView lblListHeader = (TextView) convertView.findViewById(R.id.lblListHeader);
		if (lblListHeader != null) {
			lblListHeader.setTypeface(null, Typeface.BOLD);
			lblListHeader.setText(headerTitle);
		}

		TextView lblChkRatio = (TextView) convertView.findViewById(R.id.lblChkRatio);
		if (lblChkRatio != null) {
			updateRatioValue(lblChkRatio, groupPosition);
		}

		return convertView;
	}

	public void updateRatioValue(TextView lblChkRatio, int groupPosition) {
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