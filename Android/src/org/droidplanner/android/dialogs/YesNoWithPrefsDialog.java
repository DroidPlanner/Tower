package org.droidplanner.android.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import org.droidplanner.R;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;

/**
 * Provides a yes/no (ok/cancel) dialog with the option to permanently disable
 * the dialog with a checkbox.
 */
public class YesNoWithPrefsDialog extends YesNoDialog {

	protected final static String EXTRA_PREF_KEY = "extra_dialog_pref_key";

	protected final static boolean DEFAULT_DONT_SHOW_DIALOG = false;
    private static final boolean DEFAULT_DIALOG_RESPONSE = false;

    public static YesNoWithPrefsDialog newInstance(Context context, String title,
			String msg, Listener listener, String prefKey) {
        return newInstance(context, title, msg, context.getString(android.R.string.yes),
                context.getString(android.R.string.no), listener, prefKey);
	}

    public static YesNoWithPrefsDialog newInstance(Context context, String title, String msg,
                                                   String positiveLabel, String negativeLabel,
                                                   Listener listener, String prefKey){
        if (prefKey != null && !prefKey.isEmpty()) {
            final DroidPlannerPrefs prefs = new DroidPlannerPrefs(context);
            final boolean dontShow = prefs.prefs.getBoolean(getDontShowPrefKey(prefKey),
                    DEFAULT_DONT_SHOW_DIALOG);
            if (dontShow) {
                if(listener != null) {
                    final boolean response = prefs.prefs.getBoolean(getResponsePrefKey(prefKey),
                            DEFAULT_DIALOG_RESPONSE);
                    if (response) {
                        listener.onYes();
                    }
                    else{
                        listener.onNo();
                    }
                }
                return null;
            }
        }

        YesNoWithPrefsDialog dialog = new YesNoWithPrefsDialog();

        Bundle b = new Bundle();
        b.putString(EXTRA_TITLE, title);
        b.putString(EXTRA_MESSAGE, msg);
        b.putString(EXTRA_POSITIVE_LABEL, positiveLabel);
        b.putString(EXTRA_NEGATIVE_LABEL, negativeLabel);
        b.putString(EXTRA_PREF_KEY, prefKey);

        dialog.setArguments(b);

        dialog.mListener = listener;

        return dialog;
    }

	protected DroidPlannerPrefs mPrefs;
    protected CheckBox mCheckbox;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPrefs = new DroidPlannerPrefs(getActivity());
	}

	@Override
	protected AlertDialog.Builder buildDialog(Bundle savedInstanceState) {
		final AlertDialog.Builder builder = super.buildDialog(savedInstanceState);

        final Bundle arguments = getArguments();
		final String prefKey = arguments.getString(EXTRA_PREF_KEY);
		if (prefKey == null || prefKey.isEmpty()) {
			return builder;
		}

		builder.setPositiveButton(arguments.getString(EXTRA_POSITIVE_LABEL), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
                savePreferences(prefKey, true);
				mListener.onYes();
			}
		}).setNegativeButton(arguments.getString(EXTRA_NEGATIVE_LABEL), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
                savePreferences(prefKey, false);
				mListener.onNo();
			}
		});

		return builder;
	}

    private void savePreferences(final String prefKey, final boolean isPositiveResponse){
        final SharedPreferences.Editor editor = mPrefs.prefs.edit();

        if(mCheckbox != null) {
            editor.putBoolean(getDontShowPrefKey(prefKey), mCheckbox.isChecked());
        }

        editor.putBoolean(getResponsePrefKey(prefKey), isPositiveResponse).apply();
    }

	@Override
	protected View generateContentView(Bundle savedInstanceState) {
		final View contentView = super.generateContentView(savedInstanceState);
		if (contentView == null) {
			return contentView;
		}

		final String prefKey = getArguments().getString(EXTRA_PREF_KEY);
		if (prefKey == null || prefKey.isEmpty()) {
			return contentView;
		}

		final String dontShowPrefKey = getDontShowPrefKey(prefKey);
		final boolean isChecked = mPrefs.prefs
				.getBoolean(dontShowPrefKey, DEFAULT_DONT_SHOW_DIALOG);

		mCheckbox = (CheckBox) contentView.findViewById(R.id.yes_no_dont_show_checkbox);
		mCheckbox.setVisibility(View.VISIBLE);
		mCheckbox.setChecked(isChecked);

		return contentView;
	}

    private static String getDontShowPrefKey(String basePrefKey){
        return basePrefKey + "_dont_show";
    }

    private static String getResponsePrefKey(String basePrefKey){
        return basePrefKey + "_response";
    }

}
