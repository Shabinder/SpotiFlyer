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
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavArgs
import com.shabinder.spotiflyer.R
import com.shabinder.spotiflyer.SharedViewModel
import com.shabinder.spotiflyer.databinding.TrackListFragmentBinding
import com.shabinder.spotiflyer.models.DownloadStatus
import com.shabinder.spotiflyer.models.TrackDetails
import com.shabinder.spotiflyer.models.spotify.Source
import com.shabinder.spotiflyer.recyclerView.TrackListAdapter
import com.shabinder.spotiflyer.utils.Provider.mainActivity

abstract class TrackListFragment<VM : TrackListViewModel , args: NavArgs> : Fragment() {

    protected lateinit var sharedViewModel: SharedViewModel
    protected lateinit var binding: TrackListFragmentBinding
    protected abstract var viewModel: VM
    protected abstract var adapter: TrackListAdapter
    protected abstract var source: Source
    private var intentFilter: IntentFilter? = null
    private var updateUIReceiver: BroadcastReceiver? = null
    protected abstract val args:NavArgs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(!isOnline()){
            showNoConnectionAlert()
            mainActivity.onBackPressed()
        }
        sharedViewModel = ViewModelProvider(this.requireActivity()).get(SharedViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  TrackListFragmentBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeLiveDataObservers()
    }

    /**
     *Live Data Observers
     **/
    private fun initializeLiveDataObservers() {
        viewModel.trackList.observe(viewLifecycleOwner, {
            if (!it.isNullOrEmpty()){
                Log.i("GaanaFragment","TrackList Updated")
                adapter.submitList(it, source)
                checkIfAllDownloaded()
            }
        })

        viewModel.coverUrl.observe(viewLifecycleOwner, {
            it?.let{bindImage(binding.coverImage,it, source)}
        })

        viewModel.title.observe(viewLifecycleOwner, {
            binding.titleView.text = it
        })
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
                        val position: Int = viewModel.trackList.value?.map { it.title }?.indexOf(trackDetails.title) ?: -1
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

    private fun checkIfAllDownloaded() {
        if(!viewModel.trackList.value!!.any { it.downloaded != DownloadStatus.Downloaded }){
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