package com.shabinder.musicForEveryone.fragments

import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.github.kiulian.downloader.model.formats.Format
import com.github.kiulian.downloader.model.quality.AudioQuality
import com.google.android.material.snackbar.Snackbar
import com.shabinder.musicForEveryone.R
import com.shabinder.musicForEveryone.SharedViewModel
import com.shabinder.musicForEveryone.bindImage
import com.shabinder.musicForEveryone.databinding.MainFragmentBinding
import com.shabinder.musicForEveryone.utils.YoutubeConnector
import kaaes.spotify.webapi.android.SpotifyService
import java.io.File


class MainFragment : Fragment() {
    lateinit var binding:MainFragmentBinding
    private lateinit var sharedViewModel: SharedViewModel
    var spotify : SpotifyService? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.main_fragment,container,false)

        sharedViewModel = ViewModelProvider(this.requireActivity()).get(SharedViewModel::class.java)
        spotify =  sharedViewModel.spotify
        sharedViewModel.userName.observe(viewLifecycleOwner, Observer {
            binding.message.text = it
            if(it!="Placeholder"){Snackbar.make(requireView(),"Hello, $it!", Snackbar.LENGTH_SHORT).show()}
            })

        binding.btnGetDetails.setOnClickListener {
            val spotifyLink = binding.spotifyLink.text.toString()

            val link = spotifyLink.substringAfterLast('/' , "Error").substringBefore('?')
            val type = spotifyLink.substringBeforeLast('/' , "Error").substringAfterLast('/')

            Log.i("Fragment",link)

            when(type){
                "track" -> {
                    val trackObject = sharedViewModel.getTrackDetails(link)
                    Log.i("Fragment",trackObject?.name.toString())
                    binding.name.text = trackObject?.name ?: "Error"
                    var artistNames = ""
                    trackObject?.artists?.forEach { artistNames = artistNames.plus("${it.name} ,") }
                    binding.artist.text = artistNames
                    binding.popularity.text = trackObject?.popularity.toString()
                    binding.duration.text = ((trackObject?.duration_ms!! /1000/60).toString() + "minutes")
                    binding.albumName.text = trackObject.album.name
                    bindImage(binding.imageUrl, trackObject.album.images[0].url)
                }

                "album" -> {
                    val albumObject = sharedViewModel.getAlbumDetails(link)
                    Log.i("Fragment",albumObject?.name.toString())
                    binding.name.text = albumObject?.name ?: "Error"
                    var artistNames = ""
                    albumObject?.artists?.forEach { artistNames = artistNames.plus(", ${it.name}") }
                    binding.artist.text = artistNames
                    binding.popularity.text = albumObject?.popularity.toString()
                    binding.duration.visibility = View.GONE
                    binding.textView5.visibility = View.GONE
                    binding.albumName.text = albumObject?.name
                    bindImage(binding.imageUrl, albumObject?.images?.get(0)?.url)
                }

                "playlist" -> {
                    val playlistObject = sharedViewModel.getPlaylistDetails(link)
                    Log.i("Fragment",playlistObject?.name.toString())
                    binding.name.text = playlistObject?.name ?: "Error"
                    binding.artist.visibility = View.GONE
                    binding.textView1.visibility = View.GONE
                    binding.popularity.text = playlistObject?.followers?.total.toString()
                    binding.textview3.text = "Followers"
                    binding.duration.visibility = View.GONE
                    binding.textView5.visibility = View.GONE
                    binding.textView7.text = "Total Tracks"
                    binding.albumName.text = playlistObject?.tracks?.items?.size.toString()
                    bindImage(binding.imageUrl, playlistObject?.images?.get(0)?.url)
                }

                "episode" -> {showToast("Implementation Pending")}
                "show" -> {showToast("Implementation Pending ")}
            }

            binding.spotifyLink.setText(link)
        }

        binding.btnDownload.setOnClickListener {
           val data = YoutubeConnector.search(
               "${binding.name.text} ${if(binding.artist.text != "TextView" ){binding.artist.text}else{""}}")
               ?.get(0)
            if (data==null){showToast("Youtube Request Failed!")}else{
                val ytDownloader = sharedViewModel.ytDownloader
                val video = ytDownloader?.getVideo(data.id)
                val details = video?.details()

                val outputDir = File(Environment.getExternalStorageDirectory().toString() + File.separator + "MyAudio")
                val format:Format = video?.findAudioWithQuality(AudioQuality.low)?.get(0) as Format
                val audioUrl = format.url()
                if (audioUrl != null) {
                    Log.i("ytDownloader", audioUrl)
                }else{Log.i("YT audio url is null", format.toString())}

                val file:File = video.download( format , outputDir)

                Log.i("YT File Path=> ", file.path)

                Log.i("ytDownloader", details?.title()?:"Error")
                binding.name.text = details?.title()
            }
        }

        return binding.root
    }

    private fun showToast(message:String){
        Toast.makeText(context,message,Toast.LENGTH_SHORT).show()
    }
}