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
import com.shabinder.musicForEveryone.utils.bindImage
import kaaes.spotify.webapi.android.models.Track
import kotlinx.coroutines.launch

class TrackListAdapter:RecyclerView.Adapter<TrackListAdapter.ViewHolder>(),DownloadHelper {

    var trackList = listOf<Track>()
    var totalItems:Int = 0
    var sharedViewModel = SharedViewModel()

    override fun getItemCount():Int =  totalItems

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        val view = layoutInflater.inflate(R.layout.track_list_item,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = trackList[position]
        if(totalItems == 1){holder.coverImage.visibility = View.GONE}else{
            bindImage(holder.coverImage,item.album.images[0].url)
        }

        holder.trackName.text = "${if(item.name.length > 17){"${item.name.subSequence(0,16)}..."}else{item.name}}"
        holder.artistName.text = "${item.artists[0]?.name?:""}..."
        holder.duration.text = "${item.duration_ms/1000/60} minutes, ${(item.duration_ms/1000)%60} sec"
        holder.downloadBtn.setOnClickListener{
            sharedViewModel.uiScope.launch {
                downloadTrack(sharedViewModel.ytDownloader,sharedViewModel.downloadManager,"${item.name} ${item.artists[0].name?:""}")
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