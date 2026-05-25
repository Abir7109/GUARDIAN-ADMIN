package com.securphone.app.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.securphone.app.R
import com.securphone.app.data.models.EventModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter(private val events: List<EventModel>) :
    RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false)
        return ViewHolder(
            view.findViewById(R.id.tv_event_title),
            view.findViewById(R.id.tv_event_desc),
            view.findViewById(R.id.tv_event_time)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val event = events[position]
        holder.titleView.text = event.type
        if (event.triggerSource.isNotEmpty()) {
            holder.descView.text = "via ${event.triggerSource}"
        } else {
            holder.descView.text = ""
        }
        holder.timeView.text = SimpleDateFormat("MMM dd HH:mm", Locale.getDefault())
            .format(Date(event.timestamp))
    }

    override fun getItemCount() = events.size

    class ViewHolder(
        val titleView: TextView,
        val descView: TextView,
        val timeView: TextView
    ) : RecyclerView.ViewHolder(titleView.parent as ViewGroup)
}
