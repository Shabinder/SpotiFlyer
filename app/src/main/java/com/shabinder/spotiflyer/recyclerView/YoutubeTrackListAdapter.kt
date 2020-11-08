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
import com.shabinder.spotiflyer.R
import com.shabinder.spotiflyer.databinding.TrackListItemBinding
import com.shabinder.spotiflyer.downloadHelper.YTDownloadHelper
import com.shabinder.spotiflyer.models.DownloadStatus
import com.shabinder.spotiflyer.models.Source
import com.shabinder.spotiflyer.models.TrackDetails
import com.shabinder.spotiflyer.ui.youtube.YoutubeViewModel
import com.shabinder.spotiflyer.utils.Provider
import com.shabinder.spotiflyer.utils.bindImage
import com.shabinder.spotiflyer.utils.rotateAnim
import kotlinx.coroutines.launch

class YoutubeTrackListAdapter(private val youtubeViewModel :YoutubeViewModel): ListAdapter<TrackDetails,SpotifyTrackListAdapter.ViewHolder>(YouTubeTrackDiffCallback()) {

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
            youtubeViewModel.uiScope.launch {
                bindImage(holder.binding.imageUrl,
                    "https://i.ytimg.com/vi/${item.albumArt.absolutePath.substringAfterLast("/")
                        .substringBeforeLast(".")}/hqdefault.jpg"
                    ,
                    Source.YouTube
                )
            }
        }

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
                    Toast.makeText(Provider.activity,"Processing!", Toast.LENGTH_SHORT).show()
                    holder.binding.btnDownload.setImageResource(R.drawable.ic_refresh)
                    rotateAnim(it)
                    item.downloaded = DownloadStatus.Downloading
                    youtubeViewModel.uiScope.launch {
                        val itemList = mutableListOf<TrackDetails>()
                        itemList.add(item)
                        YTDownloadHelper.downloadYTTracks(
                            youtubeViewModel.folderType,
                            youtubeViewModel.subFolder,
                            itemList
                        )
                    }
                    notifyItemChanged(position)//start showing anim!
                }
            }
        }

        holder.binding.trackName.text = "${if(item.title.length > 17){"${item.title.subSequence(0,16)}..."}else{item.title}}"
        holder.binding.artist.text = "${item.artists.get(0)}..."
        holder.binding.duration.text =  "${item.durationSec/60} minutes, ${item.durationSec%60} sec"
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