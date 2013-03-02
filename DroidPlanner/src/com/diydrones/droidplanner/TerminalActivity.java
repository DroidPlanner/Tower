package com.diydrones.droidplanner;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.ardupilotmega.msg_param_request_list;
import com.MAVLink.Messages.ardupilotmega.msg_param_value;
import com.MAVLink.Messages.ardupilotmega.msg_statustext;
import com.diydrones.droidplanner.service.MAVLinkClient;

public class TerminalActivity extends Activity {

	TextView terminal;
	Button sendButton;
	Menu menu;
	MenuItem connectButton;
	
	public MAVLinkClient MAVClient = new MAVLinkClient(this) {
		
		String additionalInfo="";
		@Override
		public void notifyReceivedData(MAVLinkMessage m) {
				String terminalMsg = "Received lenght packets\nLast packet was: " + m.msgid + "\n";
				if(m.msgid == msg_statustext.MAVLINK_MSG_ID_STATUSTEXT){
					additionalInfo+= ((msg_statustext) m).toString()+"\n";
				}
				if(m.msgid == msg_param_value.MAVLINK_MSG_ID_PARAM_VALUE){
					Log.d("PARAM",("param:"+((msg_param_value) m).getParam_Id()+"\t Value"+((msg_param_value) m).param_value));
				}
				
				terminal.setText(terminalMsg+additionalInfo);
		}
		
		@Override
		public void notifyDisconnected() {
			connectButton.setTitle(getResources().getString(R.string.menu_connect));
		}
			
		@Override
		public void notifyConnected() {
			connectButton.setTitle(getResources().getString(R.string.menu_disconnect));
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		setContentView(R.layout.terminal);
	
		terminal = (TextView) findViewById(R.id.textViewTerminal);
		sendButton = (Button) findViewById(R.id.buttonSend);
		
		MAVClient.init();
		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		MAVClient.onDestroy();
	}
	
	public void sendData(View view) {
		Log.d("PARAM", "request List");
		msg_param_request_list msg = new msg_param_request_list();
		msg.target_system = 1;
		msg.target_component = 1;
		MAVClient.sendMavPacket(msg.pack());
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
		case R.id.menu_settings:
			startActivity(new Intent(this,SettingsActivity.class));
			return true;
		case R.id.menu_connect:
			MAVClient.sendConnectMessage();			
			return true;
		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}

	@Override
	int getNavigationItem() {
		return 4;
	}
	
}
