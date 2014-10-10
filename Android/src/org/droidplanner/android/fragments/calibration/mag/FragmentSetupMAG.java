package org.droidplanner.android.fragments.calibration.mag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.fragments.calibration.SetupMainPanel;
import org.droidplanner.android.fragments.calibration.SetupSidePanel;
import org.droidplanner.android.widgets.scatterplot.ScatterPlot;
import org.droidplanner.core.MAVLink.MavLinkStreamRates;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.drone.variables.helpers.MagnetometerCalibration;
import org.droidplanner.core.drone.variables.helpers.MagnetometerCalibration.OnMagCalibrationListner;
import org.droidplanner.core.model.Drone;

import ellipsoidFit.FitPoints;

import android.util.Log;
import android.view.View;

public class FragmentSetupMAG extends SetupMainPanel implements OnDroneListener, OnMagCalibrationListner {

	private ScatterPlot plot1,plot2;
	
	private List<Float> data1 = new ArrayList<Float>();
	private List<Float> data2 = new ArrayList<Float>();

	@Override
	public int getPanelLayout() {
		return R.layout.fragment_setup_mag_main;
	}

	@Override
	public SetupSidePanel getSidePanel() {
		return null;
	}

	@Override
	public void setupLocalViews(View v) {
		plot1 = (ScatterPlot) v.findViewById(R.id.scatterPlot1);
		plot1.setTitle("XY");
		plot2 = (ScatterPlot) v.findViewById(R.id.scatterPlot2);
		plot2.setTitle("YZ");
	}

	@Override
	public void doCalibrationStep(int step) {
		Drone drone = ((DroidPlannerApp) getActivity().getApplication()).getDrone();
		MavLinkStreamRates.setupStreamRates(drone.getMavClient(), 0, 0, 0, 0, 0, 0, 50, 0);
		drone.addDroneListener(this);

		new MagnetometerCalibration(drone,this);
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
		case MAGNETOMETER:
			int[] mag = drone.getMagnetometer().getVector();
			data1.add(mag[0]/1000f);
			data1.add(mag[1]/1000f);
			plot1.newDataSet((Float[]) data1.toArray(new Float[data1.size()]));
			data2.add(mag[1]/1000f);
			data2.add(mag[2]/1000f);
			plot2.newDataSet((Float[]) data2.toArray(new Float[data2.size()]));
			break;
		default:
			break;

		}
	}

	@Override
	public void newEstimation(FitPoints ellipsoidFit, int sampleSize, int[] magVector) {
		Log.d("MAG", String.format("Sample %d\traw %s\tFit %2.1f \tCenter %s\tRadius %s",
				sampleSize, Arrays.toString(magVector), ellipsoidFit.getFitness() * 100,
				ellipsoidFit.center.toString(), ellipsoidFit.radii.toString()));
		
	}

	@Override
	public void finished(FitPoints fit) {
		Log.d("MAG", "############################################################################################");
		
	}
}
