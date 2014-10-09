package org.droidplanner.desktop.ui;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;

import org.droidplanner.core.drone.variables.helpers.MagnetometerCalibration;
import org.droidplanner.core.drone.variables.helpers.MagnetometerCalibration.OnMagCalibrationListner;
import org.droidplanner.desktop.ui.widgets.GraphPanel;

import ellipsoidFit.FitPoints;

public class MagnetometerCal implements OnMagCalibrationListner {
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
		new MagnetometerCalibration(drone,window);
	}

	@Override
	public void newEstimation(FitPoints ellipsoidFit,int sampleSize, int[] magVector) {
		System.out.println(String.format("Sample %d\traw %s\tFit %2.1f \tCenter %s\tRadius %s",sampleSize, Arrays.toString(magVector),ellipsoidFit.getFitness()*100,ellipsoidFit.center.toString(), ellipsoidFit.radii.toString()));
		data1.add((float) magVector[0]);
		data1.add((float) magVector[1]);
		plot1.newDataSet((Float[]) data1.toArray(new Float[data1.size()]));
		if (ellipsoidFit.center.isNaN() || ellipsoidFit.radii.isNaN()) {
			plot1.updateSphere(null);
		}else{
			plot1.updateSphere(new int[] {(int) ellipsoidFit.center.getEntry(0),(int) ellipsoidFit.center.getEntry(1),(int) ellipsoidFit.radii.getEntry(0),(int) ellipsoidFit.radii.getEntry(1)});
		}
		plot1.repaint(100);
	}

	@Override
	public void finished(FitPoints fit) {
		System.err.println("####################################################################################################################################\n");
	}

	public class ScatterPlot extends Canvas {

		private static final long serialVersionUID = 1L;

		private static final float SCALE_FACTOR = 1/1000f;
	
		private Float[] points = new Float[] {};

		private int[] sphere = null;

		private int halfWidth,halfHeight,halfScale;
	
		public ScatterPlot() {
			halfWidth = X_SIZE / 2;
			halfHeight = Y_SIZE / 2;
			halfScale = (halfHeight > halfWidth) ? halfWidth : halfHeight;
		}
		
		public void newDataSet(Float[] array) {
			points = array;
		}
		
		public void updateSphere(int[] sphere) {
			this.sphere = sphere;
		}

		@Override
		public void paint(Graphics canvas) {
			canvas.drawString("XX", 0, 0);
	
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
			if (sphere!=null) {
				x = mapToImgX(sphere[0]);
				y = mapToImgY(sphere[1]);
				int width = (int) scale(sphere[2]);
				int height = (int)scale(sphere[3]);
				canvas.drawOval(x-width,y-height,width*2,height*2);
			}
			
		}

		private int mapToImgX(float coord) {
			return (int) (scale(coord) + halfWidth);
		}
		private int mapToImgY(float coord) {
			return (int) (-scale(coord) + halfHeight);
		}
		private float scale(float value) {
			return SCALE_FACTOR*halfScale * value;
		}		
	
	}

}