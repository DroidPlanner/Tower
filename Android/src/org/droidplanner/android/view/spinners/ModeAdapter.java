package org.droidplanner.android.view.spinners;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.o3dr.services.android.lib.drone.property.VehicleMode;

public class ModeAdapter extends ArrayAdapter<VehicleMode> {

	public ModeAdapter(Context context, int resource) {
		super(context, resource);
	}

	public ModeAdapter(Context context, int resource, List<VehicleMode> objects) {
		super(context, resource, objects);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final VehicleMode mode = getItem(position);
		TextView view = (TextView) super.getView(position, convertView, parent);
		view.setText(mode.getLabel());
		return view;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return getView(position, convertView, parent);
	}

}