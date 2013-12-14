package com.droidplanner.connection;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
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
    private static final Set<UUID> sValidUuids = new LinkedHashSet<UUID>(BluetoothServer.UUIDS
            .length + 1);

    static {
        sValidUuids.add(UUID.fromString(UUID_SPP_DEVICE));
        for (UUID uuid : BluetoothServer.UUIDS) {
            sValidUuids.add(uuid);
        }
    }

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
    protected void openConnection() throws IOException {
        Log.d(BLUE, "Connect");

        //Reset the input, and output stream
        in = null;
        out = null;

        //Retrieve the stored address
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences
                (parentContext);
        String address = settings.getString(Constants.PREF_BLUETOOTH_DEVICE_ADDRESS, null);

        BluetoothDevice device = address == null
                ? findSerialBluetoothBoard()
                : mBluetoothAdapter.getRemoteDevice(address);

        final Set<UUID> supportedUuids = retrieveSupportedUuids(device);
        for (UUID uuid : supportedUuids) {
            try {
                bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(uuid);
                mBluetoothAdapter.cancelDiscovery();
                bluetoothSocket.connect();

                out = bluetoothSocket.getOutputStream();
                in = bluetoothSocket.getInputStream();

                //Found a good one... break
                break;
            } catch (IOException e) {
                //try another uuid
            }
        }

        if (out == null || in == null) {
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
        bluetoothSocket.close();
        Log.d(BLUE, "## BT Closed ##");
    }

    @Override
    protected void getPreferences(SharedPreferences prefs) {
        // TODO Auto-generated method stub
    }

    /**
     * Retrieves the supported list of uuid from the device uuids.
     *
     * @param device bluetooth device whose uuids to check
     * @return support uuids from the passed bluetooth device
     * @since 1.2.0
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    public Set<UUID> retrieveSupportedUuids(BluetoothDevice device) {
        Set<UUID> validUuids = new LinkedHashSet<UUID>();

        if (device != null) {
            ParcelUuid[] deviceUuids = device.getUuids();
            if (deviceUuids == null) {
                //We have yet to pair with/connect to the device, so try all the supported uuids.
                validUuids = sValidUuids;
            }
            else {
                boolean isDeviceValid = false;

                for (ParcelUuid parcelUuid : deviceUuids) {
                    final UUID uuid = parcelUuid.getUuid();
                    if (sValidUuids.contains(uuid)) {
                        validUuids.add(uuid);
                        isDeviceValid = true;
                    }
                }

                if (isDeviceValid) {
                    //The device could be a bluetooth relay server, so add the rest of the uuids to
                    // test them all
                    for (UUID uuid : sValidUuids) {
                        validUuids.add(uuid);
                    }
                }
            }
        }
        return validUuids;
    }
}
