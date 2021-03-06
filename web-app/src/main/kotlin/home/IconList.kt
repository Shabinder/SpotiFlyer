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

fun RBuilder.iconList(attrs:IconListProps.() -> Unit): ReactElement {
    return child(IconList::class){
        this.attrs(attrs)
    }
}

class IconList(props: IconListProps):RComponent<IconListProps,RState>(props) {
    override fun RBuilder.render() {
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
}