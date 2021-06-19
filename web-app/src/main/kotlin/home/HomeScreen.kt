/*
 *  * Copyright (c)  2021  Shabinder Singh
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  *  You should have received a copy of the GNU General Public License
 *  *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package home

import com.shabinder.common.main.SpotiFlyerMain
import extras.Props
import extras.RStateWrapper
import extras.RenderableComponent
import kotlinx.browser.document
import kotlinx.css.*
import kotlinx.dom.appendElement
import react.RBuilder
import styled.css
import styled.styledDiv

class HomeScreen(
    props: Props<SpotiFlyerMain>,
) : RenderableComponent<SpotiFlyerMain, RStateWrapper<SpotiFlyerMain.State>>(
    props,
    initialState = RStateWrapper(props.component.model.value)
) {

    init {
        component.model.bindToState {
            model = it
        }
    }

    override fun componentDidMount() {
        super.componentDidMount()
        // RazorPay Button
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
                link = state.model.link
                search = component::onLinkSearch
                onLinkChange = component::onInputLinkChanged
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