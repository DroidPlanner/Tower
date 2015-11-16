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

import java.io.File;
import java.util.ArrayList;

/**
 * Created by chavi on 11/16/15.
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
            ArrayList<File> files = (ArrayList<File>) args.getSerializable(GeoTagImagesService.EXTRA_GEOTAGGED_FILES);
            numFiles = files.size();
            if (files != null && numFiles > 0) {
                parent = files.get(0).getParent();
            }
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
