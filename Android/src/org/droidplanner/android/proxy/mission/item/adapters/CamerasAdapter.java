package org.droidplanner.android.proxy.mission.item.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.o3dr.services.android.lib.drone.mission.item.complex.CameraDetail;

import java.util.List;

public class CamerasAdapter extends ArrayAdapter<CameraDetail> {

	public CamerasAdapter(Context context, int resource, CameraDetail[] cameraDetails) {
		super(context, resource, cameraDetails);
	}

    public CamerasAdapter(Context context, int resource, List<CameraDetail> cameraDetails){
        super(context, resource, cameraDetails);
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView view = (TextView) super.getView(position, convertView, parent);
		view.setText(getItem(position).getName());
		return view;
	}

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        TextView view = (TextView) super.getDropDownView(position, convertView, parent);
        view.setText(getItem(position).getName());
        return view;
    }

}
