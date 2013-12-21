package com.droidplanner.calibration;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.droidplanner.drone.Drone;
import com.droidplanner.parameters.Parameter;

public class CalParameters{
	private Drone myDrone;
	protected List<String>calParameterNames = new ArrayList<String>();
	protected List<Parameter>calParameterItems = new ArrayList<Parameter>();
	
	public CalParameters(Drone myDrone) {
		this.myDrone = myDrone;
		
	}

	public void processReceivedParam() {
		if(myDrone==null)
			return;
		Parameter param = myDrone.parameters.getLastParameter();
		Log.d("CAL", param.name +": " + String.valueOf(param.value));
		calParameterItems.add(param);
		readCaliberationParameter(calParameterItems.size());
	}

	public void getCaliberationParameters(Drone drone){
		this.myDrone = drone;
		calParameterItems.clear();
		readCaliberationParameter(0);
	}
	
	private void readCaliberationParameter(int seq){
		if(seq>=calParameterNames.size())
			return;
		if(myDrone!=null)
			myDrone.parameters.ReadParameter(calParameterNames.get(seq));
	}
}
