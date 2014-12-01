package org.droidplanner.android.proxy.mission.item.fragments;

import org.droidplanner.R;
import org.droidplanner.android.widgets.spinnerWheel.CardWheelHorizontalView;
import org.droidplanner.android.widgets.spinnerWheel.adapters.NumericWheelAdapter;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.o3dr.services.android.lib.drone.mission.MissionItemType;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.item.command.SetServo;

public class SetServoFragment extends MissionDetailFragment implements
		CardWheelHorizontalView.OnCardWheelChangedListener, TextWatcher {

	@Override
	protected int getResource() {
		return R.layout.fragment_editor_detail_set_servo;
	}

	@Override
	public void onApiConnected() {
		super.onApiConnected();

        final View view = getView();
		typeSpinner.setSelection(commandAdapter.getPosition(MissionItemType.SET_SERVO));

		SetServo item = (SetServo) getMissionItems().get(0);

		final NumericWheelAdapter adapter = new NumericWheelAdapter(
				getActivity().getApplicationContext(),	R.layout.wheel_text_centered, 1, 8, "%d");
		final CardWheelHorizontalView cardChannelPicker = (CardWheelHorizontalView) view
				.findViewById(R.id.picker1);
		final EditText pwmEditText = (EditText) view.findViewById(R.id.PwmEditText);

		cardChannelPicker.setViewAdapter(adapter);
		cardChannelPicker.addChangingListener(this);
		cardChannelPicker.setCurrentValue(item.getChannel());

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
