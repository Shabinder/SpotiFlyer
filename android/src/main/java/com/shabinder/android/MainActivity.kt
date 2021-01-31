package com.shabinder.android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.setContent
import com.shabinder.android.di.appModule
import com.shabinder.common.database.appContext
import com.shabinder.common.initKoin
import com.shabinder.common.ui.SpotiFlyerMain
import org.koin.android.ext.koin.androidLogger

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val scope = rememberCoroutineScope()
            SpotiFlyerMain()

        }
    }
}