package com.bartman79.elisa.ui.chat

import android.content.Intent
import android.net.Uri
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bartman79.elisa.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

class ChatAdapter(private val messages: List<Message>) :
    RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isUser) 1 else 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layout = if (viewType == 1) R.layout.item_message_user else R.layout.item_message_eliza
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount() = messages.size

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)

        fun bind(message: Message) {
            tvMessage.text = message.text
            // Ищем текст после "по запросу" в любых кавычках
            val pattern = Pattern.compile("по запросу\\s*[\"']([^\"']+)[\"']")
            val matcher = pattern.matcher(message.text)

            if (matcher.find()) {
                val query = matcher.group(1)
                val spannable = SpannableString(message.text)
                val start = matcher.start(1) - 1
                val end = matcher.end(1) + 1

                val clickableSpan = object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        val url = "https://www.youtube.com/results?search_query=${Uri.encode(query)}"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        widget.context.startActivity(intent)
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        super.updateDrawState(ds)
                        ds.color = ContextCompat.getColor(itemView.context, R.color.primary_pink)
                        ds.isUnderlineText = true
                    }
                }
                spannable.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                tvMessage.text = spannable
                tvMessage.movementMethod = LinkMovementMethod.getInstance()
            } else {
                // Если подсказки нет, делаем обычные ссылки кликабельными
                android.text.util.Linkify.addLinks(tvMessage, android.text.util.Linkify.WEB_URLS)
            }

            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            tvTime.text = timeFormat.format(Date(message.timestamp))
        }
    }
}