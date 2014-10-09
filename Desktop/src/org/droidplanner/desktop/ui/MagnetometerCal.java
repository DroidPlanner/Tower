package org.droidplanner.desktop.ui;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;

import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.drone.variables.helpers.MagnetometerCalibration;
import org.droidplanner.core.drone.variables.helpers.MagnetometerCalibration.OnMagCalibrationListner;
import org.droidplanner.core.model.Drone;
import org.droidplanner.desktop.ui.widgets.GraphPanel;

import ellipsoidFit.FitPoints;

public class MagnetometerCal implements OnDroneListener, OnMagCalibrationListner {
	private static final int Y_SIZE = 600;
	private static final int X_SIZE = 600;
	private List<Float> data1 = new ArrayList<Float>();
	public GraphPanel graph;
	private ScatterPlot plot1;

	public MagnetometerCal() {

		JFrame frame = new JFrame("Parameters");
		plot1 = new ScatterPlot();
		frame.setPreferredSize(new Dimension(X_SIZE, Y_SIZE));
		frame.add(plot1);
		frame.pack();
		frame.setVisible(true);
	}

	static void create(org.droidplanner.core.model.Drone drone) {
		MagnetometerCal window = new MagnetometerCal();
		drone.addDroneListener(window);

		new MagnetometerCalibration(drone,window);
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
		case MAGNETOMETER:
			int[] mag = drone.getMagnetometer().getVector();
			data1.add(mag[0]/1000f);
			data1.add(mag[1]/1000f);
			plot1.newDataSet((Float[]) data1.toArray(new Float[data1.size()]));
			break;
		default:
			break;
		}
	}

	@Override
	public void newEstimation(FitPoints ellipsoidFit,int sampleSize, int[] magVector) {
		System.out.println(String.format("Sample %d\traw %s\tFit %2.1f \tCenter %s\tRadius %s",sampleSize, Arrays.toString(magVector),ellipsoidFit.getFitness()*100,ellipsoidFit.center.toString(), ellipsoidFit.radii.toString()));
		
	}

	@Override
	public void finished(FitPoints fit) {
		System.err.println("####################################################################################################################################\n");
	}

	public class ScatterPlot extends Canvas {
		private static final long serialVersionUID = 1L;
	
		private Float[] points = new Float[] {};
	
		public void newDataSet(Float[] array) {
			points = array;
			repaint();
		}
		
		@Override
		public void paint(Graphics canvas) {
			canvas.drawString("XX", 0, 0);
	
			int halfWidth = X_SIZE / 2;
			int halfHeight = Y_SIZE / 2;
			int halfScale = (halfHeight > halfWidth) ? halfWidth : halfHeight;
	
			// Draw the graph lines
			canvas.drawLine(halfWidth, 0, halfWidth, halfHeight * 2);
			canvas.drawLine(0, halfHeight, halfWidth * 2, halfHeight);
	
			// Draw the points
			int x = 0, y = 0;
			for (int i = 0; i < points.length; i += 2) {
				x = (int) (halfScale * points[i + 0] + halfWidth);
				y = (int) (-halfScale * points[i + 1] + halfHeight);
				canvas.drawArc(x, y,1,1,0,360);
			}
			canvas.drawArc(x, y,5,5,0,360);
			
			// Draw the estimated Sphere
			//canvas.drawArc(x, y,5,5,0,360);
			
		}
	
	}

}