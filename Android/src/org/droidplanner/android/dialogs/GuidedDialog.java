package org.droidplanner.android.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.google.android.gms.maps.model.LatLng;

import org.droidplanner.R;

public class GuidedDialog extends DialogFragment {

	public interface GuidedDialogListener {
		public void onForcedGuidedPoint(LatLng coord);
	}

	private GuidedDialogListener listener;
	private LatLng coord;

	public void setCoord(LatLng coord) {
		this.coord = coord;
	}

	public void setListener(GuidedDialogListener mListener) {
		this.listener = mListener;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		builder.setMessage(R.string.guided_mode_warning)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								if (coord != null) {
									listener.onForcedGuidedPoint(coord);
								}
							}
						}).setNegativeButton(android.R.string.cancel, null);

		return builder.create();
	}
}