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

import android.app.*
import android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.*
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS
import com.arthenica.mobileffmpeg.FFmpeg
import com.mpatric.mp3agic.ID3v1Tag
import com.mpatric.mp3agic.ID3v24Tag
import com.mpatric.mp3agic.Mp3File
import com.shabinder.spotiflyer.MainActivity
import com.shabinder.spotiflyer.R
import com.shabinder.spotiflyer.downloadHelper.SpotifyDownloadHelper
import com.shabinder.spotiflyer.models.DownloadObject
import com.shabinder.spotiflyer.models.Track
import com.tonyodev.fetch2.*
import com.tonyodev.fetch2core.DownloadBlock
import com.tonyodev.fetch2core.Func
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream

class ForegroundService : Service(){
    private val tag = "Foreground Service"
    private val channelId = "ForegroundDownloaderService"
    private val notificationId = 101
    private var total = 0 //Total Downloads Requested
    private var converted = 0//Total Files Converted
    private var downloaded = 0//Total Files downloaded
    private var fetch:Fetch? = null
    private var downloadManager : DownloadManager? = null
    private var downloadList = mutableListOf<DownloadObject>()
    private var serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private val requestMap = mutableMapOf<Request,Track>()
    private val downloadMap = mutableMapOf<String,Track>()
    private var speed :Long = 0
    private var defaultDirectory = Environment.DIRECTORY_MUSIC + File.separator + "SpotiFlyer" + File.separator
    private val parentDirectory = File(Environment.getExternalStorageDirectory(),
        defaultDirectory+File.separator
    )
    private var wakeLock: PowerManager.WakeLock? = null
    private var isServiceStarted = false
    var notificationLine = 0
    val messageList = mutableListOf<String>("","","","")
    private var pendingIntent:PendingIntent? = null



    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        val notificationIntent = Intent(this, MainActivity::class.java)
        pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, 0
        )
        downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        val fetchConfiguration =
            FetchConfiguration.Builder(this)
                .setDownloadConcurrentLimit(4)
                .build()

        Fetch.setDefaultInstanceConfiguration(fetchConfiguration)

        fetch = Fetch.getDefaultInstance()
//        fetch?.enableLogging(true)
        fetch?.addListener(fetchListener)
        //clearing all not completed Downloads
        //Starting fresh
        fetch?.removeAll()

        startForeground()
    }

    /**
     *Starting Service with Notification as Foreground!
     **/
    private fun startForeground() {
        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel(channelId, "Downloader Service")
            } else {
                // If earlier version channel ID is not used
                // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
                ""
            }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.down_arrowbw)
            .setNotificationSilent()
            .setSubText("Speed: $speed KB/s")
            .setStyle(NotificationCompat.InboxStyle()
                .setBigContentTitle("Total: $total  Completed:$converted")
                .addLine(messageList[0])
                .addLine(messageList[1])
                .addLine(messageList[2])
                .addLine(messageList[3]))
            .setContentIntent(pendingIntent)
            .build()
        startForeground(notificationId, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String{
        val chan = NotificationChannel(channelId,
            channelName, NotificationManager.IMPORTANCE_DEFAULT)
        chan.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // Send a notification that service is started
        Log.i(tag,"Service Started.")
        startForeground()
        //do heavy work on a background thread
        //val list = intent.getSerializableExtra("list") as List<Any?>
//        val list = intent.getParcelableArrayListExtra<DownloadObject>("list") ?: intent.extras?.getParcelableArrayList<DownloadObject>("list")
//        Log.i(tag,"Intent List Size: ${list!!.size}")
        val obj = intent.getParcelableExtra<DownloadObject>("object") ?: intent.extras?.getParcelable<DownloadObject>("object")
        obj?.let {
            total ++
//        Log.i(tag,"Intent List Size: ${list!!.size}")
            updateNotification()
            serviceScope.launch {
                    val request= Request(obj.url, obj.outputDir)
                    request.priority = Priority.NORMAL
                    request.networkType = NetworkType.ALL

                    fetch!!.enqueue(request,
                        Func {
                            obj.track?.let { it1 -> requestMap.put(it, it1) }
                            downloadList.remove(obj)
                            Log.i(tag, "Enqueuing Download")
                        },
                        Func {
                            Log.i(tag, "Enqueuing Error:${it.throwable.toString()}")}
                    )
            }
        }

        //Wake locks and misc tasks from here :
        return if (isServiceStarted){
            START_STICKY
        } else{
            Log.i(tag,"Starting the foreground service task")
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

    override fun onDestroy() {
        super.onDestroy()
        if(downloadMap.isEmpty() && converted == total){
            Handler().postDelayed({
                Log.i(tag,"Service destroyed.")
                deleteFile(parentDirectory)
                releaseWakeLock()
                stopForeground(true)
            },2000)
        }
    }

    private fun releaseWakeLock() {
        Log.i(tag,"Releasing Wake Lock")
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }
        } catch (e: Exception) {
            Log.i(tag,"Service stopped without being started: ${e.message}")
        }
        isServiceStarted = false
    }


    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        if(downloadMap.isEmpty() && converted == total ){
            Log.i(tag,"Service Removed.")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                stopForeground(true)
            } else {
                stopSelf()//System will automatically close it
            }
        }
    }

    /**
     * Deleting All Residual Files except Mp3 Files
     **/
    private fun deleteFile(dir:File) {
        Log.i(tag,"Starting Deletions in ${dir.path} ")
        val fList = dir.listFiles()
        fList?.let {
            for (file in fList) {
                if (file.isDirectory) {
                    deleteFile(file)
                } else if(file.isFile) {
                    if(file.path.toString().substringAfterLast(".") != "mp3"){
//                        Log.i(tag,"deleting ${file.path}")
                        file.delete()
                    }
                }
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
            // TODO("Not yet implemented")
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
                    messageList[0] = "Downloading ${track?.name}"
                    notificationLine = 1
                }
                1 -> {
                    messageList[1] = "Downloading ${track?.name}"
                    notificationLine = 2
                }
                2-> {
                    messageList[2] = "Downloading ${track?.name}"
                    notificationLine = 3
                }
                3 -> {
                    messageList[3] = "Downloading ${track?.name}"
                    notificationLine = 0
                }
            }
            Log.i(tag,"${track?.name} Download Started")
            updateNotification()
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
                if( message == "Downloading ${track?.name}"){
                    messageList[messageList.indexOf(message)] = ""
                }
            }
            //Notify Download Completed
            val intent = Intent()
                .setAction("track_download_completed")
                .putExtra("track",track)
            this@ForegroundService.sendBroadcast(intent)


            serviceScope.launch {
                try{
                    convertToMp3(download.file, track!!)
                    Log.i(tag,"${track.name} Download Completed")
                }catch (e:KotlinNullPointerException
                ){
                    Log.i(tag,"${track?.name} Download Failed! Error:Fetch!!!!")
                    Log.i(tag,"${track?.name} Requesting Download thru Android DM")
                    downloadUsingDM(download.request.url,download.request.file, track!!)
                    downloaded++
                    requestMap.remove(download.request)
                }
            }
            if(requestMap.keys.toList().isEmpty()) speed = 0
            updateNotification()
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
                Log.i(tag,download.error.throwable.toString())
                Log.i(tag,"${track?.name} Requesting Download thru Android DM")
                downloadUsingDM(download.request.url,download.request.file, track!!)
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
            Log.i(tag,"${track?.name} ETA: ${etaInMilliSeconds/1000} sec")
            speed = (downloadedBytesPerSecond/1000)
            updateNotification()
        }

    }

    /**
    * If fetch Fails , Android Download Manager To RESCUE!!
    **/
    fun downloadUsingDM(url:String, outputDir:String, track: Track){
        val uri = Uri.parse(url)
        val request = DownloadManager.Request(uri)
            .setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_WIFI or
                        DownloadManager.Request.NETWORK_MOBILE
            )
            .setAllowedOverRoaming(false)
            .setTitle(track.name)
            .setDescription("Spotify Downloader Working Up here...")
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_MUSIC, outputDir.removePrefix(
                Environment.getExternalStorageDirectory().toString() + Environment.DIRECTORY_MUSIC + File.separator
            ))
            .setNotificationVisibility(VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        //Start Download
        val downloadID = downloadManager?.enqueue(request)
        Log.i("DownloadManager", "Download Request Sent")

        val onDownloadComplete: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                //Fetching the download id received with the broadcast
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                //Checking if the received broadcast is for our enqueued download by matching download id
                if (downloadID == id) {
                    convertToMp3(outputDir,track)
                    converted++
                    //Unregister this broadcast Receiver
                    this@ForegroundService.unregisterReceiver(this)
                }
            }
        }
        registerReceiver(onDownloadComplete,IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    /**
     *Converting Downloaded Audio (m4a) to Mp3.( Also Applying Metadata)
     **/
    fun convertToMp3(filePath: String, track: Track){
        val m4aFile = File(filePath)

        FFmpeg.executeAsync(
            "-i $filePath -y -b:a 160k -vn ${filePath.substringBeforeLast('.') + ".mp3"}"
        ) { _, returnCode ->
            when (returnCode) {
                RETURN_CODE_SUCCESS -> {
                    Log.i(Config.TAG, "Async command execution completed successfully.")
                    m4aFile.delete()
                    writeMp3Tags(filePath.substringBeforeLast('.')+".mp3",track)
                    //FFMPEG task Completed
                    }
                RETURN_CODE_CANCEL -> {
                    Log.i(Config.TAG, "Async command execution cancelled by user.")
                }
                else -> {
                    Log.i(Config.TAG, String.format("Async command execution failed with rc=%d.", returnCode))
                }
            }
        }
    }

    private fun writeMp3Tags(filePath:String, track: Track){
        var mp3File = Mp3File(filePath)
        mp3File =  removeAllTags(mp3File)
        mp3File = setId3v1Tags(mp3File,track)
        mp3File = setId3v2Tags(mp3File,track)
        Log.i("Mp3Tags","saving file")
        mp3File.save(filePath.substringBeforeLast('.')+".new.mp3")
        val file = File(filePath)
        file.delete()
        val newFile = File((filePath.substringBeforeLast('.')+".new.mp3"))
        newFile.renameTo(file)
        converted++
        updateNotification()
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
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.down_arrowbw)
            .setSubText("Speed: $speed KB/s")
            .setNotificationSilent()
            .setStyle(NotificationCompat.InboxStyle()
                .setBigContentTitle("Total: $total  Completed:$converted")
                .addLine(messageList[0])
                .addLine(messageList[1])
                .addLine(messageList[2])
                .addLine(messageList[3]))
            .setContentIntent(pendingIntent)
            .build()
        mNotificationManager.notify(notificationId, notification)
    }

    /**
     *Modifying Mp3 Tags with MetaData!
     **/
    private fun setId3v1Tags(mp3File: Mp3File, track: Track): Mp3File {
        val id3v1Tag = ID3v1Tag()
        id3v1Tag.track = track.disc_number.toString()
        val artistsList = mutableListOf<String>()
        track.artists?.forEach { artistsList.add(it!!.name!!) }
        id3v1Tag.artist = artistsList.joinToString()
        id3v1Tag.title = track.name
        id3v1Tag.album = track.album?.name
        id3v1Tag.year = track.album?.release_date
        id3v1Tag.comment = "Genres:${track.album?.genres?.joinToString()}"
        mp3File.id3v1Tag = id3v1Tag
        return mp3File
    }
    private fun setId3v2Tags(mp3file: Mp3File, track: Track): Mp3File {
        val id3v2Tag = ID3v24Tag()
        id3v2Tag.track = track.disc_number.toString()
        val artistsList = mutableListOf<String>()
        track.artists?.forEach { artistsList.add(it!!.name!!) }
        id3v2Tag.artist = artistsList.joinToString()
        id3v2Tag.title = track.name
        id3v2Tag.album = track.album?.name
        id3v2Tag.year = track.album?.release_date
        id3v2Tag.comment = "Genres:${track.album?.genres?.joinToString()}"
        id3v2Tag.lyrics = "Gonna Implement Soon"
        val copyrights = mutableListOf<String>()
        track.album?.copyrights?.forEach { copyrights.add(it!!.type!!) }
        id3v2Tag.copyright = copyrights.joinToString()
        id3v2Tag.url = track.href
        track.ytCoverUrl?.let {
            val file = File(
                Environment.getExternalStorageDirectory(),
                SpotifyDownloadHelper.defaultDir +".Images/" + it.substringAfterLast('/',it) + ".jpeg")
            Log.i("Mp3Tags editing Tags",file.path)
            //init array with file length
            val bytesArray = ByteArray(file.length().toInt())
            val fis = FileInputStream(file)
            fis.read(bytesArray) //read file into bytes[]
            fis.close()
            id3v2Tag.setAlbumImage(bytesArray,"image/jpeg")
        }
        track.album?.let {
            val file = File(
                Environment.getExternalStorageDirectory(),
                SpotifyDownloadHelper.defaultDir +".Images/" + (it.images?.get(0)?.url!!).substringAfterLast('/') + ".jpeg")
            Log.i("Mp3Tags editing Tags",file.path)
            //init array with file length
            val bytesArray = ByteArray(file.length().toInt())
            val fis = FileInputStream(file)
            fis.read(bytesArray) //read file into bytes[]
            fis.close()
            id3v2Tag.setAlbumImage(bytesArray,"image/jpeg")
        }
        id3v2Tag.albumImage
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

}