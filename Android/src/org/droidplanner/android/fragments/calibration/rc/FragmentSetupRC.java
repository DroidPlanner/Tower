package org.droidplanner.android.fragments.calibration.rc;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import org.droidplanner.android.R;

public class FragmentSetupRC extends Fragment {

    public static final String PREFTHROTTLELIMITKEY = "throttleLimit";
    public static final String PREFRANGELIMITKEY = "rangeLimit";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setup_rc_main, container, false);

        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        SeekBar seekThrottle = (SeekBar) view.findViewById(R.id.seekThrottle);
        final TextView lblSeekThrottle = (TextView) view.findViewById(R.id.lblSeekThrottle);
        seekThrottle.setMax(100);
        seekThrottle.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                lblSeekThrottle.setText(progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                prefs.edit().putInt(PREFTHROTTLELIMITKEY, seekBar.getProgress()).apply();
            }

        });
        seekThrottle.setProgress(prefs.getInt(PREFTHROTTLELIMITKEY, 100));

        SeekBar seekRange = (SeekBar) view.findViewById(R.id.seekRange);
        final TextView lblSeekRange = (TextView) view.findViewById(R.id.lblSeekRange);
        seekRange.setMax(100);

        seekRange.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                lblSeekRange.setText(progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                prefs.edit().putInt(PREFRANGELIMITKEY, seekBar.getProgress()).apply();
            }

        });
        seekRange.setProgress(prefs.getInt(PREFRANGELIMITKEY, 100));

        return view;
    }

    public static String getTitle(Context c) {
        return "RC Setup";
    }

}
