package com.example.playlistmaker.library.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.playlistmaker.R
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.library.domain.model.Playlist

class PlaylistBottomSheetAdapter(
    private val onPlaylistClick: (Playlist) -> Unit,
    private val currentTrackId: Int? = null
) : RecyclerView.Adapter<PlaylistBottomSheetViewHolder>() {

    private var playlists: List<Playlist> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistBottomSheetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist_simple, parent, false)
        return PlaylistBottomSheetViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaylistBottomSheetViewHolder, position: Int) {
        val playlist = playlists[position]
        val isTrackInPlaylist = currentTrackId?.let { trackId ->
            playlist.trackIds.contains(trackId)
        } ?: false

        holder.bind(playlist, isTrackInPlaylist, onPlaylistClick)
    }

    override fun getItemCount(): Int = playlists.size

    fun updateData(newPlaylists: List<Playlist>) {
        playlists = newPlaylists
        notifyDataSetChanged()
    }
}