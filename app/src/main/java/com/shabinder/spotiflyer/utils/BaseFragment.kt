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

package com.shabinder.spotiflyer.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.kiulian.downloader.YoutubeDownloader
import com.shabinder.spotiflyer.R
import com.shabinder.spotiflyer.SharedViewModel
import com.shabinder.spotiflyer.databinding.TrackListFragmentBinding
import com.shabinder.spotiflyer.models.DownloadStatus
import com.shabinder.spotiflyer.models.TrackDetails
import com.shabinder.spotiflyer.models.spotify.Source
import com.shabinder.spotiflyer.networking.GaanaInterface
import com.shabinder.spotiflyer.networking.YoutubeMusicApi
import com.shabinder.spotiflyer.recyclerView.TrackListAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
abstract class BaseFragment : Fragment() {

    @Inject open lateinit var youtubeMusicApi: YoutubeMusicApi
    @Inject open lateinit var ytDownloader: YoutubeDownloader
    @Inject open lateinit var gaanaInterface: GaanaInterface
    open lateinit var sharedViewModel: SharedViewModel
    open lateinit var binding: TrackListFragmentBinding
    abstract var baseViewModel: BaseViewModel
    abstract var adapter: TrackListAdapter
    abstract var source: Source
    private var intentFilter: IntentFilter? = null
    private var updateUIReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedViewModel = ViewModelProvider(this.requireActivity()).get(SharedViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  DataBindingUtil.inflate(inflater,R.layout.track_list_fragment, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeLiveDataObservers()
    }
    /**
     *Live Data Observers
     **/
    open fun initializeLiveDataObservers() {
        baseViewModel.trackList.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()){
                Log.i("GaanaFragment","TrackList Updated")
                adapter.submitList(it, source)
                checkIfAllDownloaded()
            }
        })

        baseViewModel.coverUrl.observe(viewLifecycleOwner, {
            if(it!="Loading") bindImage(binding.coverImage,it, source)
        })

        baseViewModel.title.observe(viewLifecycleOwner, {
            binding.titleView.text = it
        })
    }

    open fun initializeBroadcast() {
        intentFilter = IntentFilter()
        intentFilter?.addAction("track_download_completed")

        updateUIReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                //UI update here
                if (intent != null){
                    val trackDetails = intent.getParcelableExtra<TrackDetails?>("track")
                    trackDetails?.let {
                        val position: Int = baseViewModel.trackList.value?.map { it.title }?.indexOf(trackDetails.title) ?: -1
                        Log.i("Track","Download Completed Intent :$position")
                        if(position != -1) {
                            val track = baseViewModel.trackList.value?.get(position)
                            track?.let{
                                it.downloaded = DownloadStatus.Downloaded
                                baseViewModel.trackList.value?.set(position, it)
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

    open fun checkIfAllDownloaded() {
        if(!baseViewModel.trackList.value!!.any { it.downloaded != DownloadStatus.Downloaded }){
            //All Tracks Downloaded
            binding.btnDownloadAll.visibility = View.GONE
            binding.downloadingFab.apply{
                setImageResource(R.drawable.ic_tick)
                visibility = View.VISIBLE
                clearAnimation()
            }
        }
    }

}