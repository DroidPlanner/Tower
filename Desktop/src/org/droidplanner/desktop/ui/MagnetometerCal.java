package org.droidplanner.desktop.ui;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.droidplanner.core.drone.variables.helpers.MagnetometerCalibration;
import org.droidplanner.core.drone.variables.helpers.MagnetometerCalibration.OnMagCalibrationListener;
import org.droidplanner.desktop.logic.Handler;
import org.droidplanner.desktop.ui.widgets.GraphPanel;

import ellipsoidFit.FitPoints;
import ellipsoidFit.ThreeSpacePoint;

public class MagnetometerCal implements OnMagCalibrationListener {
	private static final int Y_SIZE = 600;
	private static final int X_SIZE = 600;
	private static MagnetometerCalibration cal;
	private List<Float> data1 = new ArrayList<Float>(), data2 = new ArrayList<Float>();
	public GraphPanel graph;
	private ScatterPlot plot1, plot2;

	public MagnetometerCal() {

		JFrame frame = new JFrame("Parameters");
		frame.setLayout(new BorderLayout());
		plot1 = new ScatterPlot("XZ");
		plot1.setPreferredSize(new Dimension(X_SIZE / 2, Y_SIZE / 2));
		plot2 = new ScatterPlot("YZ");
		plot2.setPreferredSize(new Dimension(X_SIZE / 2, Y_SIZE / 2));
		frame.setPreferredSize(new Dimension(X_SIZE, Y_SIZE));
		frame.add(plot1, BorderLayout.EAST);
		frame.add(plot2);
		frame.pack();
		frame.setVisible(true);
	}

	static void create(org.droidplanner.core.model.Drone drone) {
		MagnetometerCal window = new MagnetometerCal();
		cal = new MagnetometerCalibration(drone, window, new Handler());
		cal.start(null);
	}

	@Override
	public void newEstimation(FitPoints ellipsoidFit, List<ThreeSpacePoint> points) {
        final int sampleSize = points.size();
        final ThreeSpacePoint magVector = points.get(sampleSize - 1);
        
		System.out.println(String.format("Sample %d\traw %03d %03d %03d\tFit %2.1f \tCenter %s\tRadius %s",
				sampleSize, (int) magVector.x, (int) magVector.y, (int) magVector.z, ellipsoidFit.getFitness() * 100,
				ellipsoidFit.center.toString(), ellipsoidFit.radii.toString()));
		
		data1.add((float) magVector.x);
		data1.add((float) magVector.z);
		plot1.newDataSet((Float[]) data1.toArray(new Float[data1.size()]));
		if (ellipsoidFit.center.isNaN() || ellipsoidFit.radii.isNaN()) {
			plot1.updateSphere(null);
		} else {
			plot1.updateSphere(new int[] { (int) ellipsoidFit.center.getEntry(0),
					(int) ellipsoidFit.center.getEntry(2), (int) ellipsoidFit.radii.getEntry(0),
					(int) ellipsoidFit.radii.getEntry(2) });
		}

		data2.add((float) magVector.y);
		data2.add((float) magVector.z);
		plot2.newDataSet((Float[]) data2.toArray(new Float[data2.size()]));
		if (ellipsoidFit.center.isNaN() || ellipsoidFit.radii.isNaN()) {
			plot2.updateSphere(null);
		} else {
			plot2.updateSphere(new int[] { (int) ellipsoidFit.center.getEntry(1),
					(int) ellipsoidFit.center.getEntry(2), (int) ellipsoidFit.radii.getEntry(1),
					(int) ellipsoidFit.radii.getEntry(2) });
		}
		
		plot1.repaint(100);
		plot2.repaint(100);

	}

	@Override
	public void finished(FitPoints fit) {
		try {
			cal.sendOffsets();
		} catch (Exception e) {
			e.printStackTrace();
		}
		cal.stop();
		System.out.println("Calibration Finished: "+fit.center.toString());
	}

	public class ScatterPlot extends Canvas {
		private static final long serialVersionUID = 1L;

		private static final float SCALE_FACTOR = 1 / 1000f;

		private Float[] points = new Float[] {};

		private int[] sphere = null;

		private int halfWidth, halfHeight, halfScale;

		private String title;

		public ScatterPlot(String title){
			this.title = title;
		}
		
		public void newDataSet(Float[] array) {
			points = array;
		}

		public void updateSphere(int[] sphere) {
			this.sphere = sphere;
		}
		
		@Override
		public void paint(Graphics canvas) {

			halfWidth = this.getWidth() / 2;
			halfHeight = this.getHeight() / 2;
			halfScale = (halfHeight > halfWidth) ? halfWidth : halfHeight;

			canvas.drawString(title, 0, 0);

			// Draw the graph lines
			canvas.drawLine(halfWidth, 0, halfWidth, halfHeight * 2);
			canvas.drawLine(0, halfHeight, halfWidth * 2, halfHeight);

			// Draw the points
			int x = 0, y = 0;
			for (int i = 0; i < points.length; i += 2) {
				x = mapToImgX(points[i + 0]);
				y = mapToImgY(points[i + 1]);
				canvas.drawOval(x, y, 2, 2);
			}
			canvas.drawOval(x - 5 / 2, y - 5 / 2, 5, 5);

			// Draw the estimated Sphere
			if (sphere != null) {
				x = mapToImgX(sphere[0]);
				y = mapToImgY(sphere[1]);
				int width = (int) scale(sphere[2]);
				int height = (int) scale(sphere[3]);
				canvas.drawOval(x - width, y - height, width * 2, height * 2);
			}
		}

		private int mapToImgX(float coord) {
			return (int) (scale(coord) + halfWidth);
		}

		private int mapToImgY(float coord) {
			return (int) (-scale(coord) + halfHeight);
		}

		private float scale(float value) {
			return SCALE_FACTOR * halfScale * value;
		}

	}

}