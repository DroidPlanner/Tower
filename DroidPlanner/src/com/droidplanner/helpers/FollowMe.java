package com.droidplanner.helpers;

import android.content.Context;
import android.widget.Toast;

import com.droidplanner.service.MAVLinkClient;

public class FollowMe {
	private MAVLinkClient MAV;
	private Context context;
	
	public FollowMe(MAVLinkClient MAVClient,Context context) {
		this.MAV = MAVClient;
		this.context = context;
	}

	public void toogleFollowMeState() {
		if(isEnabled()){
			enableFollowMe();
			Toast.makeText(context, "FollowMe Enabled", Toast.LENGTH_SHORT).show();
		}else {
			disableFollowMe();				
			Toast.makeText(context, "FollowMe Enabled", Toast.LENGTH_SHORT).show();
		}
	}
	
	public void enableFollowMe() {
		//TODO add the function that handles the follow me function
	}
	
	public void disableFollowMe() {
		//TODO add the disable for the follow me mode
	}
	
	public boolean isEnabled() {
		//TODO return true when follow me is enabled
		return false;
	}

}
