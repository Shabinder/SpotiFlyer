import home.homeScreen
import react.dom.render
import kotlinx.browser.document
import kotlinx.browser.window
import navbar.navBar

fun main() {
    window.onload = {
        render(document.getElementById("root")) {
            navBar {}
            homeScreen {
                link = ""
            }
        }
    }
}