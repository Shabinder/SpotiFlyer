import co.touchlab.kermit.Kermit
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.lifecycle.LifecycleRegistry
import com.arkivanov.decompose.lifecycle.destroy
import com.arkivanov.decompose.lifecycle.resume
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.shabinder.common.models.DownloadStatus
import com.shabinder.common.root.SpotiFlyerRoot
import com.shabinder.database.Database
import extras.renderableChild
import kotlinx.coroutines.flow.MutableSharedFlow
import react.*
import root.RootR

external interface AppProps : RProps {
    var dependencies: AppDependencies
}

fun RBuilder.app(attrs: AppProps.() -> Unit): ReactElement {
    return child(App::class){
        this.attrs(attrs)
    }
}

class App(props: AppProps): RComponent<AppProps, RState>(props) {

    private val lifecycle = LifecycleRegistry()
    private val ctx = DefaultComponentContext(lifecycle = lifecycle)
    private val dependencies = props.dependencies
    private val logger:Kermit
        get() = dependencies.logger

    private val root = SpotiFlyerRoot(ctx,
        object : SpotiFlyerRoot.Dependencies{
            override val storeFactory: StoreFactory = DefaultStoreFactory
            override val fetchPlatformQueryResult = dependencies.fetchPlatformQueryResult
            override val directories = dependencies.directories
            override val database: Database? = directories.db
            override val showPopUpMessage: (String) -> Unit = {}//TODO
            override val downloadProgressReport: MutableSharedFlow<HashMap<String, DownloadStatus>>
                = MutableSharedFlow(1)

        }
    )

    override fun componentDidMount() {
        lifecycle.resume()
    }

    override fun componentWillUnmount() {
        lifecycle.destroy()
    }

    override fun RBuilder.render() {
        renderableChild(RootR::class, root)
    }
}