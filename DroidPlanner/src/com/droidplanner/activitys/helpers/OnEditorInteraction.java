package com.droidplanner.activitys.helpers;

import com.droidplanner.drone.variables.mission.MissionItem;

public interface OnEditorInteraction {
		public void onItemLongClick(MissionItem item);
		public void onItemClick(MissionItem item);
}