package com.droidplanner.drone.variables.mission.commands;

import com.droidplanner.dialogs.mission.DialogMission;
import com.droidplanner.dialogs.mission.DialogMissionSetJump;

public class DoJump extends MissionCMD{
	private int JumpTo;
	private int repeat;
		
	@Override
	public DialogMission getDialog() {
		return new DialogMissionSetJump();
	}
}
