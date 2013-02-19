package com.MAVLink;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPacket;

public abstract class MAVLink {
	private String serverIP;
	private int serverPort;
	private boolean logEnabled;

	boolean connected = false;
	private BufferedInputStream mavIn;
	public BufferedOutputStream logWriter;
	BufferedOutputStream mavOut;
	public int receivedCount = 0;

	public abstract void onReceiveMessage(MAVLinkMessage msg);

	public abstract void onDisconnect();

	public abstract void onConnect();

	public class connectTask extends AsyncTask<String, MAVLinkMessage, String> {

		public Parser parser;
		Socket socket = null;

		@Override
		protected String doInBackground(String... message) {
			parser = new Parser();
			try {
				if (logEnabled) {					
					logWriter = getFileStream();
				}
				getTCPStream();
				
				MAVLinkMessage m;

				while (connected) {
					int data;
					if ((data = mavIn.read()) >= 0) {
						if (logEnabled) {
							logWriter.write(data);
						}							
						m = parser.mavlink_parse_char(data);
						if (m != null) {
							receivedCount++;
							publishProgress(m);
						}
					}
				}
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e){
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally{
				try {
					socket.close();
					logWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return null;
		}



		private void getTCPStream() throws UnknownHostException, IOException {
			InetAddress serverAddr = InetAddress.getByName(serverIP);
			socket = new Socket(serverAddr, serverPort);
			mavOut = new BufferedOutputStream((socket.getOutputStream()));
			Log.d("TCP Client", "TCP connection started at: "+serverIP+":"+serverPort);
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
	 * @param packet MavLink packet to be transmitted
	 */
	public void sendMavPacket(MAVLinkPacket packet) {
		byte[] buffer = packet.encodePacket();
		sendBuffer(buffer);
	}

	/**
	 * Sends a buffer thought the MAVlink stream
	 * @param buffer Buffer with the data to be transmitted
	 */
	public void sendBuffer(byte[] buffer) {
		if (mavOut != null) {
			try {
				mavOut.write(buffer);
				mavOut.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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

	/**
	 * State of the MAVlink Connection
	 * 
	 * @return true for connected
	 */
	public boolean isConnected() {
		return connected;
	}

	/**
	 * Timestamp for logs in the Mission Planner Format
	 */
	private String getTimeStamp() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss",
				Locale.US);
		String timeStamp = sdf.format(new Date());
		return timeStamp;
	}

	/**
	 * Get a file Stream for logging purposes
	 * 
	 * @return output file stream for the log file
	 */
	private BufferedOutputStream getFileStream() throws FileNotFoundException {
		String root = Environment.getExternalStorageDirectory().toString();
		File myDir = new File(root + "/DroidPlanner/logs");
		myDir.mkdirs();
		File file = new File(myDir, getTimeStamp() + ".tlog");
		if (file.exists())
			file.delete();
		BufferedOutputStream out =new BufferedOutputStream(new FileOutputStream(file));
		return out;
	}

}
