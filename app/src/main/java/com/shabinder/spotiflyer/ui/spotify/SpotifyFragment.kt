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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.SimpleItemAnimator
import com.shabinder.spotiflyer.downloadHelper.DownloadHelper
import com.shabinder.spotiflyer.models.DownloadStatus
import com.shabinder.spotiflyer.models.spotify.Source
import com.shabinder.spotiflyer.networking.YoutubeMusicApi
import com.shabinder.spotiflyer.recyclerView.TrackListAdapter
import com.shabinder.spotiflyer.utils.*
import com.shabinder.spotiflyer.utils.Provider.mainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SpotifyFragment : TrackListFragment<SpotifyViewModel,SpotifyFragmentArgs>() {

    @Inject lateinit var youtubeMusicApi: YoutubeMusicApi
    override lateinit var viewModel: SpotifyViewModel
    override lateinit var adapter: TrackListAdapter
    override var source: Source = Source.Spotify
    override val args: SpotifyFragmentArgs by navArgs()

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        initializeAll()

        val spotifyLink = args.link.substringAfter("open.spotify.com/")

        val link = spotifyLink.substringAfterLast('/', "Error").substringBefore('?')
        val type = spotifyLink.substringBeforeLast('/', "Error").substringAfterLast('/')

        Log.i("Spotify Fragment", "$type : $link")


        if(sharedViewModel.spotifyService.value == null){//Authentication pending!!
            if(isOnline()) mainActivity.authenticateSpotify()
        }

        when{
            type == "Error" || link == "Error" -> {
                showMessage("Please Check Your Link!")
                mainActivity.onBackPressed()
            }

            else -> {
                if(type == "episode" || type == "show"){//TODO Implementation
                    showMessage("Implementing Soon, Stay Tuned!")
                }
                else{
                    this.viewModel.spotifySearch(type,link)

                    binding.btnDownloadAll.setOnClickListener {
                        if(!isOnline()){
                            showNoConnectionAlert()
                            return@setOnClickListener
                        }
                        binding.btnDownloadAll.visibility = View.GONE
                        binding.downloadingFab.visibility = View.VISIBLE

                        rotateAnim(binding.downloadingFab)
                        for (track in this.viewModel.trackList.value ?: listOf()){
                            if(track.downloaded != DownloadStatus.Downloaded){
                                track.downloaded = DownloadStatus.Downloading
                                adapter.notifyItemChanged(this.viewModel.trackList.value!!.indexOf(track))
                            }
                        }
                        showMessage("Processing!")
                        sharedViewModel.uiScope.launch(Dispatchers.Default){
                            val urlList = arrayListOf<String>()
                            this@SpotifyFragment.viewModel.trackList.value?.forEach { urlList.add(it.albumArtURL) }
                            //Appending Source
                            urlList.add("spotify")
                            loadAllImages(
                                requireActivity(),
                                urlList
                            )
                        }
                        this.viewModel.uiScope.launch {
                            val finalList = viewModel.trackList.value
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
        }

        return binding.root
    }

    /**
     * Basic Initialization
     **/
    private fun initializeAll() {
        this.viewModel = ViewModelProvider(this).get(SpotifyViewModel::class.java)
        adapter = TrackListAdapter(this.viewModel)
        sharedViewModel.spotifyService.observe(viewLifecycleOwner, {
            this.viewModel.spotifyService = it
        })
        DownloadHelper.youtubeMusicApi = youtubeMusicApi
        DownloadHelper.sharedViewModel = sharedViewModel
        DownloadHelper.statusBar = binding.statusBar
        binding.trackList.adapter = adapter
        (binding.trackList.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
    }
}