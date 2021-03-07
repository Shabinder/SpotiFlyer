package extras

import kotlinext.js.Object
import kotlinext.js.jsObject

var uniqueId: Long = 0L

internal fun Any.uniqueId(): Long {
    var id: dynamic = asDynamic().__unique_id
    if (id == undefined) {
        id = ++uniqueId
        Object.defineProperty<Any, Long>(this, "__unique_id", jsObject { value = id })
    }

    return id
}
