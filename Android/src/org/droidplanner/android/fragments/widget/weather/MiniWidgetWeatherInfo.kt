package org.droidplanner.android.fragments.widget.weather

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.gms.location.LocationServices
import com.o3dr.services.android.lib.util.googleApi.GoogleApiClientManager
import com.squareup.okhttp.Request
import org.droidplanner.android.R
import org.droidplanner.android.fragments.widget.TowerWidget
import org.droidplanner.android.fragments.widget.TowerWidgets
import org.droidplanner.android.utils.NetworkUtils
import org.droidplanner.android.utils.unit.UnitManager
import org.droidplanner.android.utils.unit.systems.UnitSystem
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.io.IOException

/**
 * Created by fredia on 5/22/16.
 */
class MiniWidgetWeatherInfo : TowerWidget() {

    companion object {
        //TODO: update the weather api token for version 4.1.0. The current one is not suited for production
        private const val WEATHER_API_TOKEN = "a855ec2770848d99"

        private val JSON_LABEL_CURRENT_WEATHER = "current_observation"
        private val JSON_LABEL_DISPLAY_LOCATION = "display_location"
        private val JSON_LABEL_REQUEST_EPOCH = "local_epoch"           // Unix EPOCH (milliseconds)
        private val JSON_LABEL_WIND_SPEED_IMPERIAL = "wind_gust_mph"   // Miles per hour
        private val JSON_LABEL_WIND_SPEED_METRIC = "wind_gust_kph"     // Kilometers per hour
        private val JSON_LABEL_WIND_DIRECTION = "wind_dir"             // Compass facing
        private val JSON_LABEL_PRECIPITATION_IMPERIAL = "precip_today_in"   // in
        private val JSON_LABEL_PRECIPITATION_METRIC = "precip_today_metric"   // mm

        private val JSON_LABEL_TEMPERATURE_IMPERIAL = "temp_f"         // Fahrenheit
        private val JSON_LABEL_TEMPERATURE_METRIC = "temp_c"           // Celsius
        private val JSON_LABEL_FULL_LOCATION = "full"

        private val WEATHER_REQUEST_OBSOLETE_TIME_FRAME = 300000          // 5 minutes in milliseconds

        private val NO_WEATHER_DATA = "--"

        private val filter = IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION)
    }

    private val handler = Handler()
    private val gapiClientManager by lazy {
        GoogleApiClientManager(getContext(), handler,  arrayOf(LocationServices.API))
    }

    private val windSpeed by lazy {
        getView()?.findViewById(R.id.weather_wind_speed) as TextView
    }

    private val precipitationChances by lazy {
        getView()?.findViewById(R.id.weather_precipitation) as TextView
    }
    private val temperature by lazy {
        getView()?.findViewById(R.id.weather_temperature) as TextView
    }

    private val weatherLocation by lazy {
        getView()?.findViewById(R.id.weather_location) as TextView
    }

    private var weatherAsyncTask: WeatherFetcher? = null

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                WifiManager.NETWORK_STATE_CHANGED_ACTION -> processWeatherInfo()
            }
        }
    }

    private val fetchWeatherTask = object : GoogleApiClientManager.GoogleApiClientTask() {
        override fun doRun() {
            val userLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient)
            if (userLocation != null) {
                    weatherAsyncTask?.cancel(true)

                weatherAsyncTask = WeatherFetcher(this@MiniWidgetWeatherInfo, userLocation)
                weatherAsyncTask?.execute()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_mini_widget_weather_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onStart(){
        super.onStart()
        gapiClientManager.start()
        processWeatherInfo()
        context.registerReceiver(receiver, filter)
    }

    override fun onStop(){
        super.onStop()
        weatherAsyncTask?.cancel(true)

        context.unregisterReceiver(receiver)
        gapiClientManager.stopSafely()
    }

    override fun onApiConnected() {
    }

    override fun onApiDisconnected() {
    }

    override fun getWidgetType() = TowerWidgets.WEATHER_INFO

    private fun getWeatherUrlPath(location: Location): String {
        return getString(R.string.wunderground_url, WEATHER_API_TOKEN, location.latitude, location.longitude)
    }

    private fun fetchWeatherInformationFromServer() {
        Timber.i("Refreshing weather information.")
        gapiClientManager.addTask(fetchWeatherTask)
    }

    private fun processWeatherInfo() {

        try {
            val weatherJSONObject = JSONObject(appPrefs.getPrefWeatherInfo())

            val epoch = Integer.parseInt(weatherJSONObject.getString(JSON_LABEL_REQUEST_EPOCH))
            val expiryTime = epoch * 1000L + WEATHER_REQUEST_OBSOLETE_TIME_FRAME
            if (expiryTime < System.currentTimeMillis()) {
                if(NetworkUtils.isNetworkAvailable(getContext())) {
                    fetchWeatherInformationFromServer()
                }
            } else {
                updateViews(weatherJSONObject)
            }

        } catch (e: JSONException) {
            Timber.e("Invalid weather preference: " + e.message)
            fetchWeatherInformationFromServer()
        }

    }

    fun onJSONRetrieved(serverResponse: JSONObject?) {
        if (serverResponse == null) {
            return
        }
        val weather = serverResponse.optJSONObject(JSON_LABEL_CURRENT_WEATHER)
        if (weather != null) {
            //Store the new weather info on the preferences
            appPrefs.setPrefWeatherInfo(weather.toString())

            //Update the TextViews
            updateViews(weather)
        }
    }

    private fun updateWeatherLocation(location: String) {
            if (TextUtils.isEmpty(location)) {
                weatherLocation.text = ""
            } else {
                weatherLocation.text = location
            }
    }

    private fun updateViews(jsonObject: JSONObject) {
        if (!isAdded) {
            return
        }

        val unitSystemType = UnitManager.getUnitSystem(context).type

        var fullLocation = ""
        val displayLoc = jsonObject.optJSONObject(JSON_LABEL_DISPLAY_LOCATION)
        if (displayLoc != null) {
            fullLocation = displayLoc.optString(JSON_LABEL_FULL_LOCATION)
        }
        updateWeatherLocation(fullLocation)

        val windDirection = jsonObject.optString(JSON_LABEL_WIND_DIRECTION)
        val isWindDirectionValid = !TextUtils.isEmpty(windDirection) && windDirection != NO_WEATHER_DATA

        when (unitSystemType) {
            UnitSystem.IMPERIAL -> {
                val temperatureImperial = jsonObject.optString(JSON_LABEL_TEMPERATURE_IMPERIAL)
                if (!TextUtils.isEmpty(temperatureImperial) && temperatureImperial != NO_WEATHER_DATA) {
                    temperature.text = getString(R.string.weather_temperature_imperial, temperatureImperial)
                }

                val windSpeedImperial = jsonObject.optString(JSON_LABEL_WIND_SPEED_IMPERIAL)
                if (!TextUtils.isEmpty(windSpeedImperial) && windSpeedImperial != NO_WEATHER_DATA && isWindDirectionValid) {
                    windSpeed.text = getString(R.string.weather_wind_velocity_imperial, windSpeedImperial, windDirection)
                }

                val precipitation = jsonObject.optString(JSON_LABEL_PRECIPITATION_IMPERIAL)
                if (!TextUtils.isEmpty(precipitation) && precipitation != NO_WEATHER_DATA) {
                    precipitationChances.text = getString(R.string.weather_rain_imperial, precipitation)
                }
            }

            UnitSystem.METRIC -> {
                val temperatureMetric = jsonObject.optString(JSON_LABEL_TEMPERATURE_METRIC)
                if (!TextUtils.isEmpty(temperatureMetric) && temperatureMetric != NO_WEATHER_DATA) {
                    temperature.text = getString(R.string.weather_temperature_metric, temperatureMetric)
                }

                val windSpeedMetric = jsonObject.optString(JSON_LABEL_WIND_SPEED_METRIC)
                if (!TextUtils.isEmpty(windSpeedMetric) && windSpeedMetric != NO_WEATHER_DATA && isWindDirectionValid) {
                    windSpeed.text = getString(R.string.weather_wind_velocity_metric, windSpeedMetric, windDirection)
                }

                val precipitationMetric = jsonObject.optString(JSON_LABEL_PRECIPITATION_METRIC)
                if (!TextUtils.isEmpty(precipitationMetric) && precipitationMetric != NO_WEATHER_DATA) {
                    precipitationChances.text = getString(R.string.weather_rain_metric, precipitationMetric)
                }
            }
        }
    }

    fun fetchWeatherInformationRequest(location: Location): JSONObject? {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            return null
        }

        try {
            val httpClient = NetworkUtils.getHttpClient()

            val weatherUrl = getWeatherUrlPath(location)
            Timber.d("Checking for weatherInfo @ " + weatherUrl)

            val httpGet = Request.Builder().url(weatherUrl).build()

            //Call the Weather Service
            val response = httpClient.newCall(httpGet).execute()
            if (response.isSuccessful()) {
                val jsonText = response.body().string()
                if (!jsonText.isEmpty()) {
                    Timber.v("Server responded with: " + jsonText)
                    return JSONObject(jsonText)
                }
            } else {
                Timber.d("No response was obtained from the server. Status Code: " + response.code())
            }

        } catch (e: IOException) {
            Timber.e("Unable to access firmware server.", e)
        } catch (e: JSONException) {
            Timber.e("Unable to parse the server response.", e)
        }

        return null
    }
}