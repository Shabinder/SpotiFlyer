package com.shabinder.spotiflyer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.res.vectorResource
import androidx.core.view.WindowCompat
import com.shabinder.spotiflyer.home.Home
import com.shabinder.spotiflyer.ui.ComposeLearnTheme
import com.shabinder.spotiflyer.ui.appNameStyle
import dev.chrisbanes.accompanist.insets.ProvideWindowInsets
import dev.chrisbanes.accompanist.insets.statusBarsHeight

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This app draws behind the system bars, so we want to handle fitting system windows
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            ComposeLearnTheme {
                ProvideWindowInsets {
                    Column {
                        val appBarColor = MaterialTheme.colors.surface.copy(alpha = 0.87f)

                        // Draw a scrim over the status bar which matches the app bar
                        Spacer(Modifier.background(appBarColor).fillMaxWidth().statusBarsHeight())

                        AppBar(
                            backgroundColor = appBarColor,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Home()
                    }
                }
            }
        }
    }
}

@Composable
fun AppBar(
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        backgroundColor = backgroundColor,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    imageVector = vectorResource(R.drawable.ic_launcher_foreground)
                )
                Text(
                    text = "SpotiFlyer",
                    style = appNameStyle
                )
            }
        },
        actions = {
            Providers(AmbientContentAlpha provides ContentAlpha.medium) {
                IconButton(
                    onClick = { /* TODO: Open Settings */ }
                ) {
                    Icon(Icons.Filled.Settings, tint = Color.Gray)
                }
            }
        },
        modifier = modifier
    )
}


//@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ComposeLearnTheme {

    }
}