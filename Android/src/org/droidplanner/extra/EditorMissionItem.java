package org.droidplanner.extra;

import org.droidplanner.fragments.mission.MissionDetailFragment;

public interface EditorMissionItem {

	/**
	 * Return a new detail Fragment for this MissionItem
	 * @return
	 */
	public abstract MissionDetailFragment getDetailFragment();

}