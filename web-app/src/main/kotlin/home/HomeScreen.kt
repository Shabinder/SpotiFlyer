package home

import com.shabinder.common.di.currentPlatform
import com.shabinder.common.main.SpotiFlyerMain
import com.shabinder.common.main.SpotiFlyerMain.State
import com.shabinder.common.models.AllPlatforms
import extras.RenderableComponent
import kotlinx.browser.document
import kotlinx.coroutines.flow.Flow
import kotlinx.css.*
import kotlinx.dom.appendElement
import react.*
import styled.css
import styled.styledDiv

class HomeScreen(
    props: Props<SpotiFlyerMain>,
) : RenderableComponent<SpotiFlyerMain, State>(
    props,
    initialState = State()
) {
    override fun componentDidMount() {
        super.componentDidMount()
        val form = document.getElementById("razorpay-form")!!
        repeat(form.childNodes.length){
            form.childNodes.item(0)?.let { it1 -> form.removeChild(it1) }
            form.childNodes.item(it)?.let { it1 -> form.removeChild(it1) }
        }
        form.appendElement("script"){
            this.setAttribute("src","https://checkout.razorpay.com/v1/payment-button.js")
            this.setAttribute("async", true.toString())
            this.setAttribute("data-payment_button_id", "pl_GnKuuDBdBu0ank")
        }
    }

    override val stateFlow: Flow<SpotiFlyerMain.State> = model.models

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
    //"youtube.svg" to "https://www.youtube.com/",
    //"youtube_music.svg" to "https://music.youtube.com/"
)
private val badges = mapOf(
    "https://img.shields.io/github/v/release/Shabinder/SpotiFlyer?color=7885FF&label=SpotiFlyer&logo=android&style=for-the-badge"
            to "https://github.com/Shabinder/SpotiFlyer/releases/latest/",
    "https://img.shields.io/github/downloads/Shabinder/SpotiFlyer/total?style=for-the-badge&logo=android&color=17B2E7"
            to "https://github.com/Shabinder/SpotiFlyer/releases/latest/"
)