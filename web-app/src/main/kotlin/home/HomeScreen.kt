package home

import kotlinx.css.*
import react.*
import styled.css
import styled.styledDiv


data class HomeScreenState(var link:String): RState

external interface HomeScreenProps : RProps {
    var link: String
}

fun RBuilder.homeScreen(attrs:HomeScreenProps.() -> Unit): ReactElement {
    return child(HomeScreen::class){
        this.attrs(attrs)
    }
}

class HomeScreen(props:HomeScreenProps):RComponent<HomeScreenProps,HomeScreenState>(props) {
    override fun RBuilder.render() {
        styledDiv{
            css {
                display = Display.flex
                flexDirection = FlexDirection.column
                flexGrow = 1.0
                justifyContent = JustifyContent.center
                alignItems = Align.center
            }
            message {}
            searchBar {
                link = props.link
            }
            iconList {
                iconsAndPlatforms = iconList
                isBadge = false
            }
        }
        iconList{
            iconsAndPlatforms = badges
            isBadge = true
        }
    }
}


private val iconList = mapOf(
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