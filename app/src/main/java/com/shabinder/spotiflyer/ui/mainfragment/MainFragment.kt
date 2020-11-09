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

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.shabinder.spotiflyer.MainActivity
import com.shabinder.spotiflyer.R
import com.shabinder.spotiflyer.SharedViewModel
import com.shabinder.spotiflyer.databinding.MainFragmentBinding
import com.shabinder.spotiflyer.utils.Provider
import com.shabinder.spotiflyer.utils.isOnline
import com.shabinder.spotiflyer.utils.showMessage
import com.shabinder.spotiflyer.utils.showNoConnectionAlert
import com.shreyaspatil.easyupipayment.EasyUpiPayment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MainFragment : Fragment() {

    private lateinit var mainViewModel: MainViewModel
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var binding: MainFragmentBinding
    @Inject lateinit var easyUpiPayment: EasyUpiPayment


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.main_fragment,container,false)
        initializeAll()
        binding.btnSearch.setOnClickListener {
            if(!isOnline()){
                showNoConnectionAlert()
                return@setOnClickListener
            }
            val link = binding.linkSearch.text.toString()
            if (link.contains("spotify",true)){
                if(sharedViewModel.spotifyService.value == null){//Authentication pending!!
                    (activity as MainActivity).authenticateSpotify()
                }
                findNavController().navigate(MainFragmentDirections.actionMainFragmentToSpotifyFragment(link))
            }else if(link.contains("youtube.com",true) || link.contains("youtu.be",true) ){
                findNavController().navigate(MainFragmentDirections.actionMainFragmentToYoutubeFragment(link))
            }else showMessage("Link is Not Valid",true)
        }
        handleIntent()
        return binding.root
    }


    private fun initializeAll() {
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        sharedViewModel = ViewModelProvider(this.requireActivity()).get(SharedViewModel::class.java)
        openYTButton()
        openSpotifyButton()
        openGithubButton()
        openInstaButton()
        openLinkedInButton()
        historyButton()
        binding.usage.text = usageText()
        binding.btnDonate.setOnClickListener {
            easyUpiPayment.startPayment()
        }
    }

    private fun historyButton() {
        binding.btnHistory.setOnClickListener {
            findNavController().navigate(MainFragmentDirections.actionMainFragmentToDownloadRecord())
        }
    }

    /**
     * Handle Intent If there is any!
     **/
    private fun handleIntent() {
        sharedViewModel.intentString.observe(viewLifecycleOwner,{
            if(it != ""){
                sharedViewModel.uiScope.launch(Dispatchers.IO) {
                    //Wait for any Authentication to Finish ,
                    // this Wait prevents from multiple Authentication Requests
                    Thread.sleep(1000)
                    if(sharedViewModel.spotifyService.value == null){
                        //Not Authenticated Yet
                        Provider.activity.authenticateSpotify()
                        while (sharedViewModel.spotifyService.value == null) {
                            //Waiting for Authentication to Finish
                            Thread.sleep(1000)
                        }
                    }

                    withContext(Dispatchers.Main){
                        binding.linkSearch.setText(sharedViewModel.intentString.value)
                        binding.btnSearch.performClick()
                        sharedViewModel.intentString.value = ""
                    }
                }
            }
        })
    }

    /**
     * Implementing buttons
     **/
    private fun openSpotifyButton() {
        val manager: PackageManager = requireActivity().packageManager
        try {
            val i = manager.getLaunchIntentForPackage("com.spotify.music")
                ?: throw PackageManager.NameNotFoundException()
            i.addCategory(Intent.CATEGORY_LAUNCHER)
            binding.btnSpotify.setOnClickListener { startActivity(i) }
        } catch (e: PackageManager.NameNotFoundException) {
            val uri: Uri =
                Uri.parse("http://open.spotify.com")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            binding.btnSpotify.setOnClickListener {
                startActivity(intent)
            }
        }
    }
    private fun openYTButton() {
        val manager: PackageManager = requireActivity().packageManager
        try {
            val i = manager.getLaunchIntentForPackage("com.google.android.youtube")
                ?: throw PackageManager.NameNotFoundException()
            i.addCategory(Intent.CATEGORY_LAUNCHER)
            binding.btnYoutube.setOnClickListener { startActivity(i) }
        } catch (e: PackageManager.NameNotFoundException) {
            val uri: Uri =
                Uri.parse("http://m.youtube.com")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            binding.btnYoutube.setOnClickListener {
                startActivity(intent)
            }
        }
    }
    private fun openGithubButton() {
        val uri: Uri =
            Uri.parse("http://github.com/Shabinder/SpotiFlyer")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        binding.btnGithubSpotify.setOnClickListener {
            startActivity(intent)
        }
    }
    private fun openLinkedInButton() {
        val uri: Uri =
            Uri.parse("https://in.linkedin.com/in/shabinder")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        binding.btnLinkedin.setOnClickListener {
            startActivity(intent)
        }
    }
    private fun openInstaButton() {
        val uri: Uri =
            Uri.parse("http://www.instagram.com/mr.shabinder")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        binding.developerInstaSpotify.setOnClickListener {
            startActivity(intent)
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