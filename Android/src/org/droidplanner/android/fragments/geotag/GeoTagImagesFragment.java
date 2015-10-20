package org.droidplanner.android.fragments.geotag;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.o3dr.android.client.utils.data.tlog.TLogParser;
import com.o3dr.android.client.utils.data.tlog.TLogParserCallback;
import com.o3dr.android.client.utils.data.tlog.TLogParserFilter;
import com.o3dr.android.client.utils.geotag.GeoTagAsyncTask;

import org.droidplanner.android.R;
import org.droidplanner.android.activities.GeoTagActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

/**
 * Created by chavi on 10/15/15.
 */
public class GeoTagImagesFragment extends Fragment {
    private static final int STATE_INIT = 0;
    private static final int STATE_GEOTAGGING = 1;
    private static final int STATE_DONE_GEOTAGGING = 2;

    private GeoTagActivity activity;
    private GeoTagTask geoTagTask;
    private TextView instructionText;
    private Button geotagButton;
    private ProgressBar progressBar;
    private ImageView checkImage;
    private ImageView sdCard;
    private ImageView phone;

    private Animation sdCardAnim;
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (!(context instanceof GeoTagActivity)) {
            throw new UnsupportedOperationException("Activity is not instance of GeoTagActivity");
        }

        this.activity = (GeoTagActivity) context;
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
                        activity.finishedGeotagging();
                        break;
                }
            }
        });

        if (activity != null) {
            activity.updateTitle(R.string.geo_tag_label);
        }
    }

    private void startGeoTagging() {
        File folder = getContext().getExternalFilesDir(null);
        File tlogFile = new File(folder.getPath() + "/camera_msgs.tlog");
        Timber.d("path: " + tlogFile.getPath());

        geoTagImages(getContext(), tlogFile);
    }

    private void cancelGeoTagging() {
        geoTagTask.cancel(true);
        geoTagTask = null;
    }

    private void updateState(int state) {
        currState = state;
        switch (currState) {
            case STATE_INIT:
                geotagButton.setActivated(false);
                currState = STATE_INIT;
                geotagButton.setText(R.string.label_begin);
                sdCard.clearAnimation();
                break;
            case STATE_GEOTAGGING:
                geotagButton.setActivated(true);
                instructionText.setText(R.string.geotagging);
                geotagButton.setText(R.string.button_setup_cancel);
                sdCard.startAnimation(sdCardAnim);
                break;
            case STATE_DONE_GEOTAGGING:
                geotagButton.setActivated(true);
                geotagButton.setText(R.string.button_setup_done);
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

    private void geoTagImages(final Context context, File tlogFile) {
        final String extMount = getExternalStorage(context);
        if (extMount == null) {
            return;
        }

        final ArrayList<File> photoFiles = new ArrayList<>();

        List<File> photos = searchDir(extMount);
        if (photos != null) {
            photoFiles.addAll(photos);
        }

        if (photoFiles.size() == 0) {
            failedLoading("No photos on SD card for GoPro device.");
            return;
        }

        Uri uri = Uri.fromFile(tlogFile);
        Handler handler = new Handler();
        TLogParser.getAllEventsAsync(handler, uri, new TLogParserFilter() {

            @Override
            public boolean includeEvent(TLogParser.Event event) {
                return 180 == event.getMavLinkMessage().msgid;
            }

            @Override
            public boolean shouldIterate() {
                return true;
            }


        }, new TLogParserCallback() {
            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void onResult(List<TLogParser.Event> eventList) {
                if (eventList.size() < 0) {
                    failedLoading("No camera message events found");
                    return;
                }

                if (geoTagTask != null) {
                    geoTagTask.cancel(true);
                }
                geoTagTask = new GeoTagTask(context, eventList, photoFiles);
                geoTagTask.execute();
            }

            @Override
            public void onFailed(Exception e) {
                failedLoading(e.getMessage());
            }
        });

    }

    private String getExternalStorage(Context context) {
        boolean hasNullFile = false;

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            File[] files = context.getExternalFilesDirs(null);
            for (File extFile : files) {
                if (extFile == null) {
                    hasNullFile = true;
                } else if (Environment.isExternalStorageRemovable(extFile)) {
                    return findRootPath(extFile);
                }
            }
        }
        if (hasNullFile) {
            failedLoading("No external storage device found.");
        } else {
            failedLoading("Incompatible device. No external SD card reader found.");
        }
        return null;
    }

    private static String findRootPath(File extFile) {
        File currPath = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            currPath = extFile;
            try {
                while (Environment.isExternalStorageRemovable(currPath.getParentFile())) {
                    currPath = currPath.getParentFile();
                }
            } catch (IllegalArgumentException e) {
                //swallow
                return currPath.getAbsolutePath();
            }
        }
        return currPath.getAbsolutePath();
    }

    private static List<File> searchDir(String mount) {
        File photoDir = new File(mount + "/DCIM");
        File[] goProDirs = photoDir.listFiles();
        if (goProDirs == null || goProDirs.length == 0) {
            return null;
        }

        List<File> photoFiles = new ArrayList<>();

        for (File picDir : goProDirs) {
            if (picDir.getName().toLowerCase().contains("gopro")) {
                photoFiles.addAll(Arrays.asList(picDir.listFiles()));
            }
        }

        return photoFiles;
    }

    private class GeoTagTask extends GeoTagAsyncTask {

        public GeoTagTask(Context context, List<TLogParser.Event> events, ArrayList<File> photos) {
            super(context, events, photos);
        }

        @Override
        public void onResult(HashMap<File, File> geotaggedFiles, HashMap<File, Exception> failedFiles) {
            updateState(STATE_DONE_GEOTAGGING);
            instructionText.setText(String.format(getString(R.string.photos_geootagged), geotaggedFiles.size()));
            geoTagTask = null;
        }

        @Override
        public void onProgress(int numProcessed, int numTotal) {
            progressBar.setMax(numTotal);
            progressBar.setProgress(numProcessed);
        }

        @Override
        public void onFailed(Exception e) {
            failedLoading(e.getMessage());
            geoTagTask = null;
        }
    }
}