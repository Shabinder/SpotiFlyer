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

package com.shabinder.spotiflyer.ui.youtube

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.kiulian.downloader.YoutubeDownloader
import com.shabinder.spotiflyer.R
import com.shabinder.spotiflyer.SharedViewModel
import com.shabinder.spotiflyer.databinding.YoutubeFragmentBinding
import com.shabinder.spotiflyer.downloadHelper.YTDownloadHelper
import com.shabinder.spotiflyer.models.DownloadStatus
import com.shabinder.spotiflyer.models.Source
import com.shabinder.spotiflyer.models.TrackDetails
import com.shabinder.spotiflyer.recyclerView.YoutubeTrackListAdapter
import com.shabinder.spotiflyer.utils.bindImage
import com.shabinder.spotiflyer.utils.loadAllImages
import com.shabinder.spotiflyer.utils.rotateAnim
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class YoutubeFragment : Fragment() {

    private lateinit var binding:YoutubeFragmentBinding
    private lateinit var youtubeViewModel: YoutubeViewModel
    private lateinit var sharedViewModel: SharedViewModel
    @Inject lateinit var ytDownloader: YoutubeDownloader
    private lateinit var adapter : YoutubeTrackListAdapter
    private val sampleDomain1 = "youtube.com"
    private val sampleDomain2 = "youtu.be"
    private var intentFilter: IntentFilter? = null
    private var updateUIReceiver: BroadcastReceiver? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.youtube_fragment,container,false)
        youtubeViewModel = ViewModelProvider(this).get(YoutubeViewModel::class.java)
        sharedViewModel = ViewModelProvider(this.requireActivity()).get(SharedViewModel::class.java)
        adapter = YoutubeTrackListAdapter(youtubeViewModel)
        binding.trackList.adapter = adapter

        initializeLiveDataObservers()
        initializeBroadcast()

        val args = YoutubeFragmentArgs.fromBundle(requireArguments())
        val link = args.link
        youtubeSearch(link)
        return binding.root
    }

    private fun youtubeSearch(linkSearch:String) {
        val link = linkSearch.removePrefix("https://").removePrefix("http://")
        if(link.contains("playlist",true) || link.contains("list",true)){
            // Given Link is of a Playlist
            val playlistId = link.substringAfter("?list=").substringAfter("&list=").substringBefore("&")
            youtubeViewModel.getYTPlaylist(playlistId,ytDownloader)
        }else{//Given Link is of a Video
            var searchId = "error"
            if(link.contains(sampleDomain1,true) ){
                searchId =  link.substringAfterLast("=","error")
            }
            if(link.contains(sampleDomain2,true) && !link.contains("playlist",true) ){
                searchId = link.substringAfterLast("/","error")
            }
            if(searchId != "error") {
                youtubeViewModel.getYTTrack(searchId,ytDownloader)
            }else{showToast("Your Youtube Link is not of a Video!!")}
        }

        /*
        * Download All Tracks
        * */
        binding.btnDownloadAll.setOnClickListener {
            binding.btnDownloadAll.visibility = View.GONE
            binding.downloadingFab.visibility = View.VISIBLE

            rotateAnim(binding.downloadingFab)

            for (track in youtubeViewModel.ytTrackList.value?: listOf()){
                if(track.downloaded != DownloadStatus.Downloaded){
                    track.downloaded = DownloadStatus.Downloading
                    adapter.notifyItemChanged(youtubeViewModel.ytTrackList.value!!.indexOf(track))
                }
            }
            showToast("Processing!")
            sharedViewModel.uiScope.launch(Dispatchers.Default){
                val urlList = arrayListOf<String>()
                youtubeViewModel.ytTrackList.value?.forEach { urlList.add("https://i.ytimg.com/vi/${it.albumArt.absolutePath.substringAfterLast("/")
                    .substringBeforeLast(".")}/hqdefault.jpg")}
                //Appending Source
                urlList.add("youtube")
                loadAllImages(
                    requireActivity(),
                    urlList
                )
            }
            youtubeViewModel.uiScope.launch {
                YTDownloadHelper.downloadYTTracks(
                    type = youtubeViewModel.folderType,
                    subFolder = youtubeViewModel.subFolder,
                    tracks =  youtubeViewModel.ytTrackList.value ?: listOf()
                )
            }
        }
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
                    val trackDetails = intent.getParcelableExtra<TrackDetails?>("track")
                    trackDetails?.let {
                        val position: Int = youtubeViewModel.ytTrackList.value?.map { it.title }?.indexOf(trackDetails.title) ?: -1
                        Log.i("Track","Download Completed Intent :$position")
                        if(position != -1) {
                            val track = youtubeViewModel.ytTrackList.value?.get(position)
                            track?.let{
                                it.downloaded = DownloadStatus.Downloaded
                                youtubeViewModel.ytTrackList.value?.set(position, it)
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

    override fun onPause() {
        super.onPause()
        requireActivity().unregisterReceiver(updateUIReceiver)
    }

    private fun checkIfAllDownloaded() {
        if(!youtubeViewModel.ytTrackList.value!!.any { it.downloaded != DownloadStatus.Downloaded }){
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
    private fun initializeLiveDataObservers() {
        /**
         * CoverUrl Binding Observer!
         **/
        youtubeViewModel.coverUrl.observe(viewLifecycleOwner, {
            if(it!="Loading") bindImage(binding.coverImage,it,Source.YouTube)
        })

        /**
         * TrackList Binding Observer!
         **/
        youtubeViewModel.ytTrackList.observe(viewLifecycleOwner, {
            adapterConfig(it)
        })

        /**
         * Title Binding Observer!
         **/
        youtubeViewModel.title.observe(viewLifecycleOwner, {
            binding.titleView.text = it
        })

    }

    /**
     * Configure Recycler View Adapter
     **/
    private fun adapterConfig(list:List<TrackDetails>){
        adapter.submitList(list)
    }

    /**
     * Util. Function to create toasts!
     **/
    private fun showToast(message:String){
        Toast.makeText(context,message, Toast.LENGTH_SHORT).show()
    }

}
