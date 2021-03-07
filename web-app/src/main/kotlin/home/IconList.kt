package home

import kotlinx.css.*
import react.*
import styled.css
import styled.styledA
import styled.styledDiv
import styled.styledImg

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
            +Styles.makeRow
            margin(18.px)
            if(props.isBadge) {
                alignItems = Align.end
            }
        }
        for((icon,platformLink) in props.iconsAndPlatforms){
            styledA(href = platformLink){
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