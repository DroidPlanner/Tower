======================
First App: Hello Drone
======================

In the first example, we’ll walk through making an app that connects to a Copter and executes basic commands, including changing ArduPilot flight modes, arming, taking off and landing.


Project Setup
=============

#. Set up a basic Android Studio project.

   .. image:: _static/images/hellodrone_setup_1.png

   Make sure to use API 15 (Ice Cream Sandwich) or later.

   .. image:: _static/images/hellodrone_setup_2.png

#. Start with a blank activity.

   .. image:: _static/images/hellodrone_setup_3.png

#. Click **Finish** to create your project.

   .. image:: _static/images/hellodrone_setup_4.png


Adding the Client Library
=========================

To add the DroneKit-Android Client library to your project:

#. Open **build.gradle (Module:app)** and, under the dependencies section, add: 

   .. code-block:: bash

       compile 'com.o3dr.android:dronekit-android:3.0.+'

#. Click **Sync** in the top-right corner to re-sync the gradle:

   .. image:: _static/images/hellodrone_setup_5.png




Connecting to DroneKit-Android
==========================

Implement a ``TowerListener`` on your ``MainActivity`` to listen for events sent from the library to your app.

.. code-block:: java
   :linenos:
   :emphasize-lines: 1,4-12

	public class MainActivity extends ActionBarActivity implements TowerListener {

		// DroneKit-Android Listener
		@Override
		public void onTowerConnected() {
			
		}

		@Override
		public void onTowerDisconnected() {
			
		}

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_main);
		}
	}


Now you'll need to:

1. Add a ``ControlTower`` instance to manage the communication to the Client library.
2. Connect to the Client library on start of the ``MainActivity`` and disconnect on its stop.

   .. code-block:: java
       :linenos:
       :emphasize-lines: 3,11,18,25,28-36

       public class MainActivity extends ActionBarActivity implements TowerListener {

       private ControlTower controlTower;

       @Override
       protected void onCreate(Bundle savedInstanceState) {
           super.onCreate(savedInstanceState);
           setContentView(R.layout.activity_main);

           // Initialize the service manager
           this.controlTower = new ControlTower(getApplicationContext());

       }

       @Override
       public void onStart() {
           super.onStart();
           this.controlTower.connect(this);

       }

       @Override
       public void onStop() {
           super.onStop();
           this.controlTower.disconnect();
       }

       @Override
       public void onTowerConnected() {

       }

       @Override
       public void onTowerDisconnected() {
			
       }

       @Override
       protected void onCreate(Bundle savedInstanceState) {
           super.onCreate(savedInstanceState);
           setContentView(R.layout.activity_main);

       }
   }


Connecting to a Drone via UDP
=============================

Make sure you have your SITL instance running. Power up a simulated drone with a UDP output to the IP of your Android device.

For this example, you’ll simulate a drone in Berkeley, CA, display the telemetry console and set the output IP to your Android testing device. (You can find the IP for your Android device in **Settings | Wi-Fi**. Tap on the connection to get information about it.)

In your terminal, navigate to the folder with the cloned ardupilot repo and enter the following:

.. code-block:: bash

    sim_vehicle.sh -L 3DRBerkeley --console  --out <ANDROID_DEVICE_IP>:14550



Now that you have a virtual drone, let’s add the ability to connect to it.

First, declare that your ``MainActivity`` can act as an interface for DroneListener and implement some methods to listen for drone events.

.. code-block:: java
	:linenos:
	:emphasize-lines: 1-15

	public class MainActivity extends ActionBarActivity implements DroneListener, TowerListener {
		@Override
		public void onDroneEvent(String event, Bundle extras) {

		}

		@Override
		public void onDroneConnectionFailed(ConnectionResult result) {
			
		}

		@Override
		public void onDroneServiceInterrupted(String errorMsg) {

		}

		...
	}

Next, add an instance variable to the top of your ``MainActivity`` to keep track of the drone instance.

.. code-block:: java
	:linenos:
	:emphasize-lines: 2-3

	public class MainActivity extends ActionBarActivity implements DroneListener, TowerListener {
		private Drone drone;
		private int droneType = Type.TYPE_UNKNOWN;


In order to register with the control tower, the drone instance needs a generic Android handler. Go ahead and add a handler where you declare your instance variables.

.. code-block:: java
	:linenos:
	:emphasize-lines: 4

	public class MainActivity extends ActionBarActivity implements DroneListener, TowerListener {
		private Drone drone;
		private int droneType = Type.TYPE_UNKNOWN;
		private final Handler handler = new Handler();


Add an instantiation of the new drone upon the creation of your ``MainActivity``. After creation, the new drone will need to be registered with the control tower to be active.

.. code-block:: java
	:linenos:
	:emphasize-lines: 7
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		this.serviceManager = new ServiceManager(getApplicationContext());
		this.drone = new Drone();
	}

        @Override
        public void onTowerConnected() {
                this.controlTower.registerDrone(this.drone, this.handler);
                this.drone.registerDroneListener(this);
        }


Make sure that when the ``MainActivity`` is stopped, the drone is unregistered from the control tower. 

.. code-block:: java
	:linenos:
	:emphasize-lines: 4-8
	
	@Override
	public void onStop() {
		super.onStop();
		if (this.drone.isConnected()) {
			this.drone.disconnect();
			updateConnectedButton(false);
		}
                this.controlTower.unregisterDrone(this.drone);
                this.controlTower.disconnect();
	}

Now let's add a button in **activity_main.xml** that will connect to the drone on press. Open **activity_main.xml** and add the following:

.. code-block:: xml
	:linenos:

	<Button
		android:layout_width="150dp"
		android:layout_height="wrap_content"
		android:text="Connect"
		android:id="@+id/btnConnect"
		android:onClick="onBtnConnectTap"
		android:layout_alignParentRight="true"
		android:layout_alignParentEnd="true" />

Add a method to your ``MainActivity`` to handle the connect button press so that:

1. If the drone is connected, use this button to disconnect.
2. If the drone isn’t connected, build a set of connection parameters and connect.

.. code-block:: java
	:linenos:
	:emphasize-lines: 1-100

	public void onBtnConnectTap(View view) {
		if(this.drone.isConnected()) {
			this.drone.disconnect();
		} else {
			Bundle extraParams = new Bundle();
			extraParams.putInt(ConnectionType.EXTRA_UDP_SERVER_PORT, 14550); // Set default port to 14550

			ConnectionParameter connectionParams = new ConnectionParameter(ConnectionType.TYPE_UDP, extraParams, null);
			this.drone.connect(connectionParams);
		}
	}


Now add some UI elements to alert you when the drone is connected. Add the following UI helper method to the bottom of your ``MainActivity`` file.

.. code-block:: java
	:linenos:
	:emphasize-lines: 1-100

	protected void alertUser(String message) {
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
	}

	protected void updateConnectedButton(Boolean isConnected) {
		Button connectButton = (Button)findViewById(R.id.btnConnect);
		if (isConnected) {
			connectButton.setText("Disconnect");
		} else {
			connectButton.setText("Connect");
		}
	}

Let’s revisit the ``onDroneEvent`` method. Add the following to your ``onDroneEvent`` method to alert the user when the drone is connected:

.. code-block:: java
	:linenos:
	:emphasize-lines: 3-16

	@Override
	public void onDroneEvent(String event, Bundle extras) {
		switch (event) {
			case AttributeEvent.STATE_CONNECTED:
				alertUser("Drone Connected");
				updateConnectedButton(this.drone.isConnected());
				break;

			case AttributeEvent.STATE_DISCONNECTED:
				alertUser("Drone Disconnected");
				updateConnectedButton(this.drone.isConnected());
				break;

			default:
				break;
		}
	}

Now if you run the app and the SITL environment, you'll be able to connect to your drone!


Connecting via USB (3DR Telemetry Radio)
========================================

For USB connections, you'll need to define an extra param for the baud rate.

.. code-block:: java
	:linenos:

	Bundle extraParams = new Bundle();
	extraParams.putInt(ConnectionType.EXTRA_USB_BAUD_RATE, 57600); // Set default baud rate to 57600
	ConnectionParameter connectionParams = new ConnectionParameter(ConnectionType.TYPE_USB, extraParams, null);
	this.drone.connect(connectionParams);


Getting Telemetry from the Drone
================================

In order to get telemetry updates from the drone, you'll need to add cases for different drone events returned in ``onDroneEvent``.

.. code-block:: java
	:linenos:
	:emphasize-lines: 14-34

	@Override
	public void onDroneEvent(String event, Bundle extras) {
		switch (event) {
			case AttributeEvent.STATE_CONNECTED:
				alertUser("Drone Connected");
				updateConnectedButton(this.drone.isConnected());
				break;

			case AttributeEvent.STATE_DISCONNECTED:
				alertUser("Drone Disconnected");
				updateConnectedButton(this.drone.isConnected());
				break;

			case AttributeEvent.STATE_VEHICLE_MODE:
				updateVehicleMode();
				break;

			case AttributeEvent.TYPE_UPDATED:
				Type newDroneType = this.drone.getAttribute(AttributeType.TYPE);
				if (newDroneType.getDroneType() != this.droneType) {
					this.droneType = newDroneType.getDroneType();
					updateVehicleModesForType(this.droneType);
				}
				break;


			case AttributeEvent.SPEED_UPDATED:
				updateAltitude();
				updateSpeed();
				break;

			case AttributeEvent.HOME_UPDATED:
				updateDistanceFromHome();
				break;

			default:
				break;
		}
	}

Add some TextViews to your UI to output telemetry values. In **activity_main.xml**, add a table with ``TextViews`` and a Spinner Dropdown view that will let you change the vehicle’s modes.

.. code-block:: xml

	<TableLayout
		android:layout_width="fill_parent"
		android:layout_height="200dp"
		android:layout_below="@+id/telemetryLabel"
		android:layout_alignParentLeft="true"
		android:layout_alignParentStart="true"
		android:layout_marginTop="10dp">

		<TableRow
			android:layout_width="fill_parent"
			android:layout_height="fill_parent"
			android:id="@+id/vehTelemRow1">

			<TextView
				android:layout_width="100dp"
				android:layout_height="wrap_content"
				android:textAppearance="?android:attr/textAppearanceMedium"
				android:text="Mode:"
				android:id="@+id/vehicleModeLabelTextView"
				android:layout_column="0"
				android:paddingTop="5dp"
				android:paddingBottom="5dp" />

			<Spinner
				android:layout_width="fill_parent"
				android:layout_height="44dp"
				android:id="@+id/modeSelect"
				android:spinnerMode="dropdown"
				android:layout_below="@+id/connectionTypeLabel"
				android:layout_toLeftOf="@+id/btnConnect"
				android:layout_alignParentLeft="true"
				android:layout_alignParentStart="true"
				android:layout_column="1" />
		</TableRow>

		<TableRow
			android:layout_width="fill_parent"
			android:layout_height="fill_parent"
			android:id="@+id/vehTelemRow2">

			<TextView
				android:layout_width="100dp"
				android:layout_height="wrap_content"
				android:textAppearance="?android:attr/textAppearanceMedium"
				android:text="Altitude:"
				android:id="@+id/altitudeLabelTextView"
				android:layout_column="0"
				android:paddingTop="5dp"
				android:paddingBottom="5dp" />

			<TextView
				android:layout_width="fill_parent"
				android:layout_height="wrap_content"
				android:textAppearance="?android:attr/textAppearanceMedium"
				android:text="0m"
				android:id="@+id/altitudeValueTextView"
				android:layout_column="1"
				android:paddingTop="5dp"
				android:paddingBottom="5dp"
				android:layout_gravity="left" />
		</TableRow>

		<TableRow
			android:layout_width="fill_parent"
			android:layout_height="fill_parent"
			android:id="@+id/vehTelemRow3">

			<TextView
				android:layout_width="100dp"
				android:layout_height="wrap_content"
				android:textAppearance="?android:attr/textAppearanceMedium"
				android:text="Speed:"
				android:id="@+id/speedLabelTextView"
				android:layout_column="0"
				android:paddingTop="5dp"
				android:paddingBottom="5dp" />

			<TextView
				android:layout_width="fill_parent"
				android:layout_height="wrap_content"
				android:textAppearance="?android:attr/textAppearanceMedium"
				android:text="0m/s"
				android:id="@+id/speedValueTextView"
				android:layout_column="1"
				android:paddingTop="5dp"
				android:paddingBottom="5dp"
				android:layout_gravity="left" />
		</TableRow>

		<TableRow
			android:layout_width="fill_parent"
			android:layout_height="fill_parent"
			android:id="@+id/vehTelemRow4">

			<TextView
				android:layout_width="100dp"
				android:layout_height="wrap_content"
				android:textAppearance="?android:attr/textAppearanceMedium"
				android:text="Distance:"
				android:id="@+id/distanceLabelTextView"
				android:layout_column="0"
				android:paddingTop="5dp"
				android:paddingBottom="5dp" />

			<TextView
				android:layout_width="fill_parent"
				android:layout_height="wrap_content"
				android:textAppearance="?android:attr/textAppearanceMedium"
				android:text="0m"
				android:id="@+id/distanceValueTextView"
				android:layout_column="1"
				android:paddingTop="5dp"
				android:paddingBottom="5dp"
				android:layout_gravity="left" />
		</TableRow>

	</TableLayout>

Add a class-level Spinner variable in ``MainActivity`` so you can reference the table throughout the code.

.. code-block:: java
	:linenos:
	:emphasize-lines: 5

	public class MainActivity extends ActionBarActivity implements DroneListener, TowerListener {
		private Drone drone;
		private int droneType = Type.TYPE_UNKNOWN;
		private final Handler handler = new Handler();
		Spinner modeSelector;

Add a reference to the Spinner defined in the XML layout to the ``onCreate`` method.

.. code-block:: java
	:linenos:
	:emphasize-lines: 10-20

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		final Context context = getApplicationContext();
		this.controlTower = new ControlTower(context);
		this.drone = new Drone();

		this.modeSelector = (Spinner)findViewById(R.id.modeSelect);
		this.modeSelector.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				onFlightModeSelected(view);
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// Do nothing
			}
		});
	}

Now implement some of the methods in ``onDroneEvent`` in order to update the UI. Add the following methods to your ``MainActivity``.

.. code-block:: java
	:linenos:
	:emphasize-lines: 1-100

	public void onFlightModeSelected(View view) {
		VehicleMode vehicleMode = (VehicleMode) this.modeSelector.getSelectedItem();
		this.drone.changeVehicleMode(vehicleMode);
	}

	protected void updateVehicleModesForType(int droneType) {
		List<VehicleMode> vehicleModes =  VehicleMode.getVehicleModePerDroneType(droneType);
		ArrayAdapter<VehicleMode> vehicleModeArrayAdapter = new ArrayAdapter<VehicleMode>(this, android.R.layout.simple_spinner_item, vehicleModes);
		vehicleModeArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		this.modeSelector.setAdapter(vehicleModeArrayAdapter);
	}

	protected void updateVehicleMode() {
		State vehicleState = this.drone.getAttribute(AttributeType.STATE);
		VehicleMode vehicleMode = vehicleState.getVehicleMode();
		ArrayAdapter arrayAdapter = (ArrayAdapter)this.modeSelector.getAdapter();
		this.modeSelector.setSelection(arrayAdapter.getPosition(vehicleMode));
	}

	protected void updateAltitude() {
		TextView altitudeTextView = (TextView)findViewById(R.id.altitudeValueTextView);
		Altitude droneAltitude = this.drone.getAttribute(AttributeType.ALTITUDE);
		altitudeTextView.setText(String.format("%3.1f", droneAltitude.getAltitude()) + "m");
	}

	protected void updateSpeed() {
		TextView speedTextView = (TextView)findViewById(R.id.speedValueTextView);
		Speed droneSpeed = this.drone.getAttribute(AttributeType.SPEED);
		speedTextView.setText(String.format("%3.1f", droneSpeed.getGroundSpeed()) + "m/s");
	}

	protected void updateDistanceFromHome() {
		TextView distanceTextView = (TextView)findViewById(R.id.distanceValueTextView);
		Altitude droneAltitude = this.drone.getAttribute(AttributeType.ALTITUDE);
		double vehicleAltitude = droneAltitude.getAltitude();
		Gps droneGps = this.drone.getAttribute(AttributeType.GPS);
		LatLong vehiclePosition = droneGps.getPosition();

		double distanceFromHome =  0;

		if (droneGps.isValid()) {
			LatLongAlt vehicle3DPosition = new LatLongAlt(vehiclePosition.getLatitude(), vehiclePosition.getLongitude(), vehicleAltitude);
			Home droneHome = this.drone.getAttribute(AttributeType.HOME);
			distanceFromHome = distanceBetweenPoints(droneHome.getCoordinate(), vehicle3DPosition);
		} else {
			distanceFromHome = 0;
		}

		distanceTextView.setText(String.format("%3.1f", distanceFromHome) + "m");
	}

	protected double distanceBetweenPoints(LatLongAlt pointA, LatLongAlt pointB) {
		if (pointA == null || pointB == null) {
			return 0;
		}
		double dx = pointA.getLatitude() - pointB.getLatitude();
		double dy  = pointA.getLongitude() - pointB.getLongitude();
		double dz = pointA.getAltitude() - pointB.getAltitude();
		return Math.sqrt(dx*dx + dy*dy + dz*dz);
	}

Whoa, that's a lot of stuff. Let's go through it:

::

	public void onFlightModeSelected(View view)

This changes the drone's flight mode when the user changes the mode selector.

::

	protected void updateVehicleModesForType(int droneType)

This is triggered when the ``onDroneEvent`` tells us the type of vehicle we're dealing with. In the ``onDroneEvent``, we get the type of vehicle and the modes the vehicle can have.

::

	// Fired when the vehicle mode changes on the drone.
	protected void updateVehicleMode()


::

	// Fired when the altitude of the drone updates.
	protected void updateAltitude()


::

	// Fired when the speed of the drone updates.
	protected void updateSpeed()


::

	// A convenience method for calculating the distance between two 3D points.
	protected double distanceBetweenPoints(LatLongAlt pointA, LatLongAlt pointB)


Take Off!
=========

Add a button to your app that will allow you to arm, take off and land the drone.

.. code-block:: xml

	<Button
		android:layout_width="120dp"
		android:layout_height="wrap_content"
		android:id="@+id/btnArmTakeOff"
		android:layout_alignParentRight="true"
		android:layout_alignParentEnd="true"
		android:layout_column="1"
		android:visibility="invisible"
		android:onClick="onArmButtonTap" />

Add a method to your ``MainActivity`` to update the button's UI depending on the vehicle state:

.. code-block:: java
	:linenos:
	:emphasize-lines: 1-100

	protected void updateArmButton() {
		State vehicleState = this.drone.getAttribute(AttributeType.STATE);
		Button armButton = (Button)findViewById(R.id.btnArmTakeOff);

		if (!this.drone.isConnected()) {
			armButton.setVisibility(View.INVISIBLE);
		} else {
			armButton.setVisibility(View.VISIBLE);
		}

		if (vehicleState.isFlying()) {
			// Land
			armButton.setText("LAND");
		} else if (vehicleState.isArmed()) {
			// Take off
			armButton.setText("TAKE OFF");
		} else if (vehicleState.isConnected()){
			// Connected but not Armed
			armButton.setText("ARM");
		}
	}

Add a method to your ``MainActivity`` to handle the arm button press:

.. code-block:: java
	:linenos:
	:emphasize-lines: 1-25

	public void onArmButtonTap(View view) {
		Button thisButton = (Button)view;
		State vehicleState = this.drone.getAttribute(AttributeType.STATE);

		if (vehicleState.isFlying()) {
			// Land
			this.drone.changeVehicleMode(VehicleMode.COPTER_LAND);
		} else if (vehicleState.isArmed()) {
			// Take off
			this.drone.doGuidedTakeoff(10); // Default take off altitude is 10m
		} else if (!vehicleState.isConnected()) {
			// Connect
			alertUser("Connect to a drone first");
		} else if (vehicleState.isConnected() && !vehicleState.isArmed()){
			// Connected but not Armed
			this.drone.arm(true);
		}
	}

Finally, go back to your good old `onDroneEvent`` to link updating the arm button UI to the drone events:

.. code-block:: java
	:linenos:
	:emphasize-lines: 18-21

	@Override
	public void onDroneEvent(String event, Bundle extras) {

		switch (event) {
			case AttributeEvent.STATE_CONNECTED:
				alertUser("Drone Connected");
				updateConnectedButton(this.drone.isConnected());
				updateArmButton();

				break;

			case AttributeEvent.STATE_DISCONNECTED:
				alertUser("Drone Disconnected");
				updateConnectedButton(this.drone.isConnected());
				updateArmButton();
				break;

			case AttributeEvent.STATE_UPDATED:
			case AttributeEvent.STATE_ARMING:
				updateArmButton();
				break;

			case AttributeEvent.TYPE_UPDATED:
				Type newDroneType = this.drone.getAttribute(AttributeType.TYPE);
				if (newDroneType.getDroneType() != this.droneType) {
					this.droneType = newDroneType.getDroneType();
					updateVehicleModesForType(this.droneType);
				}
				break;

			case AttributeEvent.STATE_VEHICLE_MODE:
				updateVehicleMode();
				break;


			case AttributeEvent.SPEED_UPDATED:
				updateAltitude();
				updateSpeed();
				break;

			case AttributeEvent.HOME_UPDATED:
				updateDistanceFromHome();
				break;
			default:
				 Log.i("DRONE_EVENT", event);
				break;
		}
	}

Run your app and SITL; you'll be able to connect, arm and take off!

Summary
=======

Congratulations! You've just made your first drone app. You can find the full source code for this example on `Github <https://github.com/3drobotics/DroneKit-Android-Starter>`_.




