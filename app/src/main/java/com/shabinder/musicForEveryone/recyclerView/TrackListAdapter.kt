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

package com.shabinder.musicForEveryone.recyclerView

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.shabinder.musicForEveryone.R
import com.shabinder.musicForEveryone.SharedViewModel
import com.shabinder.musicForEveryone.downloadHelper.DownloadHelper
import com.shabinder.musicForEveryone.fragments.MainFragment
import com.shabinder.musicForEveryone.models.Track
import com.shabinder.musicForEveryone.utils.bindImage
import kotlinx.coroutines.launch
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
class TrackListAdapter:RecyclerView.Adapter<TrackListAdapter.ViewHolder>(),DownloadHelper {

    var trackList = listOf<Track>()
    var totalItems:Int = 0
    var sharedViewModel = SharedViewModel()
    var isAlbum:Boolean = false
    var mainFragment:MainFragment? = null

    override fun getItemCount():Int =  totalItems

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        val view = layoutInflater.inflate(R.layout.track_list_item,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = trackList[position]
        if(totalItems == 1 || isAlbum){holder.coverImage.visibility = View.GONE}else{
            bindImage(holder.coverImage, item.album!!.images?.get(0)?.url)
        }

        holder.trackName.text = "${if(item.name!!.length > 17){"${item.name!!.subSequence(0,16)}..."}else{item.name}}"
        holder.artistName.text = "${item.artists?.get(0)?.name?:""}..."
        holder.duration.text = "${item.duration_ms/1000/60} minutes, ${(item.duration_ms/1000)%60} sec"
        holder.downloadBtn.setOnClickListener{
            sharedViewModel.uiScope.launch {
                downloadTrack(mainFragment,sharedViewModel.ytDownloader,sharedViewModel.downloadManager,"${item.name} ${item.artists?.get(0)!!.name?:""}")
            }
        }

    }
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val trackName:TextView = itemView.findViewById(R.id.track_name)
        val artistName:TextView = itemView.findViewById(R.id.artist)
        val duration:TextView = itemView.findViewById(R.id.duration)
        val downloadBtn:ImageButton = itemView.findViewById(R.id.btn_download)
        val coverImage:ImageView = itemView.findViewById(R.id.imageUrl)
        }
}