package org.droidplanner.android.proxy.mission.item.adapters;

import org.droidplanner.R;
import org.droidplanner.android.utils.file.help.CameraInfoLoader;
import org.droidplanner.core.mission.survey.CameraInfo;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class CamerasAdapter extends ArrayAdapter<String> {

	private CameraInfoLoader loader;
	private Context context;
	private String title = "";
	CameraInfo defaultCamera = new CameraInfo();

	public CamerasAdapter(Context context, int resource) {
		super(context, resource);
		this.context = context;
		loader = new CameraInfoLoader(context);
		add(defaultCamera.name);
		addAll(loader.getCameraInfoList());
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView view = (TextView) super.getView(position, convertView, parent);
		view.setText(title);
		return view;
	}

	public CameraInfo getCamera(int position) {
		try {
			if (position == getPosition(defaultCamera.name)) {
				return defaultCamera;
			}
			return loader.openFile(getItem(position));
		} catch (Exception e) {
			Toast.makeText(context, context.getString(R.string.error_when_opening_file),
					Toast.LENGTH_SHORT).show();
			return defaultCamera;
		}
	}
}
