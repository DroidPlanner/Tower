package org.droidplanner.android.fragments.calibration.mag;

import java.util.ArrayList;
import java.util.List;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.lib.parcelables.ParcelableThreeSpacePoint;
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
import android.widget.TextView;
import android.widget.Toast;
import ellipsoidFit.FitPoints;
import ellipsoidFit.ThreeSpacePoint;

public class FragmentSetupMAG extends Fragment implements MagnetometerCalibration
        .OnMagCalibrationListener, DroneInterfaces.OnDroneListener {

    private static final int CALIBRATION_IDLE = 0;
    private static final int CALIBRATION_IN_PROGRESS = 1;
    private static final int CALIBRATION_COMPLETED = 2;

    private static final String EXTRA_CALIBRATION_STATUS = "extra_calibration_status";
    private static final String EXTRA_CALIBRATION_POINTS = "extra_calibration_points";

    private View inProgressCalibrationView;
    private Button buttonStep;
    private TextView calibrationProgress;
    private ProgressBar calibrationFitness;
	private ScatterPlot plot1,plot2;

    private int calibrationStatus = CALIBRATION_IDLE;

	private Drone drone;
	private MagnetometerCalibration calibration;

    private List<? extends ThreeSpacePoint> startPoints;

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

        calibrationProgress = (TextView) view.findViewById(R.id.calibration_progress);

        buttonStep = (Button) view.findViewById(R.id.buttonStep);
        buttonStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(calibrationStatus == CALIBRATION_COMPLETED){
                    //Clear the screen.
                    clearScreen();
                    setCalibrationStatus(CALIBRATION_IDLE);
                }
                else {
                    startCalibration();
                }
            }
        });

        Button buttonCancel = (Button) view.findViewById(R.id.buttonCancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelCalibration();
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

        if(savedInstanceState != null){
            final int calibrationStatus = savedInstanceState.getInt(EXTRA_CALIBRATION_STATUS,
                    CALIBRATION_IDLE);
            setCalibrationStatus(calibrationStatus);

            if(calibrationStatus == CALIBRATION_IN_PROGRESS){
                final ArrayList<ParcelableThreeSpacePoint> loadedPoints = savedInstanceState
                        .getParcelableArrayList(EXTRA_CALIBRATION_POINTS);
                if(loadedPoints != null && !loadedPoints.isEmpty()){
                    startPoints = loadedPoints;

                    for(ParcelableThreeSpacePoint point: loadedPoints){
                        final double x = point.x;
                        final double y = point.y;
                        final double z = point.z;

                        plot1.addData((float) x);
                        plot1.addData((float) z);

                        plot2.addData((float) y);
                        plot2.addData((float) z);
                    }

                    plot1.invalidate();
                    plot2.invalidate();
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);

        outState.putInt(EXTRA_CALIBRATION_STATUS, calibrationStatus);

        if(calibration != null) {
            final List<ThreeSpacePoint> fitPoints = calibration.getPoints();
            final int pointsCount = fitPoints.size();
            if(pointsCount > 0) {
                final ArrayList<ParcelableThreeSpacePoint> savedPoints = new
                        ArrayList<ParcelableThreeSpacePoint>(pointsCount);
                for(ThreeSpacePoint point : fitPoints){
                    savedPoints.add(new ParcelableThreeSpacePoint(point));
                }

                outState.putParcelableArrayList(EXTRA_CALIBRATION_POINTS, savedPoints);
            }
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        if(drone.getMavClient().isConnected() && !drone.getState().isFlying()){
            buttonStep.setEnabled(true);
        }
        else{
            cancelCalibration();
            buttonStep.setEnabled(false);
        }

        drone.addDroneListener(this);
        if(calibrationStatus == CALIBRATION_IN_PROGRESS){
            startCalibration();
        }
    }

    @Override
    public void onStop(){
        super.onStop();
        drone.removeDroneListener(this);
        pauseCalibration();
    }

    private void pauseCalibration(){
        if(calibration != null){
            calibration.stop();
        }
    }

    private void cancelCalibration() {
        if(calibration != null){
            calibration.stop();
            if(calibrationStatus == CALIBRATION_IN_PROGRESS){
                setCalibrationStatus(CALIBRATION_IDLE);
            }
        }
        clearScreen();
    }

    private void clearScreen(){
        plot1.reset();
        plot2.reset();
    }

	private void setCalibrationStatus(int status) {
        if(calibrationStatus == status){
            return;
        }

		calibrationStatus = status;
		switch (calibrationStatus) {
		case CALIBRATION_IN_PROGRESS:
			// Hide the 'start' button
			buttonStep.setVisibility(View.GONE);

			// Show the 'in progress view'
			inProgressCalibrationView.setVisibility(View.VISIBLE);
			calibrationFitness.setIndeterminate(true);
            calibrationProgress.setText("0 / 100");
			break;

		case CALIBRATION_COMPLETED:
			calibrationFitness.setIndeterminate(false);
			calibrationFitness.setMax(100);
			calibrationFitness.setProgress(100);

			// Hide the 'in progress view'
			inProgressCalibrationView.setVisibility(View.GONE);

			// Show the 'calibrate/done' button
			buttonStep.setVisibility(View.VISIBLE);
			buttonStep.setText(R.string.button_setup_done);
			break;

		default:
			// Hide the 'in progress view'
			inProgressCalibrationView.setVisibility(View.GONE);

			// Show the 'calibrate/done' button
			buttonStep.setVisibility(View.VISIBLE);
			buttonStep.setText(R.string.button_setup_calibrate);
			break;
		}
	}

    public void startCalibration() {
		if(calibration != null){
			if (drone.getMagnetometer().getOffsets()==null) {
				Toast.makeText(getActivity()," Please load the parameters before calibrating", Toast.LENGTH_LONG).show();
			}
			
            calibration.start(startPoints);
            startPoints = null;

            setCalibrationStatus(CALIBRATION_IN_PROGRESS);
        }
	}

    @Override
    public void newEstimation(FitPoints ellipsoidFit, List<ThreeSpacePoint> points){
        final int pointsCount = points.size();
        if(pointsCount == 0) {
            return;
        }

        if(pointsCount < MagnetometerCalibration.MIN_POINTS_COUNT){
            calibrationFitness.setIndeterminate(true);
            calibrationProgress.setText("0 / 100");
        }
        else {
            final int progress = (int) (ellipsoidFit.getFitness() * 100);
            calibrationFitness.setIndeterminate(false);
            calibrationFitness.setMax(100);
            calibrationFitness.setProgress(progress);

            calibrationProgress.setText(progress + " / 100");
        }

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
		Log.d("MAG", "Calibration Finished: "+fit.center.toString());
		Toast.makeText(getActivity(), "Calibration Finished: "+ fit.center.toString(),Toast.LENGTH_LONG).show();

		try {
			calibration.sendOffsets();
		} catch (Exception e) {
			e.printStackTrace();
		}
        drone.getStreamRates().setupStreamRatesFromPref();

        setCalibrationStatus(CALIBRATION_COMPLETED);
	}

    public static CharSequence getTitle(Context context) {
        return context.getText(R.string.setup_mag_title);
    }

    @Override
    public void onDroneEvent(DroneInterfaces.DroneEventsType event, Drone drone) {
        switch(event){
            case CONNECTED:
                buttonStep.setEnabled(true);
                break;

            case DISCONNECTED:
                cancelCalibration();
                buttonStep.setEnabled(false);
                break;
		default:
			break;
        }
    }
}
