package com.example.mviexample

import android.app.Application
import com.example.mviexample.shared.AppGraph
import com.example.mviexample.shared.data.database.DatabaseDriverFactory
import com.example.mviexample.shared.di.AppContainer

class MviApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppGraph.init(
            AppContainer(DatabaseDriverFactory(applicationContext).createDriver()),
        )
    }
}
