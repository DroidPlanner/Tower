package org.droidplanner.android.fragments.account;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    private final DroneshareClient dshareClient = new DroneshareClient();
    private DroidPlannerPrefs prefs;

    private AccountLoginListener loginListener;

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        if(!(activity instanceof AccountLoginListener)){
            throw new IllegalStateException("Parent must implement " + AccountLoginListener.class.getName());
        }

        loginListener = (AccountLoginListener) activity;
    }

    @Override
    public void onDetach(){
        super.onDetach();
        loginListener = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = new DroidPlannerPrefs(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.droneshare_account, container, false);
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
                final String usernameText = username.getText().toString();
                final String passwordText = password.getText().toString();
                new AsyncConnect(false).execute(usernameText, passwordText);
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
                final String usernameText = username.getText().toString();
                final String passwordText = password.getText().toString();
                final String emailText = email.getText().toString();
                new AsyncConnect(true).execute(usernameText, passwordText, emailText);
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
                    }

                    return Pair.create(loginResult, "Login successful!");
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
            progressDialog.dismiss();
        }

        @Override
        protected void onPostExecute(Pair<Boolean, String> result) {
            progressDialog.dismiss();
            Toast.makeText(getActivity(), result.second, Toast.LENGTH_LONG).show();
            if(result.first)
                loginListener.onLogin();
            else
                loginListener.onFailedLogin();
        }
    }
}
