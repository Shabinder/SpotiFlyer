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

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.shabinder.spotiflyer.R
import com.shabinder.spotiflyer.SharedViewModel
import com.shabinder.spotiflyer.databinding.YoutubeFragmentBinding
import com.shabinder.spotiflyer.downloadHelper.YTDownloadHelper
import com.shabinder.spotiflyer.models.Track
import com.shabinder.spotiflyer.recyclerView.YoutubeTrackListAdapter
import com.shabinder.spotiflyer.utils.bindImage

class YoutubeFragment : Fragment() {

    private lateinit var binding:YoutubeFragmentBinding
    private lateinit var youtubeViewModel: YoutubeViewModel
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var adapter : YoutubeTrackListAdapter
    private val sampleDomain1 = "youtube.com"
    private val sampleDomain2 = "youtu.be"


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.youtube_fragment,container,false)
        youtubeViewModel = ViewModelProvider(this).get(YoutubeViewModel::class.java)
        sharedViewModel = ViewModelProvider(this.requireActivity()).get(SharedViewModel::class.java)
        adapter = YoutubeTrackListAdapter()
        YTDownloadHelper.context = requireContext()
        YTDownloadHelper.statusBar = binding.StatusBarYoutube
        binding.trackListYoutube.adapter = adapter
        sharedViewModel.ytDownloader.observe(viewLifecycleOwner, Observer {
            youtubeViewModel.ytDownloader = it
        })
        initializeLiveDataObservers()

        val args = YoutubeFragmentArgs.fromBundle(requireArguments())
        val link = args.link
        youtubeSearch(link)
        return binding.root
    }

    private fun youtubeSearch(linkSearch:String) {
        val link = linkSearch.removePrefix("https://").removePrefix("http://")
        if(!link.contains("playlist",true)){
            var searchId = "error"
            if(link.contains(sampleDomain1,true) ){
                searchId =  link.substringAfterLast("=","error")
            }
            if(link.contains(sampleDomain2,true) && !link.contains("playlist",true) ){
                searchId = link.substringAfterLast("/","error")
            }
            if(searchId != "error") {
//                val coverUrl = "https://i.ytimg.com/vi/$searchId/maxresdefault.jpg"
            youtubeViewModel.getYTTrack(searchId)
                binding.btnDownloadAllYoutube.setOnClickListener {
                    //TODO
                }
            }else{showToast("Your Youtube Link is not of a Video!!")}
        }else(showToast("Your Youtube Link is not of a Video!!"))
    }

    private fun initializeLiveDataObservers() {
        /**
         * CoverUrl Binding Observer!
         **/
        youtubeViewModel.coverUrl.observe(viewLifecycleOwner, Observer {
            if(it!="Loading") bindImage(binding.youtubeCoverImage,it)
        })

        /**
         * TrackList Binding Observer!
         **/
        youtubeViewModel.ytTrack.observe(viewLifecycleOwner, Observer {
            val list = mutableListOf<Track>()
            list.add(it)
            adapterConfig(list)
        })

        youtubeViewModel.format.observe(viewLifecycleOwner, Observer {
            adapter.format = it
        })

        /**
         * Title Binding Observer!
         **/
        youtubeViewModel.title.observe(viewLifecycleOwner, Observer {
            binding.titleViewYoutube.text = it
        })

    }

    /**
     * Configure Recycler View Adapter
     **/
    private fun adapterConfig(list:List<Track>){
        adapter.sharedViewModel = sharedViewModel
        adapter.submitList(list)
    }

    /**
     * Util. Function to create toasts!
     **/
    private fun showToast(message:String){
        Toast.makeText(context,message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Util. Function To Check Connection Status
     **/
    private fun isNotOnline(): Boolean {
        val cm =
            requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        return netInfo != null && netInfo.isConnectedOrConnecting
    }
}
