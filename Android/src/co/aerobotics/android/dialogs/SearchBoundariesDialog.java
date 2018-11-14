package co.aerobotics.android.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import co.aerobotics.android.DroidPlannerApp;
import co.aerobotics.android.R;
import co.aerobotics.android.data.BoundaryDetail;
import co.aerobotics.android.data.NameWithId;
import co.aerobotics.android.data.SQLiteDatabaseHandler;

/**
 * Created by michaelwootton on 10/9/17.
 */

public class SearchBoundariesDialog extends DialogFragment {


    private String selectedBoundaryId = "";
    private OnGoToBoundaryListener onGoToBoundaryListener;



    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        final MixpanelAPI mMixpanel = MixpanelAPI.getInstance(this.getActivity(), DroidPlannerApp.getInstance().getMixpanelToken());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        SharedPreferences sharedPref = getActivity().getSharedPreferences(getActivity().getResources().getString(R.string.com_dji_android_PREF_FILE_KEY), Context.MODE_PRIVATE);
        final SQLiteDatabaseHandler sqLiteDatabaseHandler = new SQLiteDatabaseHandler(getActivity().getApplicationContext());
        final FragmentManager fragmentManager = getFragmentManager();
        View view = inflater.inflate(R.layout.fragment_boundary_search, null);
        builder.setView(view);
        builder.setPositiveButton(R.string.go, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                //get selected boundary detail
                if(!Objects.equals(selectedBoundaryId, "")) {
                    BoundaryDetail boundaryDetail = sqLiteDatabaseHandler.getBoundaryDetail(selectedBoundaryId);
                    final JSONObject properties = new JSONObject();
                    try {
                        properties.put("Boundary ID", (boundaryDetail.getBoundaryId()));
                        properties.put("Boundary Name", boundaryDetail.getName());
                        mMixpanel.track("FPA: TapGoToBoundary", properties);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    //convert string poly points to list<LatLong>
                    String points = boundaryDetail.getPoints();
                    String[] latLongPairs = points.split(" ");
                    List<LatLong> polygonPoints = convertToLatLongList(latLongPairs);
                    //pass list to MmapFragent.zoomfit
                    onGoToBoundaryListener.OnGoToBoundaySelected(polygonPoints);
                }
            }
        });

        builder.setNegativeButton(R.string.cancel, null);

        List<BoundaryDetail> boundaryDetailList = sqLiteDatabaseHandler
                .getBoundaryDetailsForFarmIds(sharedPref.getString(getActivity().getApplicationContext().getResources()
                        .getString(R.string.active_farms), "[]")
                        .replaceAll("\\[", "").replaceAll("\\]",""));
        ArrayList<NameWithId> nameWithIds = new ArrayList<NameWithId>();

        for (BoundaryDetail boundaryDetail : boundaryDetailList){
            nameWithIds.add(new NameWithId(boundaryDetail.getName(), boundaryDetail.getBoundaryId()));
        }

        Collections.sort(nameWithIds, NameWithId.Comparators.NAME);

        final ArrayAdapter<NameWithId> boundariesAdapter = new ArrayAdapter<NameWithId>(getActivity(), R.layout.spinner_add_boundary, nameWithIds);
        boundariesAdapter.setDropDownViewResource(R.layout.spinner_add_boundary_drop_down);

        final SearchableSpinner boundariesSpinner = (SearchableSpinner) view.findViewById(R.id.selectBoundarySpinner);
        boundariesSpinner.setAdapter(boundariesAdapter);

        boundariesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                NameWithId nameWithId = (NameWithId) adapterView.getItemAtPosition(i);
                selectedBoundaryId = nameWithId.id;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        boundariesSpinner.setPositiveButton(null);

        final AlertDialog dialog = builder.create();
        return dialog;
    }

    public interface OnGoToBoundaryListener{
        void OnGoToBoundaySelected(List<LatLong> polygonPoints);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            onGoToBoundaryListener = (OnGoToBoundaryListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    private List<LatLong> convertToLatLongList(String[] points){

        List<LatLong> path = new ArrayList<LatLong>();
        for (int i=0; i<points.length; i++ ){
            String[] point = points[i].split(",");
            path.add(new LatLong(Double.parseDouble(point[1]), Double.parseDouble(point[0])));
        }
        return path;
    }

}
