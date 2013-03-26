package com.droidplanner;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.MAVLink.Messages.MAVLinkMessage;
import com.droidplanner.helpers.RcOutput;
import com.droidplanner.service.MAVLinkClient;

public class RCActivity extends SuperActivity implements OnSeekBarChangeListener, OnClickListener {

	private SeekBar ch1SeekBar;
	private TextView ch1TextView;
	private SeekBar ch2SeekBar;
	private TextView ch2TextView;

	
	private Button bStart;
	private Button bStop;
	
	MenuItem connectButton;
		
	private RcOutput rcOutput;
	
	@Override
	int getNavigationItem() {
		return 3;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.rc);
		
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
		
		rcOutput = new RcOutput(MAVClient);
				
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
			rcOutput.rcOutputs[0] = progress+1000;
			ch1TextView.setText(Integer.toString(rcOutput.rcOutputs[0]));
		}else if (seekBar == ch2SeekBar) {
			rcOutput.rcOutputs[1] = progress+1000;
			ch2TextView.setText(Integer.toString(rcOutput.rcOutputs[1]));			
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
			rcOutput.enableRcOverride();
		}else if (v == bStop) {
			rcOutput.disableRcOverride();
		}
		
	}

}