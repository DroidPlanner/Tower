package org.droidplanner.extra;

import org.droidplanner.R;
import org.droidplanner.fragments.markers.MarkerManager.MarkerSource;
import org.droidplanner.fragments.markers.helpers.MarkerWithText;
import org.droidplanner.fragments.mission.MissionDetailFragment;
import org.droidplanner.fragments.mission.MissionWaypointFragment;
import org.droidplanner.mission.MissionItem;
import org.droidplanner.mission.waypoints.Waypoint;

import android.content.Context;
import android.graphics.Bitmap;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class GraphicWaypoint extends Waypoint implements EditorMissionItem,MarkerSource {

	public GraphicWaypoint(MissionItem item) {
		super(item);
	}

	@Override
	public MissionDetailFragment getDetailFragment() {
		MissionDetailFragment fragment = new MissionWaypointFragment();
		fragment.setItem(this);
		return fragment;
	}

	@Override
	public MarkerOptions build(Context context) {
		return new MarkerOptions().position(DroneHelper.CoordToLatLang(coordinate)).draggable(true).anchor(0.5f, 0.5f).icon(getIcon(context));
	}

	@Override
	public void update(Marker marker, Context context) {
		marker.setPosition(DroneHelper.CoordToLatLang(coordinate));
		marker.setIcon(getIcon(context));		
	}
	
	protected BitmapDescriptor getIcon(Context context) {
		int drawable;
		if (mission.selectionContains(this)) {
			drawable = R.drawable.ic_wp_map_selected;
		} else {
			drawable = R.drawable.ic_wp_map;
		}
		Bitmap marker = MarkerWithText.getMarkerWithTextAndDetail(drawable, Integer.toString(mission.getNumber(this)),
						getIconDetail(), context);
		return BitmapDescriptorFactory.fromBitmap(marker);
	}
	

	private String getIconDetail() {
		try {
			if (mission.getAltitudeDiffFromPreviusItem(this).valueInMeters()==0) {
				return null;
			}else{
				return null; //altitude.toString();
			}
		} catch (Exception e) {
			return null;
		}
	}
	
}