package com.example.playlistmaker.ui.utils


import android.content.ContentResolver
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object ImageStorageHelper {

     fun saveImageToInternalStorage(
        contentResolver: ContentResolver,
        uri: Uri,
        fileName: String = "playlist_cover_${System.currentTimeMillis()}.jpg"
    ): String? {
        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val file = File(FileManager.getPlaylistsDirectory(), fileName)

            inputStream?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}

object FileManager {
    private var playlistsDirectory: File? = null

    fun initPlaylistsDirectory(context: android.content.Context) {
        playlistsDirectory = File(context.filesDir, "playlist_covers")
        if (!playlistsDirectory!!.exists()) {
            playlistsDirectory!!.mkdirs()
        }
    }

    fun getPlaylistsDirectory(): File {
        return playlistsDirectory ?: throw IllegalStateException("FileManager not initialized")
    }
}