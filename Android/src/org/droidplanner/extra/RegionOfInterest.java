package org.droidplanner.extra;

import java.util.List;

import org.droidplanner.R;
import org.droidplanner.drone.variables.missionD.MissionItemD;
import org.droidplanner.drone.variables.missionD.waypoints.RegionOfInterestD;
import org.droidplanner.fragments.markers.MarkerManager.MarkerSource;
import org.droidplanner.fragments.mission.MissionDetailFragment;
import org.droidplanner.fragments.mission.MissionRegionOfInterestFragment;

import com.google.android.gms.maps.model.LatLng;

public class RegionOfInterest extends RegionOfInterestD implements MarkerSource{
	
	@Override
	public List<LatLng> getPath() throws Exception {
		throw new Exception();
	}

	@Override
	public MissionDetailFragment getDetailFragment() {
		MissionDetailFragment fragment = new MissionRegionOfInterestFragment();
		fragment.setItem(this);
		return fragment;
	}

	@Override
	protected int getIconDrawable() {
		return R.drawable.ic_wp_map;
	}
	
	@Override
	protected int getIconDrawableSelected() {
		return R.drawable.ic_wp_map_selected;
	}
	
	/*
	private static String getRoiDetail(GenericWaypoint wp, Context context) {
		if (wp.getParam1() == MAV_ROI.MAV_ROI_WPNEXT)
			return context.getString(R.string.next);
		else if (wp.getParam1() == MAV_ROI.MAV_ROI_TARGET)
			return String.format(Locale.US,"wp#%.0f", wp.getParam2());
		else if (wp.getParam1() == MAV_ROI.MAV_ROI_TARGET)
			return String.format(Locale.US,"tg#%.0f", wp.getParam2());
		else
			return "";
	}
	*/
}
