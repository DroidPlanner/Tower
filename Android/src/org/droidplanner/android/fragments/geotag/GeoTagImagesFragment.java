package org.droidplanner.android.fragments.geotag;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.droidplanner.android.R;
import org.droidplanner.android.activities.GeoTagActivity;

import java.io.File;
import java.util.ArrayList;

/**
 * Fragment that handles UI for geotagging images.
 */
public class GeoTagImagesFragment extends Fragment {
    private static final String ARG_STATE = "state";
    private static final String ARG_TOTAL = "total";
    private static final String ARG_PROGRESS = "progress";

    private static final int STATE_INIT = 0;
    private static final int STATE_GEOTAGGING = 1;

    private GeoTagActivity activity;
    private LocalBroadcastManager lbm;

    private TextView instructionText;
    private Button geotagButton;
    private ProgressBar progressBar;
    private ImageView sdCard;
    private Animation sdCardAnim;

    private int total = 0;
    private int progress = 0;

    private static final IntentFilter filter = new IntentFilter();
    static {
        filter.addAction(GeoTagImagesService.STATE_FINISHED_GEOTAGGING);
        filter.addAction(GeoTagImagesService.STATE_PROGRESS_UPDATE_GEOTAGGING);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case GeoTagImagesService.STATE_FINISHED_GEOTAGGING:
                    finishedGeotagging(intent);
                    break;
                case GeoTagImagesService.STATE_PROGRESS_UPDATE_GEOTAGGING:
                    total = intent.getIntExtra(GeoTagImagesService.EXTRA_TOTAL, 0);
                    progress = intent.getIntExtra(GeoTagImagesService.EXTRA_PROGRESS, 0);
                    updateState(STATE_GEOTAGGING);
                    break;
            }
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity activity = getActivity();
        if (!(activity instanceof GeoTagActivity)) {
            throw new IllegalStateException("Activity is not instance of " + GeoTagActivity.class.getSimpleName());
        }

        this.activity = (GeoTagActivity) activity;
        lbm = LocalBroadcastManager.getInstance(context);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_geotag_images, container, false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(ARG_STATE, currState);
        outState.putInt(ARG_TOTAL, total);
        outState.putInt(ARG_PROGRESS, progress);
        super.onSaveInstanceState(outState);
    }

    int currState = STATE_INIT;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState != null) {
            currState = savedInstanceState.getInt(ARG_STATE, STATE_INIT);
            total = savedInstanceState.getInt(ARG_TOTAL);
            progress = savedInstanceState.getInt(ARG_PROGRESS);
        }

        instructionText = (TextView) view.findViewById(R.id.instruction_text);
        geotagButton = (Button) view.findViewById(R.id.geotag_button);
        progressBar = (ProgressBar) view.findViewById(R.id.geotag_progress);
        sdCard = (ImageView) view.findViewById(R.id.sd_card);

        instructionText.setText(R.string.insert_sdcard);
        geotagButton.setText(R.string.label_begin);

        sdCardAnim = AnimationUtils.loadAnimation(getContext(), R.anim.sd_card_anim);

        updateState(currState);

        geotagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (currState) {
                    case STATE_INIT:
                        updateState(STATE_GEOTAGGING);
                        startGeoTagging();
                        break;
                    case STATE_GEOTAGGING:
                        updateState(STATE_INIT);
                        cancelGeoTagging();
                        break;
                }
            }
        });

        if (activity != null) {
            activity.updateTitle(R.string.geo_tag_label);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        lbm.registerReceiver(receiver, filter);
    }

    @Override
    public void onStop() {
        super.onStop();
        lbm.unregisterReceiver(receiver);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    private void startGeoTagging() {
        Context context = getContext();
        if (context != null) {
            Intent intent = new Intent(context, GeoTagImagesService.class);
            intent.setAction(GeoTagImagesService.ACTION_START_GEOTAGGING);
            context.startService(intent);
        }
    }

    private void cancelGeoTagging() {
        Context context = getContext();
        if (context != null) {
            Intent intent = new Intent(context, GeoTagImagesService.class);
            intent.setAction(GeoTagImagesService.ACTION_CANCEL_GEOTAGGING);
            context.startService(intent);
        }
    }

    private void updateState(int state) {
        currState = state;
        switch (currState) {
            case STATE_INIT:
                geotagButton.setActivated(false);
                currState = STATE_INIT;
                geotagButton.setText(R.string.label_begin);
                sdCard.setVisibility(View.VISIBLE);
                sdCard.startAnimation(sdCardAnim);
                progressBar.setProgress(0);
                break;
            case STATE_GEOTAGGING:
                geotagButton.setActivated(true);
                instructionText.setText(R.string.geotagging);
                geotagButton.setText(R.string.button_setup_cancel);
                sdCard.clearAnimation();
                sdCard.setVisibility(View.GONE);
                progressBar.setMax(total);
                progressBar.setProgress(progress);
                break;
        }
    }

    private void failedLoading(String message) {
        updateState(STATE_INIT);
        instructionText.setText(String.format(getString(R.string.failed_geotag), message));
    }

    private void finishedGeotagging(Intent intent) {
        boolean success = intent.getBooleanExtra(GeoTagImagesService.EXTRA_SUCCESS, false);
        if (success) {
            if (activity != null) {
                ArrayList<File> files = (ArrayList<File>) intent.getSerializableExtra(GeoTagImagesService.EXTRA_GEOTAGGED_FILES);
                activity.finishedGeotagging(files);
            }
        } else {
            String failure = intent.getStringExtra(GeoTagImagesService.EXTRA_FAILURE_MESSAGE);
            failedLoading(failure);
        }
    }
}