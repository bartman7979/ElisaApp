package com.bartman79.elisa.ui.diary

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bartman79.elisa.data.local.entity.DiaryEntry
import com.bartman79.elisa.databinding.ItemDiaryEntryBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DiaryAdapter(private val onItemClick: (DiaryEntry) -> Unit) :
    ListAdapter<DiaryEntry, DiaryAdapter.DiaryViewHolder>(DiaryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiaryViewHolder {
        val binding = ItemDiaryEntryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DiaryViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: DiaryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiaryViewHolder(
        private val binding: ItemDiaryEntryBinding,
        private val onItemClick: (DiaryEntry) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: DiaryEntry) {
            binding.tvDate.text = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                .format(Date(entry.date))
            binding.tvPreview.text = entry.content.take(50) + if (entry.content.length > 50) "..." else ""
            binding.root.setOnClickListener {
                Log.d("DiaryAdapter", "Clicked on entry id: ${entry.id}")
                onItemClick(entry)
            }
        }
    }

    class DiaryDiffCallback : DiffUtil.ItemCallback<DiaryEntry>() {
        override fun areItemsTheSame(oldItem: DiaryEntry, newItem: DiaryEntry) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: DiaryEntry, newItem: DiaryEntry) = oldItem == newItem
    }
}
