package co.aerobotics.android.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import co.aerobotics.android.DroidPlannerApp;
import co.aerobotics.android.R;
import co.aerobotics.android.activities.interfaces.APIContract;
import co.aerobotics.android.data.AeroviewPolygons;
import co.aerobotics.android.data.Authentication;
import co.aerobotics.android.data.Login;
import co.aerobotics.android.data.PostRequest;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

/**
 * Created by michaelwootton on 9/5/17.
 */

public class SignUpActivity extends AppCompatActivity {
    private UserSignUpTask mAuthTask = null;

    private EditText mEmailView;
    private EditText mPasswordView;
    private EditText mPasswordConfirmView;
    private EditText mFirstNameView;
    private EditText mLastNameView;
    private Button mSignUpButton;
    private View mSignUpForm;
    private View mProgressView;

    private MixpanelAPI mixpanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        mixpanel = MixpanelAPI.getInstance(this, DroidPlannerApp.getInstance().getMixpanelToken());
        mSignUpForm = findViewById(R.id.signup_form);
        mProgressView = findViewById(R.id.signup_progress_bar);

        mEmailView = (EditText) findViewById(R.id.signup_email);
        mPasswordView = (EditText) findViewById(R.id.signup_password);
        mPasswordConfirmView = (EditText) findViewById(R.id.signup_password_confirm);
        mFirstNameView = (EditText) findViewById(R.id.first_name);
        mLastNameView = (EditText) findViewById(R.id.last_name);
        final ImageView mImageView = (ImageView) findViewById(R.id.aeroview_signup);

        mSignUpButton = (Button) findViewById(R.id.signup_button);
        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSignUp();
            }
        });

        final View signupView = (View) findViewById(R.id.activity_signup_root);
        signupView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                //r will be populated with the coordinates of your view that area still visible.
                signupView.getWindowVisibleDisplayFrame(r);

                int heightDiff = signupView.getRootView().getHeight() - (r.bottom - r.top);
                if (heightDiff > 300) {
                    mImageView.setVisibility(View.GONE);
                } else {
                    mImageView.setVisibility(View.VISIBLE);
                }
            }
        });

        ClickableSpan onTermsCLicked = new ClickableSpan() {
            @Override
            public void onClick(View view) {

            }
        };

        SpannableString terms = new SpannableString(getString(R.string.terms_and_conditions));
        terms.setSpan(onTermsCLicked, 47, 67, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    @Override
    protected void onPause(){
        super.onPause();
        overridePendingTransition(0,0);
    }

    private void attemptSignUp(){
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        boolean cancel = false;
        View focusView = null;
        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String passwordConfirm = mPasswordConfirmView.getText().toString();
        String firstName = mFirstNameView.getText().toString();
        String lastName = mLastNameView.getText().toString();


        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (!isPasswordValid(password)){
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;

        }

        if (TextUtils.isEmpty(passwordConfirm)) {
            mPasswordConfirmView.setError(getString(R.string.error_field_required));
            focusView = mPasswordConfirmView;
            cancel = true;
        } else if (!isPasswordValid(passwordConfirm)){
            mPasswordConfirmView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordConfirmView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (TextUtils.isEmpty(firstName)){
            mFirstNameView.setError(getString(R.string.error_field_required));
            focusView = mFirstNameView;
            cancel = true;
        }

        if (TextUtils.isEmpty(lastName)){
            mLastNameView.setError(getString(R.string.error_field_required));
            focusView = mLastNameView;
            cancel = true;
        }

        if(!confirmPasswordSame(password, passwordConfirm)){
            mPasswordView.setError(getString(R.string.error_passwords_not_match));
            mPasswordConfirmView.clearFocus();
            focusView = mPasswordView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        }else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            showProgress(true);
            mAuthTask = new SignUpActivity.UserSignUpTask(email, firstName, lastName, password);
            mAuthTask.execute((Void) null);
        }

    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        if (email == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
        }
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() >= 1;
    }

    private boolean confirmPasswordSame(String password, String passwordConfirm){
        return Objects.equals(password, passwordConfirm);
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mSignUpForm.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
            mSignUpForm.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mSignUpForm.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
            mSignUpForm.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
        }
    }


    private class UserSignUpTask extends AsyncTask<Void, Void, Boolean> implements APIContract{

        private String email;
        private String firstName;
        private String lastName;
        private String password;

        UserSignUpTask(String email, String firstName, String lastName, String password){

            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            this.password = password;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            Authentication authentication = new Authentication(SignUpActivity.this.getApplicationContext());
            if (authentication.createUser(firstName, lastName, email, email, password)) {
                Login login = new Login(SignUpActivity.this.getApplicationContext(), email, password);
                return login.authenticateUser();
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                AeroviewPolygons aeroviewPolygons = new AeroviewPolygons(SignUpActivity.this);
                aeroviewPolygons.executeGetCropTypesTask();
                aeroviewPolygons.executeGetCropFamiliesTask();
                Intent intent = new Intent(SignUpActivity.this, FarmManagerActivity.class);
                SignUpActivity.this.startActivity(intent);
                finish();
            }
        }
    }
}
