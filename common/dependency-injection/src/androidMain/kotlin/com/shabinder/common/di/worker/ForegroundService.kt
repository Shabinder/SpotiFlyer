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

package com.shabinder.common.di.worker

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_CANCEL_CURRENT
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import co.touchlab.kermit.Kermit
import com.shabinder.common.di.*
import com.shabinder.common.di.utils.ParallelExecutor
import com.shabinder.common.models.DownloadResult
import com.shabinder.common.models.DownloadStatus
import com.shabinder.common.models.TrackDetails
import com.shabinder.downloader.models.formats.Format
import com.shabinder.common.models.Status
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.io.File
import kotlin.coroutines.CoroutineContext

class ForegroundService : Service(), CoroutineScope {

    private val tag: String = "Foreground Service"
    private val channelId = "ForegroundDownloaderService"
    private val notificationId = 101
    private var total = 0 // Total Downloads Requested
    private var converted = 0 // Total Files Converted
    private var downloaded = 0 // Total Files downloaded
    private var failed = 0 // Total Files failed
    private val isFinished get() = converted + failed == total
    private var isSingleDownload = false

    private lateinit var serviceJob: Job
    override val coroutineContext: CoroutineContext
        get() = serviceJob + Dispatchers.IO

    private val allTracksStatus = hashMapOf<String, DownloadStatus>()
    private var messageList = mutableListOf("", "", "", "", "")
    private var wakeLock: PowerManager.WakeLock? = null
    private var isServiceStarted = false
    private lateinit var cancelIntent: PendingIntent

    private lateinit var downloadManager: DownloadManager
    private lateinit var downloadService: ParallelExecutor
    private val ytDownloader get() = fetcher.youtubeProvider.ytDownloader
    private val fetcher: FetchPlatformQueryResult by inject()
    private val logger: Kermit by inject()
    private val dir: Dir by inject()

    override fun onBind(intent: Intent): IBinder? = null

    @SuppressLint("UnspecifiedImmutableFlag")
    override fun onCreate() {
        super.onCreate()
        serviceJob = SupervisorJob()
        downloadService = ParallelExecutor(Dispatchers.IO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(channelId, "Downloader Service")
        }
        val intent = Intent(
            this,
            ForegroundService::class.java
        ).apply { action = "kill" }
        cancelIntent = PendingIntent.getService(this, 0, intent, FLAG_CANCEL_CURRENT)
        downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    }

    @SuppressLint("WakelockTimeout")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Send a notification that service is started
        Log.i(tag, "Foreground Service Started.")
        startForeground(notificationId, getNotification())

        intent?.let {
            when (it.action) {
                "kill" -> killService()
                "query" -> {
                    val response = Intent().apply {
                        action = "query_result"
                        synchronized(allTracksStatus) {
                            putExtra("tracks", allTracksStatus)
                        }
                    }
                    sendBroadcast(response)
                }
            }

            val downloadObjects: ArrayList<TrackDetails>? = (
                it.getParcelableArrayListExtra("object") ?: it.extras?.getParcelableArrayList(
                    "object"
                )
                )

            downloadObjects?.let { list ->
                downloadObjects.size.let { size ->
                    total += size
                    isSingleDownload = (size == 1)
                }
                list.forEach { track ->
                    allTracksStatus[track.title] = DownloadStatus.Queued
                }
                updateNotification()
                downloadAllTracks(list)
            }
        }
        // Wake locks and misc tasks from here :
        return if (isServiceStarted) {
            // Service Already Started
            START_STICKY
        } else {
            isServiceStarted = true
            Log.i(tag, "Starting the foreground service task")
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
    private fun downloadAllTracks(trackList: List<TrackDetails>) {
        trackList.forEach {
            launch(Dispatchers.IO) {
                if (!it.videoID.isNullOrBlank()) { // Video ID already known!
                    downloadTrack(it.videoID!!, it)
                } else {
                    val searchQuery = "${it.title} - ${it.artists.joinToString(",")}"
                    val videoID = fetcher.youtubeMusic.getYTIDBestMatch(searchQuery, it)
                    logger.d("Service VideoID") { videoID ?: "Not Found" }
                    if (videoID.isNullOrBlank()) {
                        sendTrackBroadcast(Status.FAILED.name, it)
                        failed++
                        updateNotification()
                        allTracksStatus[it.title] = DownloadStatus.Failed
                    } else { // Found Youtube Video ID
                        downloadTrack(videoID, it)
                    }
                }
            }
        }
    }

    private suspend fun downloadTrack(videoID: String, track: TrackDetails) {
        try {
            val url = fetcher.youtubeMp3.getMp3DownloadLink(videoID)
            if (url == null) {
                val audioData: Format = ytDownloader.getVideo(videoID).getData() ?: throw Exception("Java YT Dependency Error")
                val ytUrl = audioData.url!! //We Will catch NPE
                enqueueDownload(ytUrl, track)
            } else enqueueDownload(url, track)
        } catch (e: Exception) {
            logger.d("Service YT Error") { e.message.toString() }
            sendTrackBroadcast(Status.FAILED.name, track)
            allTracksStatus[track.title] = DownloadStatus.Failed
        }
    }

    private fun enqueueDownload(url: String, track: TrackDetails) {
        // Initiating Download
        addToNotification("Downloading ${track.title}")
        logger.d(tag) { "${track.title} Download Started" }
        allTracksStatus[track.title] = DownloadStatus.Downloading()
        sendTrackBroadcast(Status.DOWNLOADING.name, track)

        // Enqueueing Download
        launch {
            downloadService.execute {
                downloadFile(url).collect {
                    when (it) {
                        is DownloadResult.Error -> {
                            launch {
                                logger.d(tag) { it.message }
                                logger.d(tag) { "${track.title} Requesting Download thru Android DM" }
                                downloadUsingDM(url, track.outputFilePath, track)
                                removeFromNotification("Downloading ${track.title}")
                                downloaded++
                            }
                            updateNotification()
                            sendTrackBroadcast(Status.FAILED.name,track)
                        }

                        is DownloadResult.Progress -> {
                            allTracksStatus[track.title] = DownloadStatus.Downloading(it.progress)
                            logger.d(tag) { "${track.title} Progress: ${it.progress} %" }

                            val intent = Intent().apply {
                                action = "Progress"
                                putExtra("progress", it.progress)
                                putExtra("track", track)
                            }
                            sendBroadcast(intent)
                        }

                        is DownloadResult.Success -> {
                            try {
                                // Save File and Embed Metadata
                                val job = launch(Dispatchers.Default) { dir.saveFileWithMetadata(it.byteArray, track) }
                                allTracksStatus[track.title] = DownloadStatus.Converting
                                sendTrackBroadcast("Converting", track)
                                addToNotification("Processing ${track.title}")
                                job.invokeOnCompletion {
                                    converted++
                                    allTracksStatus[track.title] = DownloadStatus.Downloaded
                                    sendTrackBroadcast(Status.COMPLETED.name, track)
                                    removeFromNotification("Processing ${track.title}")
                                }
                                logger.d(tag) { "${track.title} Download Completed" }
                            } catch (
                                e: KotlinNullPointerException
                            ) {
                                // Try downloading using android DM
                                logger.d(tag) { "${track.title} Download Failed! Error:Fetch!!!!" }
                                logger.d(tag) { "${track.title} Requesting Download thru Android DM" }
                                downloadUsingDM(url, track.outputFilePath, track)
                            }
                            downloaded++
                            removeFromNotification("Downloading ${track.title}")
                        }
                    }
                }
            }
        }
    }

    /**
     * If fetch Fails , Android Download Manager To RESCUE!!
     **/
    private fun downloadUsingDM(url: String, outputDir: String, track: TrackDetails) {
        launch {
            val uri = Uri.parse(url)
            val request = DownloadManager.Request(uri).apply {
                setAllowedNetworkTypes(
                    DownloadManager.Request.NETWORK_WIFI or
                        DownloadManager.Request.NETWORK_MOBILE
                )
                setAllowedOverRoaming(false)
                setTitle(track.title)
                setDescription("Spotify Downloader Working Up here...")
                setDestinationUri(File(outputDir).toUri())
                setNotificationVisibility(VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            }

            // Start Download
            val downloadID = downloadManager.enqueue(request)
            logger.d("DownloadManager") { "Download Request Sent" }

            val onDownloadComplete: BroadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    // Fetching the download id received with the broadcast
                    val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    // Checking if the received broadcast is for our enqueued download by matching download id
                    if (downloadID == id) {
                        allTracksStatus[track.title] = DownloadStatus.Converting
                        launch { dir.saveFileWithMetadata(byteArrayOf(), track); converted++ }
                        // Unregister this broadcast Receiver
                        this@ForegroundService.unregisterReceiver(this)
                    }
                }
            }
            registerReceiver(onDownloadComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        }
    }

    /**
     * This is the method that can be called to update the Notification
     */
    private fun updateNotification() {
        val mNotificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.notify(notificationId, getNotification())
    }

    private fun releaseWakeLock() {
        logger.d(tag) { "Releasing Wake Lock" }
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }
        } catch (e: Exception) {
            logger.d(tag) { "Service stopped without being started: ${e.message}" }
        }
        isServiceStarted = false
    }

    @Suppress("SameParameterValue")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String) {
        val channel = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(channel)
    }

    /**
     * Cleaning All Residual Files except Mp3 Files
     **/
    private fun cleanFiles(dir: File) {
        logger.d(tag) { "Starting Cleaning in ${dir.path} " }
        val fList = dir.listFiles()
        fList?.let {
            for (file in fList) {
                if (file.isDirectory) {
                    cleanFiles(file)
                } else if (file.isFile) {
                    if (file.path.toString().substringAfterLast(".") != "mp3") {
                        logger.d(tag) { "Cleaning ${file.path}" }
                        file.delete()
                    }
                }
            }
        }
    }

    private fun killService() {
        launch {
            logger.d(tag) { "Killing Self" }
            messageList = mutableListOf("Cleaning And Exiting", "", "", "", "")
            downloadService.close()
            updateNotification()
            cleanFiles(File(dir.defaultDir()))
            // TODO cleanFiles(File(dir.imageCacheDir()))
            messageList = mutableListOf("", "", "", "", "")
            releaseWakeLock()
            serviceJob.cancel()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                stopForeground(true)
                stopSelf()
            } else {
                stopSelf() // System will automatically close it
            }
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

    private fun getNotification(): Notification = NotificationCompat.Builder(this, channelId).run {
        setSmallIcon(R.drawable.ic_download_arrow)
        setContentTitle("Total: $total  Completed:$converted  Failed:$failed")
        setSilent(true)
        setStyle(
            NotificationCompat.InboxStyle().run {
                addLine(messageList[messageList.size - 1])
                addLine(messageList[messageList.size - 2])
                addLine(messageList[messageList.size - 3])
                addLine(messageList[messageList.size - 4])
                addLine(messageList[messageList.size - 5])
            }
        )
        addAction(R.drawable.ic_round_cancel_24, "Exit", cancelIntent)
        build()
    }

    private fun addToNotification(message: String) {
        messageList.add(message)
        updateNotification()
    }

    private fun removeFromNotification(message: String) {
        messageList.remove(message)
        updateNotification()
    }

    private fun sendTrackBroadcast(action: String, track: TrackDetails) {
        val intent = Intent().apply {
            setAction(action)
            putExtra("track", track)
        }
        this@ForegroundService.sendBroadcast(intent)
    }
}
