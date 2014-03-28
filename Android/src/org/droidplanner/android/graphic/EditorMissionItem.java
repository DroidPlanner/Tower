package org.droidplanner.android.graphic;

import org.droidplanner.android.fragments.mission.MissionDetailFragment;

public interface EditorMissionItem {

	/**
	 * Return a new detail Fragment for this MissionItem
	 * 
	 * @return
	 */
	public abstract MissionDetailFragment getDetailFragment();

}