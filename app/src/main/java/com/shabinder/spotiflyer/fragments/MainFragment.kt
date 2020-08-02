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

package com.shabinder.spotiflyer.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.ValueCallback
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.shabinder.spotiflyer.MainActivity
import com.shabinder.spotiflyer.R
import com.shabinder.spotiflyer.SharedViewModel
import com.shabinder.spotiflyer.databinding.MainFragmentBinding
import com.shabinder.spotiflyer.downloadHelper.DownloadHelper
import com.shabinder.spotiflyer.downloadHelper.DownloadHelper.applyWebViewSettings
import com.shabinder.spotiflyer.downloadHelper.DownloadHelper.downloadAllTracks
import com.shabinder.spotiflyer.models.Track
import com.shabinder.spotiflyer.recyclerView.TrackListAdapter
import com.shabinder.spotiflyer.utils.SpotifyService
import com.shabinder.spotiflyer.utils.bindImage
import com.shabinder.spotiflyer.utils.copyTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

@Suppress("DEPRECATION")
class MainFragment : Fragment() {
    private lateinit var binding:MainFragmentBinding
    private lateinit var mainViewModel: MainViewModel
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var adapter:TrackListAdapter
    private var spotifyService : SpotifyService? = null
    private var type:String = ""
    private var spotifyLink = ""
    private var i: Intent? = null
    private var webView: WebView? = null


    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.main_fragment,container,false)
        webView = binding.webView
        DownloadHelper.webView = binding.webView
        DownloadHelper.context = requireContext()
        sharedViewModel = ViewModelProvider(this.requireActivity()).get(SharedViewModel::class.java)
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        spotifyService =  sharedViewModel.spotifyService
        DownloadHelper.sharedViewModel = sharedViewModel
        DownloadHelper.statusBar = binding.StatusBar

        setUpUsageText()
        openSpotifyButton()
        openGithubButton()
        openInstaButton()

        binding.btnDonate.setOnClickListener {
            sharedViewModel.easyUpiPayment?.startPayment()
        }

        binding.btnSearch.setOnClickListener {
            val link = binding.linkSearch.text.toString()
            if(link.contains("open.spotify",true)){
                spotifySearch()
            }
            if(link.contains("youtube.com",true) || link.contains("youtu.be",true) ){
                youtubeSearch()
            }

        }
        handleIntent()
        //Handling Device Configuration Change
        if(savedInstanceState != null && savedInstanceState["searchLink"].toString() != ""){
            binding.linkSearch.setText(savedInstanceState["searchLink"].toString())
            binding.btnSearch.performClick()
            setUiVisibility()
        }
        return binding.root
    }

    private fun youtubeSearch() {
        val youtubeLink = binding.linkSearch.text.toString()
        var title = ""
        val link = youtubeLink.removePrefix("https://").removePrefix("http://")
        val sampleDomain1 = "youtube.com"
        val sampleDomain2 = "youtu.be"
        if(!link.contains("playlist",true)){
            var searchId = "error"
            if(link.contains(sampleDomain1,true) ){
                searchId =  link.substringAfterLast("=","error")
            }
            if(link.contains(sampleDomain2,true) && !link.contains("playlist",true) ){
                searchId = link.substringAfterLast("/","error")
            }
            if(searchId != "error"){
                val coverLink = "https://i.ytimg.com/vi/$searchId/maxresdefault.jpg"
                applyWebViewSettings(webView!!)
                sharedViewModel.uiScope.launch {
                    webView!!.loadUrl(youtubeLink)
                    webView!!.webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            view?.evaluateJavascript(
                                "document.getElementsByTagName(\"h1\")[0].textContent"
                                ,object : ValueCallback<String> {
                                    override fun onReceiveValue(value: String?) {
                                        title = DownloadHelper.removeIllegalChars(value.toString()).toString()
                                        Log.i("YT-id", title)
                                        Log.i("YT-id", value)
                                        Log.i("YT-id", coverLink)
                                        setUiVisibility()
                                        bindImage(binding.imageView,coverLink)
                                        binding.btnDownloadAll.setOnClickListener {
                                            showToast("Starting Download in Few Seconds")
                                            //TODO Clean This Code!
                                            DownloadHelper.downloadFile(null,"YT_Downloads",Track(name = value,ytCoverUrl = coverLink),0,sharedViewModel.ytDownloader,searchId)
                                        }
                                    }
                                })
                            }
                        }
                }
            }
        }else(showToast("Your Youtube Link is not of a Video!!"))
    }

    private fun spotifySearch(){
        spotifyLink = binding.linkSearch.text.toString()

        val link = spotifyLink.substringAfterLast('/', "Error").substringBefore('?')
        type = spotifyLink.substringBeforeLast('/', "Error").substringAfterLast('/')

        Log.i("Fragment", "$type : $link")

        if(sharedViewModel.spotifyService == null && !isOnline()){
            (activity as MainActivity).authenticateSpotify()
        }

        if (type == "Error" || link == "Error") {
            showToast("Please Check Your Link!")
        } else if(!isOnline()){
            sharedViewModel.showAlertDialog(resources,requireContext())
        } else {
            adapter = TrackListAdapter()
            binding.trackList.adapter = adapter
            adapter.sharedViewModel = sharedViewModel
            adapter.mainFragment = this
            setUiVisibility()

            if(mainViewModel.searchLink == spotifyLink){
                //it's a Device Configuration Change
                adapterConfig(mainViewModel.trackList)
                sharedViewModel.uiScope.launch {
                    bindImage(binding.imageView,mainViewModel.coverUrl)
                }
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
                                showToast("Starting Download in Few Seconds")
                                sharedViewModel.uiScope.launch {
                                    downloadAllTracks(
                                        "Tracks",
                                        null,
                                        trackList,
                                        sharedViewModel.ytDownloader
                                    )
                                }
                            }

                        }
                    }

                    "album" -> {
                        mainViewModel.searchLink = spotifyLink
                        sharedViewModel.uiScope.launch {
                            val albumObject = sharedViewModel.getAlbumDetails(link)
                            val trackList = mutableListOf<Track>()
                            albumObject!!.tracks?.items?.forEach { trackList.add(it) }
                            mainViewModel.trackList = trackList
                            mainViewModel.coverUrl = albumObject.images?.get(0)!!.url!!
                            bindImage(binding.imageView,mainViewModel.coverUrl)
                            adapter.isAlbum = true
                            adapterConfig(trackList)
                            binding.btnDownloadAll.setOnClickListener {
                                showToast("Starting Download in Few Seconds")
                                sharedViewModel.uiScope.launch {
                                    loadAllImages(trackList)
                                    downloadAllTracks(
                                        "Albums",
                                        albumObject.name,
                                        trackList,
                                        sharedViewModel.ytDownloader
                                    )
                                }
                            }
                        }


                    }

                    "playlist" -> {
                        mainViewModel.searchLink = spotifyLink
                        sharedViewModel.uiScope.launch {
                            val playlistObject = sharedViewModel.getPlaylistDetails(link)
                            val trackList = mutableListOf<Track>()
                            playlistObject!!.tracks?.items!!.forEach { trackList.add(it.track!!) }
                            mainViewModel.trackList = trackList
                            mainViewModel.coverUrl =  playlistObject.images?.get(0)!!.url!!
                            bindImage(binding.imageView,mainViewModel.coverUrl)
                            adapterConfig(trackList)
                            binding.btnDownloadAll.setOnClickListener {
                                showToast("Starting Download in Few Seconds")
                                sharedViewModel.uiScope.launch {
                                    loadAllImages(trackList)
                                    downloadAllTracks(
                                        "Playlists",
                                        playlistObject.name,
                                        trackList,
                                        sharedViewModel.ytDownloader
                                    )
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

    /**
     * Function to fetch all Images for using in mp3 tag.
     **/
    private fun loadAllImages(trackList: List<Track>) {
        trackList.forEach {
            val imgUrl = it.album!!.images?.get(0)?.url
            imgUrl?.let {
                val imgUri = imgUrl.toUri().buildUpon().scheme("https").build()
                Glide
                    .with(requireContext())
                    .asFile()
                    .load(imgUri)
                    .listener(object: RequestListener<File> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<File>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            Log.i("Glide","LoadFailed")
                            return false
                        }

                        override fun onResourceReady(
                            resource: File?,
                            model: Any?,
                            target: Target<File>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            sharedViewModel.uiScope.launch {
                                withContext(Dispatchers.IO){
                                    try {
                                        val file = File(
                                            Environment.getExternalStorageDirectory(),
                                            DownloadHelper.defaultDir+".Images/" + imgUrl.substringAfterLast('/') + ".jpeg"
                                        )
                                        resource?.copyTo(file)
                                    } catch (e: IOException) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                            return false
                        }
                    }).submit()
            }
        }
    }

    /**
     * Implementing button to Open Spotify App
    **/
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

    private fun openGithubButton() {
        val uri: Uri =
            Uri.parse("http://github.com/Shabinder/SpotiFlyer")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        binding.btnGithub.setOnClickListener {
            startActivity(intent)
        }
    }
    private fun openInstaButton() {
        val uri: Uri =
            Uri.parse("http://www.instagram.com/mr.shabinder")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        binding.developerInsta.setOnClickListener {
            startActivity(intent)
        }
    }


    /**
     * Configure Recycler View Adapter
     **/
    private fun adapterConfig(trackList: List<Track>){
        adapter.trackList = trackList.toList()
        adapter.totalItems = trackList.size
        adapter.mainFragment = this
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

    private fun setUpUsageText() {
        val spanStringBuilder = SpannableStringBuilder()
        spanStringBuilder.append(getText(R.string.d_one)).append("\n")
        spanStringBuilder.append(getText(R.string.d_two)).append("\n")
        spanStringBuilder.append(getText(R.string.d_three)).append("\n")
        binding.usage.text = spanStringBuilder
    }


    /**
     * Util. Function to create toasts!
     **/
    fun showToast(message:String){
        Toast.makeText(context,message,Toast.LENGTH_SHORT).show()
    }

    /**
     * Util. Function To Check Connection Status
     **/
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