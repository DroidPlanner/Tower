package org.droidplanner.android.widgets.adapterViews;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.o3dr.services.android.lib.drone.mission.item.raw.GlobalPositionIntMessage;

import org.droidplanner.R;

import java.util.List;

/**
 * MissionItem Adapter for the MissionItem horizontal list view. This adapter
 * updates the content of the list view item's view based on the mission item
 * type.
 */
public class LocatorItemAdapter extends ArrayAdapter<GlobalPositionIntMessage> {

	private final LayoutInflater inflater;

	public LocatorItemAdapter(Context context, List<GlobalPositionIntMessage> list) {
		super(context, 0, list);
		inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final GlobalPositionIntMessage item = getItem(position);

		final View view;
		if (convertView != null) {
			// recycle view
			view = convertView;
		} else {
			// create new view
			view = inflater.inflate(R.layout.fragment_locator_list_item, parent, false);
		}

		final TextView titleView = (TextView) view.findViewById(R.id.titleView);
		final TextView timeView = (TextView) view.findViewById(R.id.timeView);

		titleView.setText(String.valueOf(position + 1));

        int timeBootMs = item.getTime_boot_ms();
		final int min = timeBootMs / 60000;
		final int sec = (timeBootMs % 60000) / 1000;
		final int fsec = (timeBootMs % 1000) / 100;
		timeView.setText(String.format("%dm%02d.%01ds", min, sec, fsec));

		return view;
	}

}