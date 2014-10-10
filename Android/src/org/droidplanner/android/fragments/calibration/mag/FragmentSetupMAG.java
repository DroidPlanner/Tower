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
import org.droidplanner.core.drone.variables.helpers.MagnetometerCalibration;
import org.droidplanner.core.drone.variables.helpers.MagnetometerCalibration.OnMagCalibrationListner;
import org.droidplanner.core.model.Drone;

import android.util.Log;
import android.view.View;
import android.widget.Toast;
import ellipsoidFit.FitPoints;

public class FragmentSetupMAG extends SetupMainPanel implements OnMagCalibrationListner {

	private ScatterPlot plot1,plot2;
	
	private List<Float> data1 = new ArrayList<Float>();
	private List<Float> data2 = new ArrayList<Float>();

	private Drone drone;

	private MagnetometerCalibration calibration;

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
		plot1.setTitle("XZ");
		plot2 = (ScatterPlot) v.findViewById(R.id.scatterPlot2);
		plot2.setTitle("YZ");
		
		drone = ((DroidPlannerApp) getActivity().getApplication()).getDrone();
	}

	@Override
	public void doCalibrationStep(int step) {
		MavLinkStreamRates.setupStreamRates(drone.getMavClient(), 0, 0, 0, 0, 0, 0, 50, 0);

		calibration = new MagnetometerCalibration(drone,this);
	}

	@Override
	public void newEstimation(FitPoints ellipsoidFit, int sampleSize, int[] magVector) {
		Log.d("MAG", String.format("Sample %d\traw %s\tFit %2.1f \tCenter %s\tRadius %s",
				sampleSize, Arrays.toString(magVector), ellipsoidFit.getFitness() * 100,
				ellipsoidFit.center.toString(), ellipsoidFit.radii.toString()));
		
		data1.add((float) magVector[0]);
		data1.add((float) magVector[2]);
		plot1.newDataSet((Float[]) data1.toArray(new Float[data1.size()]));
		if (ellipsoidFit.center.isNaN() || ellipsoidFit.radii.isNaN()) {
			plot1.updateSphere(null);
		} else {
			plot1.updateSphere(new int[] { (int) ellipsoidFit.center.getEntry(0),
					(int) ellipsoidFit.center.getEntry(2), (int) ellipsoidFit.radii.getEntry(0),
					(int) ellipsoidFit.radii.getEntry(2) });
		}
		plot1.invalidate();
		
		data2.add((float) magVector[1]);
		data2.add((float) magVector[2]);
		plot2.newDataSet((Float[]) data2.toArray(new Float[data2.size()]));
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
		
	}
}
