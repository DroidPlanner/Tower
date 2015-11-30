package org.droidplanner.android.fragments.geotag;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.droidplanner.android.R;
import org.droidplanner.android.activities.GeoTagActivity;

/**
 * Fragment that handles UI for successfully geotagged images.
 */
public class FinishGeoTagFragment extends Fragment {

    private Activity activity;
    private String parent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_finish_geotagging, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView filesText = (TextView) view.findViewById(R.id.files_text);
        view.findViewById(R.id.finish_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activity != null) {
                    activity.finish();
                }
            }
        });

        filesText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (parent != null) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    Uri uri = Uri.parse(parent);
                    intent.setDataAndType(uri, "resource/folder");
                    startActivity(Intent.createChooser(intent, "Open folder"));
                }
            }
        });

        int numFiles = 0;
        Bundle args = getArguments();
        if (args != null) {
            numFiles = args.getInt(GeoTagActivity.NUM_IMAGE_FILES);
            parent = args.getString(GeoTagActivity.PARENT_DIR);
        }

        if (parent != null) {
            filesText.setText(String.format(getString(R.string.photos_geotagged), String.valueOf(numFiles), parent));
        } else {
            filesText.setText(getString(R.string.no_files_geotagged));
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        activity = getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

}
