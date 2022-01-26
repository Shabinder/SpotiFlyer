package com.shabinder.common.core_components.utils

import android.annotation.SuppressLint
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.engine.okhttp.OkHttpConfig
import okhttp3.OkHttpClient
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

actual fun buildHttpClient(extraConfig: HttpClientConfig<*>.() -> Unit): HttpClient {
    return HttpClient(OkHttp) {
        engine {
            preconfigured = getUnsafeOkHttpClient()
        }
        extraConfig()
    }
}

fun getUnsafeOkHttpClient(): OkHttpClient {
    return try {
        // Create a trust manager that does not validate certificate chains
        @SuppressLint("CustomX509TrustManager")
        val trustAllCerts: TrustManager = object : X509TrustManager {
            @SuppressLint("TrustAllX509TrustManager")
            override fun checkClientTrusted(chain: Array<X509Certificate?>?, authType: String?) {
            }

            @SuppressLint("TrustAllX509TrustManager")
            override fun checkServerTrusted(chain: Array<X509Certificate?>?, authType: String?) {
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        }

        // Install the all-trusting trust manager
        val sslContext: SSLContext = SSLContext.getInstance("SSL").apply {
            init(null, arrayOf(trustAllCerts), SecureRandom())
        }

        OkHttpClient.Builder().run {
            sslSocketFactory(sslContext.socketFactory, trustAllCerts as X509TrustManager)
            hostnameVerifier { _, _ -> true }
            followRedirects(true)
            build()
        }
    } catch (e: Exception) {
        throw RuntimeException(e)
    }
}