## Droidplanner v1.2.0
* Planning: renamed column header - #348
* Planning: Markers - additional UI + zooming fixes, distance while dragging marker - #324
* Planning: Markers, Mission - distance from prev waypoint in mission planner w/ live update while dragging - #327
* Planning: Fixed the area function - #358
* Parameters: fix for name column size, progress indicator - #328
* Parameters: sort by parameter name on opening parameter file - #335
* Parameters: Use APM parameter metadata if available when editing parameters - #344
* Parameters: Help display w/ description, units, valid range & accepted values where available - #345
* FlightScreen: Draggable guide-point - #347
* MapType: Made map fragments responsive to map type change, made map type more accessible - #336
* Waypoints: Fix the Waypoint parameters - #337
* Waypoints: Fixed the overlap text in the waypoint editor - #339

## Droidplanner v1.1.0
* Suport for the new telemetry module from 3DR (FT231)

## Droidplanner v1.0.2
* Fixing the waypoint numbering issue when planning a survey and reordering via the waypoint editor
* Live update flight path and distanceView while dragging
* Added RAW_SENSORS and RC_CHANNELS to preferences for compatibility w/ other MAVLink hardware on vehicle

## Droidplanner v1.0.1
* Moving the arm/disarm button to the overflow menu for safety reasons
* Fixing problems with the Do_Jump and Set_Home waypoint dialogs
* Reduce the chance of unintentionally deleting waypoints while scrolling

## Droidplanner v1.0.0
* No new changes to the codebase, just setting a milestone 

## Droidplanner v0.16.0
* Bluetooth Support
* Fixing layout problems with the Survey window

## Droidplanner v0.15.0
* New Waypoint Editor
* New Survey Interface
* Support for Physical Joysticks
* Chinise Translation
* Code Refactoring
* Minor fixes (please consult GIT history)

## Droidplanner v0.14.2
* Fixing the problem of working with big polygons

## Droidplanner v0.14.1
* Fixing a small bug in the Survey Dialog

## Droidplanner v0.14.0
* Drag-and-Drop waypoint editing
* Slide to remove waypoit
* Click to edit waypoint
* Adding a lot more info to the Survey Dialog

## Droidplanner v0.13.1
* Fixing layout problem in Survey Dialog in smaller screens

## Droidplanner v0.13.0
* Survey dialog in mission planning

## Droidplanner v0.12.4b
* Fixing a problem with the build of v0.12.4

## Droidplanner v0.12.4
* A couple of bug fixes
* Removing the "velocityLock" feature because it caused some troubles

## Droidplanner v0.12.3
* Hungarian translation
* Fix waypoint numering issue
* Fix problem with the chart screen in non-english versions

## Droidplanner v0.12.2
* New way of creating missions and polygons

## Droidplanner v0.12.1b
* Removing debug info that passed trough on the last release

## Droidplanner v0.12.1
* UDP support
* Better layout on the preference screen
* Fix the MAVLink library

## Droidplanner v0.12.0
* Add suport for direct USB connection
* Fix chinise translation

## Droidplanner v0.11.4
* Compatible with ArduRover
* Dynamicaly allocated on-screen joysticks

## Droidplanner v0.11.3
* Preferences for the RC screen (Mode1/Mode2, Inverted Channel, QuickButtons)

## Droidplanner v0.11.2
* Improving the Chart Screen
* Chinese Translation

## Droidplanner v0.11.1
* Changing the start screen back to Flight Data

## Droidplanner v0.11.0
* New Chart screen
* HUD code clean-up
* Activitys clean-up
* All markers are using the markerManager
* Colorful markers in the planning screen

## Droidplanner v0.10.3
* Improved Record-Me mode (with a bug fix)
* Now all the data on a Mission item is used
* Better handling of the markers on GCP and Planning screens

## Droidplanner v0.10.2
* Better swipe implementation on RC screen
* Bug fix on the RC screen
* Code Refactoring

## Droidplanner v0.10.1
* Fix in Offline maps
* New RC screen in portrait mode

## Droidplanner v0.10.0
* More advanced waypoint editor

## Droidplanner v0.9.3
* Fix problem in the build process

## Droidplanner v0.9.2
* Big code refactoring
* Nicer RC screen
* French Translation
* Updating to the new Google Maps API
* Orientation lock when connected

## Droidplanner v0.9.1
* Record-Me mode
* Nicer Icons
* Portuguese Translation
* Improvements to the HUD
* Change the default alt. to 50m
* Option to select diferent baud rates
* Option to keep the screen at full brightness
* Fix a bug with the Volume Control
* Fix bug in the RC Override screen

## Droidplanner v0.9.0
* New HUD by Karsten

## Droidplanner v0.8.4
* Greek language avaliable
* Latvian language avaliable

## Droidplanner v0.8.3
* German language avaliable
* Removing old Terminal screen
* APM calibration using a menu button.

## Droidplanner v0.8.2
* Fix small bug that ocurred when logging was enabled.

## Droidplanner v0.8.1
* Russian language avaliable
* Ability to read/save parameters from/to a .param file.
* Improvements to the code structure

## Droidplanner v0.8.0
* Parameter screen, with the ability to read/write/save parameters from an ArduPilot board
* Camera screen, virtual sticks to control a camera gimbal
* Follow-me mode, just a beta test of this mode (disabled by default)

## Droidplanner v0.7.8
* Fix bug in the MAVlink conenction

## Droidplanner v0.7.7
* Better handling of the MAVLink connection

## Droidplanner v0.7.6
* Map type selection
* Lock orientation in the RC override screen

## Droidplanner v0.7.5
* Added battery discharge notification
* Fix a bug on the RC override function, CH5 to 8 are now not overrided.

## Droidplanner v0.7.4
* .tlog files are now compatible with Mission Planner

## Droidplanner v0.7.3
* Improved mode TTS notifications
* Battery Capacity indication on the HUD
* Better selection of flight altitude, in planning missions and guided mode

## Droidplanner v0.7.2
* Notifications using Text To speech

## Droidplanner v0.7.1
* Improvement to the HUD layout
* Dialog for entering the guided mode altitude
* Fix autoreturn in throttle stick in RC screen

## Droidplanner v0.7.0
* Improved compatibility with ArduCopter (Compatible mode selection, and copter Icon)
* HUD layout improvements to suport multiple screen sizes and densities.

## Droidplanner v0.6.0
* Armed/Disarmed indication for copters
* Improvement to the HUD, like GPS and Battery indicators
* New plane icon
* Deletes unused screens like the old HUD
* Code restructure, should fix some bugs

## Droidplanner v0.5.4
* RC override rate now is selectable from the preferences menu

## Droidplanner v0.5.3
* Data stream rates now can be selected from the preferences menu

## Droidplanner v0.5.2
* Support devices that don't have location services.

## Droidplanner v0.5.1
* Support devices without USB host mode since they can connect via TCP

## Droidplanner v0.5.0
* USB support for 3DR telemetry module
* Minnor navigation improvement
* Fix some bugs that crashed the app

## Droidplanner v0.4.1
* Improvement to the HUD layout

## DroidPlanner v0.4.0
* RC control override

## DroidPlanner v0.3.1
* New menu for selecting the current Waypoint
* Fix map markers handling on Planning screen

## DroidPlanner v0.3
* Realease at Google Play
