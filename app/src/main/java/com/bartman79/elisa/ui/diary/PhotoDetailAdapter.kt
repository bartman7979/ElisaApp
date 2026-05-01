package com.bartman79.elisa.ui.diary

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.bartman79.elisa.R
import com.bartman79.elisa.data.local.entity.Photo
import java.io.File

class PhotoDetailAdapter : RecyclerView.Adapter<PhotoDetailAdapter.PhotoViewHolder>() {

    private var photos = listOf<Photo>()

    fun submitList(newList: List<Photo>) {
        photos = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_photo_preview_no_delete, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(photos[position])
    }

    override fun getItemCount() = photos.size

    class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivThumb: ImageView = itemView.findViewById(R.id.ivThumb)

        fun bind(photo: Photo) {
            ivThumb.load(File(photo.filePath)) {
                crossfade(true)
                placeholder(R.drawable.ic_photo_placeholder)
                error(R.drawable.ic_photo_error)
            }

            itemView.setOnClickListener {
                val file = File(photo.filePath)
                //Toast.makeText(itemView.context, "Клик! Файл: ${file.absolutePath}", Toast.LENGTH_LONG).show()
                if (file.exists()) {
                    try {
                        val uri = FileProvider.getUriForFile(
                            itemView.context,
                            "${itemView.context.packageName}.fileprovider",
                            file
                        )
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, "image/*")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        itemView.context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(itemView.context, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(itemView.context, "Файл не найден", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}