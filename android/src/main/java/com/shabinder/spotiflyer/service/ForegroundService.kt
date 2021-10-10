/*
 * Copyright (c)  2021  Shabinder Singh
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.shabinder.spotiflyer.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import co.touchlab.kermit.Kermit
import com.shabinder.common.core_components.file_manager.FileManager
import com.shabinder.common.core_components.file_manager.downloadFile
import com.shabinder.common.core_components.parallel_executor.ParallelExecutor
import com.shabinder.common.models.DownloadResult
import com.shabinder.common.models.DownloadStatus
import com.shabinder.common.models.TrackDetails
import com.shabinder.common.models.event.coroutines.SuspendableEvent
import com.shabinder.common.models.event.coroutines.failure
import com.shabinder.common.providers.FetchPlatformQueryResult
import com.shabinder.common.translations.Strings
import com.shabinder.spotiflyer.R
import io.ktor.client.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.io.File

class ForegroundService : LifecycleService() {

    private lateinit var downloadService: ParallelExecutor
    val trackStatusFlowMap = TrackStatusFlowMap(
        MutableSharedFlow(replay = 1),
        lifecycleScope
    )

    private val fetcher: FetchPlatformQueryResult by inject()
    private val logger: Kermit by inject()
    private val dir: FileManager by inject()
    private val httpClient: HttpClient by inject()

    private var messageList =
        java.util.Collections.synchronizedList(MutableList(5) { emptyMessage })
    private var wakeLock: PowerManager.WakeLock? = null
    private var isServiceStarted = false
    private val cancelIntent: PendingIntent by lazy {
        val intent = Intent(this, ForegroundService::class.java).apply { action = "kill" }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        PendingIntent.getService(this, 0, intent, flags)
    }

    /* Variables Holding Download State */
    private var total = 0
    private var converted = 0
    private var downloaded = 0
    private var failed = 0
    private val isFinished get() = converted + failed == total
    private var isSingleDownload = false

    inner class DownloadServiceBinder : Binder() {
        val service get() = this@ForegroundService
    }

    private val myBinder: IBinder = DownloadServiceBinder()

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return myBinder
    }

    override fun onCreate() {
        super.onCreate()
        downloadService = ParallelExecutor(Dispatchers.IO)
        trackStatusFlowMap.scope = lifecycleScope
        createNotificationChannel(CHANNEL_ID, "Downloader Service")
    }

    @SuppressLint("WakelockTimeout")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        downloadService.reviveIfClosed()
        // Send a notification that service is started
        Log.i(TAG, "Foreground Service Started.")
        startForeground(NOTIFICATION_ID, createNotification())

        intent?.let {
            when (it.action) {
                "kill" -> killService()
            }
        }

        // Wake locks and misc tasks from here :
        return if (isServiceStarted) {
            // Service Already Started
            START_STICKY
        } else {
            isServiceStarted = true
            Log.i(TAG, "Starting the foreground service task")
            wakeLock =
                (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                    newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "EndlessService::lock").apply {
                        acquire()
                    }
                }
            START_STICKY
        }
    }

    /**
     * Function To Download All Tracks Available in a List
     **/
    fun downloadAllTracks(trackList: List<TrackDetails>) {
        downloadService.reviveIfClosed()
        trackList.size.also { size ->
            total += size
            isSingleDownload = (size == 1)
            updateNotification()
        }

        for (track in trackList) {
            trackStatusFlowMap[track.title] = DownloadStatus.Queued
            lifecycleScope.launch {
                downloadService.executeSuspending {
                    fetcher.findBestDownloadLink(track).fold(
                        success = { res ->
                            enqueueDownload(res.first, track.apply { audioQuality = res.second })
                        },
                        failure = { error ->
                            failed++
                            updateNotification()
                            trackStatusFlowMap[track.title] = DownloadStatus.Failed(error)
                        }
                    )
                }
            }
        }
    }

    private suspend fun enqueueDownload(url: String, track: TrackDetails) {
        // Initiating Download
        addToNotification(Message(track.title, DownloadStatus.Downloading()))
        trackStatusFlowMap[track.title] = DownloadStatus.Downloading()

        // Enqueueing Download
        httpClient.downloadFile(url).collect {
            when (it) {
                is DownloadResult.Error -> {
                    logger.d(TAG) { it.message }
                    failed++
                    trackStatusFlowMap[track.title] =
                        DownloadStatus.Failed(it.cause ?: Exception(it.message))
                    removeFromNotification(Message(track.title, DownloadStatus.Downloading()))
                }

                is DownloadResult.Progress -> {
                    trackStatusFlowMap[track.title] = DownloadStatus.Downloading(it.progress)
                    // updateProgressInNotification(Message(track.title,DownloadStatus.Downloading(it.progress)))
                }

                is DownloadResult.Success -> {
                    coroutineScope {
                        SuspendableEvent {
                            // Save File and Embed Metadata
                            val job = launch(Dispatchers.Default) {
                                dir.saveFileWithMetadata(
                                    it.byteArray,
                                    track
                                ).fold(
                                    failure = { throwable ->
                                        throwable.printStackTrace()
                                        throw throwable
                                    }, success = {}
                                )
                            }

                            // Send Converting Status
                            trackStatusFlowMap[track.title] = DownloadStatus.Converting
                            addToNotification(Message(track.title, DownloadStatus.Converting))

                            // All Processing Completed for this Track
                            job.invokeOnCompletion { throwable ->
                                if (throwable != null /*&& throwable !is CancellationException*/) {
                                    // handle error
                                    failed++
                                    trackStatusFlowMap[track.title] =
                                        DownloadStatus.Failed(throwable)
                                    removeFromNotification(
                                        Message(
                                            track.title,
                                            DownloadStatus.Converting
                                        )
                                    )
                                    return@invokeOnCompletion
                                }
                                converted++
                                trackStatusFlowMap[track.title] = DownloadStatus.Downloaded
                                removeFromNotification(
                                    Message(
                                        track.title,
                                        DownloadStatus.Converting
                                    )
                                )
                            }
                            logger.d(TAG) { "${track.title} Download Completed" }
                            downloaded++
                        }.failure { error ->
                            error.printStackTrace()
                            // Download Failed
                            failed++
                            trackStatusFlowMap[track.title] = DownloadStatus.Failed(error)
                        }
                        removeFromNotification(Message(track.title, DownloadStatus.Downloading()))
                    }
                }
            }
        }
    }

    private fun releaseWakeLock() {
        logger.d(TAG) { "Releasing Wake Lock" }
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }
        } catch (e: Exception) {
            logger.d(TAG) { "Service stopped without being started: ${e.message}" }
        }
        isServiceStarted = false
    }

    @Suppress("SameParameterValue")
    private fun createNotificationChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName, NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            service.createNotificationChannel(channel)
        }
    }

    /*
    * Time To Wrap UP
    *  - `Clean Up` and `Stop this Foreground Service`
    * */
    private fun killService() {
        lifecycleScope.launch {
            logger.d(TAG) { "Killing Self" }
            resetVar()
            messageList = messageList.getEmpty().apply {
                set(index = 0, Message(Strings.cleaningAndExiting(), DownloadStatus.NotDownloaded))
            }
            downloadService.close()
            updateNotification()
            trackStatusFlowMap.apply {
                clear()
                scope = null
            }
            cleanFiles(File(dir.defaultDir()))
            // cleanFiles(File(dir.imageCacheDir()))
            messageList = messageList.getEmpty()
            releaseWakeLock()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                stopForeground(true)
                stopSelf()
            } else {
                stopSelf()
            }
        }
    }

    private fun resetVar() {
        total = 0
        downloaded = 0
        failed = 0
        converted = 0
    }

    private fun createNotification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID).run {
            setSmallIcon(R.drawable.ic_download_arrow)
            setContentTitle("${Strings.total()}: $total  ${Strings.completed()}:$converted  ${Strings.failed()}:$failed")
            setSilent(true)
            setProgress(total, failed + converted, false)
            setStyle(
                NotificationCompat.InboxStyle().run {
                    addLine(messageList[messageList.size - 1].asString())
                    addLine(messageList[messageList.size - 2].asString())
                    addLine(messageList[messageList.size - 3].asString())
                    addLine(messageList[messageList.size - 4].asString())
                    addLine(messageList[messageList.size - 5].asString())
                }
            )
            addAction(R.drawable.ic_round_cancel_24, Strings.exit(), cancelIntent)
            build()
        }

    private fun addToNotification(message: Message) {
        synchronized(messageList) {
            messageList.add(message)
        }
        updateNotification()
    }

    private fun removeFromNotification(message: Message) {
        synchronized(messageList) {
            messageList.removeAll { it.title == message.title }
        }
        updateNotification()
    }

    @Suppress("unused")
    private fun updateProgressInNotification(message: Message) {
        synchronized(messageList) {
            val index = messageList.indexOfFirst { it.title == message.title }
            messageList[index] = message
        }
        updateNotification()
    }

    // Update Notification only if Service is Still Active
    private fun updateNotification() {
        if (!downloadService.isClosed.value) {
            val mNotificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mNotificationManager.notify(NOTIFICATION_ID, createNotification())
        } else {
            // Service is Inactive so clear residual status
            resetVar()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinished) {
            killService()
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        if (isFinished) {
            killService()
        }
    }

    companion object {
        private const val TAG: String = "Foreground Service"
        private const val CHANNEL_ID = "ForegroundDownloaderService"
        private const val NOTIFICATION_ID = 101
    }
}
