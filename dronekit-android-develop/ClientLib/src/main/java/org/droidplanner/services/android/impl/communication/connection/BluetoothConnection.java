package org.droidplanner.services.android.impl.communication.connection;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;

import org.droidplanner.services.android.impl.core.MAVLink.connection.MavLinkConnectionTypes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.util.Set;
import java.util.UUID;

public class BluetoothConnection extends AndroidMavLinkConnection {
    private static final String BLUE = "BLUETOOTH";
    private static final String UUID_SPP_DEVICE = "00001101-0000-1000-8000-00805F9B34FB";
    private BluetoothAdapter mBluetoothAdapter;
    private OutputStream out;
    private InputStream in;
    private BluetoothSocket bluetoothSocket;

    private final String bluetoothAddress;

    public BluetoothConnection(Context parentContext, String btAddress) {
        super(parentContext);
        this.bluetoothAddress = btAddress;

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.d(BLUE, "Null adapters");
        }
    }

    @Override
    protected void openConnection(Bundle connectionExtras) throws IOException {
        Log.d(BLUE, "Connect");

        // Reset the bluetooth connection
        resetConnection();

        // Retrieve the stored device
        BluetoothDevice device = null;
        try {
            device = mBluetoothAdapter.getRemoteDevice(bluetoothAddress);
        } catch (IllegalArgumentException ex) {
            // invalid configuration (device may have been removed)
            // NOP fall through to 'no device'
        }

        // no device
        if (device == null) {
            device = findSerialBluetoothBoard();
        }

        Log.d(BLUE, "Trying to connect to device with address " + device.getAddress());
        Log.d(BLUE, "BT Create Socket Call...");
        bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(UUID
            .fromString(UUID_SPP_DEVICE));

        Log.d(BLUE, "BT Cancel Discovery Call...");
        mBluetoothAdapter.cancelDiscovery();

        Log.d(BLUE, "BT Connect Call...");
        bluetoothSocket.connect(); // Here the IOException will rise on BT
        // protocol/handshake error.

        Log.d(BLUE, "## BT Connected ##");
        out = bluetoothSocket.getOutputStream();
        in = bluetoothSocket.getInputStream();

        onConnectionOpened(connectionExtras);
    }

    @SuppressLint("NewApi")
    private BluetoothDevice findSerialBluetoothBoard() throws UnknownHostException {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                Log.d(BLUE, device.getName() + " #" + device.getAddress() + "#");

                final ParcelUuid[] deviceUuids = device.getUuids();
                if (deviceUuids != null && deviceUuids.length > 0) {
                    for (ParcelUuid id : device.getUuids()) {
                        Log.d(BLUE, "id:" + id.toString());
                        if (id.toString().equalsIgnoreCase(UUID_SPP_DEVICE)) {
                            Log.d(BLUE,
                                ">> Selected: " + device.getName() + " Using: " + id.toString());
                            return device;
                        }
                    }
                }
            }
        }

        throw new UnknownHostException("No Bluetooth Device found");
    }

    @Override
    protected int readDataBlock(byte[] buffer) throws IOException {
        return in.read(buffer);

    }

    @Override
    protected void sendBuffer(byte[] buffer) throws IOException {
        if (out != null) {
            out.write(buffer);
        }
    }

    @Override
    public int getConnectionType() {
        return MavLinkConnectionTypes.MAVLINK_CONNECTION_BLUETOOTH;
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
    protected void loadPreferences() {
    }
}
