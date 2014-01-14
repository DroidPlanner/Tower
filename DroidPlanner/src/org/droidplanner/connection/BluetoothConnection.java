package org.droidplanner.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.UnknownHostException;
import java.util.Set;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.ParcelUuid;
import android.util.Log;

public class BluetoothConnection extends MAVLinkConnection {
	private static final String BLUE = "BLUETOOTH";
	private static final String UUID_SPP_DEVICE = "00001101-0000-1000-8000-00805F9B34FB";
	private BluetoothAdapter mBluetoothAdapter;
	private OutputStream out;
	private InputStream in;
	private BluetoothSocket bluetoothSocket;

	public BluetoothConnection(Context parentContext) {
		super(parentContext);
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			Log.d(BLUE, "Null adapters");
		}
	}

	@Override
	protected void openConnection() throws UnknownHostException, IOException {
		
		if (!mBluetoothAdapter.isEnabled())
		{
			Log.d(BLUE, "BT Adapter disabled...");
			return;
		}
		
		resetConnection();
		
		//mBluetoothAdapter.disable();
		//mBluetoothAdapter.enable();
		

		Log.d(BLUE, "Looking for BT devs ...");
		BluetoothDevice device = findBluetoothDevice();
		
		Log.d(BLUE, "BT Create Socket Call...");		
/*
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) { 
				bluetoothSocket = device.createRfcommSocketToServiceRecord(UUID.fromString(UUID_SPP_DEVICE));
		} else {			
				Method BTSocketMethod = null;
			try {
				BTSocketMethod = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[] { UUID.class });
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				bluetoothSocket = (BluetoothSocket) BTSocketMethod.invoke(device, (UUID) UUID.fromString(UUID_SPP_DEVICE));
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

*/
		
		bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(UUID.fromString(UUID_SPP_DEVICE));
	
		Log.d(BLUE, "BT Cancel Discovery Call...");		
		mBluetoothAdapter.cancelDiscovery();		
		
		
		Log.d(BLUE, "BT Connect Call...");				
		bluetoothSocket.connect(); //Here the IOException will rise on BT protocol/handshake error.

		Log.d(BLUE, "## BT Connected ##");			

		out = bluetoothSocket.getOutputStream();
		in = bluetoothSocket.getInputStream();
	}

	@SuppressLint("NewApi")
	private BluetoothDevice findBluetoothDevice() throws UnknownHostException {
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
				.getBondedDevices();
		// If there are paired devices
		if (pairedDevices.size() > 0) {
			// Loop through paired devices
			for (BluetoothDevice device : pairedDevices) {
				// Add the name and address to an array adapter to show in a
				// ListView
				Log.d(BLUE, device.getName() + " #" + device.getAddress() + "#");
				for (ParcelUuid id : device.getUuids()) {
					// TODO maybe this will not work on newer devices
					Log.d(BLUE, "id:" + id.toString());
					if (id.toString().equalsIgnoreCase(UUID_SPP_DEVICE)) {
						Log.d(BLUE, ">> Selected: " + device.getName()
								+ " Using: " + id.toString());
						return device;
					}
				}
			}
		}
		throw new UnknownHostException("No Bluetooth Device found");
	}

	@Override
	protected void readDataBlock() throws IOException {
		iavailable = in.read(readData);

	}

	@Override
	protected void sendBuffer(byte[] buffer) throws IOException {
		if (out != null) {
			out.write(buffer);
		}
	}

	@Override
	protected void closeConnection() throws IOException {
		resetConnection();
		Log.d(BLUE, "## BT Closed ##");
	}
	
	
	private void resetConnection() throws IOException {
        if (in != null) {
                in.close(); 
                in = null;
        }

        if (out != null) {
                out.close();
                out = null;
        }

        if (bluetoothSocket != null) {
                bluetoothSocket.close();
                bluetoothSocket = null;
        }

}	
	

	@Override
	protected void getPreferences(SharedPreferences prefs) {
		// TODO Auto-generated method stub
	}

	/*
	 * private getUUID(device: BluetoothDevice) = { val uuids =
	 * Option(device.getUuids).getOrElse(Array()).map(_.getUuid) uuids.find {
	 * uuid => uuid.toString.startsWith(serialUUIDprefix) } }
	 */

}
