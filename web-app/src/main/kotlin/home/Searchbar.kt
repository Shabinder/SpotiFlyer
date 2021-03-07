package home

import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import org.w3c.dom.HTMLInputElement
import react.*
import styled.*

external interface SearchbarProps : RProps {
    var link: String
    var search:(String)->Unit
}

@Suppress("FunctionName")
fun RBuilder.SearchBar(handler:SearchbarProps.() -> Unit) = child(searchbar){
    attrs {
        handler()
    }
}



val searchbar = functionalComponent<SearchbarProps>("SearchBar"){ props ->
    val (link,setLink) = useState(props.link)

    styledDiv{
        css {
            classes = mutableListOf("searchBox")
        }
        styledInput(type = InputType.url){
            attrs {
                placeholder = "Search"
                onChangeFunction = {
                    val target = it.target as HTMLInputElement
                    setLink(target.value)
                }
                value = link
            }
            css {
                classes = mutableListOf("searchInput")
            }
        }
        styledButton {
            attrs {
                onClickFunction = {
                    props.search(link)
                }
            }
            css {
                classes = mutableListOf("searchButton")
            }
            styledImg(src = "search.svg") {
                css {
                    classes = mutableListOf("search-icon")
                }
            }
        }
    }
}
