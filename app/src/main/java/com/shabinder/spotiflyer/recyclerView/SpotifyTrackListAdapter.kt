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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.kiulian.downloader.YoutubeDownloader
import com.shabinder.spotiflyer.R
import com.shabinder.spotiflyer.databinding.TrackListItemBinding
import com.shabinder.spotiflyer.downloadHelper.SpotifyDownloadHelper.context
import com.shabinder.spotiflyer.downloadHelper.SpotifyDownloadHelper.downloadAllTracks
import com.shabinder.spotiflyer.models.Track
import com.shabinder.spotiflyer.ui.spotify.SpotifyViewModel
import com.shabinder.spotiflyer.utils.bindImage
import com.shabinder.spotiflyer.utils.rotateAnim
import kotlinx.coroutines.launch


class SpotifyTrackListAdapter: ListAdapter<Track,SpotifyTrackListAdapter.ViewHolder>(SpotifyTrackDiffCallback()) {

    var spotifyViewModel : SpotifyViewModel? = null
    var isAlbum:Boolean = false
    var ytDownloader: YoutubeDownloader? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = TrackListItemBinding.inflate(layoutInflater,parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        if(itemCount ==1 || isAlbum){
            holder.binding.imageUrl.visibility = View.GONE}else{
            spotifyViewModel!!.uiScope.launch {
                bindImage(holder.binding.imageUrl, item.album!!.images?.get(0)?.url)
            }
        }

        holder.binding.trackName.text = "${if(item.name!!.length > 17){"${item.name!!.subSequence(0,16)}..."}else{item.name}}"
        holder.binding.artist.text = "${item.artists?.get(0)?.name?:""}..."
        holder.binding.duration.text = "${item.duration_ms/1000/60} minutes, ${(item.duration_ms/1000)%60} sec"
        when (item.downloaded) {
            "Downloaded" -> {
                holder.binding.btnDownload.setImageResource(R.drawable.ic_tick)
                holder.binding.btnDownload.clearAnimation()
            }
            "Downloading" -> {
                holder.binding.btnDownload.setImageResource(R.drawable.ic_refresh)
                rotateAnim(holder.binding.btnDownload)
            }
            "notDownloaded" -> {
                holder.binding.btnDownload.setImageResource(R.drawable.ic_arrow)
                holder.binding.btnDownload.clearAnimation()
                holder.binding.btnDownload.setOnClickListener{
                    Toast.makeText(context,"Starting Download",Toast.LENGTH_SHORT).show()
                    holder.binding.btnDownload.setImageResource(R.drawable.ic_refresh)
                    rotateAnim(it)
                    item.downloaded = "Downloading"
                    spotifyViewModel!!.uiScope.launch {
                        val itemList = mutableListOf<Track>()
                        itemList.add(item)
                        downloadAllTracks(spotifyViewModel!!.folderType,spotifyViewModel!!.subFolder,itemList,ytDownloader)
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