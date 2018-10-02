package co.aerobotics.android.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.mission.item.complex.Survey;
import com.o3dr.services.android.lib.drone.mission.item.complex.SurveyDetail;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import co.aerobotics.android.DroidPlannerApp;
import co.aerobotics.android.R;
import co.aerobotics.android.activities.interfaces.APIContract;
import co.aerobotics.android.data.AeroviewPolygons;
import co.aerobotics.android.data.BoundaryDetail;
import co.aerobotics.android.data.Farm;
import co.aerobotics.android.data.PostRequest;
import co.aerobotics.android.data.SQLiteDatabaseHandler;
import co.aerobotics.android.graphic.map.PolygonData;
import co.aerobotics.android.proxy.mission.item.MissionItemProxy;

/**
 * Created by michaelwootton on 9/18/17.
 */

public class AddBoundaryCheckDialog extends DialogFragment implements APIContract {

    private String selectedCropType;
    private Farm selectedFarm;
    private SharedPreferences sharedPref;
    private SQLiteDatabaseHandler sqLiteDatabaseHandler;
    private SearchableSpinner searchableSpinnerFarmName;
    private ArrayAdapter<Farm> farmAdapter;
    private ArrayAdapter<String> cropTypeAdapter;
    private View view;
    private List<Farm> sortedFarms = new ArrayList<>();
    private List<String> sortedCropTypes;
    private Context context;
    private List<Farm> sortedSelectedFarms = new ArrayList<>();


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        final MixpanelAPI mMixpanel = MixpanelAPI.getInstance(this.getActivity(), DroidPlannerApp.getInstance().getMixpanelToken());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.dialog_add_boundary, null);
        sharedPref = getActivity().getSharedPreferences(getActivity().getResources().getString(R.string.com_dji_android_PREF_FILE_KEY), Context.MODE_PRIVATE);
        sqLiteDatabaseHandler = new SQLiteDatabaseHandler(this.getContext());
        context = getActivity().getApplicationContext();
        //sortFarmNamesAlphabetically();
        //initializeFarmAdapter();
        getAllFarmsAccessibleToActiveClient();
        getCropTypes();
        getFarmsSelectedByClient();
        initializeSelectedFarmAdapter();
        initializeFarmNameSpinner();
        initializeCropTypeAdapter();
        initializeCropTypeSpinner();
        final AlertDialog dialog = buildDialog();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = (dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    private boolean formInvalid = false;
                    private View focusView = null;
                    private EditText mBoundaryNameView;
                    @Override
                    public void onClick(View view) {
                        initializeBoundaryNameView();
                        String boundaryName = getBoundaryNameFromView(mBoundaryNameView);
                        if (isBoundaryNameTextEmpty(boundaryName)) {
                            setBoundaryViewErrorMessage(mBoundaryNameView);
                            invalidateForm();
                        }

                        if(isSelectedFarmEmpty()){
                            setFarmNameErrorMessage();
                            setFocusOnFarmSpinner();
                            invalidateForm();
                        }

                        if (formInvalid) {
                            focusView.requestFocus();
                        } else {
                            if(missionItemExists()) {
                                JSONObject postParams = getBoundaryPostParams();
                                if (isNetworkAvailable()) {
                                    makePostRequest(postParams);
                                    mMixpanel.track("FPA: BoundarySaved");
                                    // new AeroviewPolygons(getActivity()).executeClientDataTask();
                                } else {
                                    String tempId = sqLiteDatabaseHandler.addOfflineBoundaryDetail(buildBoundaryDetail(boundaryName, getFarmId()));
                                    try {
                                        postParams.put("temp_id", tempId);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    sqLiteDatabaseHandler.addRequestToOfflineBoundary(tempId, postParams.toString());
                                    PolygonData polygonData = new PolygonData(boundaryName, getPolygonPoints(), false, tempId);
                                    if (DroidPlannerApp.getInstance().polygonMap.get(tempId) == null) {
                                        DroidPlannerApp.getInstance().polygonMap.put(tempId, polygonData);
                                    }
                                    mMixpanel.track("FPA: OfflineBoundary");
                                }
                            }
                            Intent intent = new Intent(AeroviewPolygons.ACTION_POLYGON_UPDATE);
                            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
                            //Dismiss once everything is OK.
                            dialog.dismiss();
                        }
                    }

//                    private void saveBoundaryDetailToLocalDb() {
//                        sqLiteDatabaseHandler.addBoundaryDetail();
//                    }

                    private boolean missionItemExists() {
                        List<MissionItemProxy> selectedMissionItems = DroidPlannerApp.getInstance().getMissionProxy().selection.getSelected();
                        return !selectedMissionItems.isEmpty();
                    }

                    private void makePostRequest(JSONObject postParams) {
                        String token = sharedPref.getString(getContext().getResources().getString(R.string.user_auth_token), "");
                        final PostRequest postRequest = new PostRequest();
                        postRequest.setOnPostReturnedListener(new PostRequest.OnPostReturnedListener() {
                            @Override
                            public void onSuccessfulResponse() {
                                JSONObject json = postRequest.getResponseData();
                                try {
                                    String name = json.getString("name");
                                    String id = json.getString("id");
                                    Integer farmId = json.getInt("farm_id");
                                    BoundaryDetail boundaryDetail = buildBoundaryDetail(name, farmId);
                                    boundaryDetail.setBoundaryId(id);
                                    sqLiteDatabaseHandler.addBoundaryDetail(boundaryDetail);
                                    new AeroviewPolygons(context).addPolygonsToMap();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onErrorResponse() {

                            }
                        });
                        postRequest.postJSONObject(postParams, GATEWAY_ORCHARDS, token);
                    }

                    private void initializeBoundaryNameView() {
                        mBoundaryNameView = (EditText) view.findViewById(R.id.boundary_name);
                        mBoundaryNameView.setError(null);
                    }

                    private String getPolygonCoords(){
                        MissionItemProxy mission = DroidPlannerApp.getInstance().getMissionProxy().selection.getSelected().get(0);
                        Survey survey = (Survey) mission.getMissionItem();
                        List<LatLong> points = survey.getPolygonPoints();
                        StringBuilder pointListAsString = new StringBuilder();
                        for (LatLong point : points){
                            pointListAsString.append(String.format("%s,%s ", String.valueOf(point.getLongitude()),
                                    String.valueOf(point.getLatitude())));
                        }
                        return pointListAsString.toString().trim();
                    }

                    private Context getContext() {
                        return AddBoundaryCheckDialog.this.getContext();
                    }

                    private void setFocusOnFarmSpinner() {
                        focusView = searchableSpinnerFarmName;
                    }

                    private void setBoundaryViewErrorMessage(EditText editTextView) {
                        displayErrorMessage(editTextView);
                        setFocusView(editTextView);
                    }

                    private void setFocusView(EditText editTextView) {
                        focusView = editTextView;
                    }

                    private void invalidateForm() {
                        formInvalid = true;
                    }

                    private boolean isSelectedFarmEmpty() {
                        return Objects.equals(selectedFarm, "") || selectedFarm.getName().equals("");
                    }

                    private void setFarmNameErrorMessage() {
                        TextView errorText = (TextView) searchableSpinnerFarmName.getSelectedView();
                        errorText.setError("");
                        errorText.setTextColor(Color.RED);//just to highlight that this is an error
                        errorText.setText("Farm name required");
                    }

                    private void displayErrorMessage(EditText editTextView) {
                        editTextView.setError(getString(R.string.error_field_required));
                    }

                    private boolean isBoundaryNameTextEmpty(String boundaryName) {
                        return TextUtils.isEmpty(boundaryName);
                    }

                    private String getBoundaryNameFromView(EditText mBoundaryNameView) {
                        return mBoundaryNameView.getText().toString();
                    }

                    private JSONObject getBoundaryPostParams() {
                        JSONObject params = new JSONObject();
                        try {
                            params.put("name", getBoundaryNameFromView(mBoundaryNameView));
                            params.put("polygon", getPolygonCoords());
                            params.put("client_id", getClientId());
                            params.put("farm_id", getFarmId());
                            params.put("crop_type_id", getCropTypeId());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        return params;
                    }

                });
            }
        });
        return dialog;
    }

    private AlertDialog buildDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setPositiveButton(R.string.save, null);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        return builder.create();
    }

    private void initializeCropTypeAdapter() {
        cropTypeAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_add_boundary,sortedCropTypes);
        cropTypeAdapter.setDropDownViewResource(R.layout.spinner_add_boundary_drop_down);
    }

    private void getCropTypes() {
        sortedCropTypes = sqLiteDatabaseHandler.getAllCropTypes();
        Collections.sort(sortedCropTypes, String.CASE_INSENSITIVE_ORDER);
    }

    private void getFarmsSelectedByClient(){
        String activeFarmsString = sharedPref.getString(this.getResources().getString(R.string.active_farms), "[]");
        Type type = new TypeToken<ArrayList<Integer>>() { }.getType();
        List<Integer> selectedFarmIds = new Gson().fromJson(activeFarmsString, type);
        for(Farm farm : sortedFarms) {
            if (selectedFarmIds.contains(farm.getId()))
                sortedSelectedFarms.add(farm);
        }
    }

    private void sortSelectedFarmNamesAlphabetically() {
        if (sortedSelectedFarms.size() > 0) {
            Collections.sort(sortedSelectedFarms, new Comparator<Farm>() {
                @Override
                public int compare(Farm farmA, Farm farmB) {
                    return farmA.getName().toLowerCase().compareTo(farmB.getName().toLowerCase());
                }
            });
        }
    }

    private void initializeSelectedFarmAdapter() {
        if (sortedSelectedFarms.size() > 0) {
            farmAdapter = new ArrayAdapter<Farm>(getActivity(), R.layout.spinner_add_boundary, sortedSelectedFarms);
        } else {
            Farm dummyFarm = new Farm("", -1);
            ArrayList<Farm> dummyFarmList = new ArrayList<>();
            dummyFarmList.add(dummyFarm);
            farmAdapter = new ArrayAdapter<Farm>(getActivity(), R.layout.spinner_add_boundary, dummyFarmList);
        }
        farmAdapter.setDropDownViewResource(R.layout.spinner_add_boundary_drop_down);
    }


    private void getAllFarmsAccessibleToActiveClient() {
        SQLiteDatabaseHandler sqLiteDatabaseHandler = new SQLiteDatabaseHandler(context);
        String allClientIds = sharedPref.getString(this.getResources().getString(R.string.all_client_ids), "")
                .replaceAll("\\[", "").replaceAll("]","");
        List<JSONObject> farmNameIdMap = sqLiteDatabaseHandler.getFarmNamesAndIdList(allClientIds);
        String serviceProviderFarmIdsString = sharedPref.getString(this.getResources().getString(R.string.service_provider_farms), "[]");
        Type type = new TypeToken<ArrayList<Integer>>() { }.getType();
        List<Integer> serviceProviderFarmIds= new Gson().fromJson(serviceProviderFarmIdsString, type);
        for (JSONObject farm: farmNameIdMap) {
            try {
                String farmName = farm.getString("name");
                Integer id = farm.getInt("farm_id");
                if (!serviceProviderFarmIds.contains(id)) {
                    Farm farmObj = new Farm(farmName, id);
                    sortedFarms.add(farmObj);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void sortFarmNamesAlphabetically() {
        if (sortedFarms.size() > 0) {
            Collections.sort(sortedFarms, new Comparator<Farm>() {
                @Override
                public int compare(Farm farmA, Farm farmB) {
                    return farmA.getName().toLowerCase().compareTo(farmB.getName().toLowerCase());
                }
            });
        }
    }

    private void initializeFarmAdapter() {
        farmAdapter = new ArrayAdapter<Farm>(getActivity(), R.layout.spinner_add_boundary, sortedFarms);
        farmAdapter.setDropDownViewResource(R.layout.spinner_add_boundary_drop_down);
    }

    private void initializeFarmNameSpinner() {
        searchableSpinnerFarmName = (SearchableSpinner) view.findViewById(R.id.searchable_spinner_farmname);
        searchableSpinnerFarmName.setFocusable(true);
        searchableSpinnerFarmName.setFocusableInTouchMode(true);
        searchableSpinnerFarmName.setAdapter(farmAdapter);
        searchableSpinnerFarmName.setTitle("Select Farm Name");
        searchableSpinnerFarmName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedFarm = (Farm) searchableSpinnerFarmName.getSelectedItem();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void initializeCropTypeSpinner() {
        final SearchableSpinner searchableSpinnerCropType = (SearchableSpinner) view.findViewById(R.id.searchable_spinner_croptype);
        searchableSpinnerCropType.setAdapter(cropTypeAdapter);
        searchableSpinnerCropType.setTitle("Select Crop Type");
        searchableSpinnerCropType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedCropType = (String) searchableSpinnerCropType.getSelectedItem();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private Integer getClientId(){
        int farmId = getFarmId();
        return sqLiteDatabaseHandler.getFarmClientId(farmId);
    }

    private Integer getFarmId(){
        return selectedFarm.getId();
    }

    private Integer getCropTypeId(){
        return sqLiteDatabaseHandler.getCropTypeId(selectedCropType);
    }

    private List<LatLng> getPolygonPoints(){
        Survey survey = (Survey) DroidPlannerApp.getInstance().getMissionProxy().selection.getSelected().get(0).getMissionItem();
        List<LatLong> points = survey.getPolygonPoints();
        List<LatLng> path = new ArrayList<LatLng>();
        for (LatLong point : points ){
            path.add(new LatLng(point.getLatitude(), point.getLongitude()));
        }
        return path;
    }

    private String convertPolyListToString(List<LatLng> pointsList){
        String points = "";
        for(LatLng point : pointsList){
            points = points.concat(String.format("%s,%s ", String.valueOf(point.longitude), String.valueOf(point.latitude)));
        }
        return points;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private BoundaryDetail buildBoundaryDetail(String boundaryName, Integer farmId) {
        MissionItemProxy mission = DroidPlannerApp.getInstance().getMissionProxy().selection.getSelected().get(0);
        SurveyDetail surveyDetail = ((Survey) mission.getMissionItem()).getSurveyDetail();
        BoundaryDetail boundaryDetail = new BoundaryDetail();
        boundaryDetail.setFarmId(farmId);
        boundaryDetail.setName(boundaryName);
        boundaryDetail.setBoundaryId(null);
        List<LatLng> polyPoints = getPolygonPoints();
        String polyPointsString = convertPolyListToString(polyPoints);
        boundaryDetail.setPoints(polyPointsString);
        boundaryDetail.setAngle(surveyDetail.getAngle());
        boundaryDetail.setAltitude(surveyDetail.getAltitude());
        boundaryDetail.setOverlap(surveyDetail.getOverlap());
        boundaryDetail.setSidelap(surveyDetail.getSidelap());
        boundaryDetail.setSpeed(surveyDetail.getSpeed());
        boundaryDetail.setCamera(surveyDetail.getCameraDetail().toString());
        boundaryDetail.setClientId(getClientId());
        boundaryDetail.setCropTypeId(getCropTypeId());
        return boundaryDetail;
    }
}
