package org.droidplanner.android.wear.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import org.droidplanner.android.wear.R;

/**
 * Main fragment for the app. Display a google map with the user, and drone location.
 */
public class WearMapFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        return inflater.inflate(R.layout.fragment_wear_map, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        final Button connectButton = (Button) view.findViewById(R.id.toggle_drone_connection);
        if(connectButton != null){
            connectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getActivity(), "Connect", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
