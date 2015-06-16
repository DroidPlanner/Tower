package org.droidplanner.android.maps.providers.google_map

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.EditTextPreference
import android.preference.PreferenceCategory
import android.preference.PreferenceManager
import com.google.android.gms.maps.GoogleMap
import org.droidplanner.android.R
import org.droidplanner.android.maps.providers.DPMapProvider
import org.droidplanner.android.maps.providers.MapProviderPreferences

/**
 * This is the google map provider preferences. It stores and handles all preferences related to google map.
 */
public class GoogleMapPrefFragment : MapProviderPreferences() {

    companion object PrefManager {

        private val DEFAULT_TILE_PROVIDER = GoogleMapPrefConstants.GOOGLE_TILE_PROVIDER

        private val MAP_TYPE_SATELLITE = "satellite"
        private val MAP_TYPE_HYBRID = "hybrid"
        private val MAP_TYPE_NORMAL = "normal"
        private val MAP_TYPE_TERRAIN = "terrain"

        private val PREF_TILE_PROVIDERS = "pref_google_map_tile_providers"

        private val PREF_GOOGLE_TILE_PROVIDER_SETTINGS = "pref_google_tile_provider_settings"

        private val PREF_MAP_TYPE = "pref_map_type"
        private val DEFAULT_MAP_TYPE = MAP_TYPE_SATELLITE

        private val PREF_MAPBOX_TILE_PROVIDER_SETTINGS = "pref_mapbox_tile_provider_settings"

        private val PREF_MAPBOX_MAP_DOWNLOAD = "pref_mapbox_map_download"
        private val DEFAULT_MAPBOX_MAP_DOWNLOAD = false

        private val PREF_MAPBOX_TILE_SOURCE = "pref_mapbox_tile_source"
        private val DEFAULT_MAPBOX_TILE_SOURCE = GoogleMapPrefConstants.MAPBOX_AUTO_TILE_SOURCE

        private val PREF_DOWNLOAD_MENU_OPTION = "pref_download_menu_option"
        private val PREF_MAPBOX_ID = "pref_mapbox_id"
        private val PREF_MAPBOX_ACCESS_TOKEN = "pref_mapbox_access_token"

        fun getMapType(context: Context?): Int {
            var mapType = GoogleMap.MAP_TYPE_SATELLITE
            context?.let {
                val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
                val selectedType = sharedPref.getString(PREF_MAP_TYPE, DEFAULT_MAP_TYPE)
                when(selectedType){
                    MAP_TYPE_HYBRID -> mapType = GoogleMap.MAP_TYPE_HYBRID
                    MAP_TYPE_NORMAL -> mapType = GoogleMap.MAP_TYPE_NORMAL
                    MAP_TYPE_TERRAIN -> mapType = GoogleMap.MAP_TYPE_TERRAIN
                    MAP_TYPE_SATELLITE -> mapType = GoogleMap.MAP_TYPE_SATELLITE
                    else -> mapType = GoogleMap.MAP_TYPE_SATELLITE
                }
            }

            return mapType
        }

        @GoogleMapPrefConstants.TileProvider fun getMapTileProvider(context: Context?): String {
            var tileProvider = DEFAULT_TILE_PROVIDER
            context?.let {
                val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
                tileProvider = sharedPref.getString(PREF_TILE_PROVIDERS, tileProvider)
            }

            return tileProvider
        }

        @GoogleMapPrefConstants.MapboxTileSource fun getMapboxTileSource(context: Context?): String {
            var tileSource = DEFAULT_MAPBOX_TILE_SOURCE
            context?.let {
                val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
                tileSource = sharedPref.getString(PREF_MAPBOX_TILE_SOURCE, tileSource)
            }

            return tileSource
        }

        fun addDownloadMenuOption(context: Context?): Boolean {
            return if(context == null) DEFAULT_MAPBOX_MAP_DOWNLOAD else{
                val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
                sharedPref.getBoolean(PREF_MAPBOX_MAP_DOWNLOAD, DEFAULT_MAPBOX_MAP_DOWNLOAD)
            }
        }

        fun getMapboxId(context: Context?): String {
            return if(context == null) "" else{
                val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
                sharedPref.getString(PREF_MAPBOX_ID, "")
            }
        }

        fun getMapboxAccessToken(context: Context?): String {
            return if(context == null) "" else{
                val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
                sharedPref.getString(PREF_MAPBOX_ACCESS_TOKEN, "")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preferences_google_maps)
        setupPreferences()
    }

    private fun setupPreferences() {
        val context = getActivity().getApplicationContext()
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)

        setupTileProvidersPreferences(sharedPref)
        setupGoogleTileProviderPreferences(sharedPref)
        setupMapboxTileProviderPreferences(sharedPref)
    }

    private fun setupTileProvidersPreferences(sharedPref: SharedPreferences) {
        val tileProvidersKey = PREF_TILE_PROVIDERS
        val tileProvidersPref = findPreference(tileProvidersKey)
        tileProvidersPref?.let {
            val tileProvider = sharedPref.getString(tileProvidersKey, DEFAULT_TILE_PROVIDER)
            tileProvidersPref.setSummary(tileProvider)
            tileProvidersPref.setOnPreferenceChangeListener { preference, newValue ->
                run {
                    val updatedTileProvider = newValue.toString()
                    tileProvidersPref.setSummary(updatedTileProvider)
                    toggleTileProviderPrefs(updatedTileProvider)
                    true
                }
            }

            toggleTileProviderPrefs(tileProvider)
        }
    }

    private fun setupGoogleTileProviderPreferences(sharedPref: SharedPreferences) {
        val mapTypeKey = PREF_MAP_TYPE
        val mapTypePref = findPreference(mapTypeKey)
        mapTypePref?.let {
            mapTypePref.setSummary(sharedPref.getString(mapTypeKey, DEFAULT_MAP_TYPE))
            mapTypePref.setOnPreferenceChangeListener { preference, newValue ->
                run {
                    mapTypePref.setSummary(newValue.toString())
                    true
                }
            }
        }
    }

    private fun setupMapboxTileProviderPreferences(sharedPref: SharedPreferences) {
        //Setup the map tile source preference
        val tileSourcePref = findPreference(PREF_MAPBOX_TILE_SOURCE)
        tileSourcePref?.setOnPreferenceChangeListener { preference, newValue ->
            run {
                val tileSource = sharedPref.getString(PREF_MAPBOX_TILE_SOURCE, null)
                tileSource?.let { tileSourcePref.setSummary(tileSource)}
                true
            }
        }

        //Setup mapbox map download button
        val downloadMapPref = findPreference(PREF_MAPBOX_MAP_DOWNLOAD)
        downloadMapPref?.setOnPreferenceClickListener {
            //TODO: Bring up map download interface.
            true
        }

        //Add 'Download Map' to menu option
        val downloadInMenu = findPreference(PREF_DOWNLOAD_MENU_OPTION)
        downloadInMenu?.setOnPreferenceChangeListener {preference, newValue ->
            run {
                //Update the menu
                true
            }
        }

        //Setup mapbox map id
        val mapboxIdPref = findPreference(PREF_MAPBOX_ID)
        mapboxIdPref?.let {
            val mapboxId = sharedPref.getString(PREF_MAPBOX_ID, null)
            mapboxId?.let { mapboxIdPref.setSummary(mapboxId)}
        }

        //Setup mapbox access token
        val mapboxTokenPref = findPreference(PREF_MAPBOX_ACCESS_TOKEN)
        mapboxTokenPref?.let {
            val mapboxToken = sharedPref.getString(PREF_MAPBOX_ACCESS_TOKEN, null)
            mapboxToken?.let { mapboxTokenPref.setSummary(mapboxToken)}
        }
    }

    private fun toggleTileProviderPrefs(tileProvider: String){
        when(tileProvider){
            GoogleMapPrefConstants.GOOGLE_TILE_PROVIDER -> {
                enableGoogleTileProviderPrefs(true)
                enableMapboxTileProviderPrefs(false)
            }

            GoogleMapPrefConstants.MAPBOX_TILE_PROVIDER -> {
                enableGoogleTileProviderPrefs(true)
                enableMapboxTileProviderPrefs(false)
            }
        }
    }

    private fun enableGoogleTileProviderPrefs(enable: Boolean){
        enableTileProviderPrefs(PREF_GOOGLE_TILE_PROVIDER_SETTINGS, enable)
    }

    private fun enableMapboxTileProviderPrefs(enable: Boolean){
        enableTileProviderPrefs(PREF_MAPBOX_TILE_PROVIDER_SETTINGS, enable)
    }

    private fun enableTileProviderPrefs(prefKey: String, enable: Boolean){
        val prefCategory = findPreference(prefKey) as PreferenceCategory?
        prefCategory?.setEnabled(enable)
    }

    override fun getMapProvider(): DPMapProvider? = DPMapProvider.GOOGLE_MAP
}