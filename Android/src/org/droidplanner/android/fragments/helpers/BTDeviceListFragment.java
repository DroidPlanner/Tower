package org.droidplanner.android.fragments.helpers;

import java.util.Set;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.utils.Constants;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * This is used to show the user a list of bluetooth devices to connect to.
 * 
 * @author Fredia Huya-Kouadio
 * @since 1.2.0
 */
public class BTDeviceListFragment extends DialogFragment {

	/**
	 * This tag is used for logging.
	 * 
	 * @since 1.2.0
	 */
	private static final String TAG = BTDeviceListFragment.class.getName();

	/**
	 * Request code used in onActivityResult to check for bluetooth activation
	 * result.
	 * 
	 * @since 1.2.0
	 */
	public static final int REQUEST_ENABLE_BT = 111;

	/**
	 * Bluetooth adapter.
	 * 
	 * @since 1.2.0
	 */
	private BluetoothAdapter mBtAdapter;

	/**
	 * Contains the list of paired devices.
	 * 
	 * @since 1.2.0
	 */
	private BluetoothDeviceAdapter mPairedDevicesArrayAdapter;

	/**
	 * Contains the list of newly discovered devices.
	 * 
	 * @since 1.2.0
	 */
	private BluetoothDeviceAdapter mNewDevicesArrayAdapter;

	/**
	 * Title for this dialog.
	 * 
	 * @since 1.2.0
	 */
	private TextView mDeviceListTitle;

	/**
	 * Progress for this dialog, indicating the device is scanning for new
	 * bluetooth connections.
	 * 
	 * @since 1.2.0
	 */
	private ProgressBar mDeviceListProgressBar;

	/**
	 * Title for the paired devices.
	 * 
	 * @since 1.2.0
	 */
	private TextView mPairedDevicesTitle;

	/**
	 * Title for the newly discovered devices.
	 * 
	 * @since 1.2.0
	 */
	private TextView mNewDevicesTitle;

	/**
	 * The broadcast receiver listens for discovered devices, and changes the
	 * title when discovery is finished
	 * 
	 * @since 1.2.0
	 */
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				// If it's already paired, skip it, because it's been listed
				// already
				if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
					mNewDevicesArrayAdapter.add(device);
				}
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
					.equals(action)) {
				// When discovery is finished, change the dialog title, and hide
				// the progress bar.
				mDeviceListTitle.setText(R.string.select_device);
				mDeviceListProgressBar.setVisibility(View.INVISIBLE);

				if (mNewDevicesArrayAdapter.getCount() == 0) {
					mNewDevicesTitle.setText(R.string.none_found);
				}
			}
		}
	};

	/**
	 * The on-click listener for all devices in the listviews.
	 * 
	 * @since 1.2.0
	 */
	private final AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// Cancel discovery because it's costly, and we're about to connect
			mBtAdapter.cancelDiscovery();

			// Retrieve the selected bluetooth device
			final BluetoothDevice device = (BluetoothDevice) parent
					.getItemAtPosition(position);

			// Stores the mac address in the shared preferences,
			// so the bluetooth client can retrieve it on connection.
			final Activity activity = getActivity();
			final SharedPreferences.Editor editor = PreferenceManager
					.getDefaultSharedPreferences(activity).edit();
			editor.putString(Constants.PREF_BLUETOOTH_DEVICE_ADDRESS,
					device.getAddress()).apply();

			// Toggle the drone connection
			((DroidPlannerApp) activity.getApplication()).drone.MavClient
					.toggleConnectionState();

			// Dismiss the dialog
			dismiss();
		}
	};

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_ENABLE_BT:
			if (resultCode == Activity.RESULT_CANCELED) {
				// Bluetooth activation was denied by the user. Dismiss this
				// dialog.
				dismiss();
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
		setStyle(DialogFragment.STYLE_NO_TITLE, 0);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		final Activity activity = getActivity();
		final View view = inflater.inflate(
				R.layout.fragment_bluetooth_device_list, container, false);

		// Get the local bluetooth adapter
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();

		mDeviceListTitle = (TextView) view
				.findViewById(R.id.bt_device_list_title);
		mDeviceListProgressBar = (ProgressBar) view
				.findViewById(R.id.bt_scan_progress_bar);
		mNewDevicesTitle = (TextView) view.findViewById(R.id.title_new_devices);
		mPairedDevicesTitle = (TextView) view
				.findViewById(R.id.title_paired_devices);

		// Initialize the button to perform device discovery
		Button scanButton = (Button) view.findViewById(R.id.button_scan);
		scanButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				doDiscovery();
				v.setVisibility(View.GONE);
			}
		});

		// Initialize array adapters. One for already paired devices,
		// and one for newly discovered devices
		mPairedDevicesArrayAdapter = new BluetoothDeviceAdapter(activity);
		mNewDevicesArrayAdapter = new BluetoothDeviceAdapter(activity);

		// Find and set up the listview for paired devices
		ListView pairedListView = (ListView) view
				.findViewById(R.id.paired_devices);
		pairedListView.setAdapter(mPairedDevicesArrayAdapter);
		pairedListView.setOnItemClickListener(mDeviceClickListener);

		// Find and set up the listview for newly discovered devices
		ListView newDevicesListView = (ListView) view
				.findViewById(R.id.new_devices);
		newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
		newDevicesListView.setOnItemClickListener(mDeviceClickListener);

		// Register for broadcasts when a device is discovered
		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		activity.registerReceiver(mReceiver, filter);

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();

		if (mBtAdapter.isEnabled()) {
			// Get a set of currently paired devices
			Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

			// If there are paired devices, add each one to the ArrayAdapter
			if (pairedDevices.size() > 0) {
				mPairedDevicesTitle.setVisibility(View.VISIBLE);
				for (BluetoothDevice device : pairedDevices) {
					mPairedDevicesArrayAdapter.add(device);
				}
			} else {
				mPairedDevicesTitle.setText(R.string.none_paired);
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

		getActivity().unregisterReceiver(mReceiver);
	}

	/**
	 * Start device discovery with the bluetooth adapter
	 */
	private void doDiscovery() {
		// Indicate scanning in the title
		mDeviceListProgressBar.setVisibility(View.VISIBLE);
		mDeviceListTitle.setText(R.string.scanning);

		// Turn on sub title for new devices
		mNewDevicesTitle.setVisibility(View.VISIBLE);

		// If we're already discovering, stop it
		if (mBtAdapter.isDiscovering())
			mBtAdapter.cancelDiscovery();

		// Request discovery from the bluetooth adapter
		mBtAdapter.startDiscovery();
	}

	public static class BluetoothDeviceAdapter extends
			ArrayAdapter<BluetoothDevice> {

		private final LayoutInflater mInflater;

		public BluetoothDeviceAdapter(Context context) {
			super(context, R.layout.list_device_name);

			mInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView view;

			if (convertView == null) {
				view = (TextView) mInflater.inflate(R.layout.list_device_name,
						parent, false);
			} else {
				view = (TextView) convertView;
			}

			BluetoothDevice btDevice = getItem(position);
			view.setText(btDevice.getName() + "\n" + btDevice.getAddress());
			return view;
		}
	}

}