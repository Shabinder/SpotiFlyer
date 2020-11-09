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
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shabinder.spotiflyer.database.DownloadRecord
import com.shabinder.spotiflyer.databinding.DownloadRecordItemBinding
import com.shabinder.spotiflyer.models.spotify.Source
import com.shabinder.spotiflyer.ui.downloadrecord.DownloadRecordFragmentDirections
import com.shabinder.spotiflyer.utils.bindImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DownloadRecordAdapter: ListAdapter<DownloadRecord,DownloadRecordAdapter.ViewHolder>(DownloadRecordDiffCallback())  {

    private val adapterScope = CoroutineScope(Dispatchers.Default)
    //Remember To change when Submitting a Different List / Or Use New Submit List Fun
    var source:Source = Source.Spotify

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding =DownloadRecordItemBinding.inflate(layoutInflater)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        adapterScope.launch {
            bindImage(holder.binding.coverUrl,item.coverUrl,source)
        }
        holder.binding.itemName.text = item.name
        holder.binding.totalItems.text = "Tracks: ${item.totalFiles}"
        holder.binding.type.text = item.type
        holder.binding.btnAction.setOnClickListener {
        if (item.link.contains("spotify",true)){
            it.findNavController().navigate(DownloadRecordFragmentDirections.actionDownloadRecordToSpotifyFragment((item.link)))
        }else if(item.link.contains("youtube.com",true) || item.link.contains("youtu.be",true) ){
            it.findNavController().navigate(DownloadRecordFragmentDirections.actionDownloadRecordToYoutubeFragment(item.link))
        }
    }
}
    class ViewHolder(val binding: DownloadRecordItemBinding) : RecyclerView.ViewHolder(binding.root)

    fun submitList(list: MutableList<DownloadRecord>?,source: Source) {
        super.submitList(list)
        this.source = source
    }
}

class DownloadRecordDiffCallback: DiffUtil.ItemCallback<DownloadRecord>(){
    override fun areItemsTheSame(oldItem: DownloadRecord, newItem: DownloadRecord): Boolean {
        return oldItem.coverUrl == newItem.coverUrl
    }

    override fun areContentsTheSame(oldItem: DownloadRecord, newItem: DownloadRecord): Boolean {
        return oldItem == newItem
    }
}