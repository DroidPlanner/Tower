package org.droidplanner.android.proxy.mission.item.fragments;

import org.droidplanner.R;
import org.droidplanner.android.widgets.spinnerWheel.CardWheelHorizontalView;
import org.droidplanner.android.widgets.spinnerWheel.adapters.NumericWheelAdapter;
import org.droidplanner.core.mission.MissionItem;
import org.droidplanner.core.mission.MissionItemType;
import org.droidplanner.core.mission.commands.SetServo;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

public class SetServoFragment extends MissionDetailFragment implements
		CardWheelHorizontalView.OnCardWheelChangedListener, TextWatcher {

	@Override
	protected int getResource() {
		return R.layout.fragment_editor_detail_set_servo;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		typeSpinner.setSelection(commandAdapter
				.getPosition(MissionItemType.SET_SERVO));

		SetServo item = (SetServo) getMissionItems().get(0);

		final NumericWheelAdapter adapter = new NumericWheelAdapter(
				getActivity().getApplicationContext(),
				R.layout.wheel_text_centered, 1, 8, "%d");
		final CardWheelHorizontalView cardChannelPicker = (CardWheelHorizontalView) view
				.findViewById(R.id.picker1);
		final EditText pwmEditText = (EditText) view
				.findViewById(R.id.PwmEditText);

		cardChannelPicker.setViewAdapter(adapter);
		cardChannelPicker.addChangingListener(this);
		cardChannelPicker.setCurrentValue((int) item.getChannel());

		pwmEditText.setText(Integer.toString(item.getPwm()));
		pwmEditText.addTextChangedListener(this);

	}

	@Override
	public void onChanged(CardWheelHorizontalView wheel, int oldValue,
			int newValue) {
		switch (wheel.getId()) {
		case R.id.picker1:
			for (MissionItem missionItem : getMissionItems()) {
				SetServo item = (SetServo) missionItem;
				item.setChannel(newValue);
			}
			break;
		}
	}

	@Override
	public void afterTextChanged(Editable editable) {

	}

	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
			int arg3) {

	}

	@Override
	public void onTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
		if (!s.toString().isEmpty()) {
			for (MissionItem missionItem : getMissionItems()) {
				SetServo item = (SetServo) missionItem;
				item.setPwm(Integer.valueOf(s.toString()));
			}
		}

	}
}
