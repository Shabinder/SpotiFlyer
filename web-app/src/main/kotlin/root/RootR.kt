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
import extras.RenderableRootComponent
import extras.renderableChild
import home.HomeScreen
import list.ListScreen
import navbar.NavBar
import react.RBuilder
import react.RState

class RootR(props: Props<SpotiFlyerRoot>) : RenderableRootComponent<SpotiFlyerRoot, RootR.State>(
    props = props,
    initialState = State(routerState = props.model.routerState.value)
) {
    private val component: Child
        get() = model.routerState.value.activeChild.instance

    private val callBacks get() = model.callBacks

    override fun RBuilder.render() {
        NavBar {
            isBackVisible = (component is Child.List)
            popBackToHomeScreen = callBacks::popBackToHomeScreen
        }
        when(component){
            is Child.Main -> renderableChild(HomeScreen::class, (component as Child.Main).component)
            is Child.List -> renderableChild(ListScreen::class, (component as Child.List).component)
        }
    }

    init {
        model.routerState.bindToState { routerState = it }
    }
    class State(
        var routerState: RouterState<*, Child>
    ) : RState

}
