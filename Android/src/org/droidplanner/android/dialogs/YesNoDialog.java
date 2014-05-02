package org.droidplanner.android.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class YesNoDialog extends DialogFragment {

	public interface Listener {
		void onYes();

		void onNo();
	}

	public static YesNoDialog newInstance(String title, String msg,
			Listener listener) {
		YesNoDialog f = new YesNoDialog();
		Bundle b = new Bundle();
		b.putString("title", title);
		b.putString("message", msg);
		f.setArguments(b);
		f.mListener = listener;
		return f;
	}

	private Listener mListener;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder b = new AlertDialog.Builder(getActivity())
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(getArguments().getString("title"))
				.setMessage(getArguments().getString("message"))
				.setPositiveButton(android.R.string.yes,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								mListener.onYes();
							}
						})
				.setNegativeButton(android.R.string.no,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								mListener.onNo();
							}
						});

		return b.create();
	}
}
