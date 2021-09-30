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

package root

import com.arkivanov.decompose.RouterState
import com.shabinder.common.root.SpotiFlyerRoot
import com.shabinder.common.root.SpotiFlyerRoot.Child
import extras.Props
import extras.RenderableComponent
import extras.renderableChild
import home.HomeScreen
import list.ListScreen
import navbar.NavBar
import react.RBuilder

class RootR(props: Props<SpotiFlyerRoot>) : RenderableComponent<SpotiFlyerRoot, State>(
    props = props,
    initialState = State(routerState = props.component.routerState.value)
) {
    private val child: Child
        get() = component.routerState.value.activeChild.instance

    private val callBacks get() = component.callBacks

    init {
        component.routerState.bindToState { routerState = it }
    }

    override fun RBuilder.render() {
        NavBar {
            isBackVisible = (child is Child.List)
            popBackToHomeScreen = callBacks::popBackToHomeScreen
        }
        when(child){
            is Child.Main -> renderableChild(HomeScreen::class, (child as Child.Main).component)
            is Child.List -> renderableChild(ListScreen::class, (child as Child.List).component)
        }
    }
}

@Suppress("NON_EXPORTABLE_TYPE", "EXPERIMENTAL_IS_NOT_ENABLED")
@OptIn(ExperimentalJsExport::class)
@JsExport
class State(
    var routerState: RouterState<*, Child>
) : react.State
