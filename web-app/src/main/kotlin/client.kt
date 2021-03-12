import co.touchlab.kermit.Kermit
import com.shabinder.common.di.Dir
import com.shabinder.common.di.FetchPlatformQueryResult
import com.shabinder.common.di.initKoin
import react.dom.render
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

fun main() {
    window.onload = {
        render(document.getElementById("root")) {
            App {
                dependencies = AppDependencies
            }
        }
    }
}

object AppDependencies : KoinComponent {
    val appScope = CoroutineScope(Dispatchers.Default)
    val logger: Kermit
    val directories: Dir
    val fetchPlatformQueryResult: FetchPlatformQueryResult
    init {
        initKoin()
        directories = get()
        logger = get()
        fetchPlatformQueryResult = get()
        appScope.launch {
            //fetchPlatformQueryResult.spotifyProvider.authenticateSpotifyClient(true)
        }
    }
}