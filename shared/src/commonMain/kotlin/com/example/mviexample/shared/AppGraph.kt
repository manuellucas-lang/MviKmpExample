package com.example.mviexample.shared

import com.example.mviexample.shared.di.AppContainer

object AppGraph {
    lateinit var container: AppContainer
        private set

    fun init(container: AppContainer) {
        this.container = container
    }
}
