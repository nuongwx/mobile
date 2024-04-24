package com.example.datto

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.datto.API.APICallback
import com.example.datto.API.APIService
import com.example.datto.DataClass.EventResponse
import com.example.datto.DataClass.MemoryResponse
import com.example.datto.GlobalVariable.GlobalVariable
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Locale

data class Event(
    val groupName: String,
    val event: EventResponse
)

class EventAdapter(
    private val events: List<Event>,
    private val context: Context
) : androidx.recyclerview.widget.RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    inner class EventViewHolder(itemView: View) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        val eventGroupName = itemView.findViewById<android.widget.TextView>(R.id.eventGroupName)
        val eventName = itemView.findViewById<android.widget.TextView>(R.id.eventTitle)
        val eventDate = itemView.findViewById<android.widget.TextView>(R.id.eventDate)
        val eventImg = itemView.findViewById<android.widget.ImageView>(R.id.eventImage)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                val event = events[position]
                val eventDetailsFragment = EventDetails()
                val bundle = Bundle()
                bundle.putString("eventId", event.event.id)
                bundle.putString("groupName", event.groupName)
                eventDetailsFragment.arguments = bundle
                val transaction =
                    (it.context as MainActivity).supportFragmentManager.beginTransaction()
                transaction.replace(R.id.app_fragment, eventDetailsFragment)
                transaction.addToBackStack(null)
                transaction.commit()
            }
        }
    }

    fun getItem(position: Int): Event {
        return events[position]
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): EventAdapter.EventViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.events_list_items, parent, false)
        return EventViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: EventAdapter.EventViewHolder, position: Int) {
        val currentItem = events[position]

        holder.eventGroupName.text = currentItem.groupName
        holder.eventName.text = currentItem.event.name

        val inputFormat =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        val outputFormatWithYear = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val date = inputFormat.parse(currentItem.event.time.start)
        holder.eventDate.text = if (date != null) {
            "${outputFormat.format(date)} - ${
                outputFormatWithYear.format(
                    inputFormat.parse(
                        currentItem.event.time.end
                    )
                )
            }"
        } else {
            ""
        }

        if (currentItem.event.memory != null) {
            APIService(context).doGet<MemoryResponse>(
                "memories/${currentItem.event.memory}",
                object : APICallback<Any> {
                    override fun onSuccess(data: Any) {
                        Log.d("API_SERVICE", "Data: $data")

                        data as MemoryResponse

                        try {
                            val imageUrl =
                                if (data.thumbnail != null) GlobalVariable.BASE_URL + "files/" + data.thumbnail else null
                            if (imageUrl != null) {
                                Picasso.get().load(imageUrl).into(holder.eventImg)
                            } else {
                                Picasso.get().load(R.drawable.cover).into(holder.eventImg)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    override fun onError(error: Throwable) {
                        Log.e("API_SERVICE", "Error: ${error.message}")
                    }
                })
        } else {
            Picasso.get().load(R.drawable.cover).into(holder.eventImg)
        }
    }

    override fun getItemCount(): Int {
        return events.size
    }
}