package com.droidplanner.activitys;

import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.droidplanner.R;
import com.droidplanner.activitys.helpers.SuperActivity;
import com.droidplanner.drone.DroneInterfaces;
import com.droidplanner.parameters.Parameter;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText;

public class PidActivity extends SuperActivity implements
		DroneInterfaces.OnParameterManagerListner,
		DialogInterface.OnClickListener {

	private SeekBarWithText rollPSeekBar;
	private SeekBarWithText rollDSeekBar;
	private SeekBarWithText yawPSeekBar;
	private SeekBarWithText thrAclSeekBar;
	private SeekBarWithText thrMidSeekBar;

	private Parameter rollP;
	private Parameter rollD;
	private Parameter yawP;
	private Parameter thrAcl;
	private Parameter thrMid;

	private DroneInterfaces.OnParameterManagerListner parameterListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pid);
		findLocalViews();
		setupLocalViews();
	}

	private void setupLocalViews() {
		rollPSeekBar = (SeekBarWithText) findViewById(R.id.SeekBarRollPitchControl);
		rollDSeekBar = (SeekBarWithText) findViewById(R.id.SeekBarRollPitchDampenning);
		yawPSeekBar = (SeekBarWithText) findViewById(R.id.SeekBarYawControl);
		thrAclSeekBar = (SeekBarWithText) findViewById(R.id.SeekBarThrottleAccel);
		thrMidSeekBar = (SeekBarWithText) findViewById(R.id.seekBarThrottleHover);
	}

	private void findLocalViews() {
		// TODO Auto-generated method stub

	}

	private void refreshPIDValues() {
		if (drone.MavClient.isConnected()) {
			Toast.makeText(this, "Retreiving PID Values", Toast.LENGTH_SHORT)
					.show();
			drone.parameters.getAllParameters();
		} else {
			Toast.makeText(this, "Please connect first", Toast.LENGTH_SHORT)
					.show();
		}
		// drone.parameters.ReadParameter("RATE_RLL_P");
		// drone.parameters.ReadParameter("RATE_RLL_D");
		// drone.parameters.ReadParameter("RATE_YAW_P");
		// drone.parameters.ReadParameter("THR_ACCEL_P");
		// drone.parameters.ReadParameter("THR_MID");
	}

	private void updatePIDValues() {
		if(rollP==null || rollD==null || yawP==null || thrAcl==null || thrMid==null)
			return;
		
		//This is to check if data has changed - but it does not work here - Need to find out why
		if(rollP.value!=rollPSeekBar.getValue() ||
			rollD.value!=rollDSeekBar.getValue()||
			yawP.value!=yawPSeekBar.getValue()||
			thrAcl.value!=thrAclSeekBar.getValue()||
			thrMid.value!=thrMidSeekBar.getValue()){
	        
			AlertDialog.Builder builder = new AlertDialog.Builder(this);

	        builder.setMessage(
	                "Update changes to vehicle?")
	                .setCancelable(false)
	                .setPositiveButton("Ok",this)
	                .setNegativeButton("Cancel",this);
	        AlertDialog alert = builder.create();
	        alert.show();
		}
    }

	private void doUpdatePIDValues() {
		rollP.value=rollPSeekBar.getValue();
		rollD.value=rollDSeekBar.getValue();
		yawP.value=yawPSeekBar.getValue();
		thrAcl.value=thrAclSeekBar.getValue();
		thrMid.value=thrMidSeekBar.getValue();

		drone.parameters.sendParameter(rollP);
		drone.parameters.sendParameter(rollD);
		drone.parameters.sendParameter(yawP);
		drone.parameters.sendParameter(thrAcl);
		drone.parameters.sendParameter(thrMid);
		Toast.makeText(this, "PID values updated", Toast.LENGTH_SHORT).show();
	}

	public boolean onParameterReceived(Parameter parameter) {
		if (parameter.name.equals("RATE_RLL_P")) {
			Toast.makeText(this, "Rate Roll/Pitch control received",
					Toast.LENGTH_SHORT).show();
			rollP = parameter;
			rollPSeekBar.setValue(rollP.value);
		}
		if (parameter.name.equals("RATE_RLL_D")) {
			Toast.makeText(this, "Rate Roll/Pitch dampening received",
					Toast.LENGTH_SHORT).show();
			rollD = parameter;
			rollDSeekBar.setValue(rollD.value);
		}
		if (parameter.name.equals("RATE_YAW_P")) {
			Toast.makeText(this, "Rate Yaw control received",
					Toast.LENGTH_SHORT).show();
			yawP = parameter;
			yawPSeekBar.setValue(yawP.value);
		}
		if (parameter.name.equals("THR_ACCEL_P")) {
			Toast.makeText(this, "Rate Throttle accelration received",
					Toast.LENGTH_SHORT).show();
			thrAcl = parameter;
			thrAclSeekBar.setValue(thrAcl.value);
		}
		if (parameter.name.equals("THR_MID")) {
			Toast.makeText(this, "Throttle hover received", Toast.LENGTH_SHORT)
					.show();
			thrMid = parameter;
			thrMidSeekBar.setValue(thrMid.value);
		}
		return true;
	}

	@Override
	public void onResume() {
		parameterListener = drone.parameters.parameterListner;
		drone.parameters.parameterListner = this;
		super.onResume();

		refreshPIDValues();
	}

	@Override
	public void onPause() {
		drone.parameters.parameterListner = parameterListener;
		super.onPause();
	}

	@Override
	public void onClick(DialogInterface arg0, int arg1) {
		switch (arg1) {
		case -1:
			doUpdatePIDValues();
			break;
		case -2:
			break;
		}

	}

	@Override
	public void onBeginReceivingParameters() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onParameterReceived(Parameter parameter, int index, int count) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onEndReceivingParameters(List<Parameter> parameter) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onParamterMetaDataChanged() {
		// TODO Auto-generated method stub
		
	}

}