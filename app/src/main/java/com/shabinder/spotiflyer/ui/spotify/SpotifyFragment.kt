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
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.SimpleItemAnimator
import com.shabinder.spotiflyer.MainActivity
import com.shabinder.spotiflyer.R
import com.shabinder.spotiflyer.SharedViewModel
import com.shabinder.spotiflyer.databinding.TrackListFragmentBinding
import com.shabinder.spotiflyer.downloadHelper.DownloadHelper
import com.shabinder.spotiflyer.models.DownloadStatus
import com.shabinder.spotiflyer.models.TrackDetails
import com.shabinder.spotiflyer.models.spotify.Source
import com.shabinder.spotiflyer.networking.YoutubeMusicApi
import com.shabinder.spotiflyer.recyclerView.SpotifyTrackListAdapter
import com.shabinder.spotiflyer.utils.*
import com.shabinder.spotiflyer.utils.Provider.defaultDir
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@Suppress("DEPRECATION")

@AndroidEntryPoint
class SpotifyFragment : Fragment() {
    private lateinit var binding:TrackListFragmentBinding
    private lateinit var sharedViewModel: SharedViewModel
    @Inject lateinit var youtubeMusicApi: YoutubeMusicApi
    private lateinit var viewModel: SpotifyViewModel
    private lateinit var adapter:SpotifyTrackListAdapter
    private var intentFilter:IntentFilter? = null
    private var updateUIReceiver: BroadcastReceiver? = null


    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.track_list_fragment,container,false)
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
        if (type == "Error" || link == "Error") {//Incorrect Link
            showMessage("Please Check Your Link!")
        }else if(spotifyLink.contains("open.spotify",true)){//Link Validation!!
            if(type == "episode" || type == "show"){//TODO Implementation
                showMessage("Implementing Soon, Stay Tuned!")
            }
            else{
                viewModel.spotifySearch(type,link)
                if(type=="album")adapter.isAlbum = true

                binding.btnDownloadAll.setOnClickListener {
                    if(!isOnline()){
                        showNoConnectionAlert()
                        return@setOnClickListener
                    }
                    binding.btnDownloadAll.visibility = View.GONE
                    binding.downloadingFab.visibility = View.VISIBLE

                    rotateAnim(binding.downloadingFab)
                    for (track in viewModel.trackList.value!!){
                        if(track.downloaded != DownloadStatus.Downloaded){
                            track.downloaded = DownloadStatus.Downloading
                            adapter.notifyItemChanged(viewModel.trackList.value!!.indexOf(track))
                        }
                    }
                    showMessage("Processing!")
                    sharedViewModel.uiScope.launch(Dispatchers.Default){
                        val urlList = arrayListOf<String>()
                        viewModel.trackList.value?.forEach { urlList.add(it.album?.images?.get(0)?.url.toString()) }
                        //Appending Source
                        urlList.add("spotify")
                        loadAllImages(
                            requireActivity(),
                            urlList
                        )
                    }
                    viewModel.uiScope.launch {
                        val finalList = viewModel.trackList.value?.map{
                            val artistsList = mutableListOf<String>()
                            it.artists?.forEach { artist -> artistsList.add(artist!!.name!!) }
                            TrackDetails(
                                title = it.name.toString(),
                                artists = artistsList,
                                durationSec = (it.duration_ms/1000).toInt(),
                                albumArt = File(
                                    Environment.getExternalStorageDirectory(),
                                    defaultDir +".Images/" + (it.album?.images?.get(0)?.url.toString()).substringAfterLast('/') + ".jpeg"),
                                albumName = it.album?.name,
                                year = it.album?.release_date,
                                comment = "Genres:${it.album?.genres?.joinToString()}",
                                trackUrl = it.href,
                                source = Source.Spotify
                            )
                        }
                        if(finalList.isNullOrEmpty())showMessage("Not Downloading Any Song")
                        DownloadHelper.downloadAllTracks(
                            viewModel.folderType,
                            viewModel.subFolder,
                            finalList ?: listOf(),
                        )
                    }
                }
            }
        }
        return binding.root
    }

    /**
     * Basic Initialization
     **/
    private fun initializeAll() {
        sharedViewModel = ViewModelProvider(this.requireActivity()).get(SharedViewModel::class.java)
        viewModel = ViewModelProvider(this).get(SpotifyViewModel::class.java)
        sharedViewModel.spotifyService.observe(viewLifecycleOwner, {
            viewModel.spotifyService = it
        })
        adapter = SpotifyTrackListAdapter(viewModel)
        DownloadHelper.youtubeMusicApi = youtubeMusicApi
        DownloadHelper.sharedViewModel = sharedViewModel
        DownloadHelper.statusBar = binding.statusBar
        binding.trackList.adapter = adapter
        (binding.trackList.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
    }


    /**
     *Live Data Observers
     **/
    private fun initializeLiveDataObservers() {
        /**
         * CoverUrl Binding Observer!
         **/
        viewModel.coverUrl.observe(viewLifecycleOwner, {
            if(it!="Loading") bindImage(binding.coverImage,it, Source.Spotify)
        })

        /**
         * TrackList Binding Observer!
         **/
        viewModel.trackList.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()){
                Log.i("SpotifyFragment","TrackList Updated")
                adapter.submitList(it)
                checkIfAllDownloaded()
            }
        })

        /**
         * Title Binding Observer!
         **/
        viewModel.title.observe(viewLifecycleOwner, {
            binding.titleView.text = it
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
        if(!viewModel.trackList.value!!.any { it.downloaded != DownloadStatus.Downloaded }){
            //All Tracks Downloaded
            binding.btnDownloadAll.visibility = View.GONE
            binding.downloadingFab.apply{
                setImageResource(R.drawable.ic_tick)
                visibility = View.VISIBLE
                clearAnimation()
                keepScreenOn = false
            }
        }
    }
    private fun initializeBroadcast() {
        intentFilter = IntentFilter()
        intentFilter?.addAction("track_download_completed")

        updateUIReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                //UI update here
                if (intent != null){
                    val trackDetails = intent.getParcelableExtra<TrackDetails?>("track")
                    trackDetails?.let {
                        val position: Int = viewModel.trackList.value?.map { it.name }?.indexOf(trackDetails.title) ?: -1
                        Log.i("Track","Download Completed Intent :$position")
                        if(position != -1) {
                            val track = viewModel.trackList.value?.get(position)
                            track?.let{
                                it.downloaded = DownloadStatus.Downloaded
                                viewModel.trackList.value?.set(position, it)
                                adapter.notifyItemChanged(position)
                                checkIfAllDownloaded()
                            }
                        }
                    }
                }
            }
        }
        requireActivity().registerReceiver(updateUIReceiver, intentFilter)
    }

    override fun onResume() {
        super.onResume()
        initializeBroadcast()
    }

    override fun onPause() {
        super.onPause()
        requireActivity().unregisterReceiver(updateUIReceiver)
    }
}