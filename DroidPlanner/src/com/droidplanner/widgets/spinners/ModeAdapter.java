package com.droidplanner.widgets.spinners;

import java.util.ArrayList;
import java.util.List;

import com.MAVLink.Messages.ApmModes;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ModeAdapter extends ArrayAdapter<ApmModes> {
	public ArrayList<ApmModes> modes = new ArrayList<ApmModes>();
	
	public ModeAdapter(Context context, int resource, List<ApmModes> objects) {
		super(context, resource, objects);
		modes.addAll(objects);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView view = (TextView) super.getView(position, convertView, parent);
		view.setText(modes.get(position).getName());
		return view;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return getView(position, convertView, parent);
	}

}