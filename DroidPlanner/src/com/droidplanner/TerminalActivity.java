package com.droidplanner;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;

public class TerminalActivity extends SuperActivity implements OnClickListener {

	public static D2xxManager ftD2xx = null;
	
	TextView terminal;
	Button sendButton;
	Menu menu;
	MenuItem connectButton;
	FT_Device ftDev;
	
	@Override
	int getNavigationItem() {
		return 4;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.terminal);

		terminal = (TextView) findViewById(R.id.textViewTerminal);
		sendButton = (Button) findViewById(R.id.buttonSend);
		sendButton.setOnClickListener(this);
		    	
	}
	
	@Override
	protected void onResume() {
		super.onRestart();
		//TODO reimplement MAVClient.init();
	}



	@Override
	protected void onStop() {
		super.onDestroy();
		//TODO reimplement MAVClient.onDestroy();
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_terminal, menu);
		this.menu = menu;
		connectButton = menu.findItem(R.id.menu_connect);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_connect:
			//TODO reimplement MAVClient.sendConnectMessage();
			return true;
		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}
	
	//TODO reimplement 


	/*
	public MAVLinkClient MAVClient = new MAVLinkClient(this) {
	
		
		@Override
		public void notifyReceivedData(MAVLinkMessage m) {
		
		}
	
		@Override
		public void notifyDisconnected() {
			connectButton.setTitle(getResources().getString(
					R.string.menu_connect));
		}
	
		@Override
		public void notifyConnected() {
			connectButton.setTitle(getResources().getString(
					R.string.menu_disconnect));
		}
	};
	
	*/
	
	

	@Override
	public void onClick(View v) {	
	}

}
