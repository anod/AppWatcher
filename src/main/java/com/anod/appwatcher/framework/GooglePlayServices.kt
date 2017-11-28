package com.anod.appwatcher.framework

import android.content.Context
import com.anod.appwatcher.R
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

/**
 * @author alex
 * *
 * @date 7/30/14.
 */
class GooglePlayServices(private val context: ApplicationContext) {

    constructor(context: Context) : this(ApplicationContext(context))

    val isSupported: Boolean
        get() =  GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context.actual) == ConnectionResult.SUCCESS

    val availabilityMessage: String
        get() {
            val errorCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context.actual)
            if (errorCode == ConnectionResult.SERVICE_MISSING) {
                return context.getString(R.string.gms_service_missing)
            }
            if (errorCode == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED) {
                return context.getString(R.string.gms_service_update_required)
            }
            if (errorCode == ConnectionResult.SERVICE_DISABLED) {
                return context.getString(R.string.gms_service_disabled)
            }
            if (errorCode == ConnectionResult.SERVICE_INVALID) {
                return context.getString(R.string.gms_service_invalid)
            }
            return GoogleApiAvailability.getInstance().getErrorString(errorCode)
        }
    
}
