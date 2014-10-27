package org.droidplanner.android.proxy.mission.item.adapters;

import org.droidplanner.core.mission.MissionItemType;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class AdapterMissionItems extends ArrayAdapter<MissionItemType> {

	public AdapterMissionItems(Context context, int resource, MissionItemType[] objects) {
		super(context, resource, objects);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);
		((TextView) view).setText(getItem(position).getLabel());
		return view;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return getView(position, convertView, parent);
	}

}