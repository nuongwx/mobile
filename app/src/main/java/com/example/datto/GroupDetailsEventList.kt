package com.example.datto

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.datto.API.APICallback
import com.example.datto.API.APIService
import com.example.datto.DataClass.EventResponse
import java.text.SimpleDateFormat
import java.util.TimeZone

private const val ARG_PARAM1 = "groupId"

/**
 * A simple [Fragment] subclass.
 * Use the [GroupDetailsEventList.newInstance] factory method to
 * create an instance of this fragment.
 */

class EventListAdapter(private val events: ArrayList<EventResponse>) :
    RecyclerView.Adapter<EventListAdapter.EventViewHolder>() {

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val eventName: TextView = itemView.findViewById(R.id.eventNameTextView)
        val eventDes: TextView = itemView.findViewById(R.id.eventDescriptionTextView)
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
            LayoutInflater.from(parent.context)
                .inflate(R.layout.group_details_events_list_items, parent, false)
        return EventViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: EventListAdapter.EventViewHolder, position: Int) {
        val currentItem = events[position]
        holder.eventName.text = currentItem.name
        if (currentItem.description !== null) {
            holder.eventDes.text = currentItem.description
        }

        val inputFormat =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(currentItem.time.start)

        holder.date.text = SimpleDateFormat("dd", java.util.Locale.getDefault()).format(date)
        holder.month.text = SimpleDateFormat("MMM", java.util.Locale.getDefault()).format(date)
    }

    override fun getItemCount() = events.size
}


class GroupDetailsEventList : Fragment() {
    private var groupId: String? = null

    private fun configTopAppBar() {
        val appBar =
            requireActivity().findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.app_top_app_bar)
        val menuItem = appBar.menu.findItem(R.id.edit)
        menuItem.isEnabled = true
        menuItem.isVisible = true
        menuItem.setIcon(R.drawable.ic_add)
        menuItem.setOnMenuItemClickListener {
            val createEventFragment = Create()
            val bundle = Bundle()
            bundle.putString("groupId", groupId)
            createEventFragment.arguments = bundle
            parentFragmentManager.beginTransaction()
                .replace(R.id.app_fragment, createEventFragment)
                .addToBackStack("createEvent").commit()
            true
        }
        appBar.title = "Events"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            groupId = it.getString(ARG_PARAM1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? { // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_group_details_event_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configTopAppBar()

        val eventList = ArrayList<EventResponse>()
        APIService().doGet<List<EventResponse>>(
            "groups/${groupId}/events",
            object : APICallback<Any> {
                override fun onSuccess(data: Any) {
                    Log.d("API_SERVICE", "Data: $data")

                    data as List<EventResponse>

                    if (data.isEmpty()) {
                        val textView = view.findViewById<TextView>(R.id.noEventsTextView)
                        textView.isVisible = true
                    } else {

                        data.forEach {
                            eventList.add(it)
                        }

                        val eventRecyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
                        eventRecyclerView.layoutManager =
                            androidx.recyclerview.widget.LinearLayoutManager(view.context)
                        eventRecyclerView.adapter = EventListAdapter(eventList)
                        eventRecyclerView.setHasFixedSize(true)
                    }
                }

                override fun onError(error: Throwable) {
                    Log.e("API_SERVICE", "Error: ${error.message}")
                }
            })
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
        fun newInstance(param1: String) = GroupDetailsEventList().apply {
            arguments = Bundle().apply {
                putString(ARG_PARAM1, param1)
            }
        }
    }
}