package com.droidplanner.connection;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class UdpConnection extends MAVLinkConnection {
	private DatagramSocket socket;
	// private BufferedOutputStream mavOut;
	// private BufferedInputStream mavIn;

	private String serverIP = "192.168.40.255";
	private int serverPort = 14550;

	private InetAddress serverAddr;

	public UdpConnection(Context context) {
		super(context);
	}

	@Override
	protected void openConnection() throws UnknownHostException, IOException {
		getUdpStream();
	}

	@Override
	protected void readDataBlock() throws IOException {
		DatagramPacket packet = new DatagramPacket(readData, readData.length);
		socket.receive(packet);
		iavailable = packet.getLength();
	}

	@Override
	protected void sendBuffer(byte[] buffer) throws IOException {
		
		try{
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length,
					InetAddress.getByName(serverIP), 14553);
			if (socket != null) {
				socket.send(packet);

			}
		}catch (Exception e){
			e.printStackTrace();	// TODO fix the NetworkOnMainThreadException

		}
			/*
			 * if (mavOut != null) { mavOut.write(buffer); mavOut.flush(); }
			 */
	}

	@Override
	protected void closeConnection() throws IOException {
		socket.close();
	}

	@Override
	protected void getPreferences(SharedPreferences prefs) {
		// serverIP = prefs.getString("pref_server_ip", "");
		// serverPort = Integer.parseInt(prefs.getString("pref_server_port",
		// "0"));
	}

	private void getUdpStream() throws UnknownHostException, IOException {
		serverAddr = InetAddress.getByName(serverIP);
		socket = new DatagramSocket(serverPort);
		socket.setBroadcast(true);
		socket.setReuseAddress(true);
		Log.d("UDP", "Socket Open");
	}

}
