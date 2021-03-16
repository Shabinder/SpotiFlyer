package home

import kotlinx.browser.document
import kotlinx.css.*
import kotlinx.dom.appendElement
import kotlinx.dom.createElement
import kotlinx.html.SCRIPT
import kotlinx.html.id
import react.*
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

    useEffect {
        val form = document.getElementById("razorpay-form")!!
        repeat(form.childNodes.length){
            form.childNodes.item(it)?.let { it1 -> form.removeChild(it1) }
        }
        form.appendElement("script"){
            this.setAttribute("src","https://checkout.razorpay.com/v1/payment-button.js")
            this.setAttribute("async", true.toString())
            this.setAttribute("data-payment_button_id", "pl_GnKuuDBdBu0ank")
        }
    }

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
                    attrs{
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