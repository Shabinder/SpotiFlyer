package home

import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import org.w3c.dom.HTMLInputElement
import react.*
import styled.*

data class SearchbarState(var link:String):RState

external interface SearchbarProps : RProps {
    var link: String
}

fun RBuilder.searchBar(attrs:SearchbarProps.() -> Unit): ReactElement {
    return child(Searchbar::class){
        this.attrs(attrs)
    }
}

class Searchbar(props: SearchbarProps): RComponent<SearchbarProps,SearchbarState>(props) {
    init {
        state = SearchbarState(props.link)
    }

    override fun RBuilder.render() {
        styledDiv{
            css {
                classes = mutableListOf("searchBox")
            }
            styledInput(type = InputType.url){
                attrs {
                    placeholder = "Search"
                    onChangeFunction = {
                        val target = it.target as HTMLInputElement
                        setState{
                            link = target.value
                        }
                    }
                    value = state.link
                }
                css {
                    classes = mutableListOf("searchInput")
                }
            }
            styledButton {
                attrs {
                    onClickFunction = {

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

}