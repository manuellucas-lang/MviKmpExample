package com.example.mviexample.shared.di

import app.cash.sqldelight.db.SqlDriver
import com.example.mviexample.shared.data.PostsRepository
import com.example.mviexample.shared.data.PostsRepositoryImpl
import com.example.mviexample.shared.data.network.PostsApi
import com.example.mviexample.shared.database.AppDatabase

class AppContainer(
    databaseDriver: SqlDriver,
) {
    private val database = AppDatabase(databaseDriver)
    private val api = PostsApi()

    val postsRepository: PostsRepository = PostsRepositoryImpl(api, database)
}
