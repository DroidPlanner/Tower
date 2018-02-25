package org.droidplanner.services.android.impl.communication.connection.usb;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import com.o3dr.services.android.lib.gcs.link.LinkConnectionStatus;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import timber.log.Timber;

class UsbCDCConnection extends UsbConnection.UsbConnectionImpl {
    private static final String TAG = UsbCDCConnection.class.getSimpleName();
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    private static final IntentFilter intentFilter = new IntentFilter(ACTION_USB_PERMISSION);

    private final AtomicReference<UsbSerialDriver> serialDriverRef = new AtomicReference<>();

    private final PendingIntent usbPermissionIntent;

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                removeWatchdog();
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    if (device != null) {
                        //call method to set up device communication
                        try {
                            openUsbDevice(device, extrasHolder.get());
                        } catch (IOException e) {
                            Log.e(TAG, e.getMessage(), e);
                        }
                    } else {
                        LinkConnectionStatus connectionStatus = LinkConnectionStatus
                            .newFailedConnectionStatus(LinkConnectionStatus.LINK_UNAVAILABLE, "Unable to access usb device.");
                        onUsbConnectionStatus(connectionStatus);
                    }
                } else {
                    Log.d(TAG, "permission denied for device " + device);
                    LinkConnectionStatus connectionStatus = LinkConnectionStatus
                        .newFailedConnectionStatus(LinkConnectionStatus.PERMISSION_DENIED, "USB Permission denied.");
                    onUsbConnectionStatus(connectionStatus);
                }
            }
        }
    };

    private final Runnable permissionWatchdog = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "Permission request timeout.");
            LinkConnectionStatus connectionStatus = LinkConnectionStatus
                .newFailedConnectionStatus(LinkConnectionStatus.TIMEOUT, "Unable to get usb access.");
            onUsbConnectionStatus(connectionStatus);

            removeWatchdog();
        }
    };

    private final AtomicReference<Bundle> extrasHolder = new AtomicReference<>();
    private ScheduledExecutorService scheduler;

    protected UsbCDCConnection(Context context, UsbConnection parentConn, int baudRate) {
        super(context, parentConn, baudRate);
        this.usbPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
    }

    private void registerUsbPermissionBroadcastReceiver() {
        mContext.registerReceiver(broadcastReceiver, intentFilter);
    }

    private void unregisterUsbPermissionBroadcastReceiver() {
        try {
            mContext.unregisterReceiver(broadcastReceiver);
        }catch(IllegalArgumentException e){
            Timber.e(e, "Receiver was not registered.");
        }
    }

    private void removeWatchdog() {
        if (scheduler != null) {
            scheduler.shutdown();
            scheduler = null;
        }
    }

    @Override
    protected void openUsbConnection(Bundle extras) throws IOException {
        extrasHolder.set(extras);
        registerUsbPermissionBroadcastReceiver();

        // Get UsbManager from Android.
        UsbManager manager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);

        //Get the list of available devices
        List<UsbDevice> availableDevices = UsbSerialProber.getAvailableSupportedDevices(manager);
        if (availableDevices.isEmpty()) {
            Log.d(TAG, "No Devices found");
            throw new IOException("No Devices found");
        }

        //Pick the first device
        UsbDevice device = availableDevices.get(0);
        if (manager.hasPermission(device)) {
            openUsbDevice(device, extras);
        } else {
            removeWatchdog();

            scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.schedule(permissionWatchdog, 15, TimeUnit.SECONDS);
            Log.d(TAG, "Requesting permission to access usb device " + device.getDeviceName());
            manager.requestPermission(device, usbPermissionIntent);
        }
    }

    private void openUsbDevice(UsbDevice device, Bundle extras) throws IOException {
        // Get UsbManager from Android.
        UsbManager manager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);

        // Find the first available driver.
        final UsbSerialDriver serialDriver = UsbSerialProber.openUsbDevice(manager, device);

        if (serialDriver == null) {
            Log.d(TAG, "No Devices found");
            throw new IOException("No Devices found");
        } else {
            Log.d(TAG, "Opening using Baud rate " + mBaudRate);
            try {
                serialDriver.open();
                serialDriver.setParameters(mBaudRate, 8, UsbSerialDriver.STOPBITS_1, UsbSerialDriver.PARITY_NONE);

                serialDriverRef.set(serialDriver);

                onUsbConnectionOpened(extras);
            } catch (IOException e) {
                Log.e(TAG, "Error setting up device: " + e.getMessage(), e);
                try {
                    serialDriver.close();
                } catch (IOException e2) {
                    // Ignore.
                }
            }
        }
    }

    @Override
    protected int readDataBlock(byte[] readData) throws IOException {
        // Read data from driver. This call will return up to readData.length bytes.
        // If no data is received it will timeout after 200ms (as set by parameter 2)
        final UsbSerialDriver serialDriver = serialDriverRef.get();
        if(serialDriver == null)
            throw new IOException("Device is unavailable.");

        int iavailable = 0;
        try {
            iavailable = serialDriver.read(readData, 200);
        } catch (NullPointerException e) {
            final String errorMsg = "Error Reading: " + e.getMessage()
                    + "\nAssuming inaccessible USB device.  Closing connection.";
            Log.e(TAG, errorMsg, e);
            throw new IOException(errorMsg, e);
        }

        if (iavailable == 0)
            iavailable = -1;
        return iavailable;
    }

    @Override
    protected void sendBuffer(byte[] buffer) {
        // Write data to driver. This call should write buffer.length bytes
        // if data cant be sent , then it will timeout in 500ms (as set by
        // parameter 2)
        final UsbSerialDriver serialDriver = serialDriverRef.get();
        if (serialDriver != null) {
            try {
                serialDriver.write(buffer, 500);
            } catch (IOException e) {
                Log.e(TAG, "Error Sending: " + e.getMessage(), e);
            }
        }
    }

    @Override
    protected void closeUsbConnection() throws IOException {
        unregisterUsbPermissionBroadcastReceiver();

        final UsbSerialDriver serialDriver = serialDriverRef.getAndSet(null);
        if (serialDriver != null) {
            try {
                serialDriver.close();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }

    @Override
    public String toString() {
        return TAG;
    }
}
