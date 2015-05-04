package org.droidplanner.android.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;

import org.droidplanner.android.R;
import org.droidplanner.android.utils.analytics.GAUtils;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;

/**
 * Provides a yes/no (ok/cancel) dialog with the option to permanently disable
 * the dialog with a checkbox.
 */
public class YesNoWithPrefsDialog extends YesNoDialog {

    private final static int PREFERENCE_ASK_ID = R.string.pref_dialog_entry_ask;
    private final static int PREFERENCE_ALWAYS_ID = R.string.pref_dialog_entry_always;
    private final static int PREFERENCE_NEVER_ID = R.string.pref_dialog_entry_never;

    private final static int DEFAULT_PREFERENCE_ID = PREFERENCE_ASK_ID;

	protected final static String EXTRA_PREF_KEY = "extra_dialog_pref_key";


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
            final String preference = prefs.prefs.getString(prefKey,
                    context.getString(DEFAULT_PREFERENCE_ID));

            if(!preference.equals(context.getString(PREFERENCE_ASK_ID))) {
                if(listener != null){
                    if (preference.equals(context.getString(PREFERENCE_ALWAYS_ID))) {
                        listener.onYes();
                    }
                    else if(preference.equals(context.getString(PREFERENCE_NEVER_ID))){
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
        if(mCheckbox != null) {
            final SharedPreferences.Editor editor = mPrefs.prefs.edit();
            final boolean dontShow = mCheckbox.isChecked();
            if(dontShow){
                Toast.makeText(getActivity(), R.string.pref_dialog_selection_reset_desc, Toast.LENGTH_LONG).show();
                        editor.putString(prefKey,
                                getString(isPositiveResponse ? PREFERENCE_ALWAYS_ID : PREFERENCE_NEVER_ID));
            }
            else{
                editor.putString(prefKey, getString(PREFERENCE_ASK_ID));
            }

            editor.apply();

            HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder()
                    .setCategory(GAUtils.Category.PREFERENCE_DIALOGS)
                    .setAction(getArguments().getString(EXTRA_TITLE))
                    .setLabel("Response: " + (isPositiveResponse ? "Yes" : "No") + (dontShow ?
                            " (Always)" : " (Just once)"));
            GAUtils.sendEvent(eventBuilder);
        }
    }

	@Override
	protected View generateContentView(Bundle savedInstanceState) {
		final View contentView = super.generateContentView(savedInstanceState);
		if (contentView == null) {
			return null;
		}

		final String prefKey = getArguments().getString(EXTRA_PREF_KEY);
		if (prefKey == null || prefKey.isEmpty()) {
			return contentView;
		}

		mCheckbox = (CheckBox) contentView.findViewById(R.id.yes_no_dont_show_checkbox);
		mCheckbox.setVisibility(View.VISIBLE);
		mCheckbox.setChecked(isDontShowEnabled(prefKey));

		return contentView;
	}

    private boolean isDontShowEnabled(String prefKey){
        final String askSelection = getString(R.string.pref_dialog_entry_ask);
        final String preference = mPrefs.prefs.getString(prefKey, askSelection);

        return !preference.equals(askSelection);
    }
}
