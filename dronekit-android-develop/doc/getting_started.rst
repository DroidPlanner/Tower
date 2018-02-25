===============
Getting Started
===============

For this tutorial, we'll use *Android Studio* and provide all our examples as an *Android Studio project*.

To start prototyping your app, you'll need to set up a virtual drone. ArduPilot provides a software-in-the-loop (SITL) environment for Linux that simulates a copter or plane. SITL works great with Ubuntu 13.04 or later. When you’re ready to test your app in the air, grab a ready-to-fly drone from the `3DR store <http://store.3drobotics.com>`_.

.. note:: You can also run SITL in a virtual machine using a VM manager like `Virtual Box <https://www.virtualbox.org/>`_. 


Setting up SITL on Linux
========================

See the `instructions here <http://dev.ardupilot.com/wiki/setting-up-sitl-on-linux/>`_ to set up SITL on Ubuntu.

Once you have the simulated vehicle running, enter the following commands (you only have to do this once):

#. Load a default set of parameters.

   .. code-block:: bash

       STABILIZE>param load ../Tools/autotest/copter_params.parm

#. Disable the arming check.

   .. code-block:: bash

       STABILIZE>param set ARMING_CHECK 0


Setting up your Android Studio Project
======================================

For an existing app:

#. Open the **build.gradle** file inside your application module directory. Android Studio projects contain a top level **build.gradle** file and a **build.gradle** for each module. Make sure to edit the file for your application module.

#. Add a new build rule under dependencies for the latest version of the DroneKit-Android Client library. For example:

   .. code-block:: bash

       apply plugin: 'com.android.application'
       ...

       repositories {
           jcenter()
       }

       dependencies {
           compile 'com.o3dr.android:dronekit-android:3.0.+'
           ...
       }
