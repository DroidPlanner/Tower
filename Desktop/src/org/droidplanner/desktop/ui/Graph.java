package org.droidplanner.desktop.ui;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.model.Drone;
import org.droidplanner.desktop.ui.widgets.GraphPanel;

public class Graph implements OnDroneListener {
	public List<Double> data = new ArrayList<Double>();
	public GraphPanel graph;

	public Graph(String title) {
		JFrame graphFrame = new JFrame(title);
		graph = new GraphPanel(data);
		graphFrame.setPreferredSize(new Dimension(600, 300));
		graphFrame.getContentPane().add(graph);
		graphFrame.pack();
		graphFrame.setVisible(true);
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
		case SPEED:
			data.add(drone.getSpeed().getGroundSpeed().valueInMetersPerSecond());
			graph.repaint();
			break;
		default:
			break;
		}
	}

	static void createGraph(org.droidplanner.core.model.Drone drone) {
		OnDroneListener mGraph = new Graph("Graph");
		drone.addDroneListener(mGraph);
	}

}