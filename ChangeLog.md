# DroidPlanner ChangeLog
Our convention for release control is the following (for a release like DPvA.B.C_RCx):
* A - Main version, changes just with big changes in the app structure or/and layout
* B - Minor version, add's some significant feature to the app
* C - Minor changes with bugfixes, or small refactorings, from previous "A" or "B" changes.
* x - Releases Candidates (RC) are build from the main releases, but may have some feature's disabled. The "x" is used if multiple RC are needed for a new version of the app.

All the changes are logged below (preferable with the pull request numbers in parenteses):

# Releases
## Droidplanner v2.4.3
* Improve analytics (#829)
* Fixed German translation (#831)
* Restructuring Settings menu (#815)
* Updating parameters description (#828)
* Adding support for direct connections to the Pixhawk (#816)
* Travis CI integration (#821,#824,#829)
* Fixing log bufferoverflow exception (#834)

## Droidplanner v2.4.2
* Added google analytics (#813)
* Update google play services (#813)
* Update Droneshare library (#814)

## Droidplanner v2.4.1
* Add basic follow-me UI (810)
* Add support for OSM (#799)
* Fix editor tools bugs (#796)
* Fix gradle build (#795)
* Fix Eclipse Mavlink build (#797)

## Droidplanner v2.4.0
* Initial support for Follow-me (#769)
* Circle mission item, and advanced circle (#790)
* Offline/Delayed tlog auto upload to DroneShare (#783,#787)
* Fixing problems and adding test cases for the GeoTool classes (#770)

## Droidplanner v2.3.3
* Fixing mission upload problem in AC3.2 (#767)
* Adding support for Hybrid mode (#766)

## Droidplanner v2.3.2
* Change waypoint max delay to 60s (#730)
* Land mission item defaults altitude to zero (#752)
* Mission progress update are reported by TTS (#759)

## Droidplanner v2.3.1
* Improve Mission Support, Takeoff and ROI (#739)
* Fix problems with UDP connection (#742)
* Fix build settings (#748,#749)
* Add support for Droneshare (#746)

## Droidplanner v2.3.0
* Decoupling of drone code from Android (#734, #649)
* Mode change bugfix for copter other than quads (#719)
* Fixed problems added by #734 in the Coord2D class (#735,#736,#737)
* Cleaning up Android Studio files (#738,#744)
* Chinese Translation (#717) 

## Droidplanner v2.2.5
* Long press on trash tool removes the entire mission (#714)
* Clear the previous flight path when arming (#705)
* Improves the parameter download algorithm on bad links (#715)
* Fix problems with decimal point on Parameters screen (#690)
* Added a help video (#716)
* Fix minor UX issues. The message on polygon tool and the guided dialog (#691)
* Commented out the follow me preference until it's implemented (#704)
* Fixng typos and formatting (#702)

## Droidplanner v2.2.4
* Fix User Experience problems with the Survey tool (#688)
* Updated the README file (#687)
* Fix problems with the Flight Mode spinner(#686,#675)

## Droidplanner v2.2.3
* Fix User Experience problems at the Editor (#670)
* Make landscape the default camera orientation for Survey (#671)
* Fixed problem with the Drone HeartBeat detector, no more "connected" speach messages (#674)
* Fixed Toast message at the Fragments screen (#678)

## Droidplanner v2.2.2
* Implemented the Android Navigation Drawer (#653)
* Renamed the HUD to Attitude Indicator (#651)

## Droidplanner v2.2.1
* Fix errors in Land, Takeoff and RTH mission items (#655)
* German Translation and Language Selector in settings (#650)
* Fix Gradle build (#648)
* Starting to add unit testing to the code (#646)

## Droidplanner v2.2.0
* Added support for phones (#618)
* Fixing typos and whitespaces on strings.xml (#640)

## Droidplanner v2.1.2
* Clean-up of Strings.xml (#617)
* Adds a check for Google Play Services (#639)
* Adding support for Android Studio, Gradle (#623,#619)

## Droidplanner v2.1.1
* Fix Parameters screen issue (#613)
* Making flight screen mission markers un-draggable (#614)

## Droidplanner v2.1.0
* Implemented the Survey Tool

## Droidplanner v2.0.1
* Cleaning minor bugs (#581)
* Fixed typo errors(#589)
* Improved Radio setup (#593,#596)
* Removing unused assets (#594,#601)
* Small mavlink library fix (#585)
* Improved Guided mode (#595)
* Rescrict disctribution to tablets only (#597)
* Dialog for BlueTooth device selection(#598)
* Fixed typos on source-code (#605)

## Droidplanner v2.0.0
* Renamed package to org.droidplanner
* Redesing of the mission handling code
* Completely redesigned graphical user interface
* Easy to use Home, Land, and Loiter buttons
* New Artificial Horizon
* Added telemetry to the ActionBar
* Mode detail fragment
* New guided mode with changeable altitude
* New planning screen for quick mission generation
* Easy and powerful mission editing tools
* New mission listing fragment
* Drone Setup screen with multiple tabs like:Radio,Checklist,Parameters
* Improved BlueTooth Connection

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
* Activities clean-up
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
