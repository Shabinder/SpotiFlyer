/*
 *  * Copyright (c)  2021  Shabinder Singh
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  *  You should have received a copy of the GNU General Public License
 *  *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package extras

import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.ValueObserver
import react.PropsWithChildren
import react.RComponent
import react.State
import react.setState


@Suppress("EXPERIMENTAL_IS_NOT_ENABLED", "NON_EXPORTABLE_TYPE")
@OptIn(ExperimentalJsExport::class)
@JsExport
abstract class RenderableComponent<T : Any, S : State>(
    props: Props<T>,
    initialState: S
) : RComponent<Props<T>, S>(props) {

    private val subscriptions = ArrayList<Subscription<*>>()
    protected val component: T get() = props.component

    init {
        state = initialState
    }

    override fun componentDidMount() {
        subscriptions.forEach { subscribe(it) }
    }

    private fun <T : Any> subscribe(subscription: Subscription<T>) {
        subscription.value.subscribe(subscription.observer)
    }

    override fun componentWillUnmount() {
        subscriptions.forEach { unsubscribe(it) }
    }

    private fun <T : Any> unsubscribe(subscription: Subscription<T>) {
        subscription.value.unsubscribe(subscription.observer)
    }

    protected fun <T : Any> Value<T>.bindToState(buildState: S.(T) -> Unit) {
        subscriptions += Subscription(this) { data -> setState { buildState(data) } }
    }


    protected class Subscription<T : Any>(
        val value: Value<T>,
        val observer: ValueObserver<T>
    )
}

@Suppress("EXPERIMENTAL_IS_NOT_ENABLED", "NON_EXPORTABLE_TYPE")
@OptIn(ExperimentalJsExport::class)
@JsExport
class RStateWrapper<T>(
    var model: T
) : State

external interface Props<T : Any> : PropsWithChildren {
    var component: T
}