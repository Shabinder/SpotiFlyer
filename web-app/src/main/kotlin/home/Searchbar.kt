package home

import kotlinx.browser.window
import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onKeyDownFunction
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.Window
import react.*
import styled.*

external interface SearchbarProps : RProps {
    var link: String
    var search:(String)->Unit
    var onLinkChange:(String)->Unit
}

@Suppress("FunctionName")
fun RBuilder.SearchBar(handler:SearchbarProps.() -> Unit) = child(searchbar){
    attrs {
        handler()
    }
}



val searchbar = functionalComponent<SearchbarProps>("SearchBar"){ props ->
    styledDiv{
        css {
            classes = mutableListOf("searchBox")
        }
        styledInput(type = InputType.url){
            attrs {
                placeholder = "Search"
                onChangeFunction = {
                    val target = it.target as HTMLInputElement
                    props.onLinkChange(target.value)
                }
                this.onKeyDownFunction = {
                    if(it.asDynamic().key == "Enter") {
                        if(props.link.isEmpty()) window.alert("Enter a Link from Supported Platforms")
                        else props.search(props.link)
                    }
                }
                value = props.link
            }
            css {
                classes = mutableListOf("searchInput")
            }
        }
        styledButton {
            attrs {
                onClickFunction = {
                    if(props.link.isEmpty()) window.alert("Enter a Link from Supported Platforms")
                    else props.search(props.link)
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
