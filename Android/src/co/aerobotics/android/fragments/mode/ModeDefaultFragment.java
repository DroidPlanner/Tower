package co.aerobotics.android.fragments.mode;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by fhuya on 5/10/2016.
 */
public class ModeDefaultFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(co.aerobotics.android.R.layout.fragment_mode_default, container, false);
    }
}
