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

package com.shabinder.common.providers.spotify.requests

import com.shabinder.common.core_components.preference_manager.PreferenceManager
import com.shabinder.common.models.SpotiFlyerException
import com.shabinder.common.models.event.coroutines.SuspendableEvent
import com.shabinder.common.models.Actions
import com.shabinder.common.models.spotify.TokenData
import com.shabinder.common.utils.globalJson
import io.ktor.client.*
import io.ktor.client.features.auth.*
import io.ktor.client.features.auth.providers.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlin.native.concurrent.SharedImmutable

suspend fun authenticateSpotify(): SuspendableEvent<TokenData, Throwable> = SuspendableEvent {
    if (Actions.instance.isInternetAvailable) {
        spotifyAuthClient.post("https://accounts.spotify.com/api/token") {
            body = FormDataContent(Parameters.build {
                @Suppress("EXPERIMENTAL_API_USAGE_FUTURE_ERROR")
                append("grant_type", "client_credentials")
            })
        }
    } else throw SpotiFlyerException.NoInternetException()
}

@SharedImmutable
private val spotifyAuthClient by lazy {
    HttpClient {
        val (clientId, clientSecret) = PreferenceManager.instance.spotifyCredentials

        install(Auth) {
            basic {
                sendWithoutRequest { true }
                credentials {
                    BasicAuthCredentials(clientId, clientSecret)
                }
            }
        }
        install(JsonFeature) {
            serializer = KotlinxSerializer(globalJson)
        }
    }
}
