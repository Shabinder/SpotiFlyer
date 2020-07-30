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
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.os.IBinder
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
import com.shabinder.spotiflyer.downloadHelper.DownloadHelper
import com.shabinder.spotiflyer.models.DownloadObject
import com.shabinder.spotiflyer.models.Track
import com.tonyodev.fetch2.*
import com.tonyodev.fetch2core.DownloadBlock
import com.tonyodev.fetch2core.Func
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream

class ForegroundService : Service(){
    private val tag = "Foreground Service"
    private val channelId = "ForegroundDownloaderService"
    private var total = 0 //Total Downloads Requested
    private var converted = 0//Total Files Converted
    private var fetch:Fetch? = null
    private var downloadList = mutableListOf<DownloadObject>()
    private var serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private val requestMap = mutableMapOf<Request,Track>()
    private val downloadMap = mutableMapOf<String,Track>()
    private var speed :Long = 0

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, 0
        )
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("SpotiFlyer: Downloading Your Music")
            .setSubText("Speed: $speed KB/s ")
            .setNotificationSilent()
            .setOnlyAlertOnce(true)
            .setContentText("Total: $total  Downloaded: ${total - requestMap.keys.size}  Converted:$converted ")
            .setSmallIcon(R.drawable.down_arrowbw)
            .build()

        val fetchConfiguration =
            FetchConfiguration.Builder(this)
                .setDownloadConcurrentLimit(4)
                .build()

        Fetch.Impl.setDefaultInstanceConfiguration(fetchConfiguration)
        fetch = Fetch.getDefaultInstance()
        fetch?.addListener(fetchListener)
        startForeground()
    }

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
            .setOngoing(true)
            .setContentTitle("SpotiFlyer: Downloading Your Music")
            .setSubText("Speed: $speed KB/s ")
            .setNotificationSilent()
            .setOnlyAlertOnce(true)
            .setContentText("Total: $total  Downloaded: ${total - requestMap.keys.size}  Converted:$converted ")
            .setSmallIcon(R.drawable.down_arrowbw)
            .build()
        startForeground(101, notification)
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

        //do heavy work on a background thread
//        val list = intent.getSerializableExtra("list") as List<Any?>
        val list = intent.getParcelableArrayListExtra<DownloadObject>("list") ?: intent.extras?.getParcelableArrayList<DownloadObject>("list")
        Log.i(tag,"Intent List Size: ${list!!.size}")
        total += list.size
        list.forEach { downloadList.add(it as DownloadObject) }

        serviceScope.launch {
            withContext(Dispatchers.IO){
                for (downloadObject in downloadList) {
                    val request= Request(downloadObject.url, downloadObject.outputDir)
                    request.priority = Priority.NORMAL
                    request.networkType = NetworkType.ALL

                    fetch?.enqueue(request,
                        Func {
                            Log.i("DownloadManager", "Download Request Sent")
                            requestMap[it] = downloadObject.track
                            downloadList.remove(downloadObject) },
                        Func {
                            Log.i("DownloadManager", "Download Request Error:${it.throwable.toString()}")}
                    )

                }

            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        if(downloadMap.isEmpty() && converted == total){
            Log.i(tag,"Service destroyed.")
            stopForeground(true)
        }
    }


    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        if(downloadMap.isEmpty() && converted == total ){
            Log.i(tag,"Service destroyed.")
            stopSelf()
        }
    }

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
            Log.i(tag,"${track?.name} Download Started")
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
                speed = 0
                serviceScope.launch {
                    convertToMp3(download.file, track!!)
                }
                Log.i(tag,"${track?.name} Download Completed")
                requestMap.remove(download.request)
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
            Log.i(tag,download.error.throwable.toString())
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


    fun convertToMp3(filePath: String,track: Track){
        val m4aFile = File(filePath)

        val executionId = FFmpeg.executeAsync(
            "-i $filePath  -vn ${filePath.substringBeforeLast('.') + ".mp3"}"
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
            stopForeground(false)
            stopSelf()
        }

    }
    /**
     * This is the method that can be called to update the Notification
     */
    private fun updateNotification() {
        val mNotificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("SpotiFlyer: Downloading Your Music")
            .setContentText("Total: $total  Downloaded: ${total - requestMap.keys.size}  Converted:$converted ")
            .setSubText("Speed: $speed KB/s ")
            .setNotificationSilent()
            .setOnlyAlertOnce(true)
            .setSmallIcon(R.drawable.down_arrowbw)
            .build()
        mNotificationManager.notify(101, notification)
    }

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
        track.let {
            val file = File(
                Environment.getExternalStorageDirectory(),
                DownloadHelper.defaultDir +".Images/" + (it.album!!.images?.get(0)?.url!!).substringAfterLast('/') + ".jpeg")
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