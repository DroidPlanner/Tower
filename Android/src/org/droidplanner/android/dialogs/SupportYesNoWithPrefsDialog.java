package org.droidplanner.android.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;

import org.droidplanner.android.R;
import org.droidplanner.android.utils.analytics.GAUtils;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;

/**
 * Created by Fredia Huya-Kouadio on 6/17/15.
 */
public class SupportYesNoWithPrefsDialog extends SupportYesNoDialog {

    private final static int PREFERENCE_ASK_ID = R.string.pref_dialog_entry_ask;
    private final static int PREFERENCE_ALWAYS_ID = R.string.pref_dialog_entry_always;
    private final static int PREFERENCE_NEVER_ID = R.string.pref_dialog_entry_never;

    private final static int DEFAULT_PREFERENCE_ID = PREFERENCE_ASK_ID;

    protected final static String EXTRA_PREF_KEY = "extra_dialog_pref_key";


    public static SupportYesNoWithPrefsDialog newInstance(Context context, String dialogTag,
                                                          String title,
                                                          String msg, String prefKey, Listener listener) {
        return newInstance(context, dialogTag, title, msg, context.getString(android.R.string.yes),
                context.getString(android.R.string.no), prefKey, listener);
    }

    public static SupportYesNoWithPrefsDialog newInstance(Context context, String dialogTag,
                                                          String title, String msg,
                                                          String positiveLabel, String negativeLabel,
                                                          String prefKey, Listener listener) {
        if (dialogTag == null)
            throw new IllegalArgumentException("The dialog tag must not be null!");

        if (!TextUtils.isEmpty(prefKey)) {
            final DroidPlannerPrefs prefs = DroidPlannerPrefs.getInstance(context);
            final String preference = prefs.prefs.getString(prefKey, context.getString(DEFAULT_PREFERENCE_ID));

            if (!preference.equals(context.getString(PREFERENCE_ASK_ID))) {
                if (listener != null) {
                    if (preference.equals(context.getString(PREFERENCE_ALWAYS_ID))) {
                        listener.onDialogYes(dialogTag);
                    } else if (preference.equals(context.getString(PREFERENCE_NEVER_ID))) {
                        listener.onDialogNo(dialogTag);
                    }
                }

                return null;
            }
        }

        SupportYesNoWithPrefsDialog dialog = new SupportYesNoWithPrefsDialog();

        Bundle b = new Bundle();
        b.putString(EXTRA_DIALOG_TAG, dialogTag);
        b.putString(EXTRA_TITLE, title);
        b.putString(EXTRA_MESSAGE, msg);
        b.putString(EXTRA_POSITIVE_LABEL, positiveLabel);
        b.putString(EXTRA_NEGATIVE_LABEL, negativeLabel);
        b.putString(EXTRA_PREF_KEY, prefKey);

        dialog.setArguments(b);

        return dialog;
    }

    protected DroidPlannerPrefs mPrefs;
    protected CheckBox mCheckbox;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = DroidPlannerPrefs.getInstance(getActivity());
    }

    @Override
    protected AlertDialog.Builder buildDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = super.buildDialog(savedInstanceState);

        final Bundle arguments = getArguments();
        final String prefKey = arguments.getString(EXTRA_PREF_KEY);
        if (TextUtils.isEmpty(prefKey)) {
            return builder;
        }

        final String dialogTag = arguments.getString(EXTRA_DIALOG_TAG);

        builder.setPositiveButton(arguments.getString(EXTRA_POSITIVE_LABEL), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                savePreferences(prefKey, true);
                if (mListener != null)
                    mListener.onDialogYes(dialogTag);
            }
        }).setNegativeButton(arguments.getString(EXTRA_NEGATIVE_LABEL), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                savePreferences(prefKey, false);
                if (mListener != null)
                    mListener.onDialogNo(dialogTag);
            }
        });

        return builder;
    }

    private void savePreferences(final String prefKey, final boolean isPositiveResponse) {
        if (mCheckbox != null) {
            final SharedPreferences.Editor editor = mPrefs.prefs.edit();
            final boolean dontShow = mCheckbox.isChecked();
            if (dontShow) {
                Toast.makeText(getActivity(), R.string.pref_dialog_selection_reset_desc, Toast.LENGTH_LONG).show();
                editor.putString(prefKey,
                        getString(isPositiveResponse ? PREFERENCE_ALWAYS_ID : PREFERENCE_NEVER_ID));
            } else {
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

    private boolean isDontShowEnabled(String prefKey) {
        final String askSelection = getString(R.string.pref_dialog_entry_ask);
        final String preference = mPrefs.prefs.getString(prefKey, askSelection);

        return !preference.equals(askSelection);
    }
}
