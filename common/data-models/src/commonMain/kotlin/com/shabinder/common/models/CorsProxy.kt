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

package com.shabinder.common.models

import io.github.shabinder.TargetPlatforms
import io.github.shabinder.activePlatform

sealed class CorsProxy(open val url: String) {
    data class SelfHostedCorsProxy(override val url: String = "https://cors.spotiflyer.ml/cors/" /*"https://spotiflyer.azurewebsites.net/"*/) : CorsProxy(url)
    data class PublicProxyWithExtension(override val url: String = "https://cors.bridged.cc/") : CorsProxy(url)

    fun toggle(mode: CorsProxy? = null): CorsProxy {
        mode?.let {
            corsProxy = mode
            return corsProxy
        }
        corsProxy = when (corsProxy) {
            is SelfHostedCorsProxy -> PublicProxyWithExtension()
            is PublicProxyWithExtension -> SelfHostedCorsProxy()
        }
        return corsProxy
    }

    fun extensionMode(): Boolean {
        return when (corsProxy) {
            is SelfHostedCorsProxy -> false
            is PublicProxyWithExtension -> true
        }
    }
}

/*
* This Var Keeps Track for Cors Config in JS Platform
* Default Self Hosted, However ask user to use extension if possible.
* */
var corsProxy: CorsProxy = CorsProxy.SelfHostedCorsProxy()

val corsApi get() = if (activePlatform is TargetPlatforms.Js) corsProxy.url else ""
