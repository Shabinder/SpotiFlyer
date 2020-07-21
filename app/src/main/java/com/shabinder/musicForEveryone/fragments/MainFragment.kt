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


class MainFragment : Fragment(),DownloadHelper {
    lateinit var binding:MainFragmentBinding
    private lateinit var sharedViewModel: SharedViewModel
    var spotify : SpotifyService? = null
    var type:String = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.main_fragment,container,false)

        sharedViewModel = ViewModelProvider(this.requireActivity()).get(SharedViewModel::class.java)
        spotify =  sharedViewModel.spotify
        sharedViewModel.userName.observe(viewLifecycleOwner, Observer {
            binding.message.text = it
//            if(it!="Placeholder"){Snackbar.make(requireView(),"Hello, $it!", Snackbar.LENGTH_SHORT).show()}
        })

        binding.btnSearch.setOnClickListener {
            val spotifyLink = binding.spotifyLink.text.toString()

            val link = spotifyLink.substringAfterLast('/' , "Error").substringBefore('?')
            type = spotifyLink.substringBeforeLast('/' , "Error").substringAfterLast('/')

            Log.i("Fragment", "$type : $link")

            val adapter = TrackListAdapter()
            binding.trackList.adapter = adapter
            adapter.sharedViewModel  = sharedViewModel

            when(type){
                "track" -> {
                    val trackObject = sharedViewModel.getTrackDetails(link)

                    val trackList = mutableListOf<Track>()
                    trackList.add(trackObject!!)
                    adapter.totalItems = 1
                    adapter.trackList = trackList
                    adapter.notifyDataSetChanged()

                    Log.i("Adapter",trackList.size.toString())
                }

                "album" -> {
                    val albumObject = sharedViewModel.getAlbumDetails(link)

                    bindImage(binding.imageView,albumObject!!.images[1].url)
                    binding.titleView.text = albumObject.name
                    binding.titleView.visibility =View.VISIBLE
                    binding.imageView.visibility =View.VISIBLE

                    val trackList = mutableListOf<Track>()
                    albumObject.tracks?.items?.forEach { trackList.add(it as Track) }
                    adapter.totalItems = trackList.size
                    adapter.trackList = trackList
                    adapter.notifyDataSetChanged()

                    Log.i("Adapter",trackList.size.toString())

                }

                "playlist" -> {
                    val playlistObject = sharedViewModel.getPlaylistDetails(link)

                    bindImage(binding.imageView,playlistObject!!.images[0].url)
                    binding.titleView.text = playlistObject.name
                    binding.titleView.visibility =View.VISIBLE
                    binding.imageView.visibility =View.VISIBLE

                    val trackList = mutableListOf<Track>()
                    playlistObject.tracks?.items!!.forEach { trackList.add(it.track) }
                    adapter.trackList = trackList.toList()
                    adapter.totalItems = trackList.size
                    adapter.notifyDataSetChanged()
                    Log.i("Adapter",trackList.size.toString())
                }

                "episode" -> {showToast("Implementation Pending")}
                "show" -> {showToast("Implementation Pending ")}
            }

            binding.spotifyLink.setText(link)
        }
        return binding.root
    }

    private fun showToast(message:String){
        Toast.makeText(context,message,Toast.LENGTH_SHORT).show()
    }
}