package com.example.mviexample

import android.app.Application
import com.example.mviexample.features.auth.AndroidAuthRepository
import com.example.mviexample.features.auth.AuthRepository
import com.example.mviexample.shared.AppGraph
import com.example.mviexample.shared.data.database.DatabaseDriverFactory
import com.example.mviexample.shared.di.AppContainer

class MviApplication : Application() {

    /**
     * Single process-wide [AuthRepository], built once with the application context.
     * Injected into the UI from [MainActivity]; no static/global Context holders.
     */
    lateinit var authRepository: AuthRepository
        private set

    override fun onCreate() {
        super.onCreate()
        authRepository = AndroidAuthRepository(applicationContext)
        AppGraph.init(
            AppContainer(DatabaseDriverFactory(applicationContext).createDriver()),
        )
    }
}