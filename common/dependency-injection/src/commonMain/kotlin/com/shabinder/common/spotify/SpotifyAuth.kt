package com.shabinder.common.spotify

import io.ktor.client.*
import io.ktor.client.features.auth.*
import io.ktor.client.features.auth.providers.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*

suspend fun authenticateSpotify(): Token {
    return spotifyAuthClient.post("https://accounts.spotify.com/api/token"){
        body = FormDataContent(Parameters.build { append("grant_type","client_credentials") })
    }
}

private val spotifyAuthClient by lazy {
    HttpClient {
        val clientId = "694d8bf4f6ec420fa66ea7fb4c68f89d"
        val clientSecret = "02ca2d4021a7452dae2328b47a6e8fe8"

        install(Auth) {
            basic {
                sendWithoutRequest = true
                username = clientId
                password = clientSecret
            }
        }
        install(JsonFeature) {
            serializer = kotlinxSerializer
        }
    }
}

val kotlinxSerializer = KotlinxSerializer( kotlinx.serialization.json.Json {
    isLenient = true
    ignoreUnknownKeys = true
})