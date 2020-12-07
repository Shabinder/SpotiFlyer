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

package com.shabinder.spotiflyer.ui.mainfragment

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.razorpay.Checkout
import com.shabinder.spotiflyer.MainActivity
import com.shabinder.spotiflyer.R
import com.shabinder.spotiflyer.SharedViewModel
import com.shabinder.spotiflyer.databinding.MainFragmentBinding
import com.shabinder.spotiflyer.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

@AndroidEntryPoint
class MainFragment : Fragment(){

    //private val mainViewModel: MainViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var binding: MainFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MainFragmentBinding.inflate(inflater,container,false)
        initializeAll()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnSearch.setOnClickListener {
            if(!isOnline()){
                showDialog()
                return@setOnClickListener
            }
            val link = binding.linkSearch.text.toString()
            when{
                //SPOTIFY
                link.contains("spotify",true) -> {
                    if(sharedViewModel.spotifyService.value == null){//Authentication pending!!
                        (activity as MainActivity).authenticateSpotify()
                    }
                    findNavController().navigate(MainFragmentDirections.actionMainFragmentToSpotifyFragment(link))
                }

                //YOUTUBE
                link.contains("youtube.com",true) || link.contains("youtu.be",true) -> {
                    findNavController().navigate(MainFragmentDirections.actionMainFragmentToYoutubeFragment(link))
                }

                //GAANA
                link.contains("gaana",true) -> {
                    findNavController().navigate(MainFragmentDirections.actionMainFragmentToGaanaFragment(link))
                }

                else -> showMessage("Link is Not Valid",true)
            }
        }
        handleIntent()
    }

    /**
     * Handle Intent If there is any!
     **/
    private fun handleIntent() {
        sharedViewModel.intentString.observe(viewLifecycleOwner,{ it?.let {
            sharedViewModel.viewModelScope.launch(Dispatchers.IO) {
                //Wait for any Authentication to Finish ,
                // this Wait prevents from multiple Authentication Requests
                delay(1500)
                if(sharedViewModel.spotifyService.value == null){
                    //Not Authenticated Yet
                    Provider.mainActivity.authenticateSpotify()
                    while (sharedViewModel.spotifyService.value == null) {
                        //Waiting for Authentication to Finish
                        delay(1000)
                    }
                }

                withContext(Dispatchers.Main){
                    binding.linkSearch.setText(sharedViewModel.intentString.value)
                    binding.btnSearch.performClick()
                    //Intent Consumed
                    sharedViewModel.intentString.value = null
                }
            }
        }
        })
    }

    private fun initializeAll() {
        binding.apply {
            btnGaana.openPlatformOnClick("com.gaana","http://gaana.com")
            btnSpotify.openPlatformOnClick("com.spotify.music","http://open.spotify.com")
            btnYoutube.openPlatformOnClick("com.google.android.youtube","http://m.youtube.com")
            btnGithub.openPlatformOnClick("http://github.com/Shabinder/SpotiFlyer")
            btnInsta.openPlatformOnClick("http://www.instagram.com/mr.shabinder")
            btnHistory.setOnClickListener {
                findNavController().navigate(MainFragmentDirections.actionMainFragmentToDownloadRecord())
            }
            usage.text = usageText()
            btnDonate.setOnClickListener {
                startPayment()
            }
        }
    }


    private fun startPayment() {
        /*
        *  You need to pass current activity in order to let Razorpay create CheckoutActivity
        * */
        val co = Checkout().apply {
            setKeyID("rzp_live_3ZQeoFYOxjmXye")
            setImage(R.drawable.spotify_download)
        }

        try {
            val preFill = JSONObject().apply {
//                put("email","dev.shabinder@gmail.com")
//                put("contact","9780538548")
            }

            val options = JSONObject().apply {
                put("name","SpotiFlyer")
                put("description","Thanks For the Donation!")
                //You can omit the image option to fetch the image from dashboard
                put("image","https://github.com/Shabinder/SpotiFlyer/raw/master/app/SpotifyDownload.png")
                put("currency","INR")
                put("amount","4900")
                put("prefill",preFill)
            }

            co.open(requireActivity(),options)
        }catch (e: Exception){
            showMessage("Error in payment: "+ e.message)
            e.printStackTrace()
        }
    }

    private fun usageText(): SpannableStringBuilder {
        return SpannableStringBuilder()
            .append(getText(R.string.d_one)).append("\n")
            .append(getText(R.string.d_two)).append("\n")
            .append(getText(R.string.d_three)).append("\n")
            .append(getText(R.string.d_four)).append("\n")
    }

}