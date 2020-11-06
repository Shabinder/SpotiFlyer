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

package com.shabinder.spotiflyer.ui.spotify

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.SimpleItemAnimator
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.github.kiulian.downloader.YoutubeDownloader
import com.shabinder.spotiflyer.MainActivity
import com.shabinder.spotiflyer.R
import com.shabinder.spotiflyer.SharedViewModel
import com.shabinder.spotiflyer.databinding.SpotifyFragmentBinding
import com.shabinder.spotiflyer.downloadHelper.SpotifyDownloadHelper
import com.shabinder.spotiflyer.models.Track
import com.shabinder.spotiflyer.recyclerView.SpotifyTrackListAdapter
import com.shabinder.spotiflyer.utils.YoutubeMusicApi
import com.shabinder.spotiflyer.utils.bindImage
import com.shabinder.spotiflyer.utils.copyTo
import com.shabinder.spotiflyer.utils.rotateAnim
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import javax.inject.Inject

@Suppress("DEPRECATION")

@AndroidEntryPoint
class SpotifyFragment : Fragment() {
    private lateinit var binding:SpotifyFragmentBinding
    private lateinit var spotifyViewModel: SpotifyViewModel
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var adapterSpotify:SpotifyTrackListAdapter
    @Inject lateinit var ytDownloader:YoutubeDownloader
    @Inject lateinit var youtubeMusicApi: YoutubeMusicApi
    private var intentFilter:IntentFilter? = null
    private var updateUIReceiver: BroadcastReceiver? = null


    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.spotify_fragment,container,false)
        adapterSpotify = SpotifyTrackListAdapter()
        initializeAll()
        initializeLiveDataObservers()
        initializeBroadcast()

        val args = SpotifyFragmentArgs.fromBundle(requireArguments())
        val spotifyLink = args.link
        
        val link = spotifyLink.substringAfterLast('/', "Error").substringBefore('?')
        val type = spotifyLink.substringBeforeLast('/', "Error").substringAfterLast('/')

        Log.i("Fragment", "$type : $link")

        if(sharedViewModel.spotifyService.value == null){//Authentication pending!!
            (activity as MainActivity).authenticateSpotify()
        }
        if(!isOnline()){//Device Offline
            sharedViewModel.showAlertDialog(resources,requireContext())
        }else if (type == "Error" || link == "Error") {//Incorrect Link
            showToast("Please Check Your Link!")
        }else if(spotifyLink.contains("open.spotify",true)){//Link Validation!!
            if(type == "episode" || type == "show"){//TODO Implementation
                showToast("Implementing Soon, Stay Tuned!")
            }
            else{
                spotifyViewModel.spotifySearch(type,link)
                if(type=="album")adapterSpotify.isAlbum = true

                binding.btnDownloadAllSpotify.setOnClickListener {
                    for (track in spotifyViewModel.trackList.value!!){
                        if(track.downloaded != "Downloaded"){
                            track.downloaded = "Downloading"
                        }
                    }
                    binding.btnDownloadAllSpotify.visibility = View.GONE
                    binding.downloadingFabSpotify.visibility = View.VISIBLE


                    rotateAnim(binding.downloadingFabSpotify)
                    for (track in spotifyViewModel.trackList.value!!){
                        if(track.downloaded != "Downloaded"){
                            adapterSpotify.notifyItemChanged(spotifyViewModel.trackList.value!!.indexOf(track))
                        }
                    }
                    showToast("Starting Download in Few Seconds")
                    spotifyViewModel.uiScope.launch(Dispatchers.Default){loadAllImages(spotifyViewModel.trackList.value!!)}
                    spotifyViewModel.uiScope.launch {
                        SpotifyDownloadHelper.downloadAllTracks(
                            spotifyViewModel.folderType,
                            spotifyViewModel.subFolder,
                            spotifyViewModel.trackList.value!!,
                            ytDownloader
                        )
                    }
                }
            }
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        initializeBroadcast()
    }

    private fun initializeBroadcast() {
        intentFilter = IntentFilter()
        intentFilter?.addAction("track_download_completed")

        updateUIReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                //UI update here
                if (intent != null){
                    val track = intent.getParcelableExtra<Track?>("track")
                    track?.let {
                        val position: Int = spotifyViewModel.trackList.value?.indexOf(track)!!
                        Log.i("Track","Download Completed Intent :$position")
                        track.downloaded = "Downloaded"
                        if(position != -1) {
                            spotifyViewModel.trackList.value?.set(position, track)
                            adapterSpotify.notifyItemChanged(position)
                            checkIfAllDownloaded()
                        }
                    }
                }
            }
        }
        requireActivity().registerReceiver(updateUIReceiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        requireActivity().unregisterReceiver(updateUIReceiver)
    }

    /**
     *Live Data Observers
     **/
    private fun initializeLiveDataObservers() {
        /**
         * CoverUrl Binding Observer!
         **/
        spotifyViewModel.coverUrl.observe(viewLifecycleOwner, {
            if(it!="Loading") bindImage(binding.spotifyCoverImage,it)
        })

        /**
         * TrackList Binding Observer!
         **/
        spotifyViewModel.trackList.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()){
                Log.i("SpotifyFragment","TrackList Updated")
                adapterConfig(it)
                checkIfAllDownloaded()
            }
        })

        /**
         * Title Binding Observer!
         **/
        spotifyViewModel.title.observe(viewLifecycleOwner, {
            binding.titleViewSpotify.text = it
        })

        sharedViewModel.intentString.observe(viewLifecycleOwner,{
        //Waiting for Authentication to Finish with Spotify()Access Token Observe
            if(it != "" && it!=SpotifyFragmentArgs.fromBundle(requireArguments()).link){
                //New Intent Received , Time TO RELOAD
                (activity as MainActivity).onBackPressed()
            }
        })
    }

    private fun checkIfAllDownloaded() {
        if(!spotifyViewModel.trackList.value!!.any { it.downloaded != "Downloaded" }){
            //All Tracks Downloaded
            binding.btnDownloadAllSpotify.visibility = View.GONE
            binding.downloadingFabSpotify.apply{
                setImageResource(R.drawable.ic_tick)
                visibility = View.VISIBLE
                clearAnimation()
                keepScreenOn = false
            }
        }
    }

    /**
     * Basic Initialization
     **/
    private fun initializeAll() {
        sharedViewModel = ViewModelProvider(this.requireActivity()).get(SharedViewModel::class.java)
        spotifyViewModel = ViewModelProvider(this).get(SpotifyViewModel::class.java)
        sharedViewModel.spotifyService.observe(viewLifecycleOwner, Observer {
            spotifyViewModel.spotifyService = it
        })
        SpotifyDownloadHelper.context = requireContext()
        SpotifyDownloadHelper.youtubeMusicApi = youtubeMusicApi
        SpotifyDownloadHelper.spotifyViewModel = spotifyViewModel
        SpotifyDownloadHelper.statusBar = binding.StatusBarSpotify
        binding.trackListSpotify.adapter = adapterSpotify
        (binding.trackListSpotify.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
    }

    /**
     * Function to fetch all Images for using in mp3 tag.
     **/
    private suspend fun loadAllImages(trackList: List<Track>) {
        trackList.forEach {
            val imgUrl = it.album?.images?.get(0)?.url
            imgUrl?.let {
                val imgUri = imgUrl.toUri().buildUpon().scheme("https").build()
                Glide
                    .with(requireContext())
                    .asFile()
                    .load(imgUri)
                    .listener(object: RequestListener<File> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<File>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            Log.i("Glide","LoadFailed")
                            return false
                        }

                        override fun onResourceReady(
                            resource: File?,
                            model: Any?,
                            target: Target<File>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            sharedViewModel.uiScope.launch {
                                withContext(Dispatchers.IO){
                                    try {
                                        val file = File(
                                            Environment.getExternalStorageDirectory(),
                                            SpotifyDownloadHelper.defaultDir+".Images/" + imgUrl.substringAfterLast('/') + ".jpeg"
                                        )
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
    }

    /**
     * Configure Recycler View Adapter
     **/
    private fun adapterConfig(trackList: List<Track>){
        adapterSpotify.ytDownloader = ytDownloader
        adapterSpotify.spotifyViewModel = spotifyViewModel
        adapterSpotify.submitList(trackList)
    }


    /**
     * Util. Function to create toasts!
     **/
    private fun showToast(message:String){
        Toast.makeText(context,message,Toast.LENGTH_SHORT).show()
    }

    /**
     * Util. Function To Check Connection Status
     **/
    private fun isOnline(): Boolean {
        val cm =
            requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        return netInfo != null && netInfo.isConnectedOrConnecting
    }
}