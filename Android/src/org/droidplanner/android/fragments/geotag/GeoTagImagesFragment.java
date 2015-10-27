package org.droidplanner.android.fragments.geotag;

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
 * Created by chavi on 10/15/15.
 */
public class GeoTagImagesFragment extends Fragment {
    private static final int STATE_INIT = 0;
    private static final int STATE_GEOTAGGING = 1;
    private static final int STATE_DONE_GEOTAGGING = 2;

    private GeoTagActivity activity;
    private LocalBroadcastManager lbm;

    private TextView instructionText;
    private Button geotagButton;
    private ProgressBar progressBar;
    private ImageView checkImage;
    private ImageView sdCard;
    private ImageView phone;

    private Animation sdCardAnim;

    private ArrayList<File> files;

    private static final IntentFilter filter = new IntentFilter();
    static {
        filter.addAction(GeoTagImagesService.STATE_FINISHED_GEOTAGGING);
        filter.addAction(GeoTagImagesService.STATE_PROGRESS_UPDATE_GEOTAGGING);
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case GeoTagImagesService.STATE_FINISHED_GEOTAGGING:
                    finishedGeotagging(intent);
                    break;
                case GeoTagImagesService.STATE_PROGRESS_UPDATE_GEOTAGGING:
                    int total = intent.getIntExtra(GeoTagImagesService.EXTRA_TOTAL, 0);
                    int progress = intent.getIntExtra(GeoTagImagesService.EXTRA_PROGRESS, 0);
                    updateState(STATE_GEOTAGGING);
                    progressBar.setMax(total);
                    progressBar.setProgress(progress);
                    break;
            }
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (!(context instanceof GeoTagActivity)) {
            throw new UnsupportedOperationException("Activity is not instance of GeoTagActivity");
        }

        activity = (GeoTagActivity) context;
        lbm = LocalBroadcastManager.getInstance(context);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_geotag_images, container, false);
    }

    int currState = STATE_INIT;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        instructionText = (TextView) view.findViewById(R.id.instruction_text);
        geotagButton = (Button) view.findViewById(R.id.geotag_button);
        progressBar = (ProgressBar) view.findViewById(R.id.geotag_progress);
        checkImage = (ImageView) view.findViewById(R.id.check_image);
        sdCard = (ImageView) view.findViewById(R.id.sd_card);
        phone = (ImageView) view.findViewById(R.id.phone);

        instructionText.setText(R.string.insert_sdcard);
        geotagButton.setText(R.string.label_begin);

        sdCardAnim = AnimationUtils.loadAnimation(getContext(), R.anim.sd_card_anim);

        updateState(STATE_INIT);

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
                    case STATE_DONE_GEOTAGGING:
                        if (activity != null) {
                            activity.finishedGeotagging(files);
                        }
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
        Intent intent = new Intent(context, GeoTagImagesService.class);
        intent.setAction(GeoTagImagesService.ACTION_START_GEOTAGGING);
        getContext().startService(intent);
    }

    private void cancelGeoTagging() {
        Context context = getContext();
        Intent intent = new Intent(context, GeoTagImagesService.class);
        intent.setAction(GeoTagImagesService.ACTION_CANCEL_GEOTAGGING);
        getContext().startService(intent);
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
                break;
            case STATE_DONE_GEOTAGGING:
                geotagButton.setActivated(true);
                geotagButton.setText(R.string.button_setup_next);
                checkImage.setVisibility(View.VISIBLE);
                sdCard.setVisibility(View.GONE);
                phone.setVisibility(View.GONE);
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
            files = (ArrayList<File>)intent.getSerializableExtra(GeoTagImagesService.EXTRA_GEOTAGGED_FILES);
            updateState(STATE_DONE_GEOTAGGING);
            instructionText.setText(String.format(getString(R.string.photos_geotagged), files.size()));
        } else {
            String failure = intent.getStringExtra(GeoTagImagesService.EXTRA_FAILURE_MESSAGE);
            failedLoading(failure);
        }
    }
}