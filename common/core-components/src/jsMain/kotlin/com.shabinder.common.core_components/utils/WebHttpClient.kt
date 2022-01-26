package com.shabinder.common.core_components.utils

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.js.Js

actual fun buildHttpClient(extraConfig: HttpClientConfig<*>.() -> Unit): HttpClient = HttpClient(Js) {
    extraConfig()
}
