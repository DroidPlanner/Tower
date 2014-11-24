package org.droidplanner.android.utils.rc.input;

import android.content.Context;

import org.droidplanner.android.utils.rc.input.GameController.GameControllerDevice;

public enum GenericInput {
	CONTROLLER {
		@Override
		public GenericInputDevice getInstance(Context context) {
			return new GameControllerDevice(context);
		}
	},
	SENSORS {
		@Override
		public GenericInputDevice getInstance(Context context) {
			return null;
		}
	},
	TOUCH {
		@Override
		public GenericInputDevice getInstance(Context context) {
			return null;
		}
	};

	public abstract GenericInputDevice getInstance(Context context);
}
