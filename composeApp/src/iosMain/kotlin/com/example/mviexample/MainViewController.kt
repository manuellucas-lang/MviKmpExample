package com.example.mviexample

import androidx.compose.ui.window.ComposeUIViewController
import com.example.mviexample.shared.AppGraph
import com.example.mviexample.shared.data.database.DatabaseDriverFactory
import com.example.mviexample.shared.di.AppContainer
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    AppGraph.init(
        AppContainer(DatabaseDriverFactory().createDriver()),
    )
    return ComposeUIViewController { App() }
}
