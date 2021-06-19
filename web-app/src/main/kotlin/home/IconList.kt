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

import Styles
import kotlinx.css.*
import kotlinx.html.id
import react.*
import react.dom.attrs
import styled.*

external interface IconListProps : RProps {
    var iconsAndPlatforms: Map<String,String>
    var isBadge:Boolean
}

@Suppress("FunctionName")
fun RBuilder.IconList(handler:IconListProps.() -> Unit): ReactElement {
    return child(iconList){
        attrs {
            handler()
        }
    }
}

private val iconList = functionalComponent<IconListProps>("IconList") { props ->

    styledDiv {
        css {
            margin(18.px)
            if(props.isBadge) {
                classes = mutableListOf("info-banners")
            }
            + Styles.makeRow
        }
        val firstElem = props.iconsAndPlatforms.keys.elementAt(1)
        for((icon,platformLink) in props.iconsAndPlatforms){
            if(icon == firstElem && props.isBadge){
                //<form><script src="https://checkout.razorpay.com/v1/payment-button.js" data-payment_button_id="pl_GnKuuDBdBu0ank" async> </script> </form>
                styledForm {
                    attrs {
                        id = "razorpay-form"
                    }
                }
            }
            styledA(href = platformLink,target="_blank"){
                styledImg {
                    attrs {
                        src = icon
                    }
                    css {
                        classes = mutableListOf("glow-button")
                        margin(8.px)
                        if (!props.isBadge) {
                            height = 42.px
                            width = 42.px
                            borderRadius = 50.px
                        }
                    }
                }
            }
        }
    }
}