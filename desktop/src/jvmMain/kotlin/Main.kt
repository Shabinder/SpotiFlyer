import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.desktop.Window
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.extensions.compose.jetbrains.rootComponent
import com.arkivanov.mvikotlin.core.lifecycle.LifecycleRegistry
import com.arkivanov.mvikotlin.core.lifecycle.resume
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.shabinder.common.di.Dir
import com.shabinder.common.di.FetchPlatformQueryResult
import com.shabinder.common.di.initKoin
import com.shabinder.common.root.SpotiFlyerRoot
import com.shabinder.common.root.SpotiFlyerRootContent
import com.shabinder.common.ui.SpotiFlyerColors
import com.shabinder.common.ui.SpotiFlyerShapes
import com.shabinder.common.ui.SpotiFlyerTypography
import com.shabinder.database.Database

private val koin = initKoin(enableNetworkLogs = true).koin

fun main(){

    val lifecycle = LifecycleRegistry()
    lifecycle.resume()

    Window("SpotiFlyer") {
        Surface(modifier = Modifier.fillMaxSize()) {
            DesktopMaterialTheme(
//                colors = SpotiFlyerColors,
//                typography = SpotiFlyerTypography,
//                shapes = SpotiFlyerShapes
            ) {
                SpotiFlyerRootContent(rootComponent(factory = ::spotiFlyerRoot))
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
        }
    )