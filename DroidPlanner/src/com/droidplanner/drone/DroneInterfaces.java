package com.droidplanner.drone;

import com.droidplanner.drone.variables.mission.WaypointEvent_Type;
import com.droidplanner.parameters.Parameter;

import java.util.List;

public class DroneInterfaces {
	public interface MapUpdatedListner {
		public void onDroneUpdate();
		public void onDroneTypeChanged();
	}

	public interface MapConfigListener {
		public void onMapTypeChanged();
	}

	public interface DroneTypeListner {
		public void onDroneTypeChanged();
	}

    public interface VehicleTypeListener {
        public void onVehicleTypeChanged();
    }
	
	public interface InfoListner {
		public void onInfoUpdate();
	}

	public interface HomeDistanceChangedListner {
		public void onDistanceToHomeHasChanged();
	}

	public interface HudUpdatedListner {
		public void onOrientationUpdate();
		public void onSpeedAltitudeAndClimbRateUpdate();
	}

	public interface ModeChangedListener {
		public void onModeChanged();
	}

	public interface OnParameterManagerListner {
		public void onBeginReceivingParameters();
		public void onParameterReceived(Parameter parameter, int index,	int count);
		public void onEndReceivingParameters(List<Parameter> parameter);
	}

	public interface OnWaypointManagerListener {
		public void onBeginWaypointEvent(WaypointEvent_Type wpEvent);
		public void onWaypointEvent(WaypointEvent_Type wpEvent, int index,	int count);
		public void onEndWaypointEvent(WaypointEvent_Type wpEvent);
	}

	public interface OnStateListner {
		void onFlightStateChanged();

		void onArmChanged();

		void onFailsafeChanged();
	}
	
	
	public interface OnWaypointChangedListner {
			public void onMissionUpdate();
	}
	
	public interface OnTuningDataListner{
		void onNewOrientationData();

		void onNewNavigationData();
	}
	
	public interface OnRcDataChangedListner{
		void onNewOutputRcData();
		void onNewInputRcData();
	}
}