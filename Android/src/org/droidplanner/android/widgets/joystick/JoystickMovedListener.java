/**
 * Copied from https://code.google.com/p/mobile-anarchy-widgets/
 */
package org.droidplanner.android.widgets.joystick;

public interface JoystickMovedListener {
	public void OnMoved(double pan, double tilt);

	public void OnReleased();

	public void OnReturnedToCenter();
}
