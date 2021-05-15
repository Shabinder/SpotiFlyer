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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import react.RComponent
import react.RProps
import react.RState
import react.setState

abstract class RenderableComponent<
        T : Any,
        S : Any
        >(
    props: Props<T>,
    initialState: S
) : RComponent<RenderableComponent.Props<T>, RenderableComponent.State<S>>(props) {

    protected abstract val stateFlow: Value<S>
    protected val model: T get() = props.model
    protected var scope: CoroutineScope = CoroutineScope(Dispatchers.Default)

    init {
        state = State(data = initialState)
    }

    override fun componentDidMount() {
        if(!scope.isActive)
            scope = CoroutineScope(Dispatchers.Default)
        scope.launch {
            stateFlow.subscribe {
                setState { data = it }
            }
        }
    }

    override fun componentWillUnmount() {
        scope.cancel("Component Unmounted")
    }

    interface Props<T : Any> : RProps {
        var model: T
    }

    class State<S>(
        var data: S
    ):RState
}