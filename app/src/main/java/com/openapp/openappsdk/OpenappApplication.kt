package com.openapp.openappsdk

import android.app.Application
import co.openapp.sdk.LogConstants
import co.openapp.sdk.LogOptions
import co.openapp.sdk.OpenAppClient

class OpenappApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        OpenAppClient.create(this)

        OpenAppClient.updateLogOptions(
                LogOptions.Builder()
                        .setLogLevel(LogConstants.INFO)
                        .setLogLevel(LogConstants.ERROR)
                        .setLogLevel(LogConstants.WARN)
                        .setLogLevel(LogConstants.DEBUG)
                        .setMacAddressLogSetting(LogConstants.MAC_ADDRESS_FULL)
                        .setUuidsLogSetting(LogConstants.UUIDS_FULL)
                        .setShouldLogAttributeValues(true)
                        .build()
        )
    }
}