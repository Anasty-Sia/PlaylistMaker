package com.example.playlistmaker.library.ui.adapters

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import java.io.File

object PlaylistImageLoader {

    private const val DEFAULT_CORNER_RADIUS_DP = 16

    fun loadPlaylistCover(
        imageView: ImageView,
        coverImagePath: String?,
        cornerRadiusDp: Int = DEFAULT_CORNER_RADIUS_DP
    ) {
        if (!coverImagePath.isNullOrEmpty()) {
            val file = File(coverImagePath)
            if (file.exists()) {
                val cornerRadiusInPx = (cornerRadiusDp * imageView.context.resources.displayMetrics.density).toInt()
                Glide.with(imageView.context)
                    .load(file)
                    .placeholder(R.drawable.ic_placeholder_45)
                    .error(R.drawable.ic_placeholder_45)
                    .centerCrop()
                    .transform(RoundedCorners(cornerRadiusInPx))
                    .into(imageView)
            } else {
                imageView.setImageResource(R.drawable.ic_placeholder_45)
            }
        } else {
            imageView.setImageResource(R.drawable.ic_placeholder_45)
        }
    }
}