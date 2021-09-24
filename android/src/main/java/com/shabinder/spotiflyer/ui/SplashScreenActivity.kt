package com.shabinder.spotiflyer.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.lifecycle.lifecycleScope
import com.shabinder.common.core_components.analytics.AnalyticsManager
import com.shabinder.common.di.ApplicationInit
import com.shabinder.spotiflyer.MainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.scope.ScopeActivity
import org.koin.core.parameter.parametersOf

class SplashScreenActivity : AppCompatActivity() {

    private val applicationInit: ApplicationInit by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applicationInit.init()
        lifecycleScope.launch {
            delay(SPLASH_DELAY)
            startActivity(Intent(this@SplashScreenActivity, MainActivity::class.java))
            finish()
        }
    }

    companion object {
        private const val SPLASH_DELAY = 2000L
    }
}