package com.shabinder.common.di.audioToMp3

import co.touchlab.kermit.Kermit
import com.shabinder.common.models.AudioQuality
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpStatement
import io.ktor.http.isSuccess
import kotlinx.coroutines.delay

interface AudioToMp3 {

    val client: HttpClient
    val logger: Kermit

    companion object {
        operator fun invoke(
            client: HttpClient,
            logger: Kermit
        ): AudioToMp3 {
            return object : AudioToMp3 {
                override val client: HttpClient = client
                override val logger: Kermit = logger
            }
        }
    }

    suspend fun convertToMp3(
        URL: String,
        audioQuality: AudioQuality = AudioQuality.getQuality(URL.substringBeforeLast(".").takeLast(3)),
    ): String? {
        val activeHost = getHost() // ex - https://hostveryfast.onlineconverter.com/file/send
        val jobLink = convertRequest(URL, activeHost, audioQuality) // ex - https://www.onlineconverter.com/convert/309a0f2bbaeb5687b04f96b6d65b47bfdd

        // (jobStatus.contains("d")) == COMPLETION
        var jobStatus: String
        var retryCount = 40 // Set it to optimal level

        do {
            jobStatus = try {
                client.get(
                    "${activeHost.removeSuffix("send")}${jobLink.substringAfterLast("/")}"
                )
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            }
            retryCount--
            logger.i("Job Status") { jobStatus }
            if (!jobStatus.contains("d")) delay(400) // Add Delay , to give Server Time to process audio
        } while (!jobStatus.contains("d", true) && retryCount != 0)

        return if (jobStatus.equals("d", true)) {
            // Return MP3 Download Link
            "${activeHost.removeSuffix("send")}${jobLink.substringAfterLast("/")}/download"
        } else null
    }

    /*
    * Response Link Ex : `https://www.onlineconverter.com/convert/11affb6d88d31861fe5bcd33da7b10a26c`
    *  - to start the conversion
    * */
    private suspend fun convertRequest(
        URL: String,
        host: String? = null,
        audioQuality: AudioQuality = AudioQuality.KBPS160,
    ): String {
        val activeHost = host ?: getHost()
        val res = client.submitFormWithBinaryData<String>(
            url = activeHost,
            formData = formData {
                append("class", "audio")
                append("from", "audio")
                append("to", "mp3")
                append("source", "url")
                append("url", URL.replace("https:", "http:"))
                append("audio_quality", audioQuality.kbps)
            }
        ) {
            headers {
                header("Host", activeHost.getHostDomain().also { logger.i("AudioToMp3 Host") { it } })
                header("Origin", "https://www.onlineconverter.com")
                header("Referer", "https://www.onlineconverter.com/")
            }
        }.run {
            logger.d { this }
            dropLast(3) // last 3 are useless unicode char
        }

        val job = client.get<HttpStatement>(res) {
            headers {
                header("Host", "www.onlineconverter.com")
            }
        }.execute()
        logger.i("Schedule Conversion Job") { job.status.isSuccess().toString() }
        return res
    }

    // Active Host free to process conversion
    // ex - https://hostveryfast.onlineconverter.com/file/send
    private suspend fun getHost(): String {
        return client.get<String>("https://www.onlineconverter.com/get/host") {
            headers {
                header("Host", "www.onlineconverter.com")
            }
        }.also { logger.i("Active Host") { it } }
    }
    // Extract full Domain from URL
    // ex - hostveryfast.onlineconverter.com
    private fun String.getHostDomain(): String {
        return this.removePrefix("https://").substringBeforeLast(".") + "." + this.substringAfterLast(".").substringBefore("/")
    }
}
