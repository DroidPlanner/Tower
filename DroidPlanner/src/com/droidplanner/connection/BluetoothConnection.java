package com.droidplanner.connection;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.ParcelUuid;
import android.preference.PreferenceManager;
import android.util.Log;
import com.droidplanner.utils.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public class BluetoothConnection extends MAVLinkConnection {
    private static final String BLUE = "BLUETOOTH";
    private static final String UUID_SPP_DEVICE = "00001101-0000-1000-8000-00805F9B34FB";

    /**
     * Keeps the list of valid uuids the client should be looking for.
     *
     * @since 1.2.0
     */
    private final Set<UUID> sValidUuids;

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

        sValidUuids = new LinkedHashSet<UUID>();
        sValidUuids.add(UUID.fromString(UUID_SPP_DEVICE));
        for (UUID uuid : BluetoothServer.UUIDS) {
            sValidUuids.add(uuid);
        }
    }

    @Override
    protected void openConnection() throws IOException {
        Log.d(BLUE, "Connect");

        //Reset the bluetooth connection
        resetConnection();

        //Retrieve the stored address
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences
                (parentContext);
        String address = settings.getString(Constants.PREF_BLUETOOTH_DEVICE_ADDRESS, null);

        BluetoothDevice device = address == null
                ? findSerialBluetoothBoard()
                : mBluetoothAdapter.getRemoteDevice(address);

        Log.d(BLUE, "Trying to connect to device with address #" + device.getAddress());

        for (UUID uuid : sValidUuids) {
            try {
                Log.d(BLUE, "Attempting to connect with uuid #" + uuid.toString());
                bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(uuid);
                mBluetoothAdapter.cancelDiscovery();
                bluetoothSocket.connect();

                Log.d(BLUE, "Successful connection to uuid #" + uuid.toString());
                out = bluetoothSocket.getOutputStream();
                in = bluetoothSocket.getInputStream();

                //Found a good one... break
                Log.d(BLUE, "Exiting the connection process.");
                break;
            } catch (IOException e) {
                //try another uuid
                Log.d(BLUE, "Not able to connect with uuid #" + uuid.toString());
            }
        }

        if (out == null || in == null) {
            Log.d(BLUE, "No successful connection.");
            throw new IOException("Bluetooth socket connect failed.");
        }
    }

    @SuppressLint("NewApi")
    private BluetoothDevice findSerialBluetoothBoard() throws UnknownHostException {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
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

    /**
     * Reset the bluetooth connection.
     * @throws IOException
     * @since 1.2.0
     */
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
}
