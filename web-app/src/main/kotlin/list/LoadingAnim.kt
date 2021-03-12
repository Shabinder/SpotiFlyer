package list

import kotlinx.css.*
import react.*
import styled.css
import styled.styledDiv


@Suppress("FunctionName")
fun RBuilder.LoadingAnim(handler: RProps.() -> Unit): ReactElement {
    return child(loadingAnim){
        attrs {
            handler()
        }
    }
}

private val loadingAnim = functionalComponent<RProps>("Loading Animation") {
    styledDiv {

        styledDiv { css { classes = mutableListOf("sk-cube sk-cube1") } }
        styledDiv { css { classes = mutableListOf("sk-cube sk-cube2") } }
        styledDiv { css { classes = mutableListOf("sk-cube sk-cube3") } }
        styledDiv { css { classes = mutableListOf("sk-cube sk-cube4") } }
        styledDiv { css { classes = mutableListOf("sk-cube sk-cube5") } }
        styledDiv { css { classes = mutableListOf("sk-cube sk-cube6") } }
        styledDiv { css { classes = mutableListOf("sk-cube sk-cube7") } }
        styledDiv { css { classes = mutableListOf("sk-cube sk-cube8") } }
        styledDiv { css { classes = mutableListOf("sk-cube sk-cube9") } }

        css {
            classes = mutableListOf("sk-cube-grid")
            height = 60.px
            width = 60.px
        }
    }
}