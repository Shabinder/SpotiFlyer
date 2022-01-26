package com.shabinder.common.core_components.utils

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.apache.Apache
import org.apache.http.conn.ssl.NoopHostnameVerifier
import org.apache.http.conn.ssl.TrustSelfSignedStrategy
import org.apache.http.ssl.SSLContextBuilder

actual fun buildHttpClient(extraConfig: HttpClientConfig<*>.() -> Unit): HttpClient {
    return HttpClient(Apache) {
        engine {
            customizeClient {
                setSSLContext(
                    SSLContextBuilder
                        .create()
                        .loadTrustMaterial(TrustSelfSignedStrategy())
                        .build()
                )
                setSSLHostnameVerifier(NoopHostnameVerifier())
            }
        }
        extraConfig()
    }
}
