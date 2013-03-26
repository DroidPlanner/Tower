/**
 * Copied from https://code.google.com/p/mobile-anarchy-widgets/
 */
package com.droidplanner.widgets.joystick;

public interface JoystickMovedListener {
	public void OnMoved(int pan, int tilt);
	public void OnReleased();
	public void OnReturnedToCenter();
}
