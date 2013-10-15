package com.droidplanner.fragments.markers;

import java.util.Locale;

import android.content.Context;

import android.graphics.Color;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.enums.MAV_ROI;
import com.droidplanner.R;
import com.droidplanner.drone.variables.waypoint;
import com.droidplanner.fragments.markers.helpers.MarkerWithText;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class WaypointMarker {

	public static MarkerOptions build(waypoint wp, Context context) {
		return new MarkerOptions()
				.position(wp.getCoord())
				.visible(wp.getCmd().showOnMap())
				.draggable(true)
				.icon(getIcon(wp,context));
	}

	public static void update(Marker marker, waypoint wp, Context context) {
		marker.setPosition(wp.getCoord());
		marker.setIcon(getIcon(wp,context));
	}

	private static BitmapDescriptor getIcon(waypoint wp, Context context) {
		switch (wp.getCmd()) {
		default:
		case CMD_NAV_WAYPOINT:
			return BitmapDescriptorFactory.fromBitmap(MarkerWithText.getMarkerWithTextAndDetail(Color.WHITE,
					Integer.toString(wp.getNumber()), String.format("%.0fm", wp.getHeight()), context));

		case CMD_NAV_LOITER_TIME:
			return BitmapDescriptorFactory.fromBitmap(MarkerWithText.getMarkerWithTextAndDetail(0x4d4dff, // BLUE
					Integer.toString(wp.getNumber()), String.format("t %.0fm", wp.getHeight()), context));
		case CMD_NAV_LOITER_TURNS:
			return BitmapDescriptorFactory.fromBitmap(MarkerWithText.getMarkerWithTextAndDetail(0x4d4dff, // BLUE
					Integer.toString(wp.getNumber()), String.format("n %.0fm", wp.getHeight()), context));
		case CMD_NAV_LOITER_UNLIM:
			return BitmapDescriptorFactory.fromBitmap(MarkerWithText.getMarkerWithTextAndDetail(0x4d4dff, // BLUE
					Integer.toString(wp.getNumber()), String.format("* %.0fm", wp.getHeight()), context));

		case CMD_NAV_RETURN_TO_LAUNCH:
			return BitmapDescriptorFactory.fromBitmap(MarkerWithText.getMarkerWithTextAndDetail(0x00ff00, // GREEN
					Integer.toString(wp.getNumber()), String.format("%.0fm", wp.getHeight()), context));
		case CMD_NAV_LAND:
			return BitmapDescriptorFactory.fromBitmap(MarkerWithText.getMarkerWithTextAndDetail(0xffff00, // YELLOW
					Integer.toString(wp.getNumber()), "", context));
		case CMD_NAV_TAKEOFF:
			return BitmapDescriptorFactory.fromBitmap(MarkerWithText.getMarkerWithTextAndDetail(0xff00ff, // MAGENTA
					Integer.toString(wp.getNumber()), String.format("%.0fm", wp.getHeight()), context));

		case CMD_NAV_PATHPLANNING:
		case CMD_NAV_ROI:
			return BitmapDescriptorFactory.fromBitmap(MarkerWithText.getMarkerWithTextAndDetail(0x007fff, // AZURE
					Integer.toString(wp.getNumber()), getRoiDetail(wp, context), context));
		}

	}

	private static String getRoiDetail(waypoint wp, Context context)
	{
		if(wp.getParam1() == MAV_ROI.MAV_ROI_WPNEXT)
			return context.getString(R.string.next);
		else if(wp.getParam1() == MAV_ROI.MAV_ROI_TARGET)
			return String.format("wp#%.0f", wp.getParam2());
		else if(wp.getParam1() == MAV_ROI.MAV_ROI_TARGET)
			return String.format("tg#%.0f", wp.getParam2());
		else
			return "";
	}

}
