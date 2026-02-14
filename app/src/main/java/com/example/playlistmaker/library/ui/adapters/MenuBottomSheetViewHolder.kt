package com.example.playlistmaker.library.ui.adapters

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R

class MenuBottomSheetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)

    fun bind(
        menuItem: MenuBottomSheetAdapter.MenuItem,
        onMenuItemClick: (MenuBottomSheetAdapter.MenuItemType) -> Unit
    ) {
        tvTitle.text = itemView.context.getString(menuItem.titleResId)

        itemView.setOnClickListener {
            onMenuItemClick(menuItem.type)
        }
    }
}