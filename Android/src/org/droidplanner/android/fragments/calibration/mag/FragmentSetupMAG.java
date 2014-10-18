package org.droidplanner.android.fragments.calibration.mag;

import java.util.List;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.widgets.scatterplot.ScatterPlot;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.drone.variables.helpers.MagnetometerCalibration;
import org.droidplanner.core.model.Drone;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import ellipsoidFit.FitPoints;
import ellipsoidFit.ThreeSpacePoint;

public class FragmentSetupMAG extends Fragment implements MagnetometerCalibration
        .OnMagCalibrationListener {

    private View inProgressCalibrationView;
    private Button buttonStep;
    private Button buttonCancel;
    private ProgressBar calibrationFitness;
	private ScatterPlot plot1,plot2;

    private boolean isCalibrationComplete;

	private Drone drone;
	private MagnetometerCalibration calibration;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_setup_mag_main, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        plot1 = (ScatterPlot) view.findViewById(R.id.scatterPlot1);
        plot1.setTitle("XZ");

        plot2 = (ScatterPlot) view.findViewById(R.id.scatterPlot2);
        plot2.setTitle("YZ");

        inProgressCalibrationView = view.findViewById(R.id.in_progress_calibration_container);

        buttonStep = (Button) view.findViewById(R.id.buttonStep);
        buttonStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isCalibrationComplete){
                    //Clear the screen.
                    clearScreen();
                }
                else {
                    startCalibration();
                }
            }
        });

        buttonCancel = (Button) view.findViewById(R.id.buttonCancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopCalibration();
            }
        });

        calibrationFitness = (ProgressBar) view.findViewById(R.id.calibration_progress_bar);

        drone = ((DroidPlannerApp) getActivity().getApplication()).getDrone();

        calibration = new MagnetometerCalibration(drone, this, new DroneInterfaces.Handler() {
            private final Handler handler = new Handler();

            @Override
            public void removeCallbacks(Runnable thread) {
                this.handler.removeCallbacks(thread);
            }

            @Override
            public void post(Runnable thread) {
                this.handler.post(thread);
            }

            @Override
            public void postDelayed(Runnable thread, long timeout) {
                this.handler.postDelayed(thread, timeout);
            }
        });
    }

    @Override
    public void onStop(){
        super.onStop();
        stopCalibration();
        setCalibrationStatus(false);
    }

    private void stopCalibration() {
        if(calibration != null){
            calibration.stop();
        }
        clearScreen();
    }

    private void clearScreen(){
        plot1.reset();
        plot2.reset();
    }

    private void setCalibrationStatus(boolean isComplete) {
        isCalibrationComplete = isComplete;
        if(isComplete){
            buttonStep.setText(R.string.button_setup_done);
        }
        else{
            buttonStep.setText(R.string.button_setup_calibrate);
        }
    }

    public void startCalibration() {
		if(calibration != null){
            calibration.start();
        }
	}

    @Override
    public void newEstimation(FitPoints ellipsoidFit, List<ThreeSpacePoint> points){
        final int pointsCount = points.size();
        if(pointsCount == 0)
            return;

        //Grab the last point
        final ThreeSpacePoint point = points.get(pointsCount - 1);

        plot1.addData((float)point.x);
        plot1.addData((float)point.z);
        if (ellipsoidFit.center.isNaN() || ellipsoidFit.radii.isNaN()) {
            plot1.updateSphere(null);
        } else {
            plot1.updateSphere(new int[] { (int) ellipsoidFit.center.getEntry(0),
                    (int) ellipsoidFit.center.getEntry(2), (int) ellipsoidFit.radii.getEntry(0),
                    (int) ellipsoidFit.radii.getEntry(2) });
        }
        plot1.invalidate();

        plot2.addData((float) point.y);
        plot2.addData((float) point.z);
        if (ellipsoidFit.center.isNaN() || ellipsoidFit.radii.isNaN()) {
            plot2.updateSphere(null);
        } else {
            plot2.updateSphere(new int[] { (int) ellipsoidFit.center.getEntry(1),
                    (int) ellipsoidFit.center.getEntry(2), (int) ellipsoidFit.radii.getEntry(1),
                    (int) ellipsoidFit.radii.getEntry(2) });
        }
        plot2.invalidate();
    }

	@Override
	public void finished(FitPoints fit) {
		Log.d("MAG", "############################################################################################");
		Toast.makeText(getActivity(), "Calibration Finished: "+ fit.center.toString(),Toast.LENGTH_LONG).show();
		
		calibration.sendOffsets();
        drone.getStreamRates().setupStreamRatesFromPref();
	}

    public static CharSequence getTitle(Context context) {
        return context.getText(R.string.setup_mag_title);
    }
}
