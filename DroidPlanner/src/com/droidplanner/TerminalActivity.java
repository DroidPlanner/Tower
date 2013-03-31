package com.droidplanner;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.MAVLink.Parser;
import com.MAVLink.Messages.MAVLinkMessage;
import com.droidplanner.service.MAVLinkClient;
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
		
    	try {
    		ftD2xx = D2xxManager.getInstance(this);
    	} catch (D2xxManager.D2xxException ex) {
    		ex.printStackTrace();
    	}
    	
    	openCOM();
    	
    	new read_thread().start();
    			
		MAVClient.init();

	}

	private void openCOM() {
		int DevCount = ftD2xx.createDeviceInfoList(this);
    	if (DevCount < 1) {
    		Log.d("USB", "No Devices");
    		return;
    	}			
    	Log.d("USB", "Nº dev:"+DevCount);
    
    	ftDev = ftD2xx.openByIndex(this, 0);
    	
    	if(ftDev == null){
    		Log.d("USB", "COM close");
    		return;
    	}

    	
		ftDev.setBitMode((byte) 0, D2xxManager.FT_BITMODE_RESET);
		ftDev.setBaudRate(57600);
		ftDev.setDataCharacteristics(D2xxManager.FT_DATA_BITS_8,
				D2xxManager.FT_STOP_BITS_1, D2xxManager.FT_PARITY_NONE);
		ftDev.setFlowControl(D2xxManager.FT_FLOW_NONE, (byte) 0x00, (byte) 0x00);
		ftDev.setLatencyTimer((byte) 16);
		ftDev.purge((byte) (D2xxManager.FT_PURGE_TX | D2xxManager.FT_PURGE_RX));
    	
    				
		if (true == ftDev.isOpen()){
			Log.d("USB", "COM open");
		}
	}

	@Override
	protected void onStop() {
		super.onDestroy();
		MAVClient.onDestroy();
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
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		case R.id.menu_connect:
			MAVClient.sendConnectMessage();
			return true;
		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}


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
	
	
	
    public void SendMessage() {
		if (ftDev.isOpen() == false) {
			Log.e("j2xx", "SendMessage: device not open");
			return;
		}

		ftDev.setLatencyTimer((byte) 16);

		String writeData = "Teste da Serial";
		byte[] OutData = writeData.getBytes();
		ftDev.write(OutData, writeData.length());
    }
	
	private class read_thread  extends Thread	{

		public read_thread(){
			this.setPriority(Thread.MAX_PRIORITY);
		}

		@Override
		public void run()
		{
			ftDev.setLatencyTimer((byte)16);

			int iavailable;
			byte[] readData = new byte[4096];
			Parser parser = new Parser();
			MAVLinkMessage m;
			
			while(true){
				iavailable = ftDev.getQueueStatus();
				
				if(iavailable > 0){	
					if(iavailable > 4096)
						iavailable = 4096;
					ftDev.read(readData,iavailable);	
					Log.d("USB", "read:"+iavailable);
					
					for (int i = 0; i < iavailable; i++) {
						m= parser.mavlink_parse_char(readData[i]&0x00ff);
						if (m != null) {
							Log.d("MSG_"+m.msgid,m.toString() );
						}
					}
					
				}
			}
		}
	}

	@Override
	public void onClick(View v) {
		SendMessage();		
	}

}
