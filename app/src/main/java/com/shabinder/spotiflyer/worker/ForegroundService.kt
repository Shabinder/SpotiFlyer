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
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.*
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS
import com.arthenica.mobileffmpeg.FFmpeg
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.github.kiulian.downloader.YoutubeDownloader
import com.github.kiulian.downloader.model.formats.Format
import com.github.kiulian.downloader.model.quality.AudioQuality
import com.mpatric.mp3agic.ID3v1Tag
import com.mpatric.mp3agic.ID3v24Tag
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
import com.shabinder.spotiflyer.utils.Provider.imageDir
import com.tonyodev.fetch2.*
import com.tonyodev.fetch2core.DownloadBlock
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.*
import javax.inject.Inject
@AndroidEntryPoint
@RequiresApi(Build.VERSION_CODES.O)
class ForegroundService : Service(){
    private val tag = "Foreground Service"
    private val channelId = "ForegroundDownloaderService"
    private val notificationId = 101
    private var total = 0 //Total Downloads Requested
    private var converted = 0//Total Files Converted
    private var downloaded = 0//Total Files downloaded
    private var serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private val requestMap = mutableMapOf<Request, TrackDetails>()
    private val allTracksDetails = mutableListOf<TrackDetails>()
    private var defaultDir = Provider.defaultDir
    private var wakeLock: PowerManager.WakeLock? = null
    private var isServiceStarted = false
    private var notificationLine = 0
    private var messageList = mutableListOf("", "", "", "")
    private lateinit var cancelIntent:PendingIntent
    private lateinit var fetch:Fetch
    private lateinit var ytDownloader: YoutubeDownloader
    private lateinit var downloadManager : DownloadManager
    @Inject lateinit var youtubeMusicApi: YoutubeMusicApi

    override fun onBind(intent: Intent): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel(channelId,"Downloader Service")
        val intent = Intent(
            this,
            ForegroundService::class.java
        ).apply{action = "kill"}
        cancelIntent = PendingIntent.getService (this, 0 , intent , PendingIntent.FLAG_CANCEL_CURRENT )
        downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        ytDownloader = YoutubeDownloader()
        initialiseFetch()
    }

    @SuppressLint("WakelockTimeout")
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // Send a notification that service is started
        Log.i(tag, "Service Started.")
        startForeground(notificationId, getNotification())

        when (intent.action) {
            "kill" -> killService()
            "query" -> {
                val response = Intent().apply {
                    action = "query_result"
                    putParcelableArrayListExtra("tracks", allTracksDetails as ArrayList<TrackDetails> )
                }
                sendBroadcast(response)
            }
        }

        val downloadObjects: ArrayList<TrackDetails>? = (intent.getParcelableArrayListExtra("object") ?: intent.extras?.getParcelableArrayList(
            "object"
        ))
        val imagesList: ArrayList<String>? = (intent.getStringArrayListExtra("imagesList") ?: intent.extras?.getStringArrayList(
            "imagesList"
        ))

        imagesList?.let{
            serviceScope.launch {
                loadAllImages(it)
            }
        }

        downloadObjects?.let {
            total += downloadObjects.size
            updateNotification()
            it.forEach { it1 ->
                allTracksDetails.add(it1.apply { downloaded = DownloadStatus.Queued })
            }
            downloadAllTracks(it)
        }

        //Wake locks and misc tasks from here :
        return if (isServiceStarted){
            //Service Already Started
            START_STICKY
        } else{
            Log.i(tag, "Starting the foreground service task")
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
        serviceScope.launch(Dispatchers.Default){
            trackList.forEach {
                if(it.downloaded == DownloadStatus.Downloaded){//Download Already Present!!
                }else {
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
                                    val videoId = sortByBestMatch(
                                        getYTTracks(response.body().toString()),
                                        trackName = it.title,
                                        trackArtists = it.artists,
                                        trackDurationSec = it.durationSec
                                    ).keys.firstOrNull()
                                    Log.i("Service VideoID", videoId ?: "Not Found")
                                    if (videoId.isNullOrBlank()) sendTrackBroadcast(
                                        Status.FAILED.name,
                                        it
                                    )
                                    else {//Found Youtube Video ID
                                        downloadTrack(videoId, it)
                                    }
                                }

                                override fun onFailure(call: Call<String>, t: Throwable) {
                                    if (t.message.toString()
                                            .contains("Failed to connect")
                                    ) showMessage("Failed, Check Your Internet Connection!")
                                    Log.i("YT API Req. Fail", t.message.toString())
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
                val video = ytDownloader.getVideo(videoID)
                val format: Format? = try {
                    video?.findAudioWithQuality(AudioQuality.medium)?.get(0) as Format
                } catch (e: java.lang.IndexOutOfBoundsException) {
                    try {
                        video?.findAudioWithQuality(AudioQuality.high)?.get(0) as Format
                    } catch (e: java.lang.IndexOutOfBoundsException) {
                        try {
                            video?.findAudioWithQuality(AudioQuality.low)?.get(0) as Format
                        } catch (e: java.lang.IndexOutOfBoundsException) {
                            Log.i("YTDownloader", e.toString())
                            null
                        }
                    }
                }
                format?.let {
                    val url: String = format.url()
                    Log.i("DHelper Link Found", url)
                    val request= Request(url, track.outputFile).apply{
                        priority = Priority.NORMAL
                        networkType = NetworkType.ALL
                    }
                    fetch.enqueue(request,
                        {
                            requestMap[it] = track
                            Log.i(tag, "Enqueuing Download")
                        },
                        {
                            Log.i(tag, "Enqueuing Error:${it.throwable.toString()}")
                        }
                    )
                }
            }catch (e: com.github.kiulian.downloader.YoutubeException){
                Log.i("Service YT Error", e.message.toString())
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
            val track  = requestMap[download.request]
            when(notificationLine){
                0 -> {
                    messageList[0] = "Downloading ${track?.title}"
                    notificationLine = 1
                }
                1 -> {
                    messageList[1] = "Downloading ${track?.title}"
                    notificationLine = 2
                }
                2 -> {
                    messageList[2] = "Downloading ${track?.title}"
                    notificationLine = 3
                }
                3 -> {
                    messageList[3] = "Downloading ${track?.title}"
                    notificationLine = 0
                }
            }
            Log.i(tag, "${track?.title} Download Started")
            track?.let{
                allTracksDetails[allTracksDetails.map{ trackDetails -> trackDetails.title}.indexOf(it.title)] =
                    it.apply { downloaded = DownloadStatus.Downloading }
                updateNotification()
                sendTrackBroadcast(Status.DOWNLOADING.name,track)
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
            for (message in messageList){
                if( message == "Downloading ${track?.title}"){
                    //Remove Downloading Status from Notification
                    messageList[messageList.indexOf(message)] = ""
                }
            }

            serviceScope.launch {
                try{
                    track?.let {
                        convertToMp3(download.file, it)
                        allTracksDetails[allTracksDetails.map{ trackDetails -> trackDetails.title}.indexOf(it.title)] =
                            it.apply { downloaded = DownloadStatus.Converting }
                    }
                    Log.i(tag, "${track?.title} Download Completed")
                }catch (
                    e: KotlinNullPointerException
                ){
                    Log.i(tag, "${track?.title} Download Failed! Error:Fetch!!!!")
                    Log.i(tag, "${track?.title} Requesting Download thru Android DM")
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
                Log.i(tag, download.error.throwable.toString())
                Log.i(tag, "${track?.title} Requesting Download thru Android DM")
                downloadUsingDM(download.request.url, download.request.file, track!!)
                requestMap.remove(download.request)
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
            val track  = requestMap[download.request]
            Log.i(tag, "${track?.title} ETA: ${etaInMilliSeconds / 1000} sec")
            val intent = Intent().apply {
                action = "Progress"
                putExtra("progress", download.progress)
                putExtra("track", requestMap[download.request])
            }
            sendBroadcast(intent)
        }
    }

    /**
    * If fetch Fails , Android Download Manager To RESCUE!!
    **/
    fun downloadUsingDM(url: String, outputDir: String, track: TrackDetails){
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
        Log.i("DownloadManager", "Download Request Sent")

        val onDownloadComplete: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                //Fetching the download id received with the broadcast
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                //Checking if the received broadcast is for our enqueued download by matching download id
                if (downloadID == id) {
                    allTracksDetails[allTracksDetails.map{ trackDetails -> trackDetails.title}.indexOf(track.title)] =
                        track.apply { downloaded = DownloadStatus.Converting }
                    convertToMp3(outputDir, track)
                    converted++
                    //Unregister this broadcast Receiver
                    this@ForegroundService.unregisterReceiver(this)
                }
            }
        }
        registerReceiver(onDownloadComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    /**
     *Converting Downloaded Audio (m4a) to Mp3.( Also Applying Metadata)
     **/
    fun convertToMp3(filePath: String, track: TrackDetails){
        sendTrackBroadcast("Converting",track)
        val m4aFile = File(filePath)

        FFmpeg.executeAsync(
            "-i $filePath -y -b:a 160k -vn ${filePath.substringBeforeLast('.') + ".mp3"}"
        ) { _, returnCode ->
            when (returnCode) {
                RETURN_CODE_SUCCESS -> {
                    Log.i(Config.TAG, "Async command execution completed successfully.")
                    m4aFile.delete()
                    writeMp3Tags(filePath.substringBeforeLast('.') + ".mp3", track)
                    //FFMPEG task Completed
                }
                RETURN_CODE_CANCEL -> {
                    Log.i(Config.TAG, "Async command execution cancelled by user.")
                }
                else -> {
                    Log.i(
                        Config.TAG, String.format(
                            "Async command execution failed with rc=%d.",
                            returnCode
                        )
                    )
                }
            }
        }
    }

    private fun writeMp3Tags(filePath: String, track: TrackDetails){
        var mp3File = Mp3File(filePath)
        mp3File =  removeAllTags(mp3File)
        mp3File = setId3v1Tags(mp3File, track)
        mp3File = setId3v2Tags(mp3File, track)
        Log.i("Mp3Tags", "saving file")
        mp3File.save(filePath.substringBeforeLast('.') + ".new.mp3")
        val file = File(filePath)
        file.delete()
        val newFile = File((filePath.substringBeforeLast('.') + ".new.mp3"))
        newFile.renameTo(file)
        converted++
        updateNotification()
        addToLibrary(file.absolutePath)
        allTracksDetails.removeAt(allTracksDetails.map{ trackDetails -> trackDetails.title}.indexOf(track.title))
        //Notify Download Completed
        sendTrackBroadcast("track_download_completed",track)
        //All tasks completed (REST IN PEACE)
        if(converted == total){
            onDestroy()
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

    /**
     *Modifying Mp3 com.shabinder.spotiflyer.models.gaana.Tags with MetaData!
     **/
    private fun setId3v1Tags(mp3File: Mp3File, track: TrackDetails): Mp3File {
        val id3v1Tag = ID3v1Tag().apply {
            artist = track.artists.joinToString(",")
            title = track.title
            album = track.albumName
            year = track.year
            comment = "Genres:${track.comment}"
        }
        mp3File.id3v1Tag = id3v1Tag
        return mp3File
    }
    private fun setId3v2Tags(mp3file: Mp3File, track: TrackDetails): Mp3File {
        val id3v2Tag = ID3v24Tag().apply {
            artist = track.artists.joinToString(",")
            title = track.title
            album = track.albumName
            year = track.year
            comment = "Genres:${track.comment}"
            lyrics = "Gonna Implement Soon"
            url = track.trackUrl
        }
        val bytesArray = ByteArray(track.albumArt.length().toInt())
        try{
            val fis = FileInputStream(track.albumArt)
            fis.read(bytesArray) //read file into bytes[]
            fis.close()
            id3v2Tag.setAlbumImage(bytesArray, "image/jpeg")
        }catch (e: java.io.FileNotFoundException){
            Log.i("Error", "Couldn't Write Mp3 Album Art")
        }
        mp3file.id3v2Tag = id3v2Tag
        return mp3file
    }

    private fun removeAllTags(mp3file: Mp3File): Mp3File {
        if (mp3file.hasId3v1Tag()) {
            mp3file.removeId3v1Tag()
        }
        if (mp3file.hasId3v2Tag()) {
            mp3file.removeId3v2Tag()
        }
        if (mp3file.hasCustomTag()) {
            mp3file.removeCustomTag()
        }
        return mp3file
    }

    private fun releaseWakeLock() {
        Log.i(tag, "Releasing Wake Lock")
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }
        } catch (e: Exception) {
            Log.i(tag, "Service stopped without being started: ${e.message}")
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
        Log.i(tag, "Starting Cleaning in ${dir.path} ")
        val fList = dir.listFiles()
        fList?.let {
            for (file in fList) {
                if (file.isDirectory) {
                    cleanFiles(file)
                } else if(file.isFile) {
                    if(file.path.toString().substringAfterLast(".") != "mp3"){
                        Log.i(tag, "Cleaning ${file.path}")
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
        Log.i(tag,"Scanning File")
        MediaScannerConnection.scanFile(this,
            listOf(path).toTypedArray(), null,null)
    }

    /**
     * Function to fetch all Images for use in mp3 tags.
     **/
    private suspend fun loadAllImages(urlList: ArrayList<String>) {
        /*
        * Last Element of this List defines Its Source
        * */
        val source = urlList.last()
        for (url in urlList.subList(0, urlList.size - 2)) {
            val imgUri = url.toUri().buildUpon().scheme("https").build()
            Glide
                .with(this)
                .asFile()
                .load(imgUri)
                .listener(object : RequestListener<File> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<File>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.i("Glide", "LoadFailed")
                        return false
                    }

                    override fun onResourceReady(
                        resource: File?,
                        model: Any?,
                        target: Target<File>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        serviceScope.launch {
                            withContext(Dispatchers.IO) {
                                try {
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

                                        else -> File(
                                            imageDir,
                                            url.substringAfterLast('/') + ".jpeg"
                                        )
                                    }
                                    resource?.copyTo(file)
                                } catch (e: IOException) {
                                    e.printStackTrace()
                                }
                            }
                        }
                        return false
                    }
                }).submit()
        }
    }

    private fun killService() {
        serviceScope.launch{
            Log.i(tag,"Killing Self")
            messageList = mutableListOf("Cleaning And Exiting","","","")
            fetch.cancelAll()
            fetch.removeAll()
            updateNotification()
            cleanFiles(File(defaultDir))
            messageList = mutableListOf("","","","")
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
        if(converted == total){
            Handler(Looper.myLooper()!!).postDelayed({
                killService()
            }, 5000)
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        if(converted == total ){
            killService()
        }
    }

    private fun initialiseFetch() {
        val fetchConfiguration =
            FetchConfiguration.Builder(this)
                .setNamespace(channelId)
                .setDownloadConcurrentLimit(4)
                .build()

        Fetch.setDefaultInstanceConfiguration(fetchConfiguration)

        fetch = Fetch.getDefaultInstance()
        fetch.addListener(fetchListener)
        //clearing all not completed Downloads
        //Starting fresh
        fetch.removeAll()
    }

    private fun getNotification():Notification = NotificationCompat.Builder(this, channelId).run {
        setSmallIcon(R.drawable.down_arrowbw)
        setSubText("Total: $total  Completed:$converted")
        setNotificationSilent()
        setStyle(
            NotificationCompat.InboxStyle().run {
                addLine(messageList[0])
                addLine(messageList[1])
                addLine(messageList[2])
                addLine(messageList[3])
            }
        )
        addAction(R.drawable.ic_baseline_cancel_24,"Exit",cancelIntent)
        build()
    }

    fun sendTrackBroadcast(action:String,track:TrackDetails){
        val intent = Intent().apply{
            setAction(action)
            putExtra("track", track)
        }
        this@ForegroundService.sendBroadcast(intent)
    }
}