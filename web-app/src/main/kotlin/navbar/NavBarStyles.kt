package navbar

import kotlinx.css.*
import styled.StyleSheet

object NavBarStyles : StyleSheet("WelcomeStyles", isStatic = true) {
    val nav by css{
        padding(horizontal = 16.px)
        marginTop = 10.px
        backgroundColor = Color.transparent
        height = 56.px
        display = Display.flex
        flexDirection = FlexDirection.row
        alignItems = Align.center
        alignSelf = Align.stretch
    }
} 
