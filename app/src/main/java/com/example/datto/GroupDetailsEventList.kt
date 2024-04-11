package com.example.datto

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [GroupDetailsEventList.newInstance] factory method to
 * create an instance of this fragment.
 */

class EventItem(val name: String, val time: Date)

class EventListAdapter(private val events: ArrayList<EventItem>) :
    RecyclerView.Adapter<EventListAdapter.EventViewHolder>() {

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val eventName: TextView = itemView.findViewById(R.id.eventNameTextView)
        val date: TextView = itemView.findViewById(R.id.eventDateTextView)
        val month: TextView = itemView.findViewById(R.id.eventMonthTextView)
        init {
            itemView.setOnClickListener {
                Toast.makeText(itemView.context, itemView.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): EventListAdapter.EventViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.group_details_events_list_items, parent, false)
        return EventViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: EventListAdapter.EventViewHolder, position: Int) {
        val currentItem = events[position]
        holder.eventName.text = currentItem.name
        holder.date.text = SimpleDateFormat("dd", java.util.Locale.getDefault()).format(currentItem.time)
        holder.month.text = SimpleDateFormat("MMM", java.util.Locale.getDefault()).format(currentItem.time)
    }

    override fun getItemCount() = events.size
}


class GroupDetailsEventList : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? { // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_group_details_event_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val eventList = ArrayList<EventItem>()
        eventList.add(EventItem("Event 1", Date()))
        eventList.add(EventItem("Event 2", Date()))
        eventList.add(EventItem("Event 3", Date()))
        eventList.add(EventItem("Event 4", Date()))

        val eventRecyclerView = view.findViewById<RecyclerView>(R.id.eventListGroupDetailsRecyclerView)
        eventRecyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(view.context)
        eventRecyclerView.adapter = EventListAdapter(eventList)
        eventRecyclerView.setHasFixedSize(true)


    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment GroupDetailsEventList.
         */ // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) = GroupDetailsEventList().apply {
            arguments = Bundle().apply {
                putString(ARG_PARAM1, param1)
                putString(ARG_PARAM2, param2)
            }
        }
    }
}