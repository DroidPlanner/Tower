package org.droidplanner.glass.fragments;

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
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import com.google.android.glass.app.Card;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;
import org.droidplanner.DroidPlannerApp;
import org.droidplanner.R;
import org.droidplanner.fragments.helpers.BTDeviceListFragment;
import org.droidplanner.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This class is used to implement the bluetooth device selection on Glass.
 * It shows the devices as a set of glass cards to be scrolled through.
 *
 * @author Fredia Huya-Kouadio
 * @since 1.2.0
 */
public class BTDeviceCardsFragment extends DialogFragment {

    /**
     * This tag is used for logging.
     *
     * @since 1.2.0
     */
    private static final String TAG = BTDeviceCardsFragment.class.getName();

    /**
     * This is the bluetooth adapter.
     *
     * @since 1.2.0
     */
    private BluetoothAdapter mBtAdapter;

    /**
     * Cards adapter for the bluetooth devices.
     *
     * @since 1.2.0
     */
    private BluetoothDeviceCardAdapter mCardsAdapter;

    /**
     * Cards view for the bluetooth devices.
     *
     * @since 1.2.0
     */
    private CardScrollView mCardsView;

    /**
     * This broadcast listener listens for discovered devices, and adds them to the list.
     *
     * @since 1.2.0
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //Get the bluetooth device object from the intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //If it's already paired, skip it because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    mCardsAdapter.addDevice(device);
                    mCardsAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final Activity activity = getActivity();
        final View view = inflater.inflate(R.layout.fragment_bt_device_cards, container, false);

        //Get the local bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        mCardsAdapter = new BluetoothDeviceCardAdapter(activity.getApplicationContext());

        mCardsView = (CardScrollView) view.findViewById(R.id.bt_device_cards);
        mCardsView.setAdapter(mCardsAdapter);
        mCardsView.activate();
        mCardsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Perform click sound
                view.playSoundEffect(SoundEffectConstants.CLICK);

                final BluetoothDevice device = (BluetoothDevice) parent.getItemAtPosition(position);
                if (device == null)
                    return;

                //Cancel discovery because it's costly, and we're about to connect
                mBtAdapter.cancelDiscovery();

                //Stores the mac address in the shared preferences,
                // so the bluetooth client can retrieve it on connection.
                final SharedPreferences.Editor editor = PreferenceManager
                        .getDefaultSharedPreferences(activity).edit();
                editor.putString(Constants.PREF_BLUETOOTH_DEVICE_ADDRESS,
                        device.getAddress()).apply();

                //Toggle the drone connection
                ((DroidPlannerApp) activity.getApplication()).drone.MavClient
                        .toggleConnectionState();

                //Close the dialog fragment
                dismiss();
            }
        });

        //Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        activity.registerReceiver(mReceiver, filter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mBtAdapter.isEnabled()) {
            //Get a set of currently paired devices
            Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

            //If there are paired devices, add each one to the cards adapter
            for (BluetoothDevice device : pairedDevices) {
                mCardsAdapter.addDevice(device);
            }

            mCardsAdapter.notifyDataSetChanged();

            //Do discovery
            if (mBtAdapter.isDiscovering())
                mBtAdapter.cancelDiscovery();

            //Request discovery from the bluetooth adapter
            mBtAdapter.startDiscovery();
        }
        else {
            //Request that bluetooth be enabled
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                    BTDeviceListFragment.REQUEST_ENABLE_BT);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //Make sure we're not doing discovery anymore
        if (mBtAdapter != null)
            mBtAdapter.cancelDiscovery();

        getActivity().unregisterReceiver(mReceiver);
    }

    /**
     * This is the adapter for the card scroll view.
     *
     * @since 1.2.0
     */
    private static class BluetoothDeviceCardAdapter extends CardScrollAdapter {

        /**
         * Application context to initialize the glass Card.
         *
         * @since 1.2.0
         */
        private final Context mContext;

        /**
         * Contains the list of bluetooth devices.
         *
         * @since 1.2.0
         */
        private final List<BluetoothDevice> mBtDevices = new ArrayList<BluetoothDevice>();

        public BluetoothDeviceCardAdapter(Context context) {
            super();
            mContext = context;
        }

        public void addDevice(BluetoothDevice device) {
            mBtDevices.add(device);
        }

        @Override
        public int getCount() {
            if (mBtDevices.isEmpty())
                return 1;

            return mBtDevices.size();
        }

        @Override
        public Object getItem(int i) {
            if (mBtDevices.isEmpty())
                return null;

            return mBtDevices.get(i);
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            final Card card = new Card(mContext);

            if (mBtDevices.isEmpty()) {
                card.setText("No bluetooth devices!");
            }
            else {
                final BluetoothDevice device = (BluetoothDevice) getItem(i);
                card.setText(device.getName())
                        .setFootnote(device.getAddress())
                        .addImage(R.drawable.ic_action_bluetooth);
            }
            return card.getView();
        }

        @Override
        public int getPosition(Object o) {
            if (mBtDevices.isEmpty())
                return -1;

            return mBtDevices.indexOf(o);
        }
    }
}
