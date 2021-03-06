import kotlinx.css.*
import styled.StyleSheet


val colorPrimary = Color("#FC5C7D")
val colorPrimaryDark = Color("#CE1CFF")
val colorAccent = Color("#9AB3FF")
val colorOffWhite = Color("#E7E7E7")

object Styles: StyleSheet("Searchbar", isStatic = true) {
    val makeRow by css {
        display = Display.flex
        flexDirection = FlexDirection.row
        alignItems = Align.center
        alignContent = Align.center
        justifyContent = JustifyContent.center
    }
    val darkMode by css {
        backgroundColor = Color.black
        color = Color.white
    }
    val circular by css {
        borderRadius = 30.px
        borderWidth = 5.px
        borderBottomStyle = BorderStyle.solid
    }
    val circularGradient by css {
        apply(circular)
        borderColor = Color.aqua
        borderBottomColor = colorPrimary
        borderRightColor = colorPrimary
    }
    val largePadding by css { padding(20.px) }
    val mediumPadding by css { padding(14.px) }
    val smallPadding by css { padding(4.px) }
    val largeMargin by css { margin(20.px) }
    val mediumMargin by css { margin(12.px) }
    val smallMargin by css { margin(4.px) }
}