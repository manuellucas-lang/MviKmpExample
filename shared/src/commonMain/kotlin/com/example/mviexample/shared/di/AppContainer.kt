package com.example.mviexample.shared.di

import app.cash.sqldelight.db.SqlDriver
import com.example.mviexample.shared.data.OperacionesRepository
import com.example.mviexample.shared.data.OperacionesRepositoryImpl
import com.example.mviexample.shared.data.network.OperacionesApi
import com.example.mviexample.shared.database.AppDatabase

class AppContainer(
    databaseDriver: SqlDriver,
) {
    private val database = AppDatabase(databaseDriver)
    private val api = OperacionesApi()

    val operacionesRepository: OperacionesRepository = OperacionesRepositoryImpl(api, database)
}
