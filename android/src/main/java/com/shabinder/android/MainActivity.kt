package com.shabinder.android

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.setContent
import com.shabinder.common.authenticateSpotify
import com.shabinder.common.ui.SpotiFlyerMain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val scope = rememberCoroutineScope()
            SpotiFlyerMain()
            scope.launch(Dispatchers.IO) {
                val token = authenticateSpotify()
                Log.i("Spotify",token.toString())
            }
        }
    }
}