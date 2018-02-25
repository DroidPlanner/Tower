package co.aerobotics.android.dialogs;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import co.aerobotics.android.view.SlideButton;
import com.facebook.shimmer.ShimmerFrameLayout;

import co.aerobotics.android.R;

/**
 * Created by Fredia Huya-Kouadio on 4/6/15.
 */
public class SlideToUnlockDialog extends DialogFragment implements SeekBar.OnSeekBarChangeListener{

    public static final String EXTRA_UNLOCK_ACTION = "extra_unlock_action";

    public static SlideToUnlockDialog newInstance(String unlockDescription, final Runnable unlockAction){
        final SlideToUnlockDialog unlockDialog = new SlideToUnlockDialog();

        Bundle args = new Bundle();
        args.putString(SlideToUnlockDialog.EXTRA_UNLOCK_ACTION, unlockDescription);
        unlockDialog.setArguments(args);
        unlockDialog.unlockAction = unlockAction;
        return unlockDialog;
    }

    private TextView sliderText;
    private ShimmerFrameLayout shimmerContainer;
    private Runnable unlockAction;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.CustomDialogTheme);
        setCancelable(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        return inflater.inflate(R.layout.dialog_slide_to_unlock, container, false);
    }

    @Override
    public void onStart(){
        super.onStart();
        getDialog().setCanceledOnTouchOutside(true);
    }

    @Override
    public void onPause(){
        super.onPause();
        dismiss();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        final Bundle args = getArguments();
        String unlockAction = "unlock";
        if(args != null){
            unlockAction = args.getString(EXTRA_UNLOCK_ACTION, unlockAction);
        }

        sliderText = (TextView) view.findViewById(R.id.slider_text);
        sliderText.setText(R.string.unlock_slider_description);

        final SlideButton slideButton = (SlideButton) view.findViewById(R.id.unlock_slider);
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
        sliderText.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (seekBar.getProgress() < 80) {
            seekBar.setProgress(0);
            sliderText.setVisibility(View.VISIBLE);
            shimmerContainer.startShimmerAnimation();
        } else {
            seekBar.setProgress(100);
            handleSlide();
        }
    }

    private void handleSlide() {
        onSliderUnlocked();
        dismiss();
    }

    public void onSliderUnlocked(){
        if(unlockAction != null)
            unlockAction.run();
    }
}
