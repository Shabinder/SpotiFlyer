import androidx.compose.desktop.Window
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import com.shabinder.common.initKoin

private val koin = initKoin(enableNetworkLogs = true).koin

fun main() = Window {
    Column{
        Text("Hello World")
    }
}