package co.aerobotics.android.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import co.aerobotics.android.DroidPlannerApp;
import co.aerobotics.android.R;
import co.aerobotics.android.activities.interfaces.APIContract;
import co.aerobotics.android.data.NameWithId;
import co.aerobotics.android.data.PostRequest;
import co.aerobotics.android.data.SQLiteDatabaseHandler;

/**
 * Created by michaelwootton on 6/7/18.
 */

public class AddNewFarmDialog extends DialogFragment implements APIContract {

    private SharedPreferences sharedPreferences;
    private EditText farmNarmView;
    private SQLiteDatabaseHandler sqLiteDatabaseHandler;
    private ArrayAdapter<NameWithId> cropFamiliesAdapter;
    private Spinner cropFamilySpinner;
    private NameWithId selectedCropFamily = new NameWithId("Select crop type", "-1");

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
        List<NameWithId> cropFamilies = getCropFamilies();
        cropFamilies.add(0, selectedCropFamily);
        cropFamiliesAdapter = new ArrayAdapter<NameWithId>(getActivity(), R.layout.spinner_add_boundary, cropFamilies);
        cropFamiliesAdapter.setDropDownViewResource(R.layout.spinner_add_boundary_drop_down);
        cropFamilySpinner = (Spinner) view.findViewById(R.id.cropFamilySpinner);
        cropFamilySpinner.setAdapter(cropFamiliesAdapter);
        cropFamilySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                NameWithId selectedItem = (NameWithId) cropFamilySpinner.getItemAtPosition(i);
                Integer id = (Integer.valueOf(selectedItem.id));
                if (id != -1) {
                    selectedCropFamily = selectedItem;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
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
                        boolean formValid = true;
                        if (farmNarmView.getText().toString().trim().equalsIgnoreCase("")) {
                            farmNarmView.setError("Farm name required");
                            farmNarmView.requestFocus();
                            formValid = false;
                        }
                        Integer id = Integer.valueOf(selectedCropFamily.id);
                        if (id == -1) {
                            formValid = false;
                            TextView errorText = (TextView) cropFamilySpinner.getSelectedView();
                            errorText.setError("");
                            errorText.setTextColor(Color.RED);//just to highlight that this is an error
                            errorText.setText("Crop type required");
                            cropFamilySpinner.requestFocus();
                        }

                        if (formValid) {
                            if (DroidPlannerApp.getInstance().isNetworkAvailable()) {
                                final PostRequest postRequest = new PostRequest();
                                postRequest.setOnPostReturnedListener(new PostRequest.OnPostReturnedListener() {
                                    @Override
                                    public void onSuccessfulResponse() {
                                        JSONObject returnData = postRequest.getResponseData();
                                        try {
                                            String cropFamilyIds = returnData.getJSONArray("crop_family_ids").toString().replaceAll("\\[", "").replaceAll("]","");
                                            sqLiteDatabaseHandler.createFarmName(returnData.getString("name"), returnData.getInt("id"), returnData.getInt("client_id"), cropFamilyIds);
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
                    }
                });
            }
        });
        return dialog;
    }

    private List<NameWithId> getCropFamilies() {
        List<NameWithId> cropFamilies = sqLiteDatabaseHandler.getCropFamilies();
        Collections.sort(cropFamilies, NameWithId.Comparators.NAME);
        return cropFamilies;
    }

    private String getToken() {
        Context context = this.getActivity();
        return sharedPreferences.getString(context.getResources().getString(R.string.user_auth_token), "");
    }

    private JSONObject getPostParams() {
        int clientId = getClientId();
        String farmName = getFarmNameFromView();
        JSONObject jsonObject = new JSONObject();
        JSONArray cropFamilyIds = new JSONArray();
        cropFamilyIds.put(Integer.parseInt(selectedCropFamily.id));
        try {
            jsonObject.put("name", farmName);
            jsonObject.put("client_id", clientId);
            jsonObject.put("crop_family_ids", cropFamilyIds);
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
