package org.droidplanner.fragments.calibration.failsafe;

import org.droidplanner.R;
import org.droidplanner.calibration.CalParameters;
import org.droidplanner.calibration.FST_CalParameters;
import org.droidplanner.fragments.calibration.FragmentSetupSend;
import org.droidplanner.fragments.calibration.SetupSidePanel;
import org.droidplanner.fragments.helpers.SuperSetupMainPanel;
import org.droidplanner.helpers.ValueKey;
import org.droidplanner.helpers.ValueKey.ValueKeyData;
import org.droidplanner.widgets.NumberFieldEdit.NumberFieldEdit;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;

public class FragmentSetupFSFence extends SuperSetupMainPanel {

	private ValueKeyData optionsType, optionsAction;

	private CheckBox chkbxFenceEnable;
	private Spinner spinnerFenceType, spinnerFenceAction;
	private NumberFieldEdit numberFieldMargin, numberFieldRadius,
			numberFieldAltitude;

	@Override
	public int getPanelLayout() {
		return R.layout.fragment_setup_fs_fence_main;
	}

	@Override
	protected SetupSidePanel getDefaultPanel() {
		sidePanel = new FragmentSetupSend();
		sidePanel.updateTitle(R.string.setup_fs_fnc_side_title);
		sidePanel.updateDescription(R.string.setup_fs_fnc_side_desc);
		return sidePanel;
	}

	@Override
	public SetupSidePanel getSidePanel() {
		return getDefaultPanel();
	}

	@Override
	protected CalParameters getParameterHandler() {
		return new FST_CalParameters(drone);
	}

	@Override
	protected void updateCalibrationData() {
		parameters.setParamValueByName("FENCE_TYPE",
				optionsType.values[spinnerFenceType.getSelectedItemPosition()]);
		parameters
				.setParamValueByName("FENCE_ACTION",
						optionsType.values[spinnerFenceAction
								.getSelectedItemPosition()]);
		parameters.setParamValueByName("FENCE_RADIUS",
				numberFieldRadius.getValue());
		parameters.setParamValueByName("FENCE_MARGIN",
				numberFieldMargin.getValue());
		parameters.setParamValueByName("FENCE_ALT_MAX",
				numberFieldAltitude.getValue());
	}

	@Override
	protected void updatePanelInfo() {
		if (parameters == null)
			return;

		chkbxFenceEnable.setChecked(parameters
				.getParamValueByName("FENCE_ENABLE") > 0 ? true : false);

		spinnerFenceType.setSelection(
				getSpinnerIndexFromValue(
						(int) parameters.getParamValueByName("FENCE_TYPE"),
						optionsType.values), true);
		spinnerFenceAction.setSelection(
				getSpinnerIndexFromValue(
						(int) parameters.getParamValueByName("FENCE_ACTION"),
						optionsAction.values), true);

		numberFieldRadius.setValue(parameters
				.getParamValueByName("FENCE_RADIUS"));
		numberFieldMargin.setValue(parameters
				.getParamValueByName("FENCE_MARGIN"));
		numberFieldAltitude.setValue(parameters
				.getParamValueByName("FENCE_ALT_MAX"));

	}

	@Override
	public void setupLocalViews(View v) {
		chkbxFenceEnable = (CheckBox) v.findViewById(R.id.chkbxFence);

		spinnerFenceType = (Spinner) v.findViewById(R.id.spinnerFenceType);
		spinnerFenceAction = (Spinner) v.findViewById(R.id.spinnerFenceAction);

		numberFieldRadius = (NumberFieldEdit) v
				.findViewById(R.id.numberFieldEditRadius);
		numberFieldMargin = (NumberFieldEdit) v
				.findViewById(R.id.numberFieldEditMargin);
		numberFieldAltitude = (NumberFieldEdit) v
				.findViewById(R.id.numberFieldEditAltitude);

		setupSpinners();
	}

	private void setupSpinners() {
		optionsType = ValueKey.getOptions(parentActivity,
				R.array.Fence_Type_Options);
		optionsAction = ValueKey.getOptions(parentActivity,
				R.array.Fence_Action_Options);

		final ArrayAdapter<String> adapterType = new ArrayAdapter<String>(
				parentActivity, R.layout.spinner_setup_item, optionsType.keys);
		adapterType
				.setDropDownViewResource(R.layout.spinner_setup_item_dropdown);

		final ArrayAdapter<String> adapterAction = new ArrayAdapter<String>(
				parentActivity, R.layout.spinner_setup_item, optionsAction.keys);
		adapterAction
				.setDropDownViewResource(R.layout.spinner_setup_item_dropdown);

		spinnerFenceAction.setAdapter(adapterAction);
		spinnerFenceType.setAdapter(adapterType);

	}

}
