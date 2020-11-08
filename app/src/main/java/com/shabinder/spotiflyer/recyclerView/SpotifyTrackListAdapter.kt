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

package com.shabinder.spotiflyer.recyclerView

import android.annotation.SuppressLint
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shabinder.spotiflyer.R
import com.shabinder.spotiflyer.databinding.TrackListItemBinding
import com.shabinder.spotiflyer.downloadHelper.DownloadHelper.downloadAllTracks
import com.shabinder.spotiflyer.models.DownloadStatus
import com.shabinder.spotiflyer.models.TrackDetails
import com.shabinder.spotiflyer.models.spotify.Source
import com.shabinder.spotiflyer.models.spotify.Track
import com.shabinder.spotiflyer.ui.spotify.SpotifyViewModel
import com.shabinder.spotiflyer.utils.*
import com.shabinder.spotiflyer.utils.Provider.activity
import kotlinx.coroutines.launch
import java.io.File

class SpotifyTrackListAdapter(private val spotifyViewModel : SpotifyViewModel): ListAdapter<Track,SpotifyTrackListAdapter.ViewHolder>(SpotifyTrackDiffCallback()) {

    var isAlbum:Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = TrackListItemBinding.inflate(layoutInflater,parent,false)
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        if(itemCount ==1 || isAlbum){
            holder.binding.imageUrl.visibility = View.GONE}else{
            spotifyViewModel.uiScope.launch {
                //Placeholder Set
                bindImage(holder.binding.imageUrl, item.album?.images?.get(0)?.url, Source.Spotify)
            }
        }

        holder.binding.trackName.text = "${if(item.name!!.length > 17){"${item.name!!.subSequence(0,16)}..."}else{item.name}}"
        holder.binding.artist.text = "${item.artists?.get(0)?.name?:""}..."
        holder.binding.duration.text = "${item.duration_ms/1000/60} minutes, ${(item.duration_ms/1000)%60} sec"
        when (item.downloaded) {
            DownloadStatus.Downloaded -> {
                holder.binding.btnDownload.setImageResource(R.drawable.ic_tick)
                holder.binding.btnDownload.clearAnimation()
            }
            DownloadStatus.Downloading -> {
                holder.binding.btnDownload.setImageResource(R.drawable.ic_refresh)
                rotateAnim(holder.binding.btnDownload)
            }
            DownloadStatus.NotDownloaded -> {
                holder.binding.btnDownload.setImageResource(R.drawable.ic_arrow)
                holder.binding.btnDownload.clearAnimation()
                holder.binding.btnDownload.setOnClickListener{
                    if(!isOnline()){
                        showNoConnectionAlert()
                        return@setOnClickListener
                    }
                    Toast.makeText(activity,"Processing!",Toast.LENGTH_SHORT).show()
                    holder.binding.btnDownload.setImageResource(R.drawable.ic_refresh)
                    rotateAnim(it)
                    item.downloaded = DownloadStatus.Downloading
                    spotifyViewModel.uiScope.launch {
                        val itemList = mutableListOf<TrackDetails>()
                        itemList.add(item.let { track ->
                            val artistsList = mutableListOf<String>()
                            track.artists?.forEach { artist -> artistsList.add(artist!!.name!!) }
                            TrackDetails(
                                title = track.name.toString(),
                                artists = artistsList,
                                durationSec = (track.duration_ms/1000).toInt(),
                                albumArt = File(
                                    Environment.getExternalStorageDirectory(),
                                    Provider.defaultDir +".Images/" + (track.album?.images?.get(0)?.url.toString()).substringAfterLast('/') + ".jpeg"),
                                albumName = track.album?.name,
                                year = track.album?.release_date,
                                comment = "Genres:${track.album?.genres?.joinToString()}",
                                trackUrl = track.href,
                                source = Source.Spotify
                            )
                        }
                        )
                        downloadAllTracks(spotifyViewModel.folderType,spotifyViewModel.subFolder,itemList)
                    }
                    notifyItemChanged(position)//start showing anim!
                }
            }
        }
    }
    class ViewHolder(val binding: TrackListItemBinding) : RecyclerView.ViewHolder(binding.root)
}

class SpotifyTrackDiffCallback: DiffUtil.ItemCallback<Track>(){
    override fun areItemsTheSame(oldItem: Track, newItem: Track): Boolean {
        return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(oldItem: Track, newItem: Track): Boolean {
        return oldItem == newItem //Downloaded Check
    }
}