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
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.mission.item.complex.Survey;
import com.o3dr.services.android.lib.drone.mission.item.complex.SurveyDetail;
import com.toptoche.searchablespinnerlibrary.SearchableListDialog;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import org.apache.commons.lang3.text.WordUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import co.aerobotics.android.DroidPlannerApp;
import co.aerobotics.android.R;
import co.aerobotics.android.data.AeroviewPolygons;
import co.aerobotics.android.data.BoundaryDetail;
import co.aerobotics.android.data.PostBoundary;
import co.aerobotics.android.data.SQLiteDatabaseHandler;
import co.aerobotics.android.graphic.map.PolygonData;
import co.aerobotics.android.proxy.mission.item.MissionItemProxy;

/**
 * Created by michaelwootton on 9/18/17.
 */

public class AddBoundaryCheckDialog extends DialogFragment {

    private Map<String, Integer> cropTypeMap = new HashMap<>();
    private Map<String, Integer> farmMap = new HashMap<>();
    private String selectedCropType;
    private String selectedFarm;
    private SharedPreferences sharedPref;
    private EditText mBoundaryNameView;
    private EditText mCropTypeView;
    private Button mAddNewCropTypeButton;
    private SQLiteDatabaseHandler sqLiteDatabaseHandler;
    private String newCropType;
    private String newFarmName;
    private SearchableSpinner searchableSpinnerFarmName;
    private Integer clientId;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        final MixpanelAPI mMixpanel = MixpanelAPI.getInstance(this.getActivity(), DroidPlannerApp.getInstance().getMixpanelToken());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_boundary, null);
        builder.setView(view);
        sharedPref = getActivity().getSharedPreferences(getActivity().getResources().getString(R.string.com_dji_android_PREF_FILE_KEY), Context.MODE_PRIVATE);
        clientId = sharedPref.getInt(getActivity().getResources().getString(R.string.client_id), -1);
        sqLiteDatabaseHandler = new SQLiteDatabaseHandler(this.getContext());

        mBoundaryNameView = (EditText) view.findViewById(R.id.boundary_name);

        searchableSpinnerFarmName = (SearchableSpinner) view.findViewById(R.id.searchable_spinner_farmname);
        final SearchableSpinner searchableSpinnerCropType = (SearchableSpinner) view.findViewById(R.id.searchable_spinner_croptype);

        searchableSpinnerFarmName.setFocusable(true);
        searchableSpinnerFarmName.setFocusableInTouchMode(true);

        List<String> sortedFarms = sqLiteDatabaseHandler.getAllFarmNames(clientId);
        Collections.sort(sortedFarms, String.CASE_INSENSITIVE_ORDER);

        if (sortedFarms.isEmpty()){
            sortedFarms.add("");
        }

        final ArrayAdapter<String> farmAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_add_boundary, sortedFarms);
        farmAdapter.setDropDownViewResource(R.layout.spinner_add_boundary_drop_down);
        searchableSpinnerFarmName.setAdapter(farmAdapter);
        searchableSpinnerFarmName.setTitle("Select Farm Name");
        searchableSpinnerFarmName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedFarm = (String) searchableSpinnerFarmName.getSelectedItem();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        searchableSpinnerFarmName.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(newFarmName!=null && !Objects.equals(newFarmName, "")){
                    String newFarmNameCaps = WordUtils.capitalizeFully(newFarmName);
                    sqLiteDatabaseHandler.createFarmName(newFarmNameCaps, null, clientId);
                    farmAdapter.add(newFarmNameCaps);
                    farmAdapter.sort(String.CASE_INSENSITIVE_ORDER);
                    int position = farmAdapter.getPosition(newFarmNameCaps);
                    farmAdapter.notifyDataSetChanged();
                    searchableSpinnerFarmName.setSelection(position);
                }
            }
        });

        searchableSpinnerFarmName.setOnSearchTextChangedListener(new SearchableListDialog.OnSearchTextChanged() {
            @Override
            public void onSearchTextChanged(String strText) {
                newFarmName = strText.trim();
            }
        });

        //getDataForSpinner("crop_types");
        List<String> sortedCropTypes = sqLiteDatabaseHandler.getAllCropTypes();
        Collections.sort(sortedCropTypes, String.CASE_INSENSITIVE_ORDER);

        final ArrayAdapter<String> cropTypeAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_add_boundary,sortedCropTypes);
        cropTypeAdapter.setDropDownViewResource(R.layout.spinner_add_boundary_drop_down);

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

        searchableSpinnerCropType.setOnSearchTextChangedListener(new SearchableListDialog.OnSearchTextChanged() {
            @Override
            public void onSearchTextChanged(String strText) {
                newCropType = strText.trim();

            }
        });


        builder.setPositiveButton(R.string.save, null);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        mBoundaryNameView.setError(null);
                        String boundaryName = mBoundaryNameView.getText().toString();

                        boolean cancel = false;
                        View focusView = null;

                        if (TextUtils.isEmpty(boundaryName)) {
                            mBoundaryNameView.setError(getString(R.string.error_field_required));
                            focusView = mBoundaryNameView;
                            cancel = true;
                        }

                        if(Objects.equals(selectedFarm, "")){
                            //searchableSpinnerFarmName.setError("Select a Farm Name or create a new one");
                            TextView errorText = (TextView) searchableSpinnerFarmName.getSelectedView();
                            errorText.setError("");
                            errorText.setTextColor(Color.RED);//just to highlight that this is an error
                            errorText.setText("Add New Farm");
                            focusView = searchableSpinnerFarmName;
                            cancel = true;
                        }
                        if (cancel) {
                            // There was an error; don't attempt login and focus the first
                            // form field with an error.
                            focusView.requestFocus();
                        } else {
                            List<MissionItemProxy> selectedMissionItems = DroidPlannerApp.getInstance().getMissionProxy().selection.getSelected();
                            if(!selectedMissionItems.isEmpty()) {
                                MissionItemProxy mission = DroidPlannerApp.getInstance().getMissionProxy().selection.getSelected().get(0);
                                PostBoundary postBoundary = new PostBoundary(AddBoundaryCheckDialog.this.getContext(),
                                        (Survey) mission.getMissionItem(),
                                        mBoundaryNameView.getText().toString(),
                                        getFarmId(), getCropTypeId(), getClientId(),
                                        getEmail(), getPassword(), getFarmArray(), getCropTypeArray(), null);

                                if (isNetworkAvailable()) {
                                    postBoundary.post(buildBoundaryDetail(mission));
                                    mMixpanel.track("FPA: BoundarySaved");
                                    //new AeroviewPolygons(getActivity()).executeClientDataTask();
                                } else {

                                    String tempId = sqLiteDatabaseHandler.addOfflineBoundaryDetail(buildBoundaryDetail(mission));
                                    PostBoundary postOfflineBoundary = new PostBoundary(getContext().getApplicationContext(),
                                            (Survey) mission.getMissionItem(),
                                            mBoundaryNameView.getText().toString(),
                                            getFarmId(), getCropTypeId(), getClientId(),
                                            getEmail(), getPassword(), getFarmArray(), getCropTypeArray(), tempId);

                                    JSONObject jsonObject = postOfflineBoundary.generateJson();
                                    sqLiteDatabaseHandler.addRequestToOfflineBoundary(tempId, jsonObject.toString());

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
                });
            }
        });
        return dialog;
    }


    private String getEmail(){
       return sharedPref.getString(getActivity().getResources().getString(R.string.username), "");
    }

    private String getPassword(){
        return sharedPref.getString(getActivity().getResources().getString(R.string.password), "");
    }

    private Integer getClientId(){
        String jsonString = sharedPref.getString(getActivity().getResources().getString(R.string.json_data), "");
        Integer clientId = null;
        try {
            JSONObject json = new JSONObject(jsonString);
            clientId = (Integer) json.get("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return clientId;
    }

    private Integer getFarmId(){
        return sqLiteDatabaseHandler.getFarmNameId(selectedFarm, clientId);
    }

    private Integer getCropTypeId(){
        return sqLiteDatabaseHandler.getCropTypeId(selectedCropType);
    }

    private JSONArray getFarmArray (){
        return sqLiteDatabaseHandler.getLocalFarmNames(clientId);
    }

    private JSONArray getCropTypeArray(){
        return sqLiteDatabaseHandler.getLocalCropTypes();
    }

    private String getPolygonCoords(){
        Survey survey = (Survey) DroidPlannerApp.getInstance().getMissionProxy().selection.getSelected().get(0).getMissionItem();
        List<LatLong> points = survey.getPolygonPoints();
        StringBuilder pointListAsString = new StringBuilder();
        for (LatLong point : points){
            pointListAsString.append(String.format("%s,%s ", String.valueOf(point.getLongitude()),
                    String.valueOf(point.getLatitude())));
        }
        return pointListAsString.toString().trim();
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

    private BoundaryDetail buildBoundaryDetail(MissionItemProxy mission){
        SharedPreferences sharedPref = getContext().getApplicationContext().getSharedPreferences(getContext().getApplicationContext().getResources().getString(R.string.com_dji_android_PREF_FILE_KEY),Context.MODE_PRIVATE);

        BoundaryDetail boundaryDetail = new BoundaryDetail();
        boundaryDetail.setName(mBoundaryNameView.getText().toString());
        boundaryDetail.setBoundaryId(null);
        boundaryDetail.setPoints(convertPolyListToString(getPolygonPoints()));
        SurveyDetail surveyDetail = ((Survey) mission.getMissionItem()).getSurveyDetail();
        boundaryDetail.setAngle(surveyDetail.getAngle());
        boundaryDetail.setAltitude(surveyDetail.getAltitude());
        boundaryDetail.setOverlap(surveyDetail.getOverlap());
        boundaryDetail.setSidelap(surveyDetail.getSidelap());
        boundaryDetail.setSpeed(surveyDetail.getSpeed());
        boundaryDetail.setCamera(surveyDetail.getCameraDetail().toString());
        boundaryDetail.setClientId(sharedPref.getInt(getContext().getApplicationContext().getResources().getString(R.string.client_id), -1));
        boundaryDetail.setDisplay(true);
        return boundaryDetail;

    }
}
