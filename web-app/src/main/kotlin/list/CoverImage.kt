package list

import kotlinx.css.*
import kotlinx.html.id
import react.*
import styled.css
import styled.styledDiv
import styled.styledH1
import styled.styledImg


external interface CoverImageProps : RProps {
    var coverImageURL: String
    var coverName: String
}

@Suppress("FunctionName")
fun RBuilder.CoverImage(handler: CoverImageProps.() -> Unit): ReactElement {
    return child(coverImage){
        attrs {
            handler()
        }
    }
}

private val coverImage = functionalComponent<CoverImageProps>("CoverImage"){ props ->
    styledDiv {
        styledImg(src= props.coverImageURL){
            css {
                height = 220.px
                width = 220.px
            }
        }
        styledH1 {
            +props.coverName
            css {
                textAlign = TextAlign.center
            }
        }
        attrs {
            id = "cover-image"
        }
        css {
            display = Display.flex
            alignItems = Align.center
            flexDirection = FlexDirection.column
            marginTop = 12.px
        }
    }
}