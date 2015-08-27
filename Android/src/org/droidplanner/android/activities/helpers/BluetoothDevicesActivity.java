package org.droidplanner.android.activities.helpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.droidplanner.android.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * This is used to show the user a list of bluetooth devices to connect to.
 * @author Fredia Huya-Kouadio
 */
public class BluetoothDevicesActivity extends AppCompatActivity {

	/**
	 * Request code used in onActivityResult to check for bluetooth activation
	 * result.
	 */
	private static final int REQUEST_ENABLE_BT = 111;

    private static final String KEY_DISCOVERED_BT_DEVICES = "key_discovered_bt_devices";
    private static final String KEY_IS_DISCOVERY_ON = "key_is_discovery_on";

    /**
     * Sets to true is discovery is running.
     */
    private boolean mIsDiscoveryOn;

    /**
     * List used to temporary store newly discovered bluetooth devices during discovery.
     */
    private ArrayList<BluetoothDevice> mTempNewDevices;

	/**
	 * Bluetooth adapter.
	 */
	private BluetoothAdapter mBtAdapter;

	/**
	 * Contains the list of bluetooth devices.
	 */
	private BluetoothDeviceAdapter mBluetoothDevicesAdapter;

	/**
	 * Title for this dialog.
	 */
	private TextView mDeviceListTitle;

	/**
	 * Progress for this dialog, indicating the device is scanning for new
	 * bluetooth connections.
	 */
	private ProgressBar mDeviceListProgressBar;

    /**
     * Image button used to refresh the list of bluetooth devices.
     */
    private ImageButton mRefreshDeviceList;

	/**
	 * The broadcast receiver listens for discovered devices, and changes the
	 * title when discovery is finished
	 */
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				// If it's already paired, skip it, because it's been listed
				// already
				if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
					mTempNewDevices.add(device);
                    mBluetoothDevicesAdapter.setNewDevices(mTempNewDevices);
				}
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				// When discovery is finished, change the dialog title, and hide
				// the progress bar.
				mDeviceListTitle.setText(R.string.select_device);
				mDeviceListProgressBar.setVisibility(View.INVISIBLE);
                mRefreshDeviceList.setVisibility(View.VISIBLE);

                mIsDiscoveryOn = false;
			}
		}
	};

	/**
	 * The on-click listener for all devices in the listviews.
	 */
	private final AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,	long id) {
            final Object item = parent.getItemAtPosition(position);
            if(!(item instanceof BluetoothDevice))
                return;

			// Cancel discovery because it's costly, and we're about to connect
			mBtAdapter.cancelDiscovery();

			// Retrieve the selected bluetooth device
			final BluetoothDevice device = (BluetoothDevice) item;

			// Stores the mac address in the shared preferences,
			// so the bluetooth client can retrieve it on connection.
            final Context context = getApplicationContext();
			DroidPlannerPrefs mAppPrefs = new DroidPlannerPrefs(context);
            mAppPrefs.setBluetoothDeviceName(device.getName());
            mAppPrefs.setBluetoothDeviceAddress(device.getAddress());

			// Toggle the drone connection
            DroidPlannerApp.connectToDrone(context);

			// Finish the activity
			finish();
		}
	};

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_ENABLE_BT:
			if (resultCode == Activity.RESULT_CANCELED) {
				// Bluetooth activation was denied by the user. Dismiss this activity.
				finish();
			}
			break;

		default:
			super.onActivityResult(requestCode, resultCode, data);
			break;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_bluetooth_device_list);

        // Get the local bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        mDeviceListTitle = (TextView) findViewById(R.id.bt_device_list_title);
        mDeviceListProgressBar = (ProgressBar) findViewById(R.id.bt_scan_progress_bar);

        // Initialize the button to perform device discovery
        mRefreshDeviceList = (ImageButton) findViewById(R.id.bt_scan_button);
        mRefreshDeviceList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doDiscovery();
                v.setVisibility(View.INVISIBLE);
            }
        });

        // Initialize array adapters. One for already paired devices,
        // and one for newly discovered devices
        final Context context = getApplicationContext();
        mBluetoothDevicesAdapter = new BluetoothDeviceAdapter(context);

        ListView btDevicesListView = (ListView) findViewById(R.id.bt_devices_list);
        btDevicesListView.setAdapter(mBluetoothDevicesAdapter);
        btDevicesListView.setOnItemClickListener(mDeviceClickListener);

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);

        if(savedInstanceState != null){
            mTempNewDevices = savedInstanceState.getParcelableArrayList(KEY_DISCOVERED_BT_DEVICES);
            mIsDiscoveryOn = savedInstanceState.getBoolean(KEY_IS_DISCOVERY_ON);
        }
        if(mTempNewDevices == null){
            mTempNewDevices = new ArrayList<BluetoothDevice>();
        }
	}


	@Override
	public void onResume() {
		super.onResume();

		if (mBtAdapter != null && mBtAdapter.isEnabled()) {
			// Get a set of currently paired devices
			Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

			// If there are paired devices, add each one to the ArrayAdapter
            mBluetoothDevicesAdapter.setPairedDevices(pairedDevices);

            if(!mTempNewDevices.isEmpty()){
                mBluetoothDevicesAdapter.setNewDevices(mTempNewDevices);
            }

            if(mIsDiscoveryOn){
                doDiscovery();
            }
		} else {
			// Request that bluetooth be enabled
			startActivityForResult(new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		// Make sure we're not doing discovery anymore
		if (mBtAdapter != null) {
			mBtAdapter.cancelDiscovery();
		}

		unregisterReceiver(mReceiver);
	}

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);

        //Store the current list of newly discovered bluetooth devices.
        outState.putParcelableArrayList(KEY_DISCOVERED_BT_DEVICES, mTempNewDevices);

        //Store if we're currently doing discovery
        outState.putBoolean(KEY_IS_DISCOVERY_ON, mIsDiscoveryOn);
    }

	/**
	 * Start device discovery with the bluetooth adapter
	 */
	private void doDiscovery() {
		// Indicate scanning in the title
        mRefreshDeviceList.setVisibility(View.INVISIBLE);
		mDeviceListProgressBar.setVisibility(View.VISIBLE);

		mDeviceListTitle.setText(R.string.scanning);

		// If we're already discovering, stop it
		if (mBtAdapter.isDiscovering())
			mBtAdapter.cancelDiscovery();

        //Empty the temporary newly discovered list
        mTempNewDevices.clear();

		// Request discovery from the bluetooth adapter
		mIsDiscoveryOn = mBtAdapter.startDiscovery();
	}

	public static class BluetoothDeviceAdapter extends ArrayAdapter<Object> {

		private final LayoutInflater mInflater;

        private final List<Object> mPairedDevices = new ArrayList<Object>();
        private final List<Object> mNewDevices = new ArrayList<Object>();

		public BluetoothDeviceAdapter(Context context) {
			super(context, 0);
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

        public void setPairedDevices(Collection<BluetoothDevice> pairedDevices){
            mPairedDevices.clear();
            if(pairedDevices.isEmpty()) {
                mPairedDevices.add(getContext().getString(R.string.none_paired));
            }
            else{
                //Add the title first.
                mPairedDevices.add(getContext().getString(R.string.title_paired_devices));
                mPairedDevices.addAll(pairedDevices);
            }
            notifyDataSetChanged();
        }

        public void setNewDevices(List<BluetoothDevice> newDevices){
            mNewDevices.clear();
            if(newDevices.isEmpty()) {
                mNewDevices.add(getContext().getString(R.string.none_found));
            }
            else{
                //Add the title first.
                mNewDevices.add(getContext().getString(R.string.title_other_devices));
                mNewDevices.addAll(newDevices);
            }
            notifyDataSetChanged();
        }

        @Override
        public int getCount(){
            return mPairedDevices.size() + mNewDevices.size();
        }

        @Override
        public Object getItem(int position){
            final int pairedDevicesCount = mPairedDevices.size();
            if(position < pairedDevicesCount){
                return mPairedDevices.get(position);
            }

            position = position - pairedDevicesCount;
            return mNewDevices.get(position);
        }

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView view;
            Object item = getItem(position);
            if(item instanceof String){
                if (convertView != null && convertView.getId() == R.id.title_bluetooth_devices) {
                    view = (TextView) convertView;
                } else {
                    view = (TextView) mInflater.inflate(R.layout.list_device_title,	parent, false);
                }
                view.setText(item.toString());
            }
            else{
                if (convertView != null && convertView.getId() == R.id.bluetooth_device_info) {
                    view = (TextView) convertView;
                } else {
                    view = (TextView) mInflater.inflate(R.layout.list_device_name,	parent, false);
                }

                BluetoothDevice btDevice = (BluetoothDevice) item;
                view.setText(btDevice.getName() + "\n" + btDevice.getAddress());
            }

			return view;
		}
	}

}