/*
 * Copyright (C)  2020  Shabinder Singh
 *
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.shabinder.spotiflyer.worker

import android.annotation.SuppressLint
import android.app.*
import android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
import android.app.PendingIntent.FLAG_CANCEL_CURRENT
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.*
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import coil.Coil
import coil.request.ImageRequest
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS
import com.arthenica.mobileffmpeg.FFmpeg
import com.github.kiulian.downloader.YoutubeDownloader
import com.mpatric.mp3agic.Mp3File
import com.shabinder.spotiflyer.R
import com.shabinder.spotiflyer.downloadHelper.getYTTracks
import com.shabinder.spotiflyer.downloadHelper.sortByBestMatch
import com.shabinder.spotiflyer.models.DownloadStatus
import com.shabinder.spotiflyer.models.TrackDetails
import com.shabinder.spotiflyer.models.spotify.Source
import com.shabinder.spotiflyer.networking.YoutubeMusicApi
import com.shabinder.spotiflyer.networking.makeJsonBody
import com.shabinder.spotiflyer.utils.*
import com.shabinder.spotiflyer.utils.Provider.defaultDir
import com.shabinder.spotiflyer.utils.Provider.imageDir
import com.tonyodev.fetch2.*
import com.tonyodev.fetch2core.DownloadBlock
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class ForegroundService : Service(){
    private val tag = "Foreground Service"
    private val channelId = "ForegroundDownloaderService"
    private val notificationId = 101
    private var total = 0 //Total Downloads Requested
    private var converted = 0//Total Files Converted
    private var downloaded = 0//Total Files downloaded
    private var failed = 0//Total Files failed
    private val isFinished: Boolean
        get() = converted + failed == total
    private var isSingleDownload: Boolean = false
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private val requestMap = hashMapOf<Request, TrackDetails>()
    private val allTracksStatus = hashMapOf<String,DownloadStatus>()
    private var wakeLock: PowerManager.WakeLock? = null
    private var isServiceStarted = false
    private var messageList = mutableListOf("", "", "", "","")
    private val imageDir:String
        get() = imageDir(this)
    private lateinit var cancelIntent:PendingIntent
    private lateinit var fetch:Fetch
    private lateinit var downloadManager : DownloadManager
    @Inject lateinit var ytDownloader: YoutubeDownloader
    @Inject lateinit var youtubeMusicApi: YoutubeMusicApi

    override fun onBind(intent: Intent): IBinder? = null

    @SuppressLint("UnspecifiedImmutableFlag")
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(channelId,"Downloader Service")
        }
        val intent = Intent(
            this,
            ForegroundService::class.java
        ).apply{action = "kill"}
        cancelIntent = PendingIntent.getService (this, 0 , intent , FLAG_CANCEL_CURRENT )
        downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        initialiseFetch()
    }

    @SuppressLint("WakelockTimeout")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Send a notification that service is started
        log(tag, "Service Started.")
        startForeground(notificationId, getNotification())
        intent?.let{
            when (it.action) {
                "kill" -> killService()
                "query" -> {
                    val response = Intent().apply {
                        action = "query_result"
                        putExtra("tracks", allTracksStatus)
                    }
                    sendBroadcast(response)
                }
            }

            val downloadObjects: ArrayList<TrackDetails>? = (it.getParcelableArrayListExtra("object") ?: it.extras?.getParcelableArrayList(
                "object"
            ))
            val imagesList: ArrayList<String>? = (it.getStringArrayListExtra("imagesList") ?: it.extras?.getStringArrayList(
                "imagesList"
            ))

            imagesList?.let{ imageList ->
                serviceScope.launch {
                    downloadAllImages(imageList)
                }
            }

            downloadObjects?.let { list ->
                downloadObjects.size.let { size ->
                    total += size
                    isSingleDownload = (size == 1)
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
            log(tag, "Starting the foreground service task")
            isServiceStarted = true
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
            serviceScope.launch {
                if (it.downloaded == DownloadStatus.Downloaded) {//Download Already Present!!
                } else {
                    allTracksStatus[it.title] = DownloadStatus.Queued
                    if (!it.videoID.isNullOrBlank()) {//Video ID already known!
                        downloadTrack(it.videoID!!, it)
                    } else {
                        val searchQuery = "${it.title} - ${it.artists.joinToString(",")}"
                        val jsonBody = makeJsonBody(searchQuery.trim()).toJsonString()
                        youtubeMusicApi.getYoutubeMusicResponse(jsonBody).enqueue(
                            object : Callback<String> {
                                override fun onResponse(
                                    call: Call<String>,
                                    response: Response<String>
                                ) {
                                    serviceScope.launch {
                                        val videoId = sortByBestMatch(
                                            getYTTracks(response.body().toString()),
                                            trackName = it.title,
                                            trackArtists = it.artists,
                                            trackDurationSec = it.durationSec
                                        ).keys.firstOrNull()
                                        log("Service VideoID", videoId ?: "Not Found")
                                        if (videoId.isNullOrBlank()) {
                                            sendTrackBroadcast(Status.FAILED.name, it)
                                            failed++
                                            updateNotification()
                                            allTracksStatus[it.title] = DownloadStatus.Failed
                                        } else {//Found Youtube Video ID
                                            downloadTrack(videoId, it)
                                        }
                                    }
                                }

                                override fun onFailure(call: Call<String>, t: Throwable) {
                                    if (t.message.toString()
                                            .contains("Failed to connect")
                                    ) showDialog("Failed, Check Your Internet Connection!")
                                    log("YT API Req. Fail", t.message.toString())
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    fun downloadTrack(videoID:String,track: TrackDetails){
        serviceScope.launch(Dispatchers.IO) {
            try {
                val audioData = ytDownloader.getVideo(videoID).getData()

                audioData?.let {
                    val url: String = it.url()
                    log("DHelper Link Found", url)
                    val request= Request(url, track.outputFile).apply{
                        priority = Priority.NORMAL
                        networkType = NetworkType.ALL
                    }
                    fetch.enqueue(request,
                        { request1 ->
                            requestMap[request1] = track
                            log(tag, "Enqueuing Download")
                        },
                        { error ->
                            log(tag, "Enqueuing Error:${error.throwable.toString()}")
                        }
                    )
                }
            }catch (e: java.lang.Exception){
                log("Service YT Error", e.message.toString())
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
            serviceScope.launch {
                val track  = requestMap[download.request]
                addToNotification("Downloading ${track?.title}")
                log(tag, "${track?.title} Download Started")
                track?.let{
                    allTracksStatus[it.title] = DownloadStatus.Downloading
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
            serviceScope.launch {
                val track = requestMap[download.request]
                removeFromNotification("Downloading ${track?.title}")
                try{
                    track?.let {
                        convertToMp3(download.file, it)
                        allTracksStatus[it.title] = DownloadStatus.Converting
                    }
                    log(tag, "${track?.title} Download Completed")
                }catch (
                    e: KotlinNullPointerException
                ){
                    log(tag, "${track?.title} Download Failed! Error:Fetch!!!!")
                    log(tag, "${track?.title} Requesting Download thru Android DM")
                    downloadUsingDM(download.request.url, download.request.file, track!!)
                    downloaded++
                    requestMap.remove(download.request)
                }
            }
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
            serviceScope.launch {
                val track = requestMap[download.request]
                downloaded++
                log(tag, download.error.throwable.toString())
                log(tag, "${track?.title} Requesting Download thru Android DM")
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
            serviceScope.launch {
                val track  = requestMap[download.request]
                log(tag, "${track?.title} ETA: ${etaInMilliSeconds / 1000} sec")
                val intent = Intent().apply {
                    action = "Progress"
                    putExtra("progress", download.progress)
                    putExtra("track", requestMap[download.request])
                }
                sendBroadcast(intent)
            }
        }
    }

    /**
    * If fetch Fails , Android Download Manager To RESCUE!!
    **/
    fun downloadUsingDM(url: String, outputDir: String, track: TrackDetails){
        serviceScope.launch {
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
            log("DownloadManager", "Download Request Sent")

            val onDownloadComplete: BroadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    //Fetching the download id received with the broadcast
                    val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    //Checking if the received broadcast is for our enqueued download by matching download id
                    if (downloadID == id) {
                        allTracksStatus[track.title] = DownloadStatus.Converting
                        convertToMp3(outputDir, track)
                        converted++
                        //Unregister this broadcast Receiver
                        this@ForegroundService.unregisterReceiver(this)
                    }
                }
            }
            registerReceiver(onDownloadComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        }
    }

    /**
     *Converting Downloaded Audio (m4a) to Mp3.( Also Applying Metadata)
     **/
    fun convertToMp3(filePath: String, track: TrackDetails){
        serviceScope.launch {
            sendTrackBroadcast("Converting",track)
            val m4aFile = File(filePath)

            addToNotification("Processing ${track.title}")

            FFmpeg.executeAsync(
                "-i $filePath -y -b:a 160k -acodec libmp3lame -vn ${filePath.substringBeforeLast('.') + ".mp3"}"
            ) { _, returnCode ->
                when (returnCode) {
                    RETURN_CODE_SUCCESS -> {
                        log(Config.TAG, "Async command execution completed successfully.")
                        removeFromNotification("Processing ${track.title}")
                        m4aFile.delete()
                        writeMp3Tags(filePath.substringBeforeLast('.') + ".mp3", track)
                        //FFMPEG task Completed
                    }
                    RETURN_CODE_CANCEL -> {
                        log(Config.TAG, "Async command execution cancelled by user.")
                    }
                    else -> {
                        log(
                            Config.TAG, String.format(
                                "Async command execution failed with rc=%d.",
                                returnCode
                            )
                        )
                    }
                }
            }
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private fun writeMp3Tags(filePath: String, track: TrackDetails){
        serviceScope.launch {
            var mp3File = Mp3File(filePath)
            mp3File =  removeAllTags(mp3File)
            mp3File = setId3v1Tags(mp3File, track)
            mp3File = setId3v2Tags(mp3File, track,this@ForegroundService)
            log("Mp3Tags", "saving file")
            mp3File.save(filePath.substringBeforeLast('.') + ".new.mp3")
            val file = File(filePath)
            file.delete()
            val newFile = File((filePath.substringBeforeLast('.') + ".new.mp3"))
            newFile.renameTo(file)
            converted++
            updateNotification()
            addToLibrary(file.absolutePath)
            allTracksStatus.remove(track.title)
            //Notify Download Completed
            sendTrackBroadcast("track_download_completed",track)
            //All tasks completed (REST IN PEACE)
            if(isFinished && !isSingleDownload){
                delay(5000)
                onDestroy()
            }
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
        log(tag, "Releasing Wake Lock")
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }
        } catch (e: Exception) {
            log(tag, "Service stopped without being started: ${e.message}")
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
        log(tag, "Starting Cleaning in ${dir.path} ")
        val fList = dir.listFiles()
        fList?.let {
            for (file in fList) {
                if (file.isDirectory) {
                    cleanFiles(file)
                } else if(file.isFile) {
                    if(file.path.toString().substringAfterLast(".") != "mp3"){
                        log(tag, "Cleaning ${file.path}")
                        file.delete()
                    }
                }
            }
        }
    }

    /*
    * Add File to Android's Media Library.
    * */
    private fun addToLibrary(path:String) {
        log(tag,"Scanning File")
        MediaScannerConnection.scanFile(this,
            listOf(path).toTypedArray(), null,null)
    }

    /**
     * Function to fetch all Images for use in mp3 tags.
     **/
    suspend fun downloadAllImages(urlList: ArrayList<String>, func: ((resource:File) -> Unit)? = null) {
        /*
        * Last Element of this List defines Its Source
        * */
        val source = urlList.last()

        for (url in urlList.subList(0, urlList.size - 2)) {
            withContext(Dispatchers.IO) {
                val imgUri = url.toUri().buildUpon().scheme("https").build()

                val r = ImageRequest.Builder(this@ForegroundService)
                    .data(imgUri)
                    .build()

                val bitmap = Coil.execute(r).drawable?.toBitmap()
                val file = when (source) {
                    Source.Spotify.name -> {
                        File(imageDir, url.substringAfterLast('/') + ".jpeg")
                    }
                    Source.YouTube.name -> {
                        File(
                            imageDir,
                            url.substringBeforeLast('/', url)
                                .substringAfterLast(
                                    '/',
                                    url
                                ) + ".jpeg"
                        )
                    }
                    Source.Gaana.name -> {
                        File(
                            imageDir,
                            (url.substringBeforeLast('/').substringAfterLast(
                                '/'
                            )) + ".jpeg"
                        )
                    }
                    else -> File(imageDir, url.substringAfterLast('/') + ".jpeg")
                }
                if (bitmap != null) {
                    file.writeBitmap(bitmap)
                    func?.let { it(file) }
                } else log("Foreground Service", "Album Art Could Not be Fetched")
            }
        }
    }

    private fun killService() {
        serviceScope.launch{
            log(tag,"Killing Self")
            messageList = mutableListOf("Cleaning And Exiting","","","","")
            fetch.cancelAll()
            fetch.removeAll()
            updateNotification()
            cleanFiles(File(defaultDir))
            cleanFiles(File(imageDir))
            messageList = mutableListOf("","","","","")
            releaseWakeLock()
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

    private fun initialiseFetch() {
        val fetchConfiguration =
            FetchConfiguration.Builder(this).run {
                setNamespace(channelId)
                setDownloadConcurrentLimit(4)
                build()
            }

        fetch = Fetch.run {
            setDefaultInstanceConfiguration(fetchConfiguration)
            getDefaultInstance()
        }.apply {
            addListener(fetchListener)
            removeAll() //Starting fresh
        }
    }

    private fun getNotification():Notification = NotificationCompat.Builder(this, channelId).run {
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
        addAction(R.drawable.ic_round_cancel_24,"Exit",cancelIntent)
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