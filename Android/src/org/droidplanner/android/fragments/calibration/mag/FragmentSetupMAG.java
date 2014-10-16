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
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.drone.variables.helpers.MagnetometerCalibration;
import org.droidplanner.core.model.Drone;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import ellipsoidFit.FitPoints;
import ellipsoidFit.ThreeSpacePoint;

public class FragmentSetupMAG extends SetupMainPanel implements MagnetometerCalibration.OnMagCalibrationListener {

	private ScatterPlot plot1,plot2;
	
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
		MavLinkStreamRates.setupStreamRates(drone.getMavClient(), 0, 0, 0, 0, 0, 0, 30, 0);

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
	}
}
