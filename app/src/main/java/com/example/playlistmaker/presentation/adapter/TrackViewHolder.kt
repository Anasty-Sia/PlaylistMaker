package com.example.playlistmaker.presentation.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.model.Track

class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val ivArtwork: ImageView = itemView.findViewById(R.id.ivArtwork)
    private val tvTrackName: TextView = itemView.findViewById(R.id.tvTrackName)
    private val tvArtistName: TextView = itemView.findViewById(R.id.tvArtistName)
    private val tvTrackTime: TextView = itemView.findViewById(R.id.tvTrackTime)

    fun bind(item: Track) {
        tvTrackName.text = item.trackName
        tvArtistName.text = item.artistName
        tvTrackTime.text = item.getFormattedTime()

        val options = RequestOptions()
            .placeholder(R.drawable.ic_placeholder_45)
            .error(R.drawable.ic_placeholder_45)
            .transform(RoundedCorners(8))

        if (!item.artworkUrl100.isNullOrEmpty()) {
            Glide.with(itemView.context)
                .load(item.artworkUrl100)
                .apply(options)
                .into(ivArtwork)
        } else {
            Glide.with(itemView.context)
                .load(R.drawable.ic_placeholder_45)
                .apply(options)
                .into(ivArtwork)
        }
    }
}