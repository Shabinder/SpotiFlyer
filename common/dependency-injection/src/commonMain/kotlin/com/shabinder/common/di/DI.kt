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

package com.shabinder.common.di

import com.shabinder.common.core_components.coreComponentModules
import com.shabinder.common.database.databaseModule
import com.shabinder.common.providers.providersModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration

fun initKoin(enableNetworkLogs: Boolean = false, appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()

        modules(
            coreComponentModules(enableNetworkLogs),
            listOf(
                providersModule(enableNetworkLogs),
                databaseModule(),
                appInitModule(),
            )
        )
    }

// Called by IOS
fun initKoin() = initKoin(enableNetworkLogs = false) { }

private fun KoinApplication.modules(vararg moduleLists: List<Module>): KoinApplication {
    return modules(moduleLists.toList().flatten())
}