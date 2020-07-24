package com.shabinder.musicForEveryone.fragments

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.text.SpannableStringBuilder
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
import com.shabinder.musicForEveryone.models.Track
import com.shabinder.musicForEveryone.recyclerView.TrackListAdapter
import com.shabinder.musicForEveryone.utils.SpotifyService
import com.shabinder.musicForEveryone.utils.bindImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Suppress("DEPRECATION")
class MainFragment : Fragment(),DownloadHelper {
    private lateinit var binding:MainFragmentBinding
    private lateinit var mainViewModel: MainViewModel
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var adapter:TrackListAdapter
    private var spotifyService : SpotifyService? = null
    private var type:String = ""
    private var spotifyLink = ""
    private var i: Intent? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.main_fragment,container,false)

        sharedViewModel = ViewModelProvider(this.requireActivity()).get(SharedViewModel::class.java)
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        spotifyService =  sharedViewModel.spotifyService

        val spanStringBuilder = SpannableStringBuilder()
        spanStringBuilder.append(getText(R.string.d_one)).append("\n")
        spanStringBuilder.append(getText(R.string.d_two)).append("\n")
        spanStringBuilder.append(getText(R.string.d_three)).append("\n")

        binding.usage.text = spanStringBuilder
        openSpotifyButton()

        binding.btnSearch.setOnClickListener {
            sharedViewModel.isConnected.value = isOnline()
            spotifyLink = binding.linkSearch.text.toString()

            val link = spotifyLink.substringAfterLast('/', "Error").substringBefore('?')
            type = spotifyLink.substringBeforeLast('/', "Error").substringAfterLast('/')

            Log.i("Fragment", "$type : $link")

            if (type == "Error" || link == "Error") {
                showToast("Please Check Your Link!")
            } else if(sharedViewModel.isConnected.value == false){
                sharedViewModel.showAlertDialog(resources,requireContext())
            }
            else {
                adapter = TrackListAdapter()
                binding.trackList.adapter = adapter
                adapter.sharedViewModel = sharedViewModel
                adapter.mainFragment = this
                setUiVisibility()

                if(mainViewModel.searchLink == spotifyLink){
                    //it's a Device Configuration Change
                    adapterConfig(mainViewModel.trackList)
                    bindImage(binding.imageView,mainViewModel.coverUrl)
                }else{
                    when (type) {
                        "track" -> {
                            mainViewModel.searchLink = spotifyLink
                            sharedViewModel.uiScope.launch {
                                val trackObject = sharedViewModel.getTrackDetails(link)
                                val trackList = mutableListOf<Track>()
                                trackList.add(trackObject!!)
                                mainViewModel.trackList = trackList
                                mainViewModel.coverUrl = trackObject.album!!.images?.get(0)!!.url!!
                                bindImage(binding.imageView,mainViewModel.coverUrl)
                                adapterConfig(trackList)

                                binding.btnDownloadAll.setOnClickListener {
                                    sharedViewModel.uiScope.launch {
                                        withContext(Dispatchers.IO) {
                                            downloadAllTracks(
                                                trackList,
                                                sharedViewModel.ytDownloader,
                                                sharedViewModel.downloadManager
                                            )
                                        }
                                    }
                                }

                            }
                        }

                        "album" -> {
                            mainViewModel.searchLink = spotifyLink
                            sharedViewModel.uiScope.launch {
                                val albumObject = sharedViewModel.getAlbumDetails(link)
                                val trackList = mutableListOf<Track>()
                                albumObject!!.tracks?.items?.forEach { trackList.add(it!!) }
                                mainViewModel.trackList = trackList
                                mainViewModel.coverUrl = albumObject.images?.get(0)!!.url!!
                                bindImage(binding.imageView,mainViewModel.coverUrl)
                                adapter.isAlbum = true
                                adapterConfig(trackList)
                                binding.btnDownloadAll.setOnClickListener {
                                    sharedViewModel.uiScope.launch {
                                        withContext(Dispatchers.IO) {
                                            downloadAllTracks(
                                                trackList,
                                                sharedViewModel.ytDownloader,
                                                sharedViewModel.downloadManager
                                            )
                                        }
                                    }
                                }
                            }


                        }

                        "playlist" -> {
                            mainViewModel.searchLink = spotifyLink
                            sharedViewModel.uiScope.launch {
                                val playlistObject = sharedViewModel.getPlaylistDetails(link)
                                val trackList = mutableListOf<Track>()
                                playlistObject!!.tracks?.items!!.forEach { trackList.add(it?.track!!) }
                                mainViewModel.trackList = trackList
                                mainViewModel.coverUrl =  playlistObject.images?.get(0)!!.url!!
                                bindImage(binding.imageView,mainViewModel.coverUrl)
                                adapterConfig(trackList)
                                binding.btnDownloadAll.setOnClickListener {
                                    sharedViewModel.uiScope.launch {
                                        withContext(Dispatchers.IO) {
                                            downloadAllTracks(
                                                trackList,
                                                sharedViewModel.ytDownloader,
                                                sharedViewModel.downloadManager
                                            )
                                        }
                                    }
                                }
                            }

                        }

                        "episode" -> {
                            showToast("Implementation Pending")
                        }
                        "show" -> {
                            showToast("Implementation Pending ")
                        }
                    }
                }
            }
        }
        handleIntent()
        if(savedInstanceState != null && binding.linkSearch.text.toString() != ""){
            binding.linkSearch.setText(savedInstanceState["searchLink"].toString())
            binding.btnSearch.performClick()
            setUiVisibility()
        }
        return binding.root
    }

    private fun openSpotifyButton() {
        val manager: PackageManager = requireActivity().packageManager
        try {
            i = manager.getLaunchIntentForPackage("com.spotify.music")
            if (i == null) throw PackageManager.NameNotFoundException()
            i?.addCategory(Intent.CATEGORY_LAUNCHER)
            binding.btnOpenSpotify.setOnClickListener { startActivity(i) }
        } catch (e: PackageManager.NameNotFoundException) {
            binding.textView.text = getString(R.string.spotify_not_installed)
            binding.btnOpenSpotify.text = getString(R.string.spotify_web_link)
            val uri: Uri =
                Uri.parse("http://open.spotify.com")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            binding.btnOpenSpotify.setOnClickListener {
                startActivity(intent)
            }
        }
    }

    /**
     * Configure Recycler View Adapter
     **/
    private fun adapterConfig(trackList: List<Track>){
        adapter.trackList = trackList.toList()
        adapter.totalItems = trackList.size
        adapter.notifyDataSetChanged()

    }

    /**
     * Make Ui elements Visible
     **/
    private fun setUiVisibility() {
        binding.btnDownloadAll.visibility =View.VISIBLE
        binding.titleView.visibility = View.GONE
        binding.openSpotify.visibility = View.GONE
        binding.trackList.visibility = View.VISIBLE
    }

    /**
     * Handle Intent If there is any!
     **/
    private fun handleIntent() {
        binding.linkSearch.setText(sharedViewModel.intentString)
        sharedViewModel.accessToken.observe(viewLifecycleOwner, Observer {
            //Waiting for Authentication to Finish with Spotify
            if (it != ""){
                if(sharedViewModel.intentString != ""){
                    binding.btnSearch.performClick()
                    setUiVisibility()
                }
            }
        })
    }

    /**
     * Util. Function to create toasts!
     **/
    fun showToast(message:String){
        Toast.makeText(context,message,Toast.LENGTH_SHORT).show()
    }
    private fun isOnline(): Boolean {
        val cm =
            requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        return netInfo != null && netInfo.isConnectedOrConnecting
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putCharSequence("searchLink",mainViewModel.searchLink)
    }
}