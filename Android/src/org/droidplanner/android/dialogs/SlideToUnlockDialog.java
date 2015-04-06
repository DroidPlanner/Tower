package org.droidplanner.android.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.facebook.shimmer.ShimmerFrameLayout;

import org.droidplanner.android.R;
import org.droidplanner.android.widgets.SlideButton;
import org.droidplanner.android.widgets.SlideButtonListener;

/**
 * Created by Fredia Huya-Kouadio on 4/6/15.
 */
public class SlideToUnlockDialog extends DialogFragment implements SeekBar.OnSeekBarChangeListener{

    private SlideButtonListener slideListener;

    private ShimmerFrameLayout shimmerContainer;
    private SlideButton slideButton;

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);

        Fragment parentFragment = getParentFragment();
        if(parentFragment != null){
            if(!(parentFragment instanceof SlideButtonListener)){
                throw new IllegalStateException("Parent must implement " + SlideButtonListener.class.getSimpleName());
            }

            slideListener = (SlideButtonListener) parentFragment;
        }
        else {
            if (!(activity instanceof SlideButtonListener)) {
                throw new IllegalStateException("Parent must implement " + SlideButtonListener.class.getSimpleName());
            }

            slideListener = (SlideButtonListener) activity;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, 0);
        setCancelable(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        return inflater.inflate(R.layout.dialog_slide_to_unlock, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        slideButton = (SlideButton) view.findViewById(R.id.unlock_slider);
        slideButton.setSlideButtonListener(slideListener);
        slideButton.setOnSeekBarChangeListener(this);

        shimmerContainer = (ShimmerFrameLayout) view.findViewById(R.id.shimmer_view_container);
        shimmerContainer.startShimmerAnimation();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        shimmerContainer.stopShimmerAnimation();
        shimmerContainer.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (seekBar.getProgress() < 70) {
            seekBar.setProgress(0);

            shimmerContainer.setVisibility(View.VISIBLE);
            shimmerContainer.startShimmerAnimation();
        } else {
            seekBar.setProgress(100);
        }
    }
}
