package com.example.playlistmaker.library.ui.adapters

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.library.domain.model.Playlist
import java.io.File

class PlaylistBottomSheetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val ivCover = itemView.findViewById<android.widget.ImageView>(R.id.ivCover)
    private val tvName = itemView.findViewById<android.widget.TextView>(R.id.tvName)
    private val tvTrackCount = itemView.findViewById<android.widget.TextView>(R.id.tvTrackCount)
    private val ivCheck = itemView.findViewById<android.widget.ImageView>(R.id.ivCheck)

    fun bind(playlist: Playlist, isTrackInPlaylist: Boolean, onPlaylistClick: (Playlist) -> Unit) {
        if (!playlist.coverImagePath.isNullOrEmpty()) {
            val file = File(playlist.coverImagePath)
            if (file.exists()) {
                val cornerRadiusInPx = (16 * itemView.context.resources.displayMetrics.density).toInt()
                Glide.with(itemView.context)
                    .load(file)
                    .placeholder(R.drawable.ic_placeholder_45)
                    .error(R.drawable.ic_placeholder_45)
                    .centerCrop()
                    .transform(RoundedCorners(cornerRadiusInPx))
                    .into(ivCover)
            } else {
                ivCover.setImageResource(R.drawable.ic_placeholder_45)
            }
        } else {
            ivCover.setImageResource(R.drawable.ic_placeholder_45)
        }

        tvName.text = playlist.name

        val trackCountText = itemView.context.resources.getQuantityString(
            R.plurals.tracks_count,
            playlist.trackCount,
            playlist.trackCount
        )
        tvTrackCount.text = trackCountText

        ivCheck.visibility = if (isTrackInPlaylist) View.VISIBLE else View.GONE


        itemView.setOnClickListener {
            itemView.isClickable = false
            itemView.postDelayed({
                itemView.isClickable = true
            }, 500)

            onPlaylistClick(playlist)
        }
    }
}