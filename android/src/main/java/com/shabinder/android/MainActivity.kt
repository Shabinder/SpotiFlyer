package com.shabinder.android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.arkivanov.decompose.ComponentContext
import androidx.compose.ui.platform.setContent
import com.arkivanov.decompose.extensions.compose.jetbrains.rootComponent
import com.arkivanov.mvikotlin.logging.store.LoggingStoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.shabinder.common.di.Dir
import com.shabinder.common.di.FetchPlatformQueryResult
import com.shabinder.common.root.SpotiFlyerRoot
import com.shabinder.common.root.SpotiFlyerRootContent
import com.shabinder.common.ui.SpotiFlyerTheme
import com.shabinder.database.Database
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {

    val database: Database by inject()
    val fetcher: FetchPlatformQueryResult by inject()
    val dir: Dir by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SpotiFlyerTheme {
                    SpotiFlyerRootContent(rootComponent(::spotiFlyerRoot))
            }
        }
    }


    private fun spotiFlyerRoot(componentContext: ComponentContext): SpotiFlyerRoot =
        SpotiFlyerRoot(
            componentContext,
            dependencies = object : SpotiFlyerRoot.Dependencies{
                override val storeFactory = LoggingStoreFactory(DefaultStoreFactory)
                override val database = this@MainActivity.database
                override val fetchPlatformQueryResult = this@MainActivity.fetcher
                override val directories: Dir = this@MainActivity.dir
            }
        )
}
