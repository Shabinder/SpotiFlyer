package list

import kotlinx.css.*
import kotlinx.html.id
import react.RProps
import react.rFunction
import react.useState
import styled.css
import styled.styledDiv
import styled.styledH1
import styled.styledImg


external interface CoverImageProps : RProps {
    var coverImageURL: String
    var coverName: String
}

val CoverImage = rFunction<CoverImageProps>("CoverImage"){ props ->
    val (coverURL,setCoverURL) = useState(props.coverImageURL)
    val (coverName,setCoverName) = useState(props.coverName)

    styledDiv {
        styledImg(src=coverURL){
            css {
                height = 300.px
                width = 300.px
            }
        }
        styledH1 {
            +coverName
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
        }
    }
}