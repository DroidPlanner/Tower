package org.droidplanner.android.maps.providers.google_map

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import org.droidplanner.android.DroidPlannerApp
import org.droidplanner.android.R
import org.droidplanner.android.maps.providers.google_map.tiles.mapbox.offline.MapDownloader
import org.droidplanner.android.maps.providers.google_map.tiles.mapbox.offline.MapDownloaderListener
import org.droidplanner.android.utils.prefs.AutoPanMode
import java.net.HttpURLConnection
import kotlin.properties.Delegates

/**
 * Created by Fredia Huya-Kouadio on 6/17/15.
 */
public class DownloadMapboxMapActivity : AppCompatActivity() {

    val MAP_CACHE_ZOOM_LEVEL = 19

    private val mapDownloader: MapDownloader by lazy(LazyThreadSafetyMode.NONE) {
        val dpApp = application as DroidPlannerApp
        dpApp.mapDownloader
    }

    private val mapboxId: String by lazy(LazyThreadSafetyMode.NONE) {
        GoogleMapPrefFragment.PrefManager.getMapboxId(applicationContext)
    }

    private val mapboxAccessToken: String by lazy(LazyThreadSafetyMode.NONE) {
        GoogleMapPrefFragment.PrefManager.getMapboxAccessToken(applicationContext)
    }

    private val mapDownloadListener = object : MapDownloaderListener {
        override fun completionOfOfflineDatabaseMap() {
            runOnUiThread { completeMapDownload() }
        }

        override fun httpStatusError(status: Int, url: String) {
            when(status){
                HttpURLConnection.HTTP_UNAUTHORIZED -> {
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Invalid mapbox credentials! Cancelling map download...",
                                Toast.LENGTH_LONG).show()
                        cancelMapDownload()
                    }
                }
            }
        }

        override fun initialCountOfFiles(numberOfFiles: Int) {
            runOnUiThread {
                downloadProgressBar?.isIndeterminate = false
                downloadProgressBar?.max = numberOfFiles
                downloadProgressBar?.progress = 0
            }
        }

        override fun networkConnectivityError(error: Throwable?) {
        }

        override fun progressUpdate(numberOfFilesWritten: Int, numberOfFilesExcepted: Int) {
            runOnUiThread {
                downloadProgressBar?.isIndeterminate = false
                downloadProgressBar?.max = numberOfFilesExcepted
                downloadProgressBar?.progress = numberOfFilesWritten
            }
        }

        override fun sqlLiteError(error: Throwable?) {
        }

        override fun stateChanged(newState: MapDownloader.MBXOfflineMapDownloaderState?) {
            when (newState) {
                MapDownloader.MBXOfflineMapDownloaderState.MBXOfflineMapDownloaderStateRunning -> {
                    enableDownloadInstructions(false)
                    enableDownloadProgress(true, resetProgress = true)
                }

                MapDownloader.MBXOfflineMapDownloaderState.MBXOfflineMapDownloaderStateCanceling -> {
                    enableDownloadProgress(false, true)
                    enableDownloadInstructions(true)
                }
            }
        }
    }

    private var instructionsContainer: View? = null

    private var downloadProgressContainer: View? = null
    private var cancelDownloadButton: View? = null
    private var downloadProgressBar: ProgressBar? = null

    private var downloadMapFragment: DownloadMapboxMapFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download_mapbox_map)

        val fm = supportFragmentManager

        downloadMapFragment = fm.findFragmentById(R.id.map_container) as DownloadMapboxMapFragment?
        if (downloadMapFragment == null) {
            downloadMapFragment = DownloadMapboxMapFragment()
            fm.beginTransaction().add(R.id.map_container, downloadMapFragment).commit()
        }

        instructionsContainer = findViewById(R.id.download_map_instructions)
        instructionsContainer?.setOnClickListener { triggerMapDownload() }

        downloadProgressContainer = findViewById(R.id.download_map_progress)

        cancelDownloadButton = findViewById(R.id.map_bottom_bar_close_button)
        cancelDownloadButton?.setOnClickListener { cancelMapDownload() }

        downloadProgressBar = findViewById(R.id.map_download_progress_bar) as ProgressBar?

        val goToMyLocation = findViewById(R.id.my_location_button)
        goToMyLocation?.setOnClickListener { downloadMapFragment?.goToMyLocation() }
        goToMyLocation?.setOnLongClickListener {
            downloadMapFragment?.setAutoPanMode(AutoPanMode.USER)
            true
        }

        val goToDroneLocation = findViewById(R.id.drone_location_button)
        goToDroneLocation?.setOnClickListener { downloadMapFragment?.goToDroneLocation() }
        goToDroneLocation?.setOnLongClickListener {
            downloadMapFragment?.setAutoPanMode(AutoPanMode.DRONE)
            true
        }

    }

    private fun completeMapDownload() {
        Toast.makeText(applicationContext, R.string.label_map_saved, Toast.LENGTH_LONG).show()

        enableDownloadInstructions(true)
        enableDownloadProgress(false, true)
    }

    private fun cancelMapDownload() {
        mapDownloader.cancelDownload()
    }

    override fun onStart() {
        super.onStart()
        if (mapDownloader.state == MapDownloader.MBXOfflineMapDownloaderState.MBXOfflineMapDownloaderStateRunning) {
            enableDownloadInstructions(false)
            enableDownloadProgress(true, true)
        }
        mapDownloader.addMapDownloaderListener(mapDownloadListener)
    }

    override fun onStop() {
        super.onStop()

        if (isFinishing)
            cancelMapDownload()
        mapDownloader.removeMapDownloaderListener(mapDownloadListener)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        cancelMapDownload()
    }

    private fun triggerMapDownload() {
        val mapArea = downloadMapFragment?.visibleMapArea
        mapDownloader.beginDownloadingMapID(mapboxId, mapboxAccessToken, mapArea, 0, MAP_CACHE_ZOOM_LEVEL)
    }

    private fun enableDownloadInstructions(enabled: Boolean) {
        instructionsContainer?.visibility = if (enabled) View.VISIBLE else View.GONE
    }

    private fun enableDownloadProgress(enabled: Boolean, resetProgress: Boolean) {
        downloadProgressContainer?.visibility = if (enabled) View.VISIBLE else View.GONE

        if (resetProgress) {
            downloadProgressBar?.progress = 0
            downloadProgressBar?.isIndeterminate = true
        }
    }

}