package co.aerobotics.android.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLClientInfoException;

import co.aerobotics.android.DroidPlannerApp;
import co.aerobotics.android.R;
import co.aerobotics.android.activities.interfaces.APIContract;
import co.aerobotics.android.data.PostRequest;
import co.aerobotics.android.data.SQLiteDatabaseHandler;

/**
 * Created by michaelwootton on 6/7/18.
 */

public class AddNewFarmDialog extends DialogFragment implements APIContract {

    private SharedPreferences sharedPreferences;
    private EditText farmNarmView;
    private SQLiteDatabaseHandler sqLiteDatabaseHandler;

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState){
        Context context = this.getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_new_farm, null);
        sharedPreferences = context.getSharedPreferences(context.getResources().getString(R.string.com_dji_android_PREF_FILE_KEY), Context.MODE_PRIVATE);
        sqLiteDatabaseHandler = new SQLiteDatabaseHandler(getActivity().getApplicationContext());
        farmNarmView = (EditText) view.findViewById(R.id.newFarmNameText);
        builder.setView(view);
        builder.setPositiveButton(R.string.save, null);
        builder.setNegativeButton(R.string.cancel, null);
        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (DroidPlannerApp.getInstance().isNetworkAvailable()) {
                            final PostRequest postRequest = new PostRequest();
                            postRequest.setOnPostReturnedListener(new PostRequest.OnPostReturnedListener() {
                                @Override
                                public void onSuccessfulResponse() {
                                    JSONObject returnData = postRequest.getResponseData();
                                    try {
                                        sqLiteDatabaseHandler.createFarmName(returnData.getString("name"), returnData.getInt("id"), returnData.getInt("client_id"));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    dialog.dismiss();
                                }

                                @Override
                                public void onErrorResponse() {
                                    dialog.dismiss();
                                }
                            });
                            postRequest.postJSONObject(getPostParams(), APIContract.GATEWAY_FARMS, getToken());
                        } else {
                            sqLiteDatabaseHandler.addOfflineFarm(getFarmNameFromView(), getClientId());
                            dialog.dismiss();
                        }
                    }
                });
            }
        });
        return dialog;
    }

    private String getToken() {
        Context context = this.getActivity();
        return sharedPreferences.getString(context.getResources().getString(R.string.user_auth_token), "");
    }
    private JSONObject getPostParams() {
        int clientId = getClientId();
        String farmName = getFarmNameFromView();
        JSONObject jsonObject = new JSONObject();
        // String params = String.format("{\"name\":\"%s\",\"client_id\":\"%s\"}",farmName, clientId);
        try {
            jsonObject.put("name", farmName);
            jsonObject.put("client_id", clientId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;

    }

    private String getFarmNameFromView() {
        return farmNarmView.getText().toString();
    }

    private int getClientId() {
        Context context = this.getActivity();
        return sharedPreferences.getInt(context.getResources().getString(R.string.client_id), -1);
    }
}
