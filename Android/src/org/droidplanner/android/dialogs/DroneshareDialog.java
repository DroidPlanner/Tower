package org.droidplanner.android.dialogs;

import org.droidplanner.R;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;

public class DroneshareDialog extends DialogFragment {
	// FIXME - move to oncreate
	private DroidPlannerPrefs prefs;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		prefs = new DroidPlannerPrefs(getActivity());
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();

		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		View root = inflater.inflate(R.layout.droneshare_account, null);

		// init from prefs
		final EditText username = (EditText) root.findViewById(R.id.username);
		final EditText password = (EditText) root.findViewById(R.id.password);
		final EditText email = (EditText) root.findViewById(R.id.email);
		final RadioButton createNew = (RadioButton) root
				.findViewById(R.id.radioCreateNew);
		final RadioButton loginExisting = (RadioButton) root
				.findViewById(R.id.radioLoginExisting);
		final RadioButton noDroneshare = (RadioButton) root
				.findViewById(R.id.radioNoDroneshare);

		username.setText(prefs.getDroneshareLogin());
		password.setText(prefs.getDronesharePassword());
		email.setText(prefs.getDroneshareEmail());
		username.requestFocus();

		if (prefs.getDroneshareEnabled()) {
			if (!prefs.getDroneshareLogin().isEmpty()
					&& !prefs.getDronesharePassword().isEmpty())
				loginExisting.setSelected(true);
			else
				createNew.setSelected(true);
		} else
			noDroneshare.setSelected(true);

		// Save to prefs on save
		builder.setView(root)
		// Add action buttons
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						if (noDroneshare.isChecked())
							prefs.setDroneshareEnabled(false);
						else {
							prefs.setDroneshareEnabled(true);
							prefs.setDroneshareLogin(username.getText()
									.toString());
							prefs.setDronesharePassword(password.getText()
									.toString());
							prefs.setDroneshareEmail(email.getText().toString());
						}
					}
				});

		return builder.create();
	}

	static public void perhapsShow(FragmentActivity parent) {
		DroidPlannerPrefs prefs = new DroidPlannerPrefs(parent);

		int numRuns = 10; // Don't pester the user until they have played with
							// the app some...
		if (prefs.getNumberOfRuns() > numRuns
				&& prefs.getDroneshareEnabled()
				&& (prefs.getDroneshareLogin().isEmpty() || prefs
						.getDronesharePassword().isEmpty())) {
			// Create an instance of the dialog fragment and show it
			DialogFragment dialog = new DroneshareDialog();
			dialog.show(parent.getSupportFragmentManager(), "DroneshareDialog");
		}
	}
}
