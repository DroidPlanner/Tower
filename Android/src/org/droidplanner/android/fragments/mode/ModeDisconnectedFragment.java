package org.droidplanner.android.fragments.mode;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import org.droidplanner.android.R;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;

public class ModeDisconnectedFragment extends PreferenceFragmentCompat {

//	private final static IntentFilter filter = new IntentFilter(DroidPlannerPrefs.PREF_CONNECTION_TYPE);
//
//	private final BroadcastReceiver receiver = new BroadcastReceiver() {
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			switch(intent.getAction()){
//				case DroidPlannerPrefs.PREF_CONNECTION_TYPE:
//					updateConnectionSettings();
//					break;
//			}
//		}
//	};

	private DroidPlannerPrefs prefs;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
//        prefs = DroidPlannerPrefs.getInstance(getActivity().getApplicationContext());
        setPreferencesFromResource(R.xml.preferences_connection, s);
    }

//	@Override
//	public void onStart(){
//		super.onStart();
//		updateConnectionSettings();
//		LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(receiver, filter);
//	}
//
//	private void updateConnectionSettings() {
//		final int connectionType = prefs.getConnectionParameterType();
//		switch(connectionType){
//			case ConnectionType.TYPE_USB:
//				break;
//
//			case ConnectionType.TYPE_TCP:
//				break;
//
//			case ConnectionType.TYPE_UDP:
//				break;
//
//			case ConnectionType.TYPE_BLUETOOTH:
//				break;
//
//			case ConnectionType.TYPE_SOLO:
//				break;
//		}
//	}
//
//	@Override
//	public void onStop(){
//		super.onStop();
//		LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).unregisterReceiver(receiver);
//	}

//    private void setupConnectionPreferences() {
//        ListPreference connectionTypePref = (ListPreference) findPreference(DroidPlannerPrefs.PREF_CONNECTION_TYPE);
//        if (connectionTypePref != null) {
//            int defaultConnectionType = dpPrefs.getConnectionParameterType();
//            updateConnectionPreferenceSummary(connectionTypePref, defaultConnectionType);
//            connectionTypePref
//                .setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//                    @Override
//                    public boolean onPreferenceChange(Preference preference, Object newValue) {
//                        int connectionType = Integer.parseInt((String) newValue);
//                        updateConnectionPreferenceSummary(preference, connectionType);
//                        return true;
//                    }
//                });
//        }
//    }
//
//    private void setupBluetoothDevicePreferences(){
//        final ClearBTDialogPreference preference = (ClearBTDialogPreference) findPreference(DroidPlannerPrefs.PREF_BT_DEVICE_ADDRESS);
//        if(preference != null){
//            updateBluetoothDevicePreference(preference, dpPrefs.getBluetoothDeviceAddress());
//            preference.setOnResultListener(new ClearBTDialogPreference.OnResultListener() {
//                @Override
//                public void onResult(boolean result) {
//                    if (result) {
//                        updateBluetoothDevicePreference(preference, dpPrefs.getBluetoothDeviceAddress());
//                    }
//                }
//            });
//        }
//    }
}
