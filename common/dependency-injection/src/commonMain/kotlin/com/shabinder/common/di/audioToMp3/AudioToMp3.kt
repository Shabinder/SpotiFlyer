package com.shabinder.common.di.audioToMp3

import co.touchlab.kermit.Kermit
import com.shabinder.common.models.AudioQuality
import com.shabinder.common.models.SpotiFlyerException
import com.shabinder.common.models.event.coroutines.SuspendableEvent
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
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
    ): SuspendableEvent<String,Throwable> = SuspendableEvent {
        // Active Host ex - https://hostveryfast.onlineconverter.com/file/send
        // Convert Job Request ex - https://www.onlineconverter.com/convert/309a0f2bbaeb5687b04f96b6d65b47bfdd
        var (activeHost,jobLink) = convertRequest(URL, audioQuality).value

        // (jobStatus.contains("d")) == COMPLETION
        var jobStatus: String
        var retryCount = 40 // Set it to optimal level

        do {
            jobStatus = try {
                client.get(
                    "${activeHost.removeSuffix("send")}${jobLink.substringAfterLast("/")}"
                )
            } catch (e: Exception) {
                if(e is ClientRequestException && e.response.status.value == 404) {
                    // No Need to Retry, Host/Converter is Busy
                    throw SpotiFlyerException.MP3ConversionFailed()
                }
                // Try Using New Host/Converter
                convertRequest(URL, audioQuality).value.also {
                    activeHost = it.first
                    jobLink = it.second
                }
                e.printStackTrace()
                ""
            }
            retryCount--
            logger.i("Job Status") { jobStatus }
            if (!jobStatus.contains("d")) delay(600) // Add Delay , to give Server Time to process audio
        } while (!jobStatus.contains("d", true) && retryCount > 0)

        "${activeHost.removeSuffix("send")}${jobLink.substringAfterLast("/")}/download"
    }

    /*
    * Response Link Ex : `https://www.onlineconverter.com/convert/11affb6d88d31861fe5bcd33da7b10a26c`
    *  - to start the conversion
    * */
    private suspend fun convertRequest(
        URL: String,
        audioQuality: AudioQuality = AudioQuality.KBPS160,
    ): SuspendableEvent<Pair<String,String>,Throwable> = SuspendableEvent {
        val activeHost by getHost()
        val convertJob = client.submitFormWithBinaryData<String>(
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
            // logger.d { this }
            dropLast(3) // last 3 are useless unicode char
        }

        val job = client.get<HttpStatement>(convertJob) {
            headers {
                header("Host", "www.onlineconverter.com")
            }
        }.execute()
        logger.i("Schedule Conversion Job") { job.status.isSuccess().toString() }

        Pair(activeHost,convertJob)
    }

    // Active Host free to process conversion
    // ex - https://hostveryfast.onlineconverter.com/file/send
    private suspend fun getHost(): SuspendableEvent<String,Throwable> = SuspendableEvent {
        client.get<String>("https://www.onlineconverter.com/get/host") {
            headers {
                header("Host", "www.onlineconverter.com")
            }
        }//.also { logger.i("Active Host") { it } }
    }

    // Extract full Domain from URL
    // ex - hostveryfast.onlineconverter.com
    private fun String.getHostDomain(): String {
        return this.removePrefix("https://").substringBeforeLast(".") + "." + this.substringAfterLast(".").substringBefore("/")
    }
}
