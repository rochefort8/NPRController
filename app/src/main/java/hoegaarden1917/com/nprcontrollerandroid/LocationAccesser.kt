package hoegaarden1917.com.nprcontrollerandroid

import android.app.Activity
import android.content.IntentSender
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AlertDialog

import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResult
import com.google.android.gms.location.LocationSettingsStatusCodes

/**
 * Created by masanori on 2016/03/07.
 */
class LocationAccesser : GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private var apiClient: GoogleApiClient? = null
    override fun onConnectionFailed(result: ConnectionResult) {}
    override fun onConnectionSuspended(cause: Int) {}
    override fun onConnected(bundle: Bundle?) {}
    fun checkIsGpsOn(activity: FragmentActivity, bleActivity: IBleActivity) {
        // OS Version 6.0以降はGPSがOffだとScanできないのでチェック.
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        locationRequest.interval = 3000L
        locationRequest.fastestInterval = 500L
        val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)

        if (apiClient == null) {
            apiClient = GoogleApiClient.Builder(activity.applicationContext)
                    .enableAutoManage(activity, this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build()
        }

        val result = LocationServices.SettingsApi.checkLocationSettings(apiClient, builder.build())
        // ResultCallback<LocationSettingsResult>() - onResult(LocationSettingsResult settingsResult).
        result.setResultCallback { settingsResult ->
            val status = settingsResult.status
            when (status.statusCode) {
                LocationSettingsStatusCodes.SUCCESS ->
                    // GPSがOnならScan開始.
                    bleActivity.onGpsIsEnabled()
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                    // GPSがOffならIntent表示. onActivityResultで結果取得.
                    status.startResolutionForResult(
                            activity, R.string.request_enable_location)
                } catch (ex: IntentSender.SendIntentException) {
                    // Runnable() - run().
                    activity.runOnUiThread {
                        val alert = AlertDialog.Builder(activity)
                        alert.setTitle(activity.getString(R.string.error_title))
                        alert.setMessage(ex.localizedMessage)
                        // DialogInterface.OnClickListener() - onClick(DialogInterface dialog, int which).
                        alert.setPositiveButton(activity.getString(android.R.string.ok), null)
                        alert.show()
                    }
                }

                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                }
            }// Locationが無効なら無視.
        }
    }
}
