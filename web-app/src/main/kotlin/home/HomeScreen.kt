package home

import com.shabinder.common.main.SpotiFlyerMain
import extras.RenderableComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.css.*
import react.*
import styled.css
import styled.styledDiv

class HomeScreen(
    props: Props<SpotiFlyerMain>,
    override val stateFlow: Flow<State> = props.model.models.map { State(it) }
) : RenderableComponent<SpotiFlyerMain, HomeScreen.State>(
    props,
    initialState = State(data = SpotiFlyerMain.State())
) {

    override fun RBuilder.render() {
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
                link = state.data.link
                search = model::onLinkSearch
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

    class State(
        var data: SpotiFlyerMain.State
    ):RState
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