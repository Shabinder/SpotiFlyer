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
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.navArgs
import com.shabinder.spotiflyer.models.DownloadStatus
import com.shabinder.spotiflyer.models.TrackDetails
import com.shabinder.spotiflyer.models.spotify.Source
import com.shabinder.spotiflyer.networking.SpotifyService
import com.shabinder.spotiflyer.recyclerView.TrackListAdapter
import com.shabinder.spotiflyer.ui.base.tracklistbase.TrackListFragment
import com.shabinder.spotiflyer.utils.*
import com.shabinder.spotiflyer.utils.Provider.mainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SpotifyFragment : TrackListFragment<SpotifyViewModel, SpotifyFragmentArgs>() {

    override val viewModel: SpotifyViewModel by viewModels()
    override val args: SpotifyFragmentArgs by navArgs()
    override lateinit var adapter: TrackListAdapter
    override var source: Source = Source.Spotify
    private lateinit var link:String
    private lateinit var type:String
    private val spotifyService:SpotifyService?
        get() = sharedViewModel.spotifyService.value

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        initializeAll()

        var spotifyLink = "https://" + args.link.substringAfterLast("https://").substringBefore(" ").trim()
        Log.i("Spotify Fragment Link", spotifyLink)
        viewModelScope.launch(Dispatchers.IO) {

            /*
            * New Link Schema: https://link.tospotify.com/kqTBblrjQbb,
            * Fetching Standard Link: https://open.spotify.com/playlist/37i9dQZF1DX9RwfGbeGQwP?si=iWz7B1tETiunDntnDo3lSQ&amp;_branch_match_id=862039436205270630
            * */
            if (!spotifyLink.contains("open.spotify")) {
                val resolvedLink = viewModel.resolveLink(spotifyLink)
                Log.d("Spotify Resolved Link", resolvedLink)
                spotifyLink = resolvedLink
            }

            link = spotifyLink.substringAfterLast('/', "Error").substringBefore('?')
            type = spotifyLink.substringBeforeLast('/', "Error").substringAfterLast('/')

            Log.i("Spotify Fragment", "$type : $link")

            if (sharedViewModel.spotifyService.value == null) {//Authentication pending!!
                if (isOnline()) mainActivity.authenticateSpotify()
            }

            when {
                type == "Error" || link == "Error" -> {
                    showMessage("Please Check Your Link!")
                    mainActivity.onBackPressed()
                }

                else -> {
                    if (type == "episode" || type == "show") {//TODO Implementation
                        showMessage("Implementing Soon, Stay Tuned!")
                    } else {
                        viewModel.spotifySearch(type, link)

                        binding.btnDownloadAll.setOnClickListener {
                            if (!isOnline()) {
                                showNoConnectionAlert()
                                return@setOnClickListener
                            }
                            binding.btnDownloadAll.gone()
                            binding.downloadingFab.apply {
                                visible()
                                rotate()
                            }
                            showMessage("Processing!")
                            sharedViewModel.viewModelScope.launch(Dispatchers.Default) {
                                loadAllImages(
                                    requireActivity(),
                                    viewModel.trackList.value?.map { it.albumArtURL },
                                    Source.Spotify
                                )
                            }
                            viewModelScope.launch {
                                val finalList = viewModel.trackList.value?.filter{it.downloaded == DownloadStatus.NotDownloaded}
                                if (finalList.isNullOrEmpty()) showMessage("Not Downloading Any Song")
                                else downloadTracks(finalList as ArrayList<TrackDetails>)
                                for (track in viewModel.trackList.value ?: listOf()) {
                                    if (track.downloaded == DownloadStatus.NotDownloaded) {
                                        track.downloaded = DownloadStatus.Queued
                                        //adapter.notifyItemChanged(viewModel.trackList.value!!.indexOf(track))
                                    }
                                }
                                adapter.notifyDataSetChanged()
                            }
                        }
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
        sharedViewModel.spotifyService.observe(viewLifecycleOwner, {
            this.viewModel.spotifyService = it
        })
        viewModel.spotifyService = spotifyService //Temp Initialisation
        adapter = TrackListAdapter(this.viewModel)
    }
}