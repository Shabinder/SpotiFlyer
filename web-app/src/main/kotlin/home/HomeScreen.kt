package home

import com.shabinder.common.main.SpotiFlyerMain
import com.shabinder.common.main.SpotiFlyerMain.State
import extras.RenderableComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.css.*
import react.*
import styled.css
import styled.styledDiv

class HomeScreen(
    props: Props<SpotiFlyerMain>,
) : RenderableComponent<SpotiFlyerMain, State>(
    props,
    initialState = State()
) {

    override val stateFlow: Flow<SpotiFlyerMain.State> = model.models

    override fun componentDidMount() {
        if(!scope.isActive)
            scope = CoroutineScope(Dispatchers.Default)
        scope.launch {
            stateFlow.collect {
                println("Updating State = $it")
                setState { data = it }
            }
        }
    }

    override fun RBuilder.render() {
        println("Rendering New State = \"${state.data}\" ")
        styledDiv{
            css {
                display = Display.flex
                flexDirection = FlexDirection.column
                flexGrow = 1.0
                justifyContent = JustifyContent.center
                alignItems = Align.center
            }

            Message {
                text = "Your Gateway to Nirvana, for FREE!"
            }

            SearchBar {
                println("Search Props ${state.data.link}")
                link = state.data.link
                search = model::onLinkSearch
                onLinkChange = model::onInputLinkChanged
            }

            IconList {
                iconsAndPlatforms = platformIconList
                isBadge = false
            }
        }
        IconList {
            iconsAndPlatforms = badges
            isBadge = true
        }
    }
}


private val platformIconList = mapOf(
    "spotify.svg" to "https://open.spotify.com/",
    "gaana.svg" to "https://www.gaana.com/",
    "youtube.svg" to "https://www.youtube.com/",
    "youtube_music.svg" to "https://music.youtube.com/"
)
private val badges = mapOf(
    "https://img.shields.io/github/v/release/Shabinder/SpotiFlyer?color=7885FF&label=SpotiFlyer&logo=android&style=for-the-badge"
            to "https://github.com/Shabinder/SpotiFlyer/releases/latest/",
    "https://img.shields.io/github/downloads/Shabinder/SpotiFlyer/total?style=for-the-badge&logo=android&color=17B2E7"
            to "https://github.com/Shabinder/SpotiFlyer/releases/latest/"
)