package org.droidplanner.android.fragments.calibration.mag;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeEventExtra;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.State;

import org.droidplanner.android.R;
import org.droidplanner.android.fragments.helpers.ApiListenerFragment;
import org.droidplanner.android.utils.Point3D;
import org.droidplanner.android.widgets.scatterplot.ScatterPlot;

import java.util.Arrays;

public class FragmentSetupMAG extends ApiListenerFragment {

    private static final int MIN_POINTS_COUNT = 250;

	private static final int CALIBRATION_IDLE = 0;
	private static final int CALIBRATION_IN_PROGRESS = 1;
	private static final int CALIBRATION_COMPLETED = 2;

	private static final String EXTRA_CALIBRATION_STATUS = "extra_calibration_status";
	private static final String EXTRA_CALIBRATION_POINTS = "extra_calibration_points";

	private static final IntentFilter intentFilter = new IntentFilter();
	static {
		intentFilter.addAction(AttributeEvent.STATE_CONNECTED);
		intentFilter.addAction(AttributeEvent.STATE_DISCONNECTED);
        intentFilter.addAction(AttributeEvent.CALIBRATION_MAG_STARTED);
        intentFilter.addAction(AttributeEvent.CALIBRATION_MAG_ESTIMATION);
        intentFilter.addAction(AttributeEvent.CALIBRATION_MAG_COMPLETED);
	}

	private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (AttributeEvent.STATE_CONNECTED.equals(action)) {
				buttonStep.setEnabled(true);
			}
            else if (AttributeEvent.STATE_DISCONNECTED.equals(action)) {
				cancelCalibration();
				buttonStep.setEnabled(false);
			}
            else if(AttributeEvent.CALIBRATION_MAG_STARTED.equals(action)){
                double[] pointsX = intent.getDoubleArrayExtra(AttributeEventExtra.EXTRA_CALIBRATION_MAG_POINTS_X);
                double[] pointsY = intent.getDoubleArrayExtra(AttributeEventExtra.EXTRA_CALIBRATION_MAG_POINTS_Y);
                double[] pointsZ = intent.getDoubleArrayExtra(AttributeEventExtra.EXTRA_CALIBRATION_MAG_POINTS_Z);

                inProgressPoints = Point3D.fromDoubleArrays(pointsX, pointsY, pointsZ);

                setCalibrationStatus(CALIBRATION_IN_PROGRESS);
            }
            else if(AttributeEvent.CALIBRATION_MAG_ESTIMATION.equals(action)){
                double[] pointsX = intent.getDoubleArrayExtra(AttributeEventExtra.EXTRA_CALIBRATION_MAG_POINTS_X);
                double[] pointsY = intent.getDoubleArrayExtra(AttributeEventExtra.EXTRA_CALIBRATION_MAG_POINTS_Y);
                double[] pointsZ = intent.getDoubleArrayExtra(AttributeEventExtra.EXTRA_CALIBRATION_MAG_POINTS_Z);

                inProgressPoints = Point3D.fromDoubleArrays(pointsX, pointsY, pointsZ);

                final int pointsCount = inProgressPoints == null ? 0 : inProgressPoints.length;
                if (pointsCount == 0) {
                    return;
                }

                final double fitness = intent.getDoubleExtra(AttributeEventExtra.EXTRA_CALIBRATION_MAG_FITNESS,
                        0);
                final double[] fitCenter = intent.getDoubleArrayExtra(AttributeEventExtra
                        .EXTRA_CALIBRATION_MAG_FIT_CENTER);
                final double[] fitRadii = intent.getDoubleArrayExtra(AttributeEventExtra
                        .EXTRA_CALIBRATION_MAG_FIT_RADII);

                if (pointsCount < MIN_POINTS_COUNT) {
                    calibrationFitness.setIndeterminate(true);
                    calibrationProgress.setText("0 / 100");
                } else {
                    final int progress = (int) (fitness * 100);
                    calibrationFitness.setIndeterminate(false);
                    calibrationFitness.setMax(100);
                    calibrationFitness.setProgress(progress);

                    calibrationProgress.setText(progress + " / 100");
                }

                // Grab the last point
                final Point3D point = inProgressPoints[pointsCount - 1];

                plot1.addData((float) point.x);
                plot1.addData((float) point.z);
                if (fitCenter == null || fitRadii == null) {
                    plot1.updateSphere(null);
                } else {
                    plot1.updateSphere(new int[] { (int) fitCenter[0],  (int) fitCenter[2],
                            (int) fitRadii[0],  (int) fitRadii[2] });
                }
                plot1.invalidate();

                plot2.addData((float) point.y);
                plot2.addData((float) point.z);
                if (fitCenter == null || fitRadii == null) {
                    plot2.updateSphere(null);
                } else {
                    plot2.updateSphere(new int[] { (int) fitCenter[1],  (int) fitCenter[2],
                            (int) fitRadii[1],  (int) fitRadii[2]  });
                }
                plot2.invalidate();

            }
            else if(AttributeEvent.CALIBRATION_MAG_COMPLETED.equals(action)){
                double[] offsets = intent.getDoubleArrayExtra(AttributeEventExtra.EXTRA_CALIBRATION_MAG_OFFSETS);
                if(offsets != null) {
                    String offsetsSummary = Arrays.toString(offsets);
                    Log.d("MAG", "Calibration Finished: " + offsetsSummary);
                    Toast.makeText(getActivity(), "Calibration Finished: " + offsetsSummary,
                            Toast.LENGTH_LONG).show();
                }

                setCalibrationStatus(CALIBRATION_COMPLETED);
            }
		}
	};

	private View inProgressCalibrationView;
	private Button buttonStep;
	private TextView calibrationProgress;
	private ProgressBar calibrationFitness;
	private ScatterPlot plot1, plot2;

	private int calibrationStatus = CALIBRATION_IDLE;

    private Point3D[] startPoints;
    private Point3D[] inProgressPoints;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_setup_mag_main, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		plot1 = (ScatterPlot) view.findViewById(R.id.scatterPlot1);
		plot1.setTitle("XZ");

		plot2 = (ScatterPlot) view.findViewById(R.id.scatterPlot2);
		plot2.setTitle("YZ");

		inProgressCalibrationView = view.findViewById(R.id.in_progress_calibration_container);

		calibrationProgress = (TextView) view.findViewById(R.id.calibration_progress);

		buttonStep = (Button) view.findViewById(R.id.buttonStep);
		buttonStep.setEnabled(false);
		buttonStep.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (calibrationStatus == CALIBRATION_COMPLETED) {
					// Clear the screen.
					clearScreen();
					setCalibrationStatus(CALIBRATION_IDLE);
				} else {
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

		if (savedInstanceState != null) {
			final int calibrationStatus = savedInstanceState.getInt(EXTRA_CALIBRATION_STATUS,
					CALIBRATION_IDLE);
			setCalibrationStatus(calibrationStatus);

			if (calibrationStatus == CALIBRATION_IN_PROGRESS) {
				final Point3D[] loadedPoints = (Point3D[]) savedInstanceState
						.getParcelableArray(EXTRA_CALIBRATION_POINTS);
				if (loadedPoints != null && loadedPoints.length > 0) {
					startPoints = loadedPoints;

					for (Point3D point : loadedPoints) {
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
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt(EXTRA_CALIBRATION_STATUS, calibrationStatus);

		if (getDrone().isConnected() && inProgressPoints != null && inProgressPoints.length > 0){
				outState.putParcelableArray(EXTRA_CALIBRATION_POINTS, inProgressPoints);
		}
	}

	private void pauseCalibration() {
		if (getDrone().isConnected()) {
			getDrone().stopMagnetometerCalibration();
		}
	}

	private void cancelCalibration() {
        if (getDrone().isConnected()) {
            getDrone().stopMagnetometerCalibration();
			if (calibrationStatus == CALIBRATION_IN_PROGRESS) {
				setCalibrationStatus(CALIBRATION_IDLE);
			}
		}
		clearScreen();
	}

	private void clearScreen() {
		plot1.reset();
		plot2.reset();
	}

	private void setCalibrationStatus(int status) {
		if (calibrationStatus == status) {
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
		Drone dpApi = getDrone();
		if (dpApi.isConnected()) {
            double[][] result = Point3D.fromPoint3Ds(startPoints);
            dpApi.startMagnetometerCalibration(result[0], result[1], result[2]);
			startPoints = null;
		}
	}

	public static CharSequence getTitle(Context context) {
		return context.getText(R.string.setup_mag_title);
	}

	@Override
	public void onApiConnected() {
        Drone drone = getDrone();
        State droneState = drone.getAttribute(AttributeType.STATE);
		if (droneState.isConnected() && !droneState.isFlying()) {
			buttonStep.setEnabled(true);
		} else {
			cancelCalibration();
			buttonStep.setEnabled(false);
		}

		getBroadcastManager().registerReceiver(broadcastReceiver, intentFilter);
		if (calibrationStatus == CALIBRATION_IN_PROGRESS) {
			startCalibration();
		}
	}

	@Override
	public void onApiDisconnected() {
		getBroadcastManager().unregisterReceiver(broadcastReceiver);
		pauseCalibration();
	}
}
