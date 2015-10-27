package org.droidplanner.android.activities;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.droidplanner.android.R;
import org.droidplanner.android.maps.providers.google_map.GoogleMapPrefFragment;
import org.droidplanner.android.utils.connection.DroneKitCloudClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.swagger.client.ApiException;
import io.swagger.client.model.RecapProperties;
import io.swagger.client.model.RecapResult;
import io.swagger.client.model.SceneResult;

/**
 * Created by chavi on 10/23/15.
 */
public class GetRecapImageActivity extends DrawerNavigationUI {
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private DroneKitCloudClient defaultApi = new DroneKitCloudClient();
    private Handler handler = new Handler();

    private TextView contentText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_get_recap_image);

        contentText = (TextView) findViewById(R.id.get_recap_image_text);
        Button getStatus = (Button) findViewById(R.id.get_status);
        Button getRequest = (Button) findViewById(R.id.get_request);
        Button getUrl = (Button) findViewById(R.id.get_image_url);

        getStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRecapStatus();
            }
        });

        getRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRecapRequest();
            }
        });

        getUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRecapUrl();
            }
        });
    }

    @Override
    protected int getNavigationDrawerMenuItemId() {
        return R.id.navigation_get_recap_image;
    }

    @Override
    protected int getToolbarId() {
        return R.id.actionbar_toolbar;
    }

    private void getRecapStatus() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                String lastRecapId = GoogleMapPrefFragment.PrefManager.getLastRecapId(getApplicationContext());
                String token = GoogleMapPrefFragment.PrefManager.getDroneKitToken(getApplicationContext());
                try {
                    RecapProperties recapProp = defaultApi.actionsRecapRecapIdPropertiesGet(lastRecapId, "key", token);
                    updateText(recapProp.toString());
                } catch (ApiException e) {
                    updateText("Failed to get recap job " + e + " code: " + e.getCode());
                }
            }
        });
    }

    private void getRecapRequest() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                String lastRecapId = GoogleMapPrefFragment.PrefManager.getLastRecapId(getApplicationContext());
                String token = GoogleMapPrefFragment.PrefManager.getDroneKitToken(getApplicationContext());
                try {
                    RecapResult recapProp = defaultApi.actionsRecapRecapIdGet(lastRecapId, "key", token);
                    updateText(recapProp.toString());
                } catch (ApiException e) {
                    updateText("Failed to get recap job " + e + " code: " + e.getCode());
                }
            }
        });
    }

    private void getRecapUrl() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                String lastRecapId = GoogleMapPrefFragment.PrefManager.getLastRecapId(getApplicationContext());
                String token = GoogleMapPrefFragment.PrefManager.getDroneKitToken(getApplicationContext());
                try {
                    SceneResult recapResult = defaultApi.actionsRecapRecapIdResultsGet(lastRecapId, "key", token);
                    updateText(recapResult.toString());
                } catch (ApiException e) {
                    updateText("Failed to get recap job " + e + " code: " + e.getCode());
                }
            }
        });
    }

    private void updateText(final String text) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                contentText.setText(text);
            }
        });
    }
}
