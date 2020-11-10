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

package com.shabinder.spotiflyer.ui.downloadrecord

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayout
import com.shabinder.spotiflyer.databinding.DownloadRecordFragmentBinding
import com.shabinder.spotiflyer.models.spotify.Source
import com.shabinder.spotiflyer.recyclerView.DownloadRecordAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DownloadRecordFragment : Fragment() {

    private lateinit var downloadRecordViewModel: DownloadRecordViewModel
    private lateinit var binding: DownloadRecordFragmentBinding
    private lateinit var adapter: DownloadRecordAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DownloadRecordFragmentBinding.inflate(inflater,container,false)
        downloadRecordViewModel = ViewModelProvider(this).get(DownloadRecordViewModel::class.java)
        adapter = DownloadRecordAdapter()
        binding.downloadRecordList.adapter = adapter

        downloadRecordViewModel.downloadRecordList.observe(viewLifecycleOwner, {
            if(it.isNotEmpty()){
                resetLists()
                for (downloadRecord in it) {
                    when{
                        downloadRecord.link.contains("spotify",true) -> downloadRecordViewModel.spotifyList.add(downloadRecord)
                        downloadRecord.link.contains("gaana",true) -> downloadRecordViewModel.gaanaList.add(downloadRecord)
                        else -> downloadRecordViewModel.ytList.add(downloadRecord)
                    }
                }
                when(binding.tabLayout.selectedTabPosition){
                    0-> adapter.submitList(downloadRecordViewModel.spotifyList,Source.Spotify)
                    1-> adapter.submitList(downloadRecordViewModel.gaanaList,Source.Gaana)
                    2-> adapter.submitList(downloadRecordViewModel.ytList,Source.YouTube)
                }
            }
        })


        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when(tab?.position){
                    0-> adapter.submitList(downloadRecordViewModel.spotifyList,Source.Spotify)
                    1-> adapter.submitList(downloadRecordViewModel.gaanaList,Source.Gaana)
                    2-> adapter.submitList(downloadRecordViewModel.ytList,Source.YouTube)
                }
            }
            override fun onTabReselected(tab: TabLayout.Tab?) {}
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
        })

        return  binding.root
    }

    private fun resetLists() {
        downloadRecordViewModel.spotifyList = mutableListOf()
        downloadRecordViewModel.ytList = mutableListOf()
        downloadRecordViewModel.gaanaList = mutableListOf()
    }

}