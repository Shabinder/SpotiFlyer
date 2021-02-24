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
import android.app.*
import android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
import android.app.PendingIntent.FLAG_CANCEL_CURRENT
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.*
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import co.touchlab.kermit.Kermit
import com.github.kiulian.downloader.YoutubeDownloader
import com.shabinder.common.di.Dir
import com.shabinder.common.di.FetchPlatformQueryResult
import com.shabinder.common.di.getData
import com.shabinder.common.models.DownloadStatus
import com.shabinder.common.models.TrackDetails
import com.shabinder.common.ui.R.*
import com.tonyodev.fetch2.*
import com.tonyodev.fetch2core.DownloadBlock
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import java.io.File
import java.util.*
import kotlin.coroutines.CoroutineContext

class ForegroundService : Service(),CoroutineScope{
    private val tag: String = "Foreground Service"
    private val channelId = "ForegroundDownloaderService"
    private val notificationId = 101
    private var total = 0 //Total Downloads Requested
    private var converted = 0//Total Files Converted
    private var downloaded = 0//Total Files downloaded
    private var failed = 0//Total Files failed
    private val isFinished: Boolean
        get() = converted + failed == total
    private var isSingleDownload: Boolean = false

    private lateinit var serviceJob :Job
    override val coroutineContext: CoroutineContext
        get() = serviceJob + Dispatchers.IO

    private val requestMap = hashMapOf<Request, TrackDetails>()
    private val allTracksStatus = hashMapOf<String, DownloadStatus>()
    private var wakeLock: PowerManager.WakeLock? = null
    private var isServiceStarted = false
    private var messageList = mutableListOf("", "", "", "","")
    private lateinit var cancelIntent:PendingIntent
    private lateinit var downloadManager : DownloadManager

    private val fetcher: FetchPlatformQueryResult by inject()
    private val logger: Kermit by inject()
    private val fetch: Fetch by inject()
    private val dir: Dir by inject()
    private val ytDownloader:YoutubeDownloader
        get() = fetcher.youtubeProvider.ytDownloader
    
    
    override fun onBind(intent: Intent): IBinder? = null

    @SuppressLint("UnspecifiedImmutableFlag")
    override fun onCreate() {
        super.onCreate()
        serviceJob = SupervisorJob()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(channelId,"Downloader Service")
        }
        val intent = Intent(
            this,
            ForegroundService::class.java
        ).apply{action = "kill"}
        cancelIntent = PendingIntent.getService (this, 0 , intent , FLAG_CANCEL_CURRENT )
        downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        fetch.removeAllListeners().addListener(fetchListener)
    }

    @SuppressLint("WakelockTimeout")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Send a notification that service is started
        Log.i(tag,"Foreground Service Started.")
        startForeground(notificationId, getNotification())

        intent?.let{
            when (it.action) {
                "kill" -> killService()
                "query" -> {
                    val response = Intent().apply {
                        action = "query_result"
                        synchronized(allTracksStatus){
                            putExtra("tracks", allTracksStatus)
                        }
                    }
                    sendBroadcast(response)
                }
            }

            val downloadObjects: ArrayList<TrackDetails>? = (it.getParcelableArrayListExtra("object") ?: it.extras?.getParcelableArrayList(
                "object"
            ))

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
        //Wake locks and misc tasks from here :
        return if (isServiceStarted){
            //Service Already Started
            START_STICKY
        } else{
            isServiceStarted = true
            Log.i(tag,"Starting the foreground service task")
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
            launch {
                if (!it.videoID.isNullOrBlank()) {//Video ID already known!
                    downloadTrack(it.videoID!!, it)
                } else {
                    val searchQuery = "${it.title} - ${it.artists.joinToString(",")}"
                    val videoID = fetcher.youtubeMusic.getYTIDBestMatch(searchQuery,it)
                    logger.d("Service VideoID") { videoID ?: "Not Found" }
                    if (videoID.isNullOrBlank()) {
                        sendTrackBroadcast(Status.FAILED.name, it)
                        failed++
                        updateNotification()
                        allTracksStatus[it.title] = DownloadStatus.Failed
                    } else {//Found Youtube Video ID
                        downloadTrack(videoID, it)
                    }
                }
            }
        }
    }
    

    private fun downloadTrack(videoID:String, track: TrackDetails){
        launch {
            try {
                /*val audioData = ytDownloader.getVideo(videoID).getData()

                audioData?.let {
                    val url: String = it.url()
                    logger.d("DHelper Link Found") { url }
                }*/
                val url = fetcher.youtubeMp3.getMp3DownloadLink(videoID)
                if (url == null){
                    sendTrackBroadcast(Status.FAILED.name,track)
                    allTracksStatus[track.title] = DownloadStatus.Failed
                } else{
                    val request= Request(url, track.outputFilePath).apply{
                        priority = Priority.NORMAL
                        networkType = NetworkType.ALL
                    }
                    fetch.enqueue(request,
                        { request1 ->
                            requestMap[request1] = track
                            logger.d(tag){"Enqueuing Download"}
                        },
                        { error ->
                            logger.d(tag){"Enqueuing Error:${error.throwable.toString()}"}
                        }
                    )
                }
            }catch (e: java.lang.Exception){
                logger.d("Service YT Error"){e.message.toString()}
            }
        }
    }

    /**
     * Fetch Listener/ Responsible for Fetch Behaviour
     **/
    private var fetchListener: FetchListener = object : FetchListener {
        override fun onQueued(
            download: Download,
            waitingOnNetwork: Boolean
        ) {
            requestMap[download.request]?.let { sendTrackBroadcast(Status.QUEUED.name, it) }
        }

        override fun onRemoved(download: Download) {
            // TODO("Not yet implemented")
        }

        override fun onResumed(download: Download) {
            // TODO("Not yet implemented")
        }

        override fun onStarted(
            download: Download,
            downloadBlocks: List<DownloadBlock>,
            totalBlocks: Int
        ) {
            launch {
                val track  = requestMap[download.request]
                addToNotification("Downloading ${track?.title}")
                logger.d(tag){"${track?.title} Download Started"}
                track?.let{
                    allTracksStatus[it.title] = DownloadStatus.Downloading()
                    sendTrackBroadcast(Status.DOWNLOADING.name,track)
                }
            }
        }

        override fun onWaitingNetwork(download: Download) {
            // TODO("Not yet implemented")
        }

        override fun onAdded(download: Download) {
            // TODO("Not yet implemented")
        }

        override fun onCancelled(download: Download) {
            // TODO("Not yet implemented")
        }

        override fun onCompleted(download: Download) {
            val track = requestMap[download.request]
            try{
                track?.let {
                    val job = launch { dir.saveFileWithMetadata(byteArrayOf(),it) }
                    allTracksStatus[it.title] = DownloadStatus.Converting
                    sendTrackBroadcast("Converting",it)
                    addToNotification("Processing ${it.title}")
                    job.invokeOnCompletion { _ ->
                        converted++
                        allTracksStatus[it.title] = DownloadStatus.Downloaded
                        sendTrackBroadcast(Status.COMPLETED.name,it)
                        removeFromNotification("Processing ${it.title}")
                    }
                }
                logger.d(tag){"${track?.title} Download Completed"}
            }catch (
                e: KotlinNullPointerException
            ){
                logger.d(tag){"${track?.title} Download Failed! Error:Fetch!!!!"}
                logger.d(tag){"${track?.title} Requesting Download thru Android DM"}
                downloadUsingDM(download.request.url, download.request.file, track!!)
            }
            downloaded++
            requestMap.remove(download.request)
            removeFromNotification("Downloading ${track?.title}")
        }

        override fun onDeleted(download: Download) {
            // TODO("Not yet implemented")
        }

        override fun onDownloadBlockUpdated(
            download: Download,
            downloadBlock: DownloadBlock,
            totalBlocks: Int
        ) {
            // TODO("Not yet implemented")
        }

        override fun onError(download: Download, error: Error, throwable: Throwable?) {
            launch {
                val track = requestMap[download.request]
                downloaded++
                logger.d(tag){download.error.throwable.toString()}
                logger.d(tag){"${track?.title} Requesting Download thru Android DM"}
                downloadUsingDM(download.request.url, download.request.file, track!!)
                requestMap.remove(download.request)
                removeFromNotification("Downloading ${track.title}")
            }
            updateNotification()
        }

        override fun onPaused(download: Download) {
            // TODO("Not yet implemented")
        }

        override fun onProgress(
            download: Download,
            etaInMilliSeconds: Long,
            downloadedBytesPerSecond: Long
        ) {
            launch {
                requestMap[download.request]?.run {
                    allTracksStatus[title] = DownloadStatus.Downloading(download.progress)
                    logger.d(tag){"${title} ETA: ${etaInMilliSeconds / 1000} sec"}


                    val intent = Intent().apply {
                        action = "Progress"
                        putExtra("progress", download.progress)
                        putExtra("track", this@run)
                    }
                    sendBroadcast(intent)
                }
            }
        }
    }

    /**
    * If fetch Fails , Android Download Manager To RESCUE!!
    **/
    fun downloadUsingDM(url: String, outputDir: String, track: TrackDetails){
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

            //Start Download
            val downloadID = downloadManager.enqueue(request)
            logger.d("DownloadManager"){"Download Request Sent"}

            val onDownloadComplete: BroadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    //Fetching the download id received with the broadcast
                    val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    //Checking if the received broadcast is for our enqueued download by matching download id
                    if (downloadID == id) {
                        allTracksStatus[track.title] = DownloadStatus.Converting
                        launch { dir.saveFileWithMetadata(byteArrayOf(),track);converted++ }
                        //Unregister this broadcast Receiver
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
        logger.d(tag){"Releasing Wake Lock"}
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }
        } catch (e: Exception) {
            logger.d(tag){"Service stopped without being started: ${e.message}"}
        }
        isServiceStarted = false
    }

    @Suppress("SameParameterValue")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String){
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
        logger.d(tag){"Starting Cleaning in ${dir.path} "}
        val fList = dir.listFiles()
        fList?.let {
            for (file in fList) {
                if (file.isDirectory) {
                    cleanFiles(file)
                } else if(file.isFile) {
                    if(file.path.toString().substringAfterLast(".") != "mp3"){
                        logger.d(tag){ "Cleaning ${file.path}"}
                        file.delete()
                    }
                }
            }
        }
    }

    private fun killService() {
        launch{
            logger.d(tag){"Killing Self"}
            messageList = mutableListOf("Cleaning And Exiting","","","","")
            fetch.cancelAll()
            fetch.removeAll()
            updateNotification()
            cleanFiles(File(dir.defaultDir()))
            //TODO cleanFiles(File(dir.imageCacheDir()))
            messageList = mutableListOf("","","","","")
            releaseWakeLock()
            serviceJob.cancel()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                stopForeground(true)
            } else {
                stopSelf()//System will automatically close it
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if(isFinished){
            killService()
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        if(isFinished){
            killService()
        }
    }

    private fun getNotification():Notification = NotificationCompat.Builder(this, channelId).run {
        setSmallIcon(drawable.ic_download_arrow)
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
        addAction(drawable.ic_round_cancel_24,"Exit",cancelIntent)
        build()
    }

    private fun addToNotification(message:String){
        messageList.add(message)
        updateNotification()
    }

    private fun removeFromNotification(message: String){
        messageList.remove(message)
        updateNotification()
    }

    fun sendTrackBroadcast(action:String,track:TrackDetails){
        val intent = Intent().apply{
            setAction(action)
            putExtra("track", track)
        }
        this@ForegroundService.sendBroadcast(intent)
    }
}

private fun Fetch.removeAllListeners():Fetch{
    for (listener in this.getListenerSet()) {
        this.removeListener(listener)
    }
    return this
}