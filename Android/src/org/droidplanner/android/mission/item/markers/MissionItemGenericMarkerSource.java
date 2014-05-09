package org.droidplanner.android.mission.item.markers;

import org.droidplanner.android.mission.item.MissionItemRender;

public class MissionItemGenericMarkerSource {

	protected final MissionItemRender mMarkerOrigin;
	
	public MissionItemGenericMarkerSource(MissionItemRender origin) {
		mMarkerOrigin = origin;
	}

	public static MissionItemGenericMarkerSource newInstance(MissionItemRender origin) {
		MissionItemGenericMarkerSource markerSource;
	    switch (origin.getMissionItem().getType()) {
	        case LAND:
	            markerSource = new LandMarkerSource(origin);
	            break;
	
	        case LOITER:
	        case LOITER_INF:
	        case LOITERN:
	        case LOITERT:
	            markerSource = new LoiterMarkerSource(origin);
	            break;
	
	        case ROI:
	            markerSource = new ROIMarkerSource(origin);
	            break;
		
	        case WAYPOINT:
	            markerSource = new WaypointMarkerSource(origin);
	            break;
	            
	        case SURVEY:
	        	markerSource = new MissionItemSurveyMarkerSource(origin);
	        	break;
	
	        default:
	            markerSource = null;
	            break;
	    }
	
	    return markerSource;
	}

	public MissionItemRender getMarkerOrigin() {
	    return mMarkerOrigin;
	}

}