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
import com.shabinder.spotiflyer.databinding.TrackListItemBinding
import com.shabinder.spotiflyer.models.Source
import com.shabinder.spotiflyer.models.TrackDetails
import com.shabinder.spotiflyer.utils.bindImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class YoutubeTrackListAdapter: ListAdapter<TrackDetails,SpotifyTrackListAdapter.ViewHolder>(YouTubeTrackDiffCallback()) {

    private val adapterScope = CoroutineScope(Dispatchers.Default)

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
            adapterScope.launch {
                bindImage(holder.binding.imageUrl,
                    "https://i.ytimg.com/vi/${item.albumArt.absolutePath.substringAfterLast("/")
                        .substringBeforeLast(".")}/maxresdefault.jpg"
                    ,
                    Source.YouTube
                )
            }
        }

        holder.binding.trackName.text = "${if(item.title.length > 17){"${item.title.subSequence(0,16)}..."}else{item.title}}"
        holder.binding.artist.text = "${item.artists.get(0)}..."
        holder.binding.duration.text =  "${item.durationSec/60} minutes, ${item.durationSec%60} sec"
        holder.binding.btnDownload.setOnClickListener{
            adapterScope.launch {
//                YTDownloadHelper.downloadFile(null,"YT_Downloads",item,format)
            }
        }
    }
}
class YouTubeTrackDiffCallback: DiffUtil.ItemCallback<TrackDetails>(){
    override fun areItemsTheSame(oldItem: TrackDetails, newItem: TrackDetails): Boolean {
        return oldItem.title == newItem.title
    }

    override fun areContentsTheSame(oldItem: TrackDetails, newItem: TrackDetails): Boolean {
        return oldItem == newItem
    }
}