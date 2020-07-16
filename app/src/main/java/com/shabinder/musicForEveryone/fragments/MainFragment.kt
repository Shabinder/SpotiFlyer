package com.shabinder.musicForEveryone.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.shabinder.musicForEveryone.MainActivity
import com.shabinder.musicForEveryone.R
import com.shabinder.musicForEveryone.ui.main.MainViewModel
import kaaes.spotify.webapi.android.SpotifyService

class MainFragment : Fragment() {

    var spotify: SpotifyService? = null


    companion object {
        fun newInstance() = MainFragment()
        const val CLIENT_ID:String = "4fe3fecfe5334023a1472516cc99d805"
        const val spotify_client_secret:String = "0f02b7c483c04257984695007a4a8d5c"
    }

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        if(spotify==null){
           authenticateSpotify()
        }

        requestPermission()

        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    fun authenticateSpotify() {
        val builder: AuthenticationRequest.Builder = Builder(
            CLIENT_ID,
            AuthenticationResponse.Type.TOKEN,
            MainActivity.REDIRECT_URI
        )
        builder.setScopes(arrayOf("user-read-private", "streaming"))
        val request: AuthenticationRequest = builder.build()
        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        // TODO: Use the ViewModel
    }

}