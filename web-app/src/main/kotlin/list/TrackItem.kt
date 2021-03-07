package list

import react.RProps
import react.rFunction

external interface TrackItemProps : RProps {
    var coverImageURL: String
    var coverName: String
}

val trackItem = rFunction<TrackItemProps>("Track-Item"){

}
