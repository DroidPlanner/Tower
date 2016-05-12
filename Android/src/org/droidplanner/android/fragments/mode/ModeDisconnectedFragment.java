package org.droidplanner.android.fragments.mode;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;

import com.o3dr.services.android.lib.drone.connection.ConnectionType;

import org.droidplanner.android.R;
import org.droidplanner.android.dialogs.ClearBTDialogPreference;
import org.droidplanner.android.dialogs.ClearBTPreferenceFragmentCompat;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;

public class ModeDisconnectedFragment extends PreferenceFragmentCompat {

	private final static IntentFilter filter = new IntentFilter();
	static {
		filter.addAction(DroidPlannerPrefs.PREF_CONNECTION_TYPE);
		filter.addAction(DroidPlannerPrefs.PREF_BT_DEVICE_ADDRESS);
	}

	private final BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			switch(intent.getAction()){
				case DroidPlannerPrefs.PREF_CONNECTION_TYPE:
					updateConnectionSettings();
					break;

				case DroidPlannerPrefs.PREF_BT_DEVICE_ADDRESS:
					updateBluetoothDevicePreference();
					break;
			}
		}
	};

	private DroidPlannerPrefs prefs;

	private PreferenceScreen rootPref;
	private PreferenceCategory usbPrefs;
	private PreferenceCategory tcpPrefs;
	private PreferenceCategory udpPrefs;
	private PreferenceCategory bluetoothPrefs;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        prefs = DroidPlannerPrefs.getInstance(getActivity().getApplicationContext());
        setPreferencesFromResource(R.xml.preferences_connection, s);

		loadConnectionPreferences();
        updateBluetoothDevicePreference();
    }

	@Override
	public void onStart(){
		super.onStart();

        DialogFragment dialogFragment = getDialogFragment();
        if(dialogFragment != null){
            dialogFragment.dismissAllowingStateLoss();
        }

		updateConnectionSettings();
		LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(receiver, filter);
	}

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        if (preference instanceof ClearBTDialogPreference) {
            DialogFragment dialogFragment = getDialogFragment();
            if (dialogFragment == null) {
                dialogFragment = ClearBTPreferenceFragmentCompat.newInstance(preference);
                dialogFragment.setTargetFragment(this, 0);
                dialogFragment.show(getFragmentManager(), "android.support.v7.preference.PreferenceFragment.DIALOG");
            }
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }

	private void updateConnectionSettings() {
        if(this.rootPref == null)
            return;

		hideAllPrefs();

		final int connectionType = prefs.getConnectionParameterType();
		switch(connectionType){
			case ConnectionType.TYPE_USB:
				this.rootPref.addPreference(this.usbPrefs);
				break;

			case ConnectionType.TYPE_TCP:
				this.rootPref.addPreference(this.tcpPrefs);
				break;

			case ConnectionType.TYPE_UDP:
				this.rootPref.addPreference(this.udpPrefs);
				break;

			case ConnectionType.TYPE_BLUETOOTH:
				this.rootPref.addPreference(this.bluetoothPrefs);
				break;

			case ConnectionType.TYPE_SOLO:
				break;
		}
	}

	@Override
	public void onStop(){
		super.onStop();
        DialogFragment dialogFragment = getDialogFragment();
        if(dialogFragment != null){
            dialogFragment.dismissAllowingStateLoss();
        }

		LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).unregisterReceiver(receiver);
	}

    private DialogFragment getDialogFragment(){
        return (DialogFragment) getFragmentManager().findFragmentByTag("android.support.v7.preference.PreferenceFragment.DIALOG");
    }

	private void hideAllPrefs(){
		if(this.rootPref != null)
			this.rootPref.removeAll();
	}

	private void loadConnectionPreferences(){
		this.rootPref = getPreferenceScreen();
		this.usbPrefs = (PreferenceCategory) findPreference("pref_usb");
		this.tcpPrefs = (PreferenceCategory) findPreference("pref_server");
		this.udpPrefs = (PreferenceCategory) findPreference("pref_server_udp");
		this.bluetoothPrefs = (PreferenceCategory) findPreference("pref_bluetooth");
	}

    private void updateBluetoothDevicePreference(){
		final ClearBTDialogPreference preference = (ClearBTDialogPreference) findPreference(DroidPlannerPrefs.PREF_BT_DEVICE_ADDRESS);
        if(preference == null)
            return;

		String deviceAddress = prefs.getBluetoothDeviceAddress();

        if(TextUtils.isEmpty(deviceAddress)) {
            preference.setEnabled(false);
            preference.setTitle(R.string.pref_no_saved_bluetooth_device_title);
            preference.setSummary("");
        }
        else{
            preference.setEnabled(true);
            preference.setSummary(deviceAddress);

            final String deviceName = prefs.getBluetoothDeviceName();
            if(deviceName != null){
                preference.setTitle(getString(R.string.pref_forget_bluetooth_device_title, deviceName));
            }
            else
                preference.setTitle(getString(R.string.pref_forget_bluetooth_device_address));
        }
    }
}
