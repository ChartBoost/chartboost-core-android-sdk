/*
 * Copyright 2023-2024 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core.environment

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import android.telephony.TelephonyManager
import android.webkit.WebView
import androidx.core.app.ActivityCompat
import com.chartboost.core.ChartboostCoreLogger
import com.chartboost.core.Utils
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.android.gms.appset.AppSet
import com.google.android.gms.appset.AppSetIdInfo
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import java.lang.Double.max
import java.util.*

internal class Environment() :
    BaseEnvironment,
    AdvertisingEnvironment,
    AnalyticsEnvironment,
    AttributionEnvironment {
    private var sessionStartTimeMillis: Long? = null
    private var appContext: Context? = null

    /**
     * The list of observers to notify when a [ObservableEnvironmentProperty] changes.
     */
    private val observers = mutableListOf<EnvironmentObserver>()

    suspend fun startSession(context: Context) {
        appContext = context
        if (sessionStartTimeMillis == null) {
            sessionStartTimeMillis = SystemClock.uptimeMillis()
            appSessionIdentifier = UUID.randomUUID().toString()
            Utils.safeExecute {
                fetchAndReturnUserAgent()
            }
            updateAppSetId(context)
        }
    }

    /**
     * Resets the session start time and restarts the session.
     */
    suspend fun restartSession(context: Context) {
        sessionStartTimeMillis = null
        startSession(context)
    }

    override val appSessionDurationSeconds: Double
        get() =
            sessionStartTimeMillis?.let {
                ((SystemClock.uptimeMillis() - it) / 1000.0)
            } ?: 0.0

    override var appSessionIdentifier: String? = null
        private set

    override var appVersion: String? = null
        get() {
            appContext?.let { context ->
                try {
                    // If we have a context, let's grab the package info from the package manager to grab the version.
                    val packageManager = context.packageManager
                    val packageName = context.packageName
                    // Check the nullability of the packageManager && packageName in case the device has settings
                    // that prevent us from getting a PackageManager and packageName.
                    if (packageManager != null && packageName != null) {
                        val packageInfo =
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                packageManager.getPackageInfo(
                                    packageName,
                                    PackageManager.PackageInfoFlags.of(0),
                                )
                            } else {
                                packageManager.getPackageInfo(packageName, 0)
                            }
                        // Checking the nullability of the package info as a last check.
                        // Most likely, the getPackageInfo will throw an error if something else goes wrong.
                        packageInfo?.let {
                            field = it.versionName
                        }
                    }
                } catch (nameNotFoundException: PackageManager.NameNotFoundException) {
                    ChartboostCoreLogger.e("Exception raised while retrieving appVersionName: ${nameNotFoundException.message}")
                }
            }
            return field
        }
        private set

    override var frameworkName: String? = null
        internal set(value) {
            field = value
            notifyObservers(ObservableEnvironmentProperty.FRAMEWORK_NAME)
        }

    override var frameworkVersion: String? = null
        internal set(value) {
            field = value
            notifyObservers(ObservableEnvironmentProperty.FRAMEWORK_VERSION)
        }

    override var isUserUnderage: Boolean = false
        internal set(value) {
            field = value
            notifyObservers(ObservableEnvironmentProperty.IS_USER_UNDERAGE)
        }

    override val networkConnectionType: NetworkConnectionType
        @SuppressLint("MissingPermission")
        get() {
            appContext?.let { context ->
                // Handle connection type for Android M (API 23) & up.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // Get a ConnectivityManager
                    val connectivityManager =
                        context.applicationContext.getSystemService(ConnectivityManager::class.java)
                    connectivityManager?.let { connectivity ->
                        // Get the active network.
                        val activeNetwork =
                            connectivity.getNetworkCapabilities(connectivity.activeNetwork)
                        // Check the network's capability, determine its connection, and return the
                        // numerical value we need for our BidRequest. Otherwise, continue.
                        activeNetwork?.let {
                            when {
                                it.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> return NetworkConnectionType.WIRED
                                it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> return NetworkConnectionType.WIFI
                                else -> {} // continue
                            }
                        }
                    }
                } else {
                    // Handle connection type for ethernet and wifi for Android L (API 22) & lower.
                    val connectivityManager =
                        context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                    connectivityManager?.activeNetworkInfo?.let { info ->
                        if (info.isConnected) {
                            when (info.type) {
                                ConnectivityManager.TYPE_ETHERNET -> return NetworkConnectionType.WIRED
                                ConnectivityManager.TYPE_WIFI -> return NetworkConnectionType.WIFI
                            }
                        }
                    }
                }
            }

            var networkType = 0

            appContext?.let { context ->
                try {
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.READ_PHONE_STATE,
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        val telephonyManager =
                            context
                                .getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager

                        telephonyManager?.let {
                            networkType =
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    it.dataNetworkType
                                } else {
                                    it.networkType
                                }
                        }
                    }
                } catch (securityException: SecurityException) {
                    // let networkType stay 0
                }
            }

            return when (networkType) {
                // WIFI
                TelephonyManager.NETWORK_TYPE_IWLAN -> NetworkConnectionType.WIFI
                // 2G
                TelephonyManager.NETWORK_TYPE_GPRS,
                TelephonyManager.NETWORK_TYPE_GSM,
                TelephonyManager.NETWORK_TYPE_EDGE,
                TelephonyManager.NETWORK_TYPE_CDMA,
                TelephonyManager.NETWORK_TYPE_1xRTT,
                TelephonyManager.NETWORK_TYPE_IDEN,
                -> NetworkConnectionType.CELLULAR_2G
                // 3G
                TelephonyManager.NETWORK_TYPE_UMTS,
                TelephonyManager.NETWORK_TYPE_EVDO_0,
                TelephonyManager.NETWORK_TYPE_EVDO_A,
                TelephonyManager.NETWORK_TYPE_HSDPA,
                TelephonyManager.NETWORK_TYPE_HSUPA,
                TelephonyManager.NETWORK_TYPE_HSPA,
                TelephonyManager.NETWORK_TYPE_EVDO_B,
                TelephonyManager.NETWORK_TYPE_EHRPD,
                TelephonyManager.NETWORK_TYPE_HSPAP,
                TelephonyManager.NETWORK_TYPE_TD_SCDMA,
                -> NetworkConnectionType.CELLULAR_3G
                // 4G
                TelephonyManager.NETWORK_TYPE_LTE -> NetworkConnectionType.CELLULAR_4G
                // 5G
                TelephonyManager.NETWORK_TYPE_NR -> NetworkConnectionType.CELLULAR_5G
                // Unknown connection type
                TelephonyManager.NETWORK_TYPE_UNKNOWN -> NetworkConnectionType.UNKNOWN
                else -> NetworkConnectionType.UNKNOWN
            }
        }

    override var playerIdentifier: String? = null
        internal set(value) {
            field = value
            notifyObservers(ObservableEnvironmentProperty.PLAYER_IDENTIFIER)
        }

    override var publisherAppIdentifier: String? = null
        internal set(value) {
            field = value
            notifyObservers(ObservableEnvironmentProperty.PUBLISHER_APP_IDENTIFIER)
        }

    override var publisherSessionIdentifier: String? = null
        internal set(value) {
            field = value
            notifyObservers(ObservableEnvironmentProperty.PUBLISHER_SESSION_IDENTIFIER)
        }

    override val bundleIdentifier: String?
        get() = appContext?.packageName

    override val deviceLocale: String
        get() = Locale.getDefault().toString()

    override val deviceMake: String
        get() = Build.MANUFACTURER

    override val deviceModel: String
        get() = Build.MODEL

    override val osName: String = "Android"

    override val osVersion: String
        get() = Build.VERSION.RELEASE

    override val screenHeightPixels: Int?
        get() = appContext?.resources?.displayMetrics?.heightPixels

    override val screenScale: Float?
        get() = appContext?.resources?.displayMetrics?.density

    override val screenWidthPixels: Int?
        get() = appContext?.resources?.displayMetrics?.widthPixels

    /**
     * Local copy of the user agent so Environment doesn't have to fetch it repeatedly.
     */
    private var userAgent: String? = null

    /**
     * Local copy of the vendor identifier.
     */
    private var vendorIdentifier: String? = null

    /**
     * The vendor identifier scope. This defaults to `UNKNOWN` until it is set.
     */
    private var vendorIdentifierScope: VendorIdScope = VendorIdScope.UNKNOWN

    override val volume: Double?
        get() {
            val audioManager: AudioManager =
                appContext?.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return null
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toDouble()
            val minVolume =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    audioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC).toDouble()
                } else {
                    0.0
                }
            val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toDouble()
            if (maxVolume - minVolume == 0.0) {
                return null
            }
            // Here we want to get the current volume divided by the max volume. However, we need
            // to account for the fact that the minimum volume could be not 0, in which case
            // we need to offset that for both the current and max volume. There's also a degenerate
            // case where min volume is greater than current volume, so we clamp it to 0.
            return max((currentVolume - minVolume) / (maxVolume - minVolume), 0.0)
        }

    override suspend fun getAdvertisingIdentifier(): String? = advertisingIdClient()?.id ?: advertisingIdNonGooglePlay()?.id

    override suspend fun getLimitAdTrackingEnabled(): Boolean? =
        advertisingIdClient()?.isLimitAdTrackingEnabled
            ?: advertisingIdNonGooglePlay()?.lmt?.let { it == 1 }

    override suspend fun getUserAgent(): String? {
        return withContext(Main) {
            fetchAndReturnUserAgent()
        }
    }

    override suspend fun getVendorIdentifier(): String? {
        appContext?.let {
            updateAppSetId(it)
        }
        return vendorIdentifier
    }

    override suspend fun getVendorIdentifierScope(): VendorIdScope {
        appContext?.let {
            updateAppSetId(it)
        }
        return vendorIdentifierScope
    }

    /**
     * Add an observer to be notified when a property changes.
     */
    override fun addObserver(observer: EnvironmentObserver) {
        CoroutineScope(Main.immediate).launch {
            observers.add(observer)
        }
    }

    /**
     * Remove an observer from being notified when a property changes.
     */
    override fun removeObserver(observer: EnvironmentObserver) {
        CoroutineScope(Main.immediate).launch {
            observers.remove(observer)
        }
    }

    /**
     * Notify all observers that a property has changed.
     *
     * @param property The property that changed.
     */
    private fun notifyObservers(property: ObservableEnvironmentProperty) {
        CoroutineScope(Dispatchers.Main.immediate).launch {
            observers.forEach {
                it.onChanged(property)
            }
        }
    }

    /**
     * Fetch and return the user agent. Call this only on the Main thread after Environment has
     * been initialized.
     */
    private fun fetchAndReturnUserAgent(): String? {
        val localUserAgent = userAgent
        if (!localUserAgent.isNullOrBlank()) {
            return localUserAgent
        }
        try {
            appContext?.let { context ->
                val webView = WebView(context)
                return webView.settings.userAgentString.also {
                    userAgent = it
                    webView.destroy()
                }
            } ?: ChartboostCoreLogger.w("Context null. Cannot fetch user agent.")
        } catch (exception: Exception) {
            ChartboostCoreLogger.w("Fetching user agent failed: ${exception.message}")
        }
        return localUserAgent
    }

    private suspend fun updateAppSetId(appContext: Context) =
        withContext(IO) {
            try {
                val task = Tasks.await(AppSet.getClient(appContext).appSetIdInfo)
                vendorIdentifierScope =
                    when (task.scope) {
                        AppSetIdInfo.SCOPE_DEVELOPER -> VendorIdScope.DEVELOPER
                        AppSetIdInfo.SCOPE_APP -> VendorIdScope.APPLICATION
                        else -> VendorIdScope.UNKNOWN
                    }
                vendorIdentifier = task.id
            } catch (e: Exception) {
                ChartboostCoreLogger.e("Exception raised while retrieving AppSet ID: ${e.message}")
            }
        }

    private suspend fun advertisingIdClient(): AdvertisingIdClient.Info? {
        return withContext(IO) {
            try {
                appContext?.let { context ->
                    AdvertisingIdClient.getAdvertisingIdInfo(context)
                } ?: run {
                    ChartboostCoreLogger.e("Application context is not available.")
                    null
                }
            } catch (e: GooglePlayServicesNotAvailableException) {
                null
            }
        }
    }

    /**
     * A data class that is used to store a queried lmt and the advertising id.
     */
    private data class NonGooglePlayAdvertisingClient(val id: String?, val lmt: Int)

    /**
     *  A private function that returns a data class with lmt and an advertising id.
     *  Returns null if an error is found when retrieving the info.
     */
    private suspend fun advertisingIdNonGooglePlay(): NonGooglePlayAdvertisingClient? {
        return withContext(IO) {
            try {
                // Query the advertising_id and limit_ad_tracking of the device and set it to the
                // NonGooglePlayAdvertisingClient data object.
                appContext?.contentResolver?.let { contentResolver ->
                    // Settings.Secure.getInt throws an error exception if value is not found.
                    val lmt: Int =
                        try {
                            (Settings.Secure.getInt(contentResolver, "limit_ad_tracking"))
                        } catch (e: Settings.SettingNotFoundException) {
                            ChartboostCoreLogger.e("Exception raised while retrieving lmt ${e.message}")
                            return@withContext null
                        }

                    // Settings.Secure.getString returns null if not present.
                    Settings.Secure.getString(contentResolver, "advertising_id")?.let { id ->
                        // Set the id and lmt to our data object.
                        return@withContext NonGooglePlayAdvertisingClient(id, lmt)
                    }
                }
            } catch (e: RuntimeException) {
                ChartboostCoreLogger.e("Exception raised while retrieving ad information: ${e.message}")
            }
            null
        }
    }
}
