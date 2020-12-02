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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.navArgs
import com.shabinder.spotiflyer.models.DownloadStatus
import com.shabinder.spotiflyer.models.TrackDetails
import com.shabinder.spotiflyer.models.spotify.Source
import com.shabinder.spotiflyer.recyclerView.TrackListAdapter
import com.shabinder.spotiflyer.ui.base.tracklistbase.TrackListFragment
import com.shabinder.spotiflyer.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val sampleDomain2 = "youtu.be"
private const val sampleDomain1 = "youtube.com"

@AndroidEntryPoint
class YoutubeFragment : TrackListFragment<YoutubeViewModel, YoutubeFragmentArgs>(){

    override val viewModel: YoutubeViewModel by viewModels()
    override lateinit var adapter : TrackListAdapter
    override var source: Source = Source.YouTube
    override val args: YoutubeFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        adapter = TrackListAdapter(viewModel)

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
            viewModel.getYTPlaylist(playlistId)
        }else{//Given Link is of a Video
            var searchId = "error"
            if(link.contains(sampleDomain1,true) ){
                searchId =  link.substringAfterLast("=","error")
            }
            if(link.contains(sampleDomain2,true) ){
                searchId = link.substringAfterLast("/","error")
            }
            if(searchId != "error") {
                this.viewModel.getYTTrack(searchId)
            }else{showMessage("Your Youtube Link is not of a Video!!")}
        }

        /*
        * Download All Tracks
        * */
        binding.btnDownloadAll.setOnClickListener {
            if(!isOnline()){
                showNoConnectionAlert()
                return@setOnClickListener
            }
            binding.btnDownloadAll.gone()
            binding.downloadingFab.apply{
                visible()
                rotate()
            }
            showMessage("Processing!")
            sharedViewModel.viewModelScope.launch(Dispatchers.Default){
                loadAllImages(requireActivity(), viewModel.trackList.value?.map{it.albumArtURL}, Source.YouTube)
            }
            viewModel.viewModelScope.launch {
                downloadTracks((viewModel.trackList.value?.filter { it.downloaded == DownloadStatus.NotDownloaded } ?: arrayListOf()) as ArrayList<TrackDetails>)
                for (track in viewModel.trackList.value?: listOf()){
                    if(track.downloaded == DownloadStatus.NotDownloaded){
                        track.downloaded = DownloadStatus.Queued
                        //adapter.notifyItemChanged(viewModel.trackList.value!!.indexOf(track))
                    }
                }
                adapter.notifyDataSetChanged()
            }
        }
    }
}
