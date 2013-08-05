package com.droidplanner.connection;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;

public class UdpConnection extends MAVLinkConnection {

	private DatagramSocket socket;
	private int serverPort;

	private int hostPort;
	private InetAddress hostAdd;

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
		hostAdd=packet.getAddress();
		hostPort = packet.getPort();
		iavailable = packet.getLength();
	}

	@Override
	protected void sendBuffer(byte[] buffer) throws IOException {
		new UdpSender().execute(buffer);
	}
	
	private class UdpSender extends AsyncTask<byte[], Integer, Integer> {

		@Override
		protected Integer doInBackground(byte[]... params) {
			try{			
				byte[] buffer = params[0];
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length,
						hostAdd, hostPort);
				socket.send(packet);
				
			}catch (Exception e){
				e.printStackTrace();				
			}
			return null;			
		}
	}
	

	@Override
	protected void closeConnection() throws IOException {
		socket.close();
	}

	@Override
	protected void getPreferences(SharedPreferences prefs) {
		serverPort = Integer.parseInt(prefs.getString("pref_udp_server_port",
		"14550"));
	}

	private void getUdpStream() throws UnknownHostException, IOException {
		socket = new DatagramSocket(serverPort);
		socket.setBroadcast(true);
		socket.setReuseAddress(true);
	}
	
	@SuppressWarnings("unused")
	private InetAddress getBroadcastAddress() throws IOException {
	    WifiManager wifi = (WifiManager) parentContext.getSystemService(Context.WIFI_SERVICE);
	    DhcpInfo dhcp = wifi.getDhcpInfo();
	    // handle null somehow

	    int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
	    byte[] quads = new byte[4];
	    for (int k = 0; k < 4; k++)
	      quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
	    return InetAddress.getByAddress(quads);
	}


}
