package com.MAVLink;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPacket;
import com.droidplanner.helpers.FileManager;
import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;

public abstract class MAVLink {
	private String serverIP;
	private int serverPort;
	private boolean logEnabled;

	public static final int TCP = 0;
	public static final int USB = 1;
	
	int connectionType = USB;
	
	boolean connected = false;
	private BufferedInputStream mavIn;
	public BufferedOutputStream logWriter;
	BufferedOutputStream mavOut;
	public int receivedCount = 0;
	private D2xxManager ftD2xx;
	private Context parentContext;
	private FT_Device ftDev;

	public abstract void onReceiveMessage(MAVLinkMessage msg);

	public abstract void onDisconnect();

	public abstract void onConnect();

	public class connectTask extends AsyncTask<String, MAVLinkMessage, String> {

		public Parser parser;
		Socket socket = null;
		private int iavailable;
		private byte[] readData = new byte[4096];
		int i;

		@Override
		protected String doInBackground(String... message) {
			parser = new Parser();
			try {
				if (logEnabled) {
					logWriter = FileManager.getTLogFileStream();
				}

				switch (connectionType) {
								default:
								case TCP:
									getTCPStream();
									break;
								case USB:
									getUSBDriver();
									break;
								}

				MAVLinkMessage m;

				while (connected) {

					switch (connectionType) {
					default:
					case TCP:
						break;
					case USB:
						iavailable = ftDev.getQueueStatus();

						if (iavailable > 0) {
							if (iavailable > 4096)
								iavailable = 4096;
							ftDev.read(readData, iavailable);
						}
						break;
					}

					if (logEnabled) {
						logWriter.write(readData, 0, iavailable);
					}

					for (i = 0; i < iavailable; i++) {
						m = parser.mavlink_parse_char(readData[i] & 0x00ff);
						if (m != null) {
							receivedCount++;
							publishProgress(m);
						}
					}
				}
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (socket != null) {
						socket.close();
					}
					if (logEnabled) {
						logWriter.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return null;
		}

		private void getUSBDriver() {
			
			int DevCount = ftD2xx.createDeviceInfoList(parentContext);
	    	if (DevCount < 1) {
	    		Log.d("USB", "No Devices");
	    		return;
	    	}			
	    	Log.d("USB", "Nº dev:"+DevCount);
	    
	    	ftDev = ftD2xx.openByIndex(parentContext, 0);
	    	
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

		private void getTCPStream() throws UnknownHostException, IOException {
			InetAddress serverAddr = InetAddress.getByName(serverIP);
			socket = new Socket(serverAddr, serverPort);
			mavOut = new BufferedOutputStream((socket.getOutputStream()));
			Log.d("TCP Client", "TCP connection started at: " + serverIP + ":"
					+ serverPort);
			// receive the message which the server sends back
			mavIn = new BufferedInputStream(socket.getInputStream());
		}
		
		

		@Override
		protected void onProgressUpdate(MAVLinkMessage... values) {
			super.onProgressUpdate(values);
			onReceiveMessage(values[0]);
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			Log.d("TCP IN", "Disconected");
			closeConnection();
		}
	}

	/**
	 * Format and send a Mavlink packet via the MAVlink stream
	 * 
	 * @param packet
	 *            MavLink packet to be transmitted
	 */
	public void sendMavPacket(MAVLinkPacket packet) {
		byte[] buffer = packet.encodePacket();
		sendBuffer(buffer);
	}

	/**
	 * Sends a buffer thought the MAVlink stream
	 * 
	 * @param buffer
	 *            Buffer with the data to be transmitted
	 */
	public void sendBuffer(byte[] buffer) {
		switch (connectionType) {
		case USB:
			if(ftDev.isOpen()){
				ftDev.write(buffer);
			}
			break;
		case TCP:			
			if (mavOut != null) {
				try {
					mavOut.write(buffer);
					mavOut.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			break;
		}
	}

	/*
	 * Close the MAVlink Connection
	 */
	public void closeConnection() {
		Log.d("TCP IN", "closing TCP");
		connected = false;
		onDisconnect();
	}

	/**
	 * Start the MAVlink Connection
	 * 
	 * @param port
	 * @param serverIP
	 * @param logEnabled
	 */
	public void openConnection(String serverIP, int port, boolean logEnabled) {
		Log.d("TCP IN", "starting TCP");
		connected = true;
		this.serverIP = serverIP;
		this.serverPort = port;
		this.logEnabled = logEnabled;
		new connectTask().execute("");
		onConnect();
	}
	
	
	public void openConnection(D2xxManager  ftD2xx, boolean logEnabled,Context context) {
		Log.d("USB", "starting USB conection");
		connected = true;
		this.ftD2xx = ftD2xx;
		this.logEnabled = logEnabled;
		parentContext = context;
		new connectTask().execute("");
		onConnect();
	}

	/**
	 * State of the MAVlink Connection
	 * 
	 * @return true for connected
	 */
	public boolean isConnected() {
		return connected;
	}
	
	
}
