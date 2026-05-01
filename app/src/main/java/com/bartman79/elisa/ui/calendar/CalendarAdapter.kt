package com.bartman79.elisa.ui.calendar

import android.graphics.Color
import android.graphics.drawable.LayerDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bartman79.elisa.R

class CalendarAdapter(
    private val onDayClick: (DayItem) -> Unit
) : ListAdapter<DayItem, CalendarAdapter.DayViewHolder>(DayDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)
        return DayViewHolder(view, onDayClick)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DayViewHolder(
        itemView: View,
        private val onDayClick: (DayItem) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvDay: TextView = itemView.findViewById(R.id.tvDay)

        fun bind(day: DayItem) {
            tvDay.text = day.dayOfMonth.toString()
            tvDay.alpha = if (day.isCurrentMonth) 1.0f else 0.3f

            val bgRes = when {
                day.isMenstruation -> R.drawable.bg_cycle_day_menstruation
                day.isOvulation -> R.drawable.bg_cycle_day_ovulation
                day.isFertile -> R.drawable.bg_cycle_day_fertile
                day.isPredicted -> R.drawable.bg_cycle_day_predicted
                else -> R.drawable.bg_transparent
            }

            if (day.isToday) {
                // Накладываем обводку поверх основного фона
                val layers = arrayOf(
                    ContextCompat.getDrawable(tvDay.context, bgRes)!!,
                    ContextCompat.getDrawable(tvDay.context, R.drawable.bg_today)!!
                )
                val layerDrawable = LayerDrawable(layers)
                tvDay.background = layerDrawable
            } else {
                tvDay.setBackgroundResource(bgRes)
            }

            // Устанавливаем цвет текста (можно оставить как было)
            when {
                day.isMenstruation || day.isOvulation -> tvDay.setTextColor(Color.WHITE)
                else -> tvDay.setTextColor(Color.BLACK)
            }

            itemView.setOnClickListener {
                if (day.isCurrentMonth) {
                    onDayClick(day)
                }
            }
        }

    }

    class DayDiffCallback : DiffUtil.ItemCallback<DayItem>() {
        override fun areItemsTheSame(oldItem: DayItem, newItem: DayItem) = oldItem.date == newItem.date
        override fun areContentsTheSame(oldItem: DayItem, newItem: DayItem) = oldItem == newItem
    }
}