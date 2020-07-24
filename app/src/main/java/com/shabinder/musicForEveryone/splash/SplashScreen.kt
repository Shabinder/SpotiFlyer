package com.shabinder.musicForEveryone.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.shabinder.musicForEveryone.MainActivity
import com.shabinder.musicForEveryone.R

class SplashScreen : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)

        val splashTimeout = 600
        val homeIntent = Intent(this@SplashScreen, MainActivity::class.java)
        Handler().postDelayed({
            //TODO:Bring Initial Setup here
            startActivity(homeIntent)
            finish()
        }, splashTimeout.toLong())
    }

}