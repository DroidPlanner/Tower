package org.droidplanner.android.fragments.mission;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

class AdapterMissionItems extends ArrayAdapter<MissionItemTypes> {

	public AdapterMissionItems(Context context, int resource,
			MissionItemTypes[] objects) {
		super(context, resource, objects);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);
		((TextView) view).setText(getItem(position).getName());
		return view;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return getView(position, convertView, parent);
	}

}