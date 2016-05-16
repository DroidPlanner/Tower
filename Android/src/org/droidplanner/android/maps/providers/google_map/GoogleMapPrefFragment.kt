package org.droidplanner.android.maps.providers.google_map

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.preference.*
import android.support.v4.util.ArrayMap
import android.text.TextUtils
import android.widget.Toast
import com.google.android.gms.maps.GoogleMap
import org.droidplanner.android.R
import org.droidplanner.android.dialogs.EditInputDialog
import org.droidplanner.android.maps.providers.DPMapProvider
import org.droidplanner.android.maps.providers.MapProviderPreferences
import org.droidplanner.android.maps.providers.google_map.GoogleMapPrefConstants.*

/**
 * This is the google map provider preferences. It stores and handles all preferences related to google map.
 */
class GoogleMapPrefFragment : MapProviderPreferences(), EditInputDialog.Listener {

    companion object PrefManager {

        private val MAPBOX_ACCESS_TOKEN_DIALOG_TAG = "Mapbox access token dialog"
        private val MAPBOX_ID_DIALOG_TAG = "Mapbox map credentials dialog"

        val DEFAULT_TILE_PROVIDER = GOOGLE_TILE_PROVIDER

        val MAP_TYPE_SATELLITE = "satellite"
        val MAP_TYPE_HYBRID = "hybrid"
        val MAP_TYPE_NORMAL = "normal"
        val MAP_TYPE_TERRAIN = "terrain"

        val PREF_TILE_PROVIDERS = "pref_google_map_tile_providers"

        const val PREF_TILE_PROVIDER_SETTINGS_SCREEN = "pref_tile_provider_settings"

        val PREF_GOOGLE_TILE_PROVIDER_SETTINGS = "pref_google_tile_provider_settings"

        val PREF_MAP_TYPE = "pref_map_type"
        val DEFAULT_MAP_TYPE = MAP_TYPE_SATELLITE

        const val PREF_MAPBOX_TILE_PROVIDER_SETTINGS = "pref_mapbox_tile_provider_settings"

        const val PREF_ARCGIS_MAP_DOWNLOAD = "pref_arcgis_map_download"
        val PREF_MAPBOX_MAP_DOWNLOAD = "pref_mapbox_map_download"

        val PREF_DOWNLOAD_MENU_OPTION = "pref_download_menu_option"
        val DEFAULT_DOWNLOAD_MENU_OPTION = false

        val PREF_MAPBOX_ID = "pref_mapbox_id"
        val PREF_MAPBOX_ACCESS_TOKEN = "pref_mapbox_access_token"

        val PREF_MAPBOX_LEARN_MORE = "pref_mapbox_learn_more"

        val PREF_ENABLE_OFFLINE_LAYER = "pref_enable_offline_map_layer"
        val DEFAULT_OFFLINE_LAYER_ENABLED = false

        const val PREF_ARCGIS_TILE_PROVIDER_SETTINGS = "pref_arcgis_tile_provider_settings"

        const val PREF_ARCGIS_MAP_TYPE = "pref_arcgis_service"
        const val DEFAULT_ARCGIS_MAP_TYPE = R.string.label_nat_geo_world_map

        fun getArcGISMapType(context: Context): String {
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
            return sharedPref.getString(PREF_ARCGIS_MAP_TYPE, context.getString(DEFAULT_ARCGIS_MAP_TYPE))
        }

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

        @TileProvider fun getMapTileProvider(context: Context?): String {
            var tileProvider = DEFAULT_TILE_PROVIDER
            context?.let {
                val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
                tileProvider = sharedPref.getString(PREF_TILE_PROVIDERS, tileProvider)
            }

            return tileProvider
        }

        fun setMapTileProvider(context: Context?, @TileProvider tileProvider: String?){
            context?.let {
                val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
                sharedPref.edit().putString(PREF_TILE_PROVIDERS, tileProvider).apply()
            }
        }

        fun isOfflineMapLayerEnabled(context: Context?): Boolean {
            return if(context == null){
                DEFAULT_OFFLINE_LAYER_ENABLED
            }
            else{
                val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
                sharedPref.getBoolean(PREF_ENABLE_OFFLINE_LAYER, DEFAULT_OFFLINE_LAYER_ENABLED)
            }
        }

        fun addDownloadMenuOption(context: Context?): Boolean {
            return if(context == null) DEFAULT_DOWNLOAD_MENU_OPTION else{
                val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
                sharedPref.getBoolean(PREF_DOWNLOAD_MENU_OPTION, DEFAULT_DOWNLOAD_MENU_OPTION)
            }
        }

        fun getMapboxId(context: Context?): String {
            return if(context == null) "" else{
                val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
                sharedPref.getString(PREF_MAPBOX_ID, "")
            }
        }

        private fun setMapboxId(context: Context?, mapboxId: String?){
            context?.let {
                val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
                sharedPref.edit().putString(PREF_MAPBOX_ID, mapboxId).apply()
            }
        }

        fun getMapboxAccessToken(context: Context?): String {
            return if(context == null) "" else{
                val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
                sharedPref.getString(PREF_MAPBOX_ACCESS_TOKEN, "")
            }
        }

        private fun setMapboxAccessToken(context: Context?, mapboxToken: String?){
            context?.let {
                val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
                sharedPref.edit().putString(PREF_MAPBOX_ACCESS_TOKEN, mapboxToken).apply()
            }
        }
    }

    private val accessTokenDialog = EditInputDialog.newInstance(MAPBOX_ACCESS_TOKEN_DIALOG_TAG, "Enter mapbox access token", "mapbox access token", false)

    private var tileProvidersPref: ListPreference? = null
    private val tileProviderSettingsScreen: PreferenceScreen by lazy {
        findPreference(PREF_TILE_PROVIDER_SETTINGS_SCREEN) as PreferenceScreen
    }

    private val providerSettingsScreens = ArrayMap<@TileProvider String, PreferenceCategory>(3)

    override fun onCreate(savedInstanceState: Bundle?) {
        super<MapProviderPreferences>.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preferences_google_maps)
        setupPreferences()
    }

    private fun setupPreferences() {
        val context = activity.applicationContext
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)

        setupGoogleTileProviderPreferences(sharedPref)
        setupArcGISTileProviderPreferences(sharedPref)
        setupMapboxTileProviderPreferences(sharedPref)

        setupTileProvidersPreferences(sharedPref)
    }

    private fun isMapboxIdSet() = !TextUtils.isEmpty(getMapboxId(getContext()))

    private fun isMapboxAccessTokenSet() = !TextUtils.isEmpty(getMapboxAccessToken(getContext()))

    private fun areMapboxCredentialsSet() = isMapboxAccessTokenSet() && isMapboxIdSet()

    private fun enableTileProvider(provider: String, persistPreference: Boolean){
        val tileProviderPref = findPreference(PREF_TILE_PROVIDERS) as ListPreference?
        if(tileProviderPref != null) {
            enableTileProvider(tileProviderPref, provider, persistPreference)
        }
    }

    private fun enableTileProvider(tileProviderPref: ListPreference?, provider: String, persistPreference: Boolean){
        if(persistPreference){
            tileProviderPref?.value = provider
            setMapTileProvider(getContext(), provider)
        }

        tileProviderPref?.summary = provider
        toggleTileProviderPrefs(provider)
    }

    override fun onCancel(dialogTag: String) {}

    override fun onOk(dialogTag: String, input: CharSequence?) {
        val context = getContext()

        when (dialogTag) {
            MAPBOX_ID_DIALOG_TAG -> {
                if (TextUtils.isEmpty(input)) {
                    Toast.makeText(context, R.string.label_invalid_mapbox_id, Toast.LENGTH_LONG)
                            .show()
                } else {
                    //Save the mapbox id to preferences
                    updateMapboxId(input?.toString() ?: "", true)

                    //Check if the mapbox access token is set enable the mapbox tile
                    // provider
                    if (isMapboxAccessTokenSet()) {
                        enableTileProvider(tileProvidersPref, MAPBOX_TILE_PROVIDER, true)
                    }
                    else {
                        //Check if the mapbox access token is set
                        accessTokenDialog?.show(fragmentManager,
                                MAPBOX_ACCESS_TOKEN_DIALOG_TAG)
                    }
                }
            }

            MAPBOX_ACCESS_TOKEN_DIALOG_TAG -> {
                if(TextUtils.isEmpty(input)){
                    Toast.makeText(context, R.string.label_invalid_mapbox_access_token,
                            Toast.LENGTH_LONG).show()
                }
                else{
                    //Save the mapbox access token to preferences
                    updateMapboxAccessToken(input?.toString() ?: "", true)

                    //Check if the mapbox id is set to enable the mapbox tile provider.
                    if(isMapboxIdSet()){
                        enableTileProvider(tileProvidersPref, MAPBOX_TILE_PROVIDER, true)
                    }
                }
            }

        }
    }

    private fun setupTileProvidersPreferences(sharedPref: SharedPreferences) {
        //Populate the map of tile providers to their settings screen
        providerSettingsScreens.put(GoogleMapPrefConstants.GOOGLE_TILE_PROVIDER, findPreference(PREF_GOOGLE_TILE_PROVIDER_SETTINGS) as PreferenceCategory)
        providerSettingsScreens.put(GoogleMapPrefConstants.MAPBOX_TILE_PROVIDER, findPreference(PREF_MAPBOX_TILE_PROVIDER_SETTINGS) as PreferenceCategory)
        providerSettingsScreens.put(GoogleMapPrefConstants.ARC_GIS_TILE_PROVIDER, findPreference(PREF_ARCGIS_TILE_PROVIDER_SETTINGS) as PreferenceCategory)

        val tileProvidersKey = PREF_TILE_PROVIDERS
        tileProvidersPref = findPreference(tileProvidersKey) as ListPreference?

        if(tileProvidersPref != null){
            val tileProvider = sharedPref.getString(tileProvidersKey, DEFAULT_TILE_PROVIDER)
            tileProvidersPref?.summary = tileProvider
            tileProvidersPref?.setOnPreferenceChangeListener { preference, newValue ->
                    val updatedTileProvider = newValue.toString()

                    var acceptChange = true
                    if (updatedTileProvider == MAPBOX_TILE_PROVIDER) {
                        //Check if the mapbox id and access token are set.

                        if (!areMapboxCredentialsSet()) {
                            //Show a dialog requesting the user to enter its mapbox id and access token
                            acceptChange = false

                            val inputDialog = if (!isMapboxIdSet()) {
                                EditInputDialog.newInstance(MAPBOX_ID_DIALOG_TAG, "Enter mapbox id", "mapbox id", false)
                            } else {
                                if (!isMapboxAccessTokenSet()) {
                                    accessTokenDialog
                                } else {
                                    null
                                }
                            }

                            inputDialog?.show(fragmentManager, MAPBOX_ID_DIALOG_TAG)
                        }
                    }

                    if(acceptChange) {
                        enableTileProvider(tileProvidersPref, updatedTileProvider, false)
                        true
                    }
                    else{
                        false
                    }
            }

            toggleTileProviderPrefs(tileProvider)
        }
    }

    private fun setupGoogleTileProviderPreferences(sharedPref: SharedPreferences) {
        val mapTypeKey = PREF_MAP_TYPE
        val mapTypePref = findPreference(mapTypeKey)
        mapTypePref?.let {
            mapTypePref.summary = sharedPref.getString(mapTypeKey, DEFAULT_MAP_TYPE)
            mapTypePref.setOnPreferenceChangeListener { preference, newValue ->
                mapTypePref.summary = newValue.toString()
                true
            }
        }
    }

    private fun setupArcGISTileProviderPreferences(sharedPref: SharedPreferences){
        val context = getContext()

        //Setup mapbox map download button
        val downloadMapPref = findPreference(PREF_ARCGIS_MAP_DOWNLOAD)
        downloadMapPref?.setOnPreferenceClickListener {
            startActivity(Intent(context, DownloadMapboxMapActivity::class.java))
            true
        }

        val mapTypeKey = PREF_ARCGIS_MAP_TYPE
        val mapTypePref = findPreference(mapTypeKey)
        mapTypePref?.let {
            mapTypePref.summary = sharedPref.getString(mapTypeKey, getString(DEFAULT_ARCGIS_MAP_TYPE))
            mapTypePref.setOnPreferenceChangeListener { preference, newValue ->
                mapTypePref.summary = newValue.toString()
                true
            }
        }

        //Offline layer checkbox
        val offlineLayerPref = findPreference(PREF_ENABLE_OFFLINE_LAYER) as CheckBoxPreference?
        offlineLayerPref?.let {
            offlineLayerPref.isChecked = sharedPref.getBoolean(PREF_ENABLE_OFFLINE_LAYER, DEFAULT_OFFLINE_LAYER_ENABLED)
        }

        //Download menu option
        val downloadMenuPref = findPreference(PREF_DOWNLOAD_MENU_OPTION) as CheckBoxPreference?
        downloadMenuPref?.let {
            downloadMenuPref.isChecked = sharedPref.getBoolean(PREF_DOWNLOAD_MENU_OPTION, DEFAULT_DOWNLOAD_MENU_OPTION)
        }
    }

    private fun setupMapboxTileProviderPreferences(sharedPref: SharedPreferences) {
        val context = getContext()

        //Setup mapbox map download button
        val downloadMapPref = findPreference(PREF_MAPBOX_MAP_DOWNLOAD)
        downloadMapPref?.setOnPreferenceClickListener {
            startActivity(Intent(context, DownloadMapboxMapActivity::class.java))
            true
        }

        //Offline layer checkbox
        val offlineLayerPref = findPreference(PREF_ENABLE_OFFLINE_LAYER) as CheckBoxPreference?
        offlineLayerPref?.let {
            offlineLayerPref.isChecked = sharedPref.getBoolean(PREF_ENABLE_OFFLINE_LAYER, DEFAULT_OFFLINE_LAYER_ENABLED)
        }

        //Download menu option
        val downloadMenuPref = findPreference(PREF_DOWNLOAD_MENU_OPTION) as CheckBoxPreference?
        downloadMenuPref?.let {
            downloadMenuPref.isChecked = sharedPref.getBoolean(PREF_DOWNLOAD_MENU_OPTION, DEFAULT_DOWNLOAD_MENU_OPTION)
        }

        //Setup mapbox map id
        val mapboxIdPref = findPreference(PREF_MAPBOX_ID)
        if(mapboxIdPref != null) {
            val mapboxId = sharedPref.getString(PREF_MAPBOX_ID, null)
            mapboxId?.let { mapboxIdPref.summary = mapboxId }
            mapboxIdPref.setOnPreferenceChangeListener { preference, newValue ->
                val newMapboxId = newValue.toString()
                if(TextUtils.isEmpty(newMapboxId)){
                    Toast.makeText(context, R.string.label_invalid_mapbox_id, Toast.LENGTH_LONG)
                            .show()
                }

                updateMapboxId(newMapboxId, false)
                true
            }
        }

        //Setup mapbox access token
        val mapboxTokenPref = findPreference(PREF_MAPBOX_ACCESS_TOKEN)
        if(mapboxTokenPref != null) {
            val mapboxToken = sharedPref.getString(PREF_MAPBOX_ACCESS_TOKEN, null)
            mapboxToken?.let { mapboxTokenPref.summary = mapboxToken }
            mapboxTokenPref.setOnPreferenceChangeListener {preference, newValue ->
                val mapboxAccessToken = newValue.toString()
                if(TextUtils.isEmpty(mapboxAccessToken)){
                    Toast.makeText(context, R.string.label_invalid_mapbox_access_token,
                            Toast.LENGTH_LONG).show()
                }

                updateMapboxAccessToken(mapboxAccessToken, false)
                true
            }
        }

        //Setup the learn more about mapbox map button
        val mapboxLearnMorePref = findPreference(PREF_MAPBOX_LEARN_MORE)
        mapboxLearnMorePref?.setOnPreferenceClickListener {
            startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://www.mapbox.com/plans/")))
            true
        }
    }

    private fun updateMapboxId(id: String, persist: Boolean){
        val mapboxIdPref = findPreference(PREF_MAPBOX_ID)
        mapboxIdPref?.let {
            val summary = if (TextUtils.isEmpty(id)) {
                enableTileProvider(GOOGLE_TILE_PROVIDER, true)
                getString(R.string.pref_hint_mapbox_id)
            } else {
                id
            }

            mapboxIdPref.summary = summary
        }

        if(persist)
            setMapboxId(getContext(), id)
    }

    private fun updateMapboxAccessToken(token: String, persist: Boolean){
        val mapboxTokenPref = findPreference(PREF_MAPBOX_ACCESS_TOKEN)
        if(mapboxTokenPref != null) {
            val summary = if (TextUtils.isEmpty(token)) {
                enableTileProvider(GOOGLE_TILE_PROVIDER, true)
                getString(R.string.pref_hint_mapbox_access_token)
            }
            else {
                token
            }
            mapboxTokenPref.summary = summary
        }

        if(persist)
            setMapboxAccessToken(getContext(), token)
    }

    private fun toggleTileProviderPrefs(@TileProvider tileProvider: String){
        tileProviderSettingsScreen.removeAll()
        val providerSettingsScreen = providerSettingsScreens[tileProvider] ?: return
        tileProviderSettingsScreen.addPreference(providerSettingsScreen)

        val context = activity.applicationContext
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)

        when(tileProvider){
            GoogleMapPrefConstants.GOOGLE_TILE_PROVIDER -> setupGoogleTileProviderPreferences(sharedPref)
            GoogleMapPrefConstants.MAPBOX_TILE_PROVIDER -> setupMapboxTileProviderPreferences(sharedPref)
            GoogleMapPrefConstants.ARC_GIS_TILE_PROVIDER -> setupArcGISTileProviderPreferences(sharedPref)
        }
    }

    override fun getMapProvider(): DPMapProvider? = DPMapProvider.GOOGLE_MAP
}
