package com.shabinder.musicForEveryone.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.shabinder.musicForEveryone.MainViewModel
import com.shabinder.musicForEveryone.R
import com.shabinder.musicForEveryone.databinding.MainFragmentBinding
import kaaes.spotify.webapi.android.SpotifyService


class MainFragment : Fragment() {
    lateinit var binding:MainFragmentBinding
    private lateinit var mainViewModel: MainViewModel
    var spotify : SpotifyService? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.main_fragment,container,false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mainViewModel = ViewModelProvider(this.requireActivity()).get(MainViewModel::class.java)
        spotify =  mainViewModel.spotify
        mainViewModel.userName.observeForever {
            binding.message.text = it
        }

    }

}