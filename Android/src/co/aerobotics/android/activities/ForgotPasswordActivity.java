package co.aerobotics.android.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONException;

import co.aerobotics.android.DroidPlannerApp;
import co.aerobotics.android.R;
import co.aerobotics.android.activities.interfaces.APIContract;
import co.aerobotics.android.data.PostRequest;

public class ForgotPasswordActivity extends AppCompatActivity {

    private Button requestPasswordButton;
    private EditText emailView;
    private UserPasswordTask mRequestTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        emailView = (EditText) findViewById(R.id.request_email);

        requestPasswordButton = (Button) findViewById(R.id.request_passord_button);

        requestPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestNewPassword();
            }
        });
    }

    private void requestNewPassword(){
        if(mRequestTask != null){
            return;
        }

        emailView.setError(null);
        String email = emailView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(email)) {
            emailView.setError(getString(R.string.error_field_required));
            focusView = emailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            emailView.setError(getString(R.string.error_invalid_email));
            focusView = emailView;
            cancel = true;
        }
        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {

            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }

            mRequestTask = new UserPasswordTask(email);
            mRequestTask.execute();
        }

    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }


    private class UserPasswordTask extends AsyncTask<Void, Void, Boolean> implements APIContract{

        private String email;

        UserPasswordTask(String email){
            this.email = email;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            String jsonStr = String.format("{\"email\":\"%s\"}",email);

            PostRequest postRequest = new PostRequest();
            postRequest.post(jsonStr, APIContract.GATEWAY_PASSWORD_RESET, "null");

            do {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (!postRequest.isServerResponseReceived());

            return !postRequest.isServerError();
        }

        @Override
        protected void onPostExecute(final Boolean success){
            mRequestTask = null;

            if (success){
                setResultToToast("An email has been sent with further instructions");
                Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                ForgotPasswordActivity.this.startActivity(intent);
                finish();
            } else {
                setResultToToast("Request failed");
            }

        }
    }

    private void setResultToToast(final String string) {
        ForgotPasswordActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ForgotPasswordActivity.this, string, Toast.LENGTH_LONG).show();
            }
        });
    }
}
