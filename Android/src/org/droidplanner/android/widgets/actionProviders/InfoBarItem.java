package org.droidplanner.android.widgets.actionProviders;

import android.content.Context;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import com.MAVLink.Messages.ApmModes;

import org.droidplanner.R;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.android.widgets.spinners.ModeAdapter;
import org.droidplanner.android.widgets.spinners.SpinnerSelfSelect;

import java.util.Collections;
import java.util.List;

/**
 * Set of actions supported by the info bar
 */
public abstract class InfoBarItem {

	/**
	 * Default value when there's no data to display.
	 */
	protected static final String sDefaultValue = "--";

	/**
	 * Id for the info action.
	 */
	protected final int mItemId;

	/**
	 * Info bar item view.
	 */
	protected View mItemView;

	protected InfoBarItem(Context context, View parentView, Drone drone,
			int itemId) {
		mItemId = itemId;
		initItemView(context, parentView, drone);
	}

	/**
	 * This initializes the view backing this info bar item.
	 * 
	 * @param context
	 *            application context
	 * @param parentView
	 *            parent view for the info bar item
	 * @param drone
	 *            current drone state
	 */
	protected void initItemView(final Context context, View parentView,
			Drone drone) {
		mItemView = parentView.findViewById(mItemId);
	}

	/**
	 * @return the info bar item view.
	 */
	public View getItemView() {
		return mItemView;
	}

	public void updateItemView(Context context, Drone drone) {
	}

	/**
	 * This is used, during the creation of the
	 * {@link org.droidplanner.android.widgets.actionProviders.InfoBarActionProvider}
	 * class, to initialize the info bar action popup window.
	 * 
	 * @param context
	 *            application context
	 */
	protected static PopupWindow initPopupWindow(Context context,
			int popupViewRes) {
		if (popupViewRes == 0)
			return null;

		final LayoutInflater inflater = LayoutInflater.from(context);
		final View popupView = inflater.inflate(popupViewRes, null);

		final PopupWindow popup = new PopupWindow(popupView,
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT, true);
		popup.setBackgroundDrawable(context.getResources().getDrawable(
				R.drawable.panel_white_bg));

		return popup;
	}

	/**
	 * Home info bar item: displays the distance of the drone from its home
	 * location.
	 */
	public static class HomeInfo extends InfoBarItem {

		public HomeInfo(Context context, View parentView, Drone drone) {
			super(context, parentView, drone, R.id.bar_home);
		}

		@Override
		public void updateItemView(final Context context, final Drone drone) {
			if (mItemView != null) {
				String update = drone == null ? "--" : String.format(
						"Home\n%s", drone.home.getDroneDistanceToHome()
								.toString());
				((TextView) mItemView).setText(update);
			}
		}
	}

	/**
	 * Gps info bar item: displays the count of satellites, and other gps
	 * information.
	 */
	public static class GpsInfo extends InfoBarItem {
		public GpsInfo(Context context, View parentView, Drone drone) {
			super(context, parentView, drone, R.id.bar_gps);
		}

		@Override
		public void updateItemView(final Context context, final Drone drone) {
			if (mItemView != null) {
				String update = drone == null ? "--" : String.format(
						"Satellite\n%d, %s", drone.GPS.getSatCount(),
						drone.GPS.getFixType());

				((TextView) mItemView).setText(update);
			}
		}
	}

	/**
	 * Flight time info bar item: displays the amount of time the drone is
	 * armed.
	 */
	public static class FlightTimeInfo extends InfoBarItem {

		/**
		 * This is the period for the flight time update.
		 */
		protected final static long FLIGHT_TIMER_PERIOD = 1000l; // 1 second

		/**
		 * This is the layout resource id for the popup window.
		 */
		protected static final int sPopupWindowLayoutId = R.layout.popup_info_flight_time;

		/**
		 * This popup is used to offer the user the option to reset the flight
		 * time.
		 */
		protected PopupWindow mPopup;

		/**
		 * This handler is used to update the flight time value.
		 */
		protected Handler mHandler;

		/**
		 * Handle to the current drone state.
		 */
		protected Drone mDrone;

		/**
		 * Runnable used to update the drone flight time.
		 */
		protected Runnable mFlightTimeUpdater;

		public FlightTimeInfo(Context context, View parentView, Drone drone) {
			super(context, parentView, drone, R.id.bar_propeller);
		}

		@Override
		protected void initItemView(final Context context, View parentView,
				final Drone drone) {
			super.initItemView(context, parentView, drone);
			if (mItemView == null)
				return;

			mHandler = new Handler();

			mFlightTimeUpdater = new Runnable() {
				@Override
				public void run() {
					mHandler.removeCallbacks(this);
					if (mDrone == null)
						return;

					if (mItemView != null) {
						long timeInSeconds = mDrone.state.getFlightTime();
						long minutes = timeInSeconds / 60;
						long seconds = timeInSeconds % 60;

						((TextView) mItemView).setText(String.format(
								"Air Time\n%02d:%02d", minutes, seconds));
					}

					mHandler.postDelayed(this, FLIGHT_TIMER_PERIOD);
				}
			};

			mPopup = initPopupWindow(context, sPopupWindowLayoutId);
			final View popupView = mPopup.getContentView();
			popupView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mDrone != null) {
						mDrone.state.resetFlightTimer();
					}
					mPopup.dismiss();
				}
			});

			mItemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mPopup == null)
						return;

					mPopup.showAsDropDown(mItemView);
				}
			});

			updateItemView(context, drone);
		}

		@Override
		public void updateItemView(final Context context, final Drone drone) {
			mDrone = drone;
			if (mItemView == null)
				return;

			mHandler.removeCallbacks(mFlightTimeUpdater);
			if (drone != null) {
				mFlightTimeUpdater.run();
			} else {
				((TextView) mItemView).setText("--:--");
			}
		}
	}

	/**
	 * BatteryInfo info bar item: displays the drone remaining voltage, and
	 * ratio of remaining to full voltage.
	 */
	public static class BatteryInfo extends InfoBarItem {
		public BatteryInfo(Context context, View parentView, Drone drone) {
			super(context, parentView, drone, R.id.bar_battery);
		}

		@Override
		public void updateItemView(Context context, Drone drone) {
			if (mItemView != null) {
				String update = drone == null ? "--" : String.format(
						"%2.1fv\n%2.0f%%", drone.battery.getBattVolt(),
						drone.battery.getBattRemain());

				((TextView) mItemView).setText(update);
			}
		}
	}

	/**
	 * Radio signal info bar item: displays the drone radio signal strength.
	 */
	public static class SignalInfo extends InfoBarItem {

		/**
		 * This is the layout resource id for the popup window.
		 */
		protected static final int sPopupWindowLayoutId = R.layout.popup_info_signal;

		/**
		 * This popup is used to show additional signal info.
		 */
		protected PopupWindow mPopup;

		/*
		 * Radio signal sub views.
		 */
		private TextView mRssiView;
		private TextView mRemRssiView;
		private TextView mNoiseView;
		private TextView mRemNoiseView;
		private TextView mFadeView;
		private TextView mRemFadeView;

		public SignalInfo(Context context, View parentView, Drone drone) {
			super(context, parentView, drone, R.id.bar_signal);
		}

		@Override
		protected void initItemView(Context context, View parentView,
				Drone drone) {
			super.initItemView(context, parentView, drone);
			if (mItemView == null)
				return;

			mPopup = initPopupWindow(context, sPopupWindowLayoutId);

			final View popupView = mPopup.getContentView();
			mRssiView = (TextView) popupView.findViewById(R.id.bar_signal_rssi);
			mRemRssiView = (TextView) popupView
					.findViewById(R.id.bar_signal_remrssi);
			mNoiseView = (TextView) popupView
					.findViewById(R.id.bar_signal_noise);
			mRemNoiseView = (TextView) popupView
					.findViewById(R.id.bar_signal_remnoise);
			mFadeView = (TextView) popupView.findViewById(R.id.bar_signal_fade);
			mRemFadeView = (TextView) popupView
					.findViewById(R.id.bar_signal_remfade);

			mItemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mPopup == null)
						return;

					mPopup.showAsDropDown(mItemView);
				}
			});

			updateItemView(context, drone);
		}

		@Override
		public void updateItemView(Context context, final Drone drone) {
			if (mItemView == null)
				return;

			String infoUpdate;
			if (drone == null) {
				infoUpdate = sDefaultValue;

				mRssiView.setText(sDefaultValue);
				mRemRssiView.setText(sDefaultValue);
				mNoiseView.setText(sDefaultValue);
				mRemNoiseView.setText(sDefaultValue);
				mFadeView.setText(sDefaultValue);
				mRemFadeView.setText(sDefaultValue);
			} else {
				infoUpdate = String.format("%d%%",
						drone.radio.getSignalStrength());

				mRssiView.setText(String.format("RSSI %2.0f dB",
						drone.radio.getRssi()));
				mRemRssiView.setText(String.format("RemRSSI %2.0f dB",
						drone.radio.getRemRssi()));
				mNoiseView.setText(String.format("Noise %2.0f dB",
						drone.radio.getNoise()));
				mRemNoiseView.setText(String.format("RemNoise %2.0f dB",
						drone.radio.getRemNoise()));
				mFadeView.setText(String.format("Fade %2.0f dB",
						drone.radio.getFadeMargin()));
				mRemFadeView.setText(String.format("RemFade %2.0f dB",
						drone.radio.getRemFadeMargin()));
			}

			mPopup.update();
			((TextView) mItemView).setText(infoUpdate);
		}
	}

	/**
	 * Flight/APM modes info bar item: allows the user to select/view the drone
	 * flight mode.
	 */
	public static class FlightModesInfo extends InfoBarItem {

		/**
		 * Stores the type of the current drone state.
		 */
		private int mLastDroneType = -1;

		/**
		 * This is the spinner modes adapter.
		 */
		private ModeAdapter mModeAdapter;

		/**
		 * Handle to the current drone state.
		 */
		private Drone mDrone;

		public FlightModesInfo(Context context, View parentView, Drone drone) {
			super(context, parentView, drone, R.id.bar_flight_mode);
		}

		@Override
		protected void initItemView(final Context context, View parentView,
				final Drone drone) {
			super.initItemView(context, parentView, drone);
			if (mItemView == null)
				return;

			final SpinnerSelfSelect modesSpinner = (SpinnerSelfSelect) mItemView;

			mModeAdapter = new ModeAdapter(context, R.layout.spinner_drop_down);
			modesSpinner.setAdapter(mModeAdapter);

			modesSpinner
					.setOnSpinnerItemSelectedListener(new SpinnerSelfSelect.OnSpinnerItemSelectedListener() {
						@Override
						public void onSpinnerItemSelected(Spinner parent,
								int position, String text) {
							if (mDrone != null) {
								final ApmModes newMode = (ApmModes) parent
										.getItemAtPosition(position);
								mDrone.state.changeFlightMode(newMode);
							}
						}
					});

			updateItemView(context, drone);
		}

		@Override
		public void updateItemView(final Context context, final Drone drone) {
			mDrone = drone;

			if (mItemView == null)
				return;

			final SpinnerSelfSelect modesSpinner = (SpinnerSelfSelect) mItemView;
			final int droneType = drone == null ? -1 : drone.type.getType();
			if (droneType != mLastDroneType) {
				final List<ApmModes> flightModes = droneType == -1 ? Collections
						.<ApmModes> emptyList() : ApmModes
						.getModeList(droneType);

				mModeAdapter.clear();
				mModeAdapter.addAll(flightModes);
				mModeAdapter.notifyDataSetChanged();

				mLastDroneType = droneType;
			}

			if (mDrone != null)
				modesSpinner.forcedSetSelection(mModeAdapter
						.getPosition(mDrone.state.getMode()));
		}
	}

	/**
	 * This is used on normal screen devices ( 320dp <= x < 540dp) to reduce the
	 * width of the info bar. Items typically on the info bar are moved to the
	 * popup displayed by clicking on this item's icon.
	 */
	public static class PhoneExtraInfo extends InfoBarItem {

		/**
		 * This is the list of info bar items stored as extra.
		 */
		protected InfoBarItem[] mExtraInfoBarItems;

		/**
		 * This is the layout resource id for the popup window.
		 */
		private static final int sPopupWindowLayoutId = R.layout.popup_info_phone_extra;

		/**
		 * This popup is used to show additional signal info.
		 */
		private PopupWindow mPopup;

		public PhoneExtraInfo(Context context, View parentView, Drone drone) {
			super(context, parentView, drone, R.id.bar_phone_extra);
		}

		@Override
		protected void initItemView(Context context, View parentView,
				Drone drone) {
			super.initItemView(context, parentView, drone);
			if (mItemView == null)
				return;

			// Initialize the popup window.
			mPopup = initPopupWindow(context, sPopupWindowLayoutId);
			final View popupView = mPopup.getContentView();

			mExtraInfoBarItems = new InfoBarItem[] {
					new HomeInfo(context, popupView, drone),
					new GpsInfo(context, popupView, drone),
					new BatteryInfo(context, popupView, drone),
					new ExtraFlightTimeInfo(context, popupView, drone,
							mItemView),
					new ExtraSignalInfo(context, popupView, drone, mItemView) };

			mItemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mPopup == null)
						return;

					mPopup.showAsDropDown(mItemView);
				}
			});

			updateItemView(context, drone);
		}

		@Override
		public void updateItemView(final Context context, final Drone drone) {
			if (mItemView == null)
				return;

			// Update the popup window content.
			for (InfoBarItem infoItem : mExtraInfoBarItems) {
				infoItem.updateItemView(context, drone);
			}
		}

		private static class ExtraFlightTimeInfo extends FlightTimeInfo {

			/**
			 * This is the anchor view for the parent popup window. It will
			 * allow this popup to be shown, as it's currently not possible to
			 * launch a popup window from another popup window view.
			 */
			private final View mWindowView;

			public ExtraFlightTimeInfo(Context context, View parentView,
					Drone drone, View windowView) {
				super(context, parentView, drone);
				mWindowView = windowView;
			}

			@Override
			protected void initItemView(Context context, final View parentView,
					Drone drone) {
				super.initItemView(context, parentView, drone);
				if (mItemView == null)
					return;

				mItemView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (mPopup == null)
							return;

						int yLoc = mWindowView.getBottom()
								+ mItemView.getBottom();
						mPopup.showAtLocation(mWindowView, Gravity.RIGHT
								| Gravity.TOP, 0, yLoc);
					}
				});
			}
		}

		private static class ExtraSignalInfo extends SignalInfo {

			/**
			 * This is the anchor view for the parent popup window. It will
			 * allow this popup to be shown, as it's currently not possible to
			 * launch a popup window from another popup window view.
			 */
			private final View mWindowView;

			public ExtraSignalInfo(Context context, View parentView,
					Drone drone, View windowView) {
				super(context, parentView, drone);
				mWindowView = windowView;
			}

			@Override
			protected void initItemView(Context context, final View parentView,
					Drone drone) {
				super.initItemView(context, parentView, drone);
				if (mItemView == null)
					return;

				mItemView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (mPopup == null)
							return;

						int yLoc = mWindowView.getBottom()
								+ mItemView.getBottom();
						mPopup.showAtLocation(mWindowView, Gravity.RIGHT
								| Gravity.TOP, 0, yLoc);
					}
				});
			}
		}
	}
}
