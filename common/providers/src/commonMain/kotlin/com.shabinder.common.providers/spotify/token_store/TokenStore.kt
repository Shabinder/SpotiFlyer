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

package com.shabinder.common.providers.spotify.token_store

import co.touchlab.kermit.Kermit
import com.shabinder.common.core_components.file_manager.FileManager
import com.shabinder.common.database.TokenDBQueries
import com.shabinder.common.models.spotify.TokenData
import com.shabinder.common.providers.spotify.requests.authenticateSpotify
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class TokenStore(
    private val fileManager: FileManager,
    private val logger: Kermit,
) {
    private val db: TokenDBQueries?
        get() = fileManager.db?.tokenDBQueries

    private fun save(token: TokenData) {
        if (!token.access_token.isNullOrBlank() && token.expiry != null)
            db?.add(token.access_token!!, token.expiry!! + Clock.System.now().epochSeconds)
    }

    @OptIn(DelicateCoroutinesApi::class)
    suspend fun getToken(): TokenData? {
        var token: TokenData? = db?.select()?.executeAsOneOrNull()?.let {
            TokenData(it.accessToken, null, it.expiry)
        }
        logger.d { "System Time:${Clock.System.now().epochSeconds} , Token Expiry:${token?.expiry}" }
        if ((Clock.System.now().epochSeconds > (token?.expiry ?: 0)) || token == null) {
            logger.d { "Requesting New Token" }
            token = authenticateSpotify().component1()
            GlobalScope.launch { token?.access_token?.let { save(token) } }
        }
        return token
    }
}
