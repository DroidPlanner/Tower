package org.droidplanner.android.fragments.calibration;

import org.droidplanner.R;
import org.droidplanner.android.fragments.helpers.SuperSetupFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * This fragment displays the progress for the RC setup process.
 */
public class FragmentSetupProgress extends SetupSidePanel {
	private TextView textTitle;
	private TextView textProgress;
	private TextView textAction;
	private TextView textDesc;
	private ProgressBar pb;
	private int titleId = 0, descId = 0;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		final SuperSetupFragment setupFragment = (SuperSetupFragment) getParentFragment();

		final View view = inflater.inflate(
				R.layout.fragment_setup_panel_progress, container, false);

		textTitle = (TextView) view.findViewById(R.id.setupTitle);
		textDesc = (TextView) view.findViewById(R.id.setupDesc);
		textAction = (TextView) view.findViewById(R.id.setupProgressTitle);
		textProgress = (TextView) view.findViewById(R.id.setupProgressRatio);

		if (titleId != 0 && textTitle != null)
			textTitle.setText(titleId);
		if (descId != 0)
			textDesc.setText(descId);

		textProgress.setText("0/0");

		pb = (ProgressBar) view.findViewById(R.id.setupProgressBar);
		pb.setIndeterminate(true);

		final Button btnCancel = (Button) view.findViewById(R.id.buttonCancel);
		btnCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (setupFragment != null) {
					setupFragment.doCalibrationStep(0);
				}
			}
		});

		return view;
	}

	public void updateProgress(int index, int count, String txt) {
		if (textAction != null) {
			textAction.setText(txt);
		}

		if (pb != null) {
			pb.setIndeterminate(false);
			pb.setMax(count);
			pb.setProgress(index);
		}

		if (textProgress != null) {
			textProgress.setText(String.valueOf(index) + "/"
					+ String.valueOf(count));
		}
	}

	@Override
	public void updateDescription(int idDescription) {
		this.descId = idDescription;
		if (textDesc != null)
			textDesc.setText(idDescription);
	}

	@Override
	public void updateTitle(int idTitle) {
		this.titleId = idTitle;
		if (textTitle != null)
			textTitle.setText(idTitle);
	}

}
