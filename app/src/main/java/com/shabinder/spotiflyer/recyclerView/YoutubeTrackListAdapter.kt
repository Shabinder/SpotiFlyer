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
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.github.kiulian.downloader.model.formats.Format
import com.shabinder.spotiflyer.SharedViewModel
import com.shabinder.spotiflyer.databinding.TrackListItemBinding
import com.shabinder.spotiflyer.downloadHelper.YTDownloadHelper
import com.shabinder.spotiflyer.models.Track
import com.shabinder.spotiflyer.utils.bindImage
import kotlinx.coroutines.launch

class YoutubeTrackListAdapter: ListAdapter<Track,SpotifyTrackListAdapter.ViewHolder>(YouTubeTrackDiffCallback()) {

    var format:Format? = null
    var sharedViewModel = SharedViewModel()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SpotifyTrackListAdapter.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = TrackListItemBinding.inflate(layoutInflater,parent,false)
//        val view = layoutInflater.inflate(R.layout.track_list_item,parent,false)
        return SpotifyTrackListAdapter.ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SpotifyTrackListAdapter.ViewHolder, position: Int) {
        val item = getItem(position)
        if(itemCount == 1){
            holder.binding.imageUrl.visibility = View.GONE}else{
            sharedViewModel.uiScope.launch {
                bindImage(holder.binding.imageUrl, item.ytCoverUrl)
            }
        }

        holder.binding.trackName.text = "${if(item.name!!.length > 17){"${item.name!!.subSequence(0,16)}..."}else{item.name}}"
        holder.binding.artist.text = "${item.artists?.get(0)?.name?:""}..."
        holder.binding.duration.text =  "${item.duration_ms/1000/60} minutes, ${(item.duration_ms/1000)%60} sec"
        holder.binding.btnDownload.setOnClickListener{
            sharedViewModel.uiScope.launch {
                YTDownloadHelper.downloadFile(null,"YT_Downloads",item,format)
            }
        }
    }
}
class YouTubeTrackDiffCallback: DiffUtil.ItemCallback<Track>(){
    override fun areItemsTheSame(oldItem: Track, newItem: Track): Boolean {
        return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(oldItem: Track, newItem: Track): Boolean {
        return oldItem == newItem
    }

}