package com.example.playlistmaker.library.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R

class MenuBottomSheetAdapter(
    private val onMenuItemClick: (MenuItemType) -> Unit
) : RecyclerView.Adapter<MenuBottomSheetViewHolder>() {

    private val menuItems = listOf(
        MenuItem(R.string.share, MenuItemType.SHARE),
        MenuItem(R.string.edit_info, MenuItemType.EDIT),
        MenuItem(R.string.delete_playlist, MenuItemType.DELETE)
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuBottomSheetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_menu_bottom_sheet, parent, false)
        return MenuBottomSheetViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuBottomSheetViewHolder, position: Int) {
        holder.bind(menuItems[position], onMenuItemClick)
    }

    override fun getItemCount(): Int = menuItems.size

    data class MenuItem(
        val titleResId: Int,
        val type: MenuItemType
    )

    enum class MenuItemType {
        SHARE, EDIT, DELETE
    }
}