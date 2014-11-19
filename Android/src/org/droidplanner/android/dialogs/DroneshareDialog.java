package org.droidplanner.android.dialogs;

import org.droidplanner.R;
import org.droidplanner.android.utils.analytics.GAUtils;
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

import com.google.android.gms.analytics.HitBuilders;

public class DroneshareDialog extends DialogFragment {

    private static final String DRONESHARE_PROMPT_ACTION = "droneshare_prompt";

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
		final RadioButton createNew = (RadioButton) root.findViewById(R.id.radioCreateNew);
		final RadioButton loginExisting = (RadioButton) root.findViewById(R.id.radioLoginExisting);
		final RadioButton noDroneshare = (RadioButton) root.findViewById(R.id.radioNoDroneshare);

		username.setText(prefs.getDroneshareLogin());
		password.setText(prefs.getDronesharePassword());
		email.setText(prefs.getDroneshareEmail());
		username.requestFocus();

		if (prefs.getDroneshareEnabled()) {
			if (!prefs.getDroneshareLogin().isEmpty() && !prefs.getDronesharePassword().isEmpty())
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
                        final HitBuilders.SocialBuilder socialBuilder = new HitBuilders
                                .SocialBuilder()
                                .setNetwork(GAUtils.Category.DRONESHARE)
                                .setAction(DRONESHARE_PROMPT_ACTION);
						if (noDroneshare.isChecked()) {
                            prefs.setDroneshareEnabled(false);
                            socialBuilder.setTarget("disabled");
                        }
						else {
							prefs.setDroneshareEnabled(true);
							prefs.setDroneshareLogin(username.getText().toString());
							prefs.setDronesharePassword(password.getText().toString());
							prefs.setDroneshareEmail(email.getText().toString());

                            if(createNew.isChecked()){
                                socialBuilder.setTarget("sign up");
                            }
                            else if(loginExisting.isChecked()){
                                socialBuilder.setTarget("login");
                            }
						}

                        GAUtils.sendEvent(socialBuilder);
					}
				});

		return builder.create();
	}

    @Override
    public void onDismiss(DialogInterface dialog){
        super.onDismiss(dialog);

        final HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder()
                .setCategory(GAUtils.Category.DRONESHARE)
                .setAction(DRONESHARE_PROMPT_ACTION)
                .setLabel("droneshare prompt dismissed");

        GAUtils.sendEvent(eventBuilder);
    }

	static public void perhapsShow(FragmentActivity parent) {
		DroidPlannerPrefs prefs = new DroidPlannerPrefs(parent);

		int numRuns = 10; // Don't pester the user until they have played with the app some...
		if (prefs.getNumberOfRuns() > numRuns
				&& prefs.getDroneshareEnabled()
				&& (prefs.getDroneshareLogin().isEmpty() || prefs.getDronesharePassword().isEmpty())) {
	//		Create an instance of the dialog fragment and show it
			DialogFragment dialog = new DroneshareDialog();
			dialog.show(parent.getSupportFragmentManager(), "DroneshareDialog");

            final HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder()
                    .setCategory(GAUtils.Category.DRONESHARE)
                    .setAction(DRONESHARE_PROMPT_ACTION)
                    .setLabel("droneshare prompt shown");
            GAUtils.sendEvent(eventBuilder);
        }
	}
}
