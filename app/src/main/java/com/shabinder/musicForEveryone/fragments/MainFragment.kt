package com.shabinder.musicForEveryone.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.shabinder.musicForEveryone.R
import com.shabinder.musicForEveryone.SharedViewModel
import com.shabinder.musicForEveryone.databinding.MainFragmentBinding
import com.shabinder.musicForEveryone.downloadHelper.DownloadHelper
import com.shabinder.musicForEveryone.recyclerView.TrackListAdapter
import com.shabinder.musicForEveryone.utils.bindImage
import kaaes.spotify.webapi.android.SpotifyService
import kaaes.spotify.webapi.android.models.Track
import kotlinx.coroutines.launch


class MainFragment : Fragment(),DownloadHelper {
    lateinit var binding:MainFragmentBinding
    private lateinit var sharedViewModel: SharedViewModel
    var spotify : SpotifyService? = null
    var type:String = ""
    var spotifyLink = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.main_fragment,container,false)

        sharedViewModel = ViewModelProvider(this.requireActivity()).get(SharedViewModel::class.java)
        spotify =  sharedViewModel.spotify

        binding.btnSearch.setOnClickListener {
            spotifyLink = binding.spotifyLink.text.toString()

            val link = spotifyLink.substringAfterLast('/' , "Error").substringBefore('?')
            type = spotifyLink.substringBeforeLast('/' , "Error").substringAfterLast('/')

            Log.i("Fragment", "$type : $link")

            val adapter = TrackListAdapter()
            binding.trackList.adapter = adapter
            adapter.sharedViewModel  = sharedViewModel
            when(type){
                "track" -> {
                    sharedViewModel.uiScope.launch{
                        val trackObject = sharedViewModel.getTrackDetails(link)

                        binding.imageView.visibility =View.VISIBLE

                        val trackList = mutableListOf<Track>()
                        trackList.add(trackObject!!)
                        bindImage(binding.imageView, trackObject.album.images[0].url)
                        adapter.totalItems = 1
                        adapter.trackList = trackList
                        adapter.notifyDataSetChanged()
                        Log.i("Adapter",trackList.size.toString())
                        binding.btnDownloadAll.setOnClickListener { downloadAllTracks(trackList) }
                    }
                }

                "album" -> {
                    sharedViewModel.uiScope.launch{

                        val albumObject = sharedViewModel.getAlbumDetails(link)
//                        binding.titleView.text = albumObject!!.name
//                        binding.titleView.visibility =View.VISIBLE
                        binding.imageView.visibility =View.VISIBLE
                        binding.btnDownloadAll.visibility =View.VISIBLE
                        val trackList = mutableListOf<Track>()
                        albumObject!!.tracks?.items?.forEach { trackList.add(it as Track) }
                        adapter.totalItems = trackList.size
                        adapter.trackList = trackList
                        adapter.notifyDataSetChanged()

                        Log.i("Adapter",trackList.size.toString())

                        bindImage(binding.imageView, albumObject.images[0].url)
                        binding.btnDownloadAll.setOnClickListener { downloadAllTracks(trackList) }

                    }


                }

                "playlist" -> {
                    sharedViewModel.uiScope.launch{
                        val playlistObject = sharedViewModel.getPlaylistDetails(link)
                        binding.btnDownloadAll.visibility =View.VISIBLE


                        binding.imageView.visibility =View.VISIBLE
//                        binding.titleView.text = "${if(playlistObject!!.name.length > 18){"${playlistObject.name.subSequence(0,17)}..."}else{playlistObject.name}}"
//                        binding.titleView.visibility =View.VISIBLE
//                        binding.playlistOwner.visibility =View.VISIBLE
//                        binding.playlistOwner.text = "by: ${playlistObject.owner.display_name}"
                        val trackList = mutableListOf<Track>()
                        playlistObject!!.tracks?.items!!.forEach { trackList.add(it.track) }
                        adapter.trackList = trackList.toList()
                        adapter.totalItems = trackList.size
                        adapter.notifyDataSetChanged()

                        Log.i("Adapter",trackList.size.toString())

                        bindImage(binding.imageView, playlistObject.images[0].url)

                        binding.btnDownloadAll.setOnClickListener { downloadAllTracks(trackList) }

                    }

                    }


                "episode" -> {showToast("Implementation Pending")}
                "show" -> {showToast("Implementation Pending ")}
            }
        }
        binding.spotifyLink.setText(sharedViewModel.intentString)
        sharedViewModel.userName.observe(viewLifecycleOwner, Observer {
            //Waiting for Authentication to Finish with Spotify
            if (it != "Placeholder"){
                if(sharedViewModel.intentString != ""){binding.btnSearch.performClick()}
            }
        })

        return binding.root
    }

    private fun downloadAllTracks(trackList : List<Track>) {
        sharedViewModel.uiScope.launch {
            trackList.forEach { downloadTrack(sharedViewModel.ytDownloader,sharedViewModel.downloadManager,"${it.name} ${it.artists[0].name?:""}") }
        }
    }

    private fun showToast(message:String){
        Toast.makeText(context,message,Toast.LENGTH_SHORT).show()
    }
}