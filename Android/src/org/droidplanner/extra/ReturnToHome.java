package org.droidplanner.extra;


import org.droidplanner.drone.variables.missionD.MissionItemD;
import org.droidplanner.drone.variables.missionD.commands.ReturnToHomeD;
import org.droidplanner.fragments.mission.MissionDetailFragment;
import org.droidplanner.fragments.mission.MissionRTLFragment;
import org.droidplanner.helpers.units.Altitude;


public class ReturnToHome extends ReturnToHomeD{
	@Override
	public MissionDetailFragment getDetailFragment() {
		MissionDetailFragment fragment = new MissionRTLFragment();
		fragment.setItem(this);
		return fragment;
	}
}
