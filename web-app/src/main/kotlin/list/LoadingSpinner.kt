package list

import kotlinx.css.px
import kotlinx.css.width
import react.*
import styled.css
import styled.styledDiv

@Suppress("FunctionName")
fun RBuilder.LoadingSpinner(handler: RProps.() -> Unit): ReactElement {
    return child(loadingSpinner){
        attrs {
            handler()
        }
    }
}

private val loadingSpinner = functionalComponent<RProps>("Loading-Spinner") {
    styledDiv {
        styledDiv{}
        styledDiv{}
        styledDiv{}
        styledDiv{}
        css{
            classes = mutableListOf("lds-ring")
            width = 50.px
        }
    }
}