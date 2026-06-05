package com.example.kotlinclient

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.kotlinclient.api_client.AuthInterceptor
import com.example.kotlinclient.api_client.TokenStorage
import com.example.kotlinclient.di.databaseModule
import com.example.kotlinclient.di.networkModule
import com.example.kotlinclient.di.notificationModule
import com.example.kotlinclient.di.preferencesModule
import com.example.kotlinclient.di.repositoryModule
import com.example.kotlinclient.di.viewModelModule
import com.example.kotlinclient.sync.SyncWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module
import java.util.concurrent.TimeUnit

class App : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val tokenStorage: TokenStorage by inject()
    private val authInterceptor: AuthInterceptor by inject()

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            modules(
                module { single { applicationScope } },
                networkModule,
                databaseModule,
                preferencesModule,
                repositoryModule,
                viewModelModule,
                notificationModule
            )
        }

        schedulePeriodSync()
        triggerOneTimeSync()
        registerNetworkCallback()
    }

    private fun schedulePeriodSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            SyncWorker.WORK_NAME_PERIODIC,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicRequest
        )
    }

    private fun registerNetworkCallback() {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        cm.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                triggerOneTimeSync()
                tryRefreshTokensInBackground()
            }
        })
    }

    private fun tryRefreshTokensInBackground() {
        if (!tokenStorage.hasTokens()) return
        val accessToken = tokenStorage.getAccessToken() ?: return
        if (!authInterceptor.isExpiredOrExpiringSoon(accessToken)) return
        applicationScope.launch(Dispatchers.IO) {
            authInterceptor.tryRefresh()
        }
    }

    fun triggerOneTimeSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniqueWork(
            SyncWorker.WORK_NAME_ONE_SHOT,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}
