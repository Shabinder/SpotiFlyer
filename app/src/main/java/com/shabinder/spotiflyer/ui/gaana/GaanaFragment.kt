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

package com.shabinder.spotiflyer.ui.gaana

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.shabinder.spotiflyer.downloadHelper.DownloadHelper
import com.shabinder.spotiflyer.models.DownloadStatus
import com.shabinder.spotiflyer.models.spotify.Source
import com.shabinder.spotiflyer.recyclerView.TrackListAdapter
import com.shabinder.spotiflyer.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GaanaFragment : TrackListFragment<GaanaViewModel,GaanaFragmentArgs>() {

    override lateinit var viewModel: GaanaViewModel
    override lateinit var adapter: TrackListAdapter
    override var source: Source = Source.Gaana
    override val args: GaanaFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        viewModel = ViewModelProvider(this).get(GaanaViewModel::class.java)
        adapter = TrackListAdapter(viewModel)

        val gaanaLink = GaanaFragmentArgs.fromBundle(requireArguments()).link.substringAfter("gaana.com/")
        //Link Schema: https://gaana.com/type/link
        val link = gaanaLink.substringAfterLast('/', "error")
        val type = gaanaLink.substringBeforeLast('/', "error").substringAfterLast('/')

        Log.i("Gaana Fragment", "$type : $link")

        when{
            type == "Error" || link == "Error" -> {
                showMessage("Please Check Your Link!")
                Provider.mainActivity.onBackPressed()
            }

            else -> {
                viewModel.gaanaSearch(type,link)

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
                        viewModel.trackList.value?.forEach { urlList.add(it.albumArtURL) }
                        //Appending Source
                        urlList.add("gaana")
                        loadAllImages(
                            requireActivity(),
                            urlList
                        )
                    }
                    viewModel.uiScope.launch {
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
        return binding.root
    }
}