package org.droidplanner.android.fragments.helpers;

import org.droidplanner.R;
import org.droidplanner.android.activities.interfaces.HelpProvider;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * Used to show the help options to the user.
 */
public class HelpDialogFragment extends DialogFragment {

	/**
	 * Provide access to user help.
	 */
	private HelpProvider mHelpProvider;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (!(activity instanceof HelpProvider)) {
			throw new IllegalStateException("Parent activity must implement "
					+ HelpProvider.class.getName());
		}

		mHelpProvider = (HelpProvider) activity;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final Activity activity = (Activity) mHelpProvider;
		return new AlertDialog.Builder(activity)
				.setTitle(R.string.help)
				.setItems(mHelpProvider.getHelpItems()[0],
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								activity.startActivity(new Intent(
										Intent.ACTION_VIEW,
										Uri.parse(mHelpProvider.getHelpItems()[1][which]
												.toString())));
							}
						}).create();
	}

	public static HelpDialogFragment newInstance() {
		return new HelpDialogFragment();
	}
}
