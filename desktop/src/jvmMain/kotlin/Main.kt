import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.desktop.Window
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.extensions.compose.jetbrains.rootComponent
import com.arkivanov.mvikotlin.core.lifecycle.LifecycleRegistry
import com.arkivanov.mvikotlin.core.lifecycle.resume
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.shabinder.common.di.Dir
import com.shabinder.common.di.DownloadProgressFlow
import com.shabinder.common.di.FetchPlatformQueryResult
import com.shabinder.common.di.initKoin
import com.shabinder.common.root.SpotiFlyerRoot
import com.shabinder.common.root.SpotiFlyerRootContent
import com.shabinder.common.ui.SpotiFlyerColors
import com.shabinder.common.ui.SpotiFlyerShapes
import com.shabinder.common.ui.SpotiFlyerTypography
import com.shabinder.common.ui.colorOffWhite
import com.shabinder.database.Database
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

private val koin = initKoin(enableNetworkLogs = true).koin

fun main(){

    val lifecycle = LifecycleRegistry()
    lifecycle.resume()

    Window("SpotiFlyer") {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black,
            contentColor = colorOffWhite
        ) {
            DesktopMaterialTheme(
                colors = SpotiFlyerColors,
                typography = SpotiFlyerTypography,
                shapes = SpotiFlyerShapes
            ) {
                val callBacks = SpotiFlyerRootContent(rootComponent(factory = ::spotiFlyerRoot)).callBacks
                val scope = rememberCoroutineScope()
                scope.launch {
                    DownloadProgressFlow.collect {

                    }
                }
            }
        }
    }
}

private fun spotiFlyerRoot(componentContext: ComponentContext): SpotiFlyerRoot =
    SpotiFlyerRoot(
        componentContext = componentContext,
        dependencies = object : SpotiFlyerRoot.Dependencies {
            override val storeFactory = DefaultStoreFactory
            override val database: Database = koin.get()
            override val fetchPlatformQueryResult: FetchPlatformQueryResult = koin.get()
            override val directories: Dir = koin.get()
            override val downloadProgressReport = DownloadProgressFlow
        }
    )