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
	private boolean isUpdating = false;
	
	public CalParameters(Drone myDrone) {
		this.myDrone = myDrone;
		
	}

	public void processReceivedParam() {
		if(myDrone==null){
			return;
		}
		Parameter param = myDrone.parameters.getLastParameter();
		Log.d("CAL", param.name +": " + String.valueOf(param.value));
		if(isUpdating){
			compareCalibrationParameter(param);
		}
		else{
			calParameterItems.add(param);
			readCalibrationParameter(calParameterItems.size());
		}
	}

	private void compareCalibrationParameter(Parameter param) {
		Parameter paramRef = calParameterItems.get(calParameterItems.size()-1);
		
		if(paramRef.name.equalsIgnoreCase(param.name) &&
				paramRef.value==param.value){
			Log.d("CAL", "Comp: " + paramRef.name +" : " + param.name);
			Log.d("CAL", "Comp: " + String.valueOf(paramRef.value) +" : " + String.valueOf(param.value));
			calParameterItems.remove(calParameterItems.size()-1);
		}
		sendCalibrationParameters();		
	}

	public void getCalibrationParameters(Drone drone){
		this.myDrone = drone;
		calParameterItems.clear();
		readCalibrationParameter(0);
	}
	
	private void readCalibrationParameter(int seq){
		if(seq>=calParameterNames.size())
			return;
		if(myDrone!=null)
			myDrone.parameters.ReadParameter(calParameterNames.get(seq));
	}
	public void sendCalibrationParameters(){
		isUpdating = true;
		if(calParameterItems.size()>0 && myDrone!=null){
			myDrone.parameters.sendParameter(calParameterItems.get(calParameterItems.size()-1));
		}
		else {
			isUpdating = false;
		}
	}
}
