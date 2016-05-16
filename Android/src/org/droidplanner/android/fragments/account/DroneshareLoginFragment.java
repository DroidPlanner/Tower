package org.droidplanner.android.fragments.account;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.droidplanner.android.R;
import org.droidplanner.android.activities.interfaces.AccountLoginListener;
import org.droidplanner.android.utils.connection.DroneshareClient;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;

import java.io.IOException;

public class DroneshareLoginFragment extends Fragment {

    private static final String DRONESHARE_PROMPT_ACTION = "droneshare_prompt";
    private static final short DRONESHARE_MIN_PASSWORD = 7;
    private final DroneshareClient dshareClient = new DroneshareClient();
    private DroidPlannerPrefs prefs;

    private AccountLoginListener loginListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof AccountLoginListener)) {
            throw new IllegalStateException("Parent must implement " + AccountLoginListener.class.getName());
        }

        loginListener = (AccountLoginListener) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        loginListener = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = DroidPlannerPrefs.getInstance(getActivity().getApplicationContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_droneshare_login, container, false);
    }

    @Override
    public void onViewCreated(View root, Bundle savedInstanceState) {
        super.onViewCreated(root, savedInstanceState);

        final EditText username = (EditText) root.findViewById(R.id.username);
        final EditText password = (EditText) root.findViewById(R.id.password);

        final View loginSection = root.findViewById(R.id.login_box);
        loginSection.setVisibility(View.VISIBLE);

        final View signupSection = root.findViewById(R.id.signup_box);
        signupSection.setVisibility(View.GONE);

        // Login section
        final Button loginButton = (Button) root.findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Validate required fields
                if (validateField(username) && validateField(password)) {
                    final String usernameText = username.getText().toString();
                    final String passwordText = password.getText().toString();

                    // Hide the soft keyboard, otherwise can remain after logging in.
                    hideSoftInput();
                    new AsyncConnect(false).execute(usernameText, passwordText);
                }
            }
        });

        final TextView signupToggle = (TextView) root.findViewById(R.id.switch_to_signup);
        signupToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginSection.setVisibility(View.GONE);
                signupSection.setVisibility(View.VISIBLE);
            }
        });

        //Signup section
        final EditText email = (EditText) root.findViewById(R.id.email);
        final Button signupButton = (Button) root.findViewById(R.id.signup_button);
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If we have all required fields...
                if (validateField(username) && validateField(password) && validateField(email)) {
                    final String usernameText = username.getText().toString();
                    final String passwordText = password.getText().toString();
                    final String emailText = email.getText().toString();

                    // Hide the soft keyboard, otherwise can remain after logging in.
                    hideSoftInput();

                    new AsyncConnect(true).execute(usernameText, passwordText, emailText);
                }
            }
        });

        final TextView loginToggle = (TextView) root.findViewById(R.id.switch_to_login);
        loginToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginSection.setVisibility(View.VISIBLE);
                signupSection.setVisibility(View.GONE);
            }
        });

        username.setText(prefs.getDroneshareLogin());
        password.setText(prefs.getDronesharePassword());
        email.setText(prefs.getDroneshareEmail());

        // Field validation - we need to add these after the fields have been set
        // otherwise validation will fail on empty fields (ugly).
        username.addTextChangedListener(new TextValidator(username) {
            @Override public void validate(TextView textView, String text) {
                // TODO: validate on acceptable characters, etc
                if(text.length() == 0)
                    textView.setError("Please enter a username");
                else
                    textView.setError(null);
            }
        });

        password.addTextChangedListener(new TextValidator(password) {
            @Override public void validate(TextView textView, String text) {
                if(loginSection.getVisibility() == View.VISIBLE && (
                    text.length() < 1))
                    // Since some accounts have been created with < 7 and no digits, allow login
                    textView.setError("Please enter a password");
               else if(loginSection.getVisibility() == View.GONE && (
                        text.length() < DRONESHARE_MIN_PASSWORD || !text.matches(".*\\d+.*")))
                    // New accounts require at least 7 characters and digit
                    textView.setError("Use at least 7 characters and one digit");
                else
                    textView.setError(null);
            }
        });

        email.addTextChangedListener(new TextValidator(email) {
            @Override public void validate(TextView textView, String text) {
                if(text.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(text).matches())
                    textView.setError("Please enter a valid email");
                else
                    textView.setError(null);
            }
        });

        // Save to prefs on save
//        builder.setView(root)
//                // Add action buttons
//                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int id) {
//                        final HitBuilders.SocialBuilder socialBuilder = new HitBuilders
//                                .SocialBuilder()
//                                .setNetwork(GAUtils.Category.DRONESHARE)
//                                .setAction(DRONESHARE_PROMPT_ACTION);
//                        if (noDroneshare.isChecked()) {
//                            prefs.setDroneshareEnabled(false);
//                            socialBuilder.setTarget("disabled");
//                        } else {
//                            prefs.setDroneshareEnabled(true);
//                            prefs.setDroneshareLogin(username.getText().toString());
//                            prefs.setDronesharePassword(password.getText().toString());
//                            prefs.setDroneshareEmail(email.getText().toString());
//
//                            if (createNew.isChecked()) {
//                                socialBuilder.setTarget("sign up");
//                            } else if (loginExisting.isChecked()) {
//                                socialBuilder.setTarget("login");
//                            }
//                        }
//
//                        GAUtils.sendEvent(socialBuilder);
//                    }
//                });
    }

//    @Override
//    public void onDismiss(DialogInterface dialog) {
//        super.onDismiss(dialog);
//
//        final HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder()
//                .setCategory(GAUtils.Category.DRONESHARE)
//                .setAction(DRONESHARE_PROMPT_ACTION)
//                .setLabel("droneshare prompt dismissed");
//
//        GAUtils.sendEvent(eventBuilder);
//    }

    private void hideSoftInput() {
        // Hide the soft keyboard and unfocus any active inputs.
        final Activity activity = getActivity();
        View view = activity.getCurrentFocus();
        if (view != null) {
            final InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputManager != null)
                inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private Boolean validateField(EditText field) {
        // Trigger text changed to see if we have any errors:
        field.setText(field.getText());
        return field.getError() == null;
    }

    private static abstract class TextValidator implements TextWatcher {
        // Wrapper for TextWatcher, providing a shorthand method of field specific validations
        private final TextView textView;

        public TextValidator(TextView textView) {
            this.textView = textView;
        }

        public abstract void validate(TextView textView, String text);

        @Override
        final public void afterTextChanged(Editable s) {
            String text = textView.getText().toString();
            validate(textView, text);
        }

        @Override
        final public void beforeTextChanged(CharSequence s, int start, int count, int after) { /* Don't care */ }

        @Override
        final public void onTextChanged(CharSequence s, int start, int before, int count) { /* Don't care */ }
    }

    private class AsyncConnect extends AsyncTask<String, Void, Pair<Boolean, String>> {

        private final boolean isSignup;
        private final ProgressDialog progressDialog;

        AsyncConnect(boolean isSignup) {
            this.isSignup = isSignup;
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setIndeterminate(true);
            progressDialog.setTitle(isSignup ? "Signing up..." : "Logging...");
        }

        @Override
        protected Pair<Boolean, String> doInBackground(String... params) {
            final String username = params[0];
            final String password = params[1];

            try {
                if (isSignup) {
                    final String email = params[2];
                    final int signupResult = dshareClient.signupUser(username, password, email);
                    switch (signupResult) {
                        case DroneshareClient.SIGNUP_SUCCESSFUL:
                            prefs.setDroneshareEmail(email);
                            prefs.setDroneshareLogin(username);
                            prefs.setDronesharePassword(password);
                            return Pair.create(true, "Account creation successful!");

                        case DroneshareClient.SIGNUP_USERNAME_NOT_AVAILABLE:
                            return Pair.create(false, "Username is not available.");

                        case DroneshareClient.SIGNUP_FAILED:
                        default:
                            return Pair.create(false, "Account creation failed.");
                    }
                } else {
                    boolean loginResult = dshareClient.login(username, password);
                    if (loginResult) {
                        prefs.setDroneshareLogin(username);
                        prefs.setDronesharePassword(password);
                        return Pair.create(true, "Login successful!");
                    }
                    else{
                        return Pair.create(false, "Login failed!");
                    }
                }
            } catch (IOException e) {
                return Pair.create(false, e.getMessage());
            }
        }

        @Override
        protected void onPreExecute() {
            progressDialog.show();
        }

        @Override
        protected void onCancelled() {
            if (progressDialog.isShowing())
                progressDialog.dismiss();
        }

        @Override
        protected void onPostExecute(Pair<Boolean, String> result) {
            if (progressDialog.isShowing())
                progressDialog.dismiss();

            if (loginListener != null) {
                Toast.makeText(getActivity(), result.second, Toast.LENGTH_LONG).show();
                if (result.first)
                    loginListener.onLogin();
                else
                    loginListener.onFailedLogin();
            }
        }
    }
}
