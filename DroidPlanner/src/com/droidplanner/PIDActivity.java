package com.droidplanner;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.ardupilotmega.msg_rc_channels_override;
import com.droidplanner.service.MAVLinkClient;

public class PIDActivity extends SuperActivity implements OnSeekBarChangeListener, OnClickListener {
	private ScheduledExecutorService scheduleTaskExecutor;

	private SeekBar ch1SeekBar;
	private TextView ch1TextView;
	private SeekBar ch2SeekBar;
	private TextView ch2TextView;

	
	private Button bStart;
	private Button bStop;
	
	MenuItem connectButton;
		
	private RcOutput rcTask;
	
	@Override
	int getNavigationItem() {
		return 3;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.pid);
		
		ch1TextView =(TextView) findViewById(R.id.ch1TextView);
		ch1SeekBar = (SeekBar) findViewById(R.id.ch1SeekBar);
		ch1SeekBar.setOnSeekBarChangeListener(this);
		
		ch2TextView =(TextView) findViewById(R.id.ch2TextView);
		ch2SeekBar = (SeekBar) findViewById(R.id.ch2SeekBar);
		ch2SeekBar.setOnSeekBarChangeListener(this);
		
		bStart = (Button) findViewById(R.id.bStart);
		bStart.setOnClickListener(this);
		bStop = (Button) findViewById(R.id.bstop);
		bStop.setOnClickListener(this);
		
		MAVClient.init();
				
		rcTask = new RcOutput(MAVClient);
		scheduleTaskExecutor = Executors.newScheduledThreadPool(5);
		scheduleTaskExecutor.scheduleWithFixedDelay( rcTask ,1000, 20, TimeUnit.MILLISECONDS);

	}

	@Override
	protected void onStop() {
		super.onDestroy();
		MAVClient.onDestroy();
	}


	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_pid, menu);
		connectButton = menu.findItem(R.id.menu_connect);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:
			return true;
		case R.id.menu_connect:
			MAVClient.sendConnectMessage();
			return true;
		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}

	private MAVLinkClient MAVClient = new MAVLinkClient(this) {
		
		@Override
		public void notifyReceivedData(MAVLinkMessage m) {
			
		}
		
		@Override
		public void notifyConnected() {
			connectButton.setTitle(getResources().getString(
					R.string.menu_disconnect));
		}

		@Override
		public void notifyDisconnected() {
			connectButton.setTitle(getResources().getString(
					R.string.menu_connect));
		}
	};
	
	
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if(seekBar == ch1SeekBar){
			rcTask.rcOutputs[0] = progress+1000;
			ch1TextView.setText(Integer.toString(rcTask.rcOutputs[0]));
		}else if (seekBar == ch2SeekBar) {
			rcTask.rcOutputs[1] = progress+1000;
			ch2TextView.setText(Integer.toString(rcTask.rcOutputs[1]));			
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		
	}
	
	@Override
	public void onClick(View v) {
		if(v == bStart){
		}else if (v == bStop) {
			//scheduleTaskExecutor.shutdownNow();
			//disableOverride(MAVClient);	
		}
		
	}

	class RcOutput implements Runnable{
		private MAVLinkClient MAV;
		
		private static final int DISABLE_OVERRIDE = 0;
		private static final int RC_TRIM = 1500;
		public int [] rcOutputs = new int[8];

		public RcOutput(MAVLinkClient mAVClient) {
			MAV = mAVClient;
			Arrays.fill(rcOutputs, RC_TRIM);
		}

		@Override
		public void run() {
			sendRcOverrideMsg();
		}
		
		public void disableOverride(MAVLinkClient mAVClient) {
			Arrays.fill(rcOutputs, DISABLE_OVERRIDE);	
			sendRcOverrideMsg(); 	// Just to be sure send 3 disable
			sendRcOverrideMsg();
			sendRcOverrideMsg();		
		}
		
		public void sendRcOverrideMsg(){
			msg_rc_channels_override msg = new msg_rc_channels_override();
			msg.chan1_raw = (short) rcOutputs[0];
			msg.chan2_raw = (short) rcOutputs[1];
			msg.chan3_raw = (short) rcOutputs[2];
			msg.chan4_raw = (short) rcOutputs[3];
			msg.chan5_raw = (short) rcOutputs[4];
			msg.chan6_raw = (short) rcOutputs[5];
			msg.chan7_raw = (short) rcOutputs[6];
			msg.chan8_raw = (short) rcOutputs[7];
			msg.target_system = 1;
			msg.target_component = 1;
			Log.d("RC", "send");
			MAV.sendMavPacket(msg.pack());	
		}
		

	}
	
	

}