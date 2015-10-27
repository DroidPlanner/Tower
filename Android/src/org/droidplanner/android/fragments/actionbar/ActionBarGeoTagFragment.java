package org.droidplanner.android.fragments.actionbar;

import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.droidplanner.android.R;

/**
 * Created by chavi on 10/19/15.
 */
public class ActionBarGeoTagFragment extends Fragment {

    private TextView titleText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_action_bar_geotag, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        titleText = (TextView) view.findViewById(R.id.geotag_title);
    }

    public void updateTitle(@StringRes int title) {
        titleText.setText(title);
    }
}
